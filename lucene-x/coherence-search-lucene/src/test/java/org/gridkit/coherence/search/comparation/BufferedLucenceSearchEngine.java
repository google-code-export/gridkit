package org.gridkit.coherence.search.comparation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.instantiated.InstantiatedIndex;
import org.apache.lucene.store.instantiated.InstantiatedIndexWriter;
import org.apache.lucene.util.Version;

public class BufferedLucenceSearchEngine implements LuceneSearchEngine {

	private static String DOC_KEY = "#DOC_KEY#";
	
	private RAMDirectory dir = new RAMDirectory();

//	private IndexSearcher searcher;
	private SearchIndex indexHead;
	
	private Semaphore queueLimit = new Semaphore(1024);
	private int readersCount;
	private int readersThreshold = 4;
	
	private Semaphore mergeRequired = new Semaphore(0); 
	
	private int mergeThreshold = 16;
	private int mergeTimeout = 1000;
	
	private Thread mergerThread;

	private Analyzer analyzer;
	
	public BufferedLucenceSearchEngine() throws CorruptIndexException, LockObtainFailedException, IOException {
		
		analyzer = new WhitespaceAnalyzer(Version.LUCENE_33);

		indexHead = new SearchIndex(new InstantiatedIndex());
		readersCount = 1;
		
		mergerThread = new Thread(new MergeWorker());
		mergerThread.setDaemon(true);
		mergerThread.setName("Merge worker");
		mergerThread.start();
	}
	
	
	@Override
	public void insertDocument(Object key, IndexableDocument doc) throws CorruptIndexException, LockObtainFailedException, IOException {
		Document dd = doc.getDocument();
		dd.add(new Field(DOC_KEY, key.toString(), Store.YES, Index.NOT_ANALYZED_NO_NORMS));

		try {
			queueLimit.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		synchronized(this) {
			indexDelete(new Term(DOC_KEY, key.toString()), indexHead);
			indexHead = indexAppend(indexHead, dd);
		}
		
		if (readersCount > readersThreshold) {
			mergeRequired.release();
		}
	}

	@Override
	public void updateDocument(Object key, IndexableDocument doc) throws CorruptIndexException, LockObtainFailedException,	IOException {
		insertDocument(key, doc);
	}

	@Override
	public void deleteDocument(Object key) throws CorruptIndexException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IndexSearcher acquireSearcher() throws CorruptIndexException, IOException {
		synchronized(this) {
			IndexReader[] readers = new IndexReader[readersCount];
			int n = 0;
			SearchIndex node = indexHead;
			while(node != null) {
				readers[n++] = node.reader;
				node = node.next;
			}
			MultiReader mreader = new MultiReader(readers);
			return new IndexSearcher(mreader);
		}
	}

	@Override
	public synchronized void releaseSearcher(IndexSearcher searcher) {
		try {
			searcher.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return "Buffered (queue " + (readersCount - 1) + ")";
	}
	
	SearchIndex singleDoc(Document doc) {
		++readersCount;
		InstantiatedIndex ii = new InstantiatedIndex();
		InstantiatedIndexWriter iw = ii.indexWriterFactory(analyzer, true);
		iw.addDocument(doc);
		try {
			iw.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return new SearchIndex(ii);
	}
	
	SearchIndex indexAppend(SearchIndex index, Document doc) {
		if (index.reader.maxDoc() >= 1 || index.underMerge) {
			SearchIndex node = singleDoc(doc);
			node.next = index;
			return node;
		}
		else {
			int maxDoc = index.reader.maxDoc();
			InstantiatedIndexWriter writer = index.ii.indexWriterFactory(analyzer, false);
			writer.addDocument(doc);
			try {
				writer.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			index.reader = index.ii.indexReaderFactory();
			if (maxDoc == index.reader.maxDoc()) {
				new String();
			}
			return index; 
		}
	}
	
	void indexDelete(Term term, SearchIndex index) {
		try {
			while(index != null) {
				if (index.reader.docFreq(term) != 0) {
					index.reader.deleteDocuments(term);
					index.reader.commit(null);
					if (index.underMerge) {
						if (index.pendingRemoves == null) {
							index.pendingRemoves = new ArrayList<Term>();					
						}
						index.pendingRemoves.add(term);
					}
					return;
				}
				else {
					index = index.next;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	SearchIndex indexMerge(SearchIndex index1, SearchIndex index2) {
		MultiReader reader = new MultiReader(index1.reader, index2.reader);
		InstantiatedIndex ii;
		try {
			ii = new InstantiatedIndex(reader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new SearchIndex(ii);
	}
	
	private static class SearchIndex {
		
		InstantiatedIndex ii;
		IndexReader reader;
		
		boolean underMerge;
		List<Term> pendingRemoves;
		SearchIndex next;		
		
		public SearchIndex(InstantiatedIndex ii) {
			this.ii = ii;
			this.reader = ii.indexReaderFactory();
		}
	}
	
	private static final int BULK_MERGE_LIMIT = 128;
	
	class MergeWorker implements Runnable {

		@Override
		public void run() {
			try {
				while(true) {
					
					if (readersCount < readersThreshold) {
						mergeRequired.tryAcquire(200, TimeUnit.MILLISECONDS);
						mergeRequired.drainPermits();
					}
					
					tryMerge(0);
//					tryMerge(BULK_MERGE_LIMIT);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private void tryMerge(int start) {
			
			while(true) {
				
				int size = 0;
				SearchIndex node = indexHead;
				SearchIndex startNode = indexHead;
				SearchIndex nextNode = null;
				int nodesToMerge = 0;

				synchronized(BufferedLucenceSearchEngine.this) {
					while(node != null) {
						if (node.reader.maxDoc() < start) {
							node = node.next;
							startNode = node;
							continue;
						}
						else {
							if (size < BULK_MERGE_LIMIT) {
								size += node.reader.numDocs();
							}
							else if (size > node.reader.numDocs()) {
								nextNode = node.next;
								break;
							}
							else {
								size += node.reader.numDocs();
							}
							node = node.next;
						}
					}
					if (startNode == null || startNode.next == nextNode) {
						return;
					}
					nodesToMerge = lockNodes(startNode, nextNode);
				}
				IndexReader reader = collectReader(startNode, nextNode);
				InstantiatedIndex ii;
//				System.out.println("Merging " + nodesToMerge + " indexes, " + reader.maxDoc() + " documents");
				try {
					ii = new InstantiatedIndex(reader);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				synchronized(BufferedLucenceSearchEngine.this){
					SearchIndex newNode = new SearchIndex(ii);
					processPendingDeletes(newNode.reader, startNode, nextNode);
					newNode.next = nextNode;
					
					if (indexHead == startNode) {
						indexHead = newNode;
						queueLimit.release(nodesToMerge);
					}
					else {
						SearchIndex rnode = indexHead;
						while(rnode.next != startNode) {
							rnode = rnode.next;
						}
						rnode.next = newNode;
					}
					readersCount -= nodesToMerge - 1;
					queueLimit.release(nodesToMerge - 1);
					int count = 0;
					SearchIndex inode = indexHead;
					while(inode != null) {
						count++;
						inode = inode.next;
					}
					if (readersCount != count) {
						new String();
					}
				}				
			}
		}

		private void processPendingDeletes(IndexReader ireader, SearchIndex startNode, SearchIndex nextNode) {
			SearchIndex node = startNode;
			while(node != nextNode) {
				if (node.pendingRemoves != null) {
					for(Term term: node.pendingRemoves) {
						try {
							ireader.deleteDocuments(term);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
				node = node.next;
			}
			try {
				ireader.commit(null);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private IndexReader collectReader(SearchIndex startNode, SearchIndex nextNode) {
			List<IndexReader> readers = new ArrayList<IndexReader>();
			SearchIndex node = startNode;
			while(node != nextNode) {
				readers.add(node.reader);
				node = node.next;
			}
			
			return new MultiReader(readers.toArray(new IndexReader[readers.size()]));
		}

		private int lockNodes(SearchIndex startNode, SearchIndex nextNode) {
			int n = 0;
			SearchIndex node = startNode;
			while(node != nextNode) {
				node.underMerge = true;
				n++;
				node = node.next;
			}
			return n;
		}
	}
}
