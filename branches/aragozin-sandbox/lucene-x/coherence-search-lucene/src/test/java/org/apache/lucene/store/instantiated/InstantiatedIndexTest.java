package org.apache.lucene.store.instantiated;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.Version;
import org.junit.Test;

public class InstantiatedIndexTest {

	@Test
	public void test_simple_document_removal_1() throws IOException {
		
		Document doc1 = new Document();
		doc1.add(new Field("id", "doc1", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc1.add(new Field("text", "quick brown fox", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));

		Document doc2 = new Document();
		doc2.add(new Field("id", "doc2", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc2.add(new Field("text", "quick brown fox", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
		
		InstantiatedIndex index = new InstantiatedIndex();
		InstantiatedIndexWriter writer = index.indexWriterFactory(new WhitespaceAnalyzer(Version.LUCENE_33), true);
		writer.addDocument(doc1);
		writer.addDocument(doc2);
		
		writer.close();
		
		IndexReader reader = index.indexReaderFactory();
		reader.deleteDocuments(new Term("id", "doc1"));

		Assert.assertEquals(1, reader.numDocs());
		Assert.assertEquals(2, reader.maxDoc());
		Assert.assertEquals(2, reader.docFreq(new Term("text","fox")));
		
		index = new InstantiatedIndex(reader);
		reader = index.indexReaderFactory();
		
		Assert.assertEquals(1, reader.numDocs());
		Assert.assertEquals(1, reader.maxDoc());
		Assert.assertEquals(1, reader.docFreq(new Term("text","fox")));
	}

	@Test
	public void test_simple_document_removal_2() throws IOException {
		
		Document doc1 = new Document();
		doc1.add(new Field("id", "doc1", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc1.add(new Field("text", "quick brown fox", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));

		Document doc2 = new Document();
		doc2.add(new Field("id", "doc2", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc2.add(new Field("text", "quick brown fox", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));

		Document doc3 = new Document();
		doc3.add(new Field("id", "doc3", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc3.add(new Field("text", "over lazy dog", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));

		Document doc4 = new Document();
		doc4.add(new Field("id", "doc4", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc4.add(new Field("text", "over lazy dog", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
		
		InstantiatedIndex index = new InstantiatedIndex();
		InstantiatedIndexWriter writer = index.indexWriterFactory(new WhitespaceAnalyzer(Version.LUCENE_33), true);
		writer.addDocument(doc1);
		writer.addDocument(doc2);
		writer.addDocument(doc3);
		writer.addDocument(doc4);
		
		writer.close();
		
		IndexReader reader = index.indexReaderFactory();
		reader.deleteDocuments(new Term("id", "doc1"));
		reader.deleteDocuments(new Term("id", "doc4"));

		Assert.assertEquals(2, reader.numDocs());
		Assert.assertEquals(4, reader.maxDoc());
		Assert.assertEquals(2, reader.docFreq(new Term("text","fox")));
		Assert.assertEquals(2, reader.docFreq(new Term("text","over")));
		
		index = new InstantiatedIndex(reader);
		reader = index.indexReaderFactory();
		
		Assert.assertEquals(2, reader.numDocs());
		Assert.assertEquals(2, reader.maxDoc());
		Assert.assertEquals(1, reader.docFreq(new Term("text","fox")));
		Assert.assertEquals(1, reader.docFreq(new Term("text","dog")));
	}

	@Test
	public void test_simple_index_merge() throws IOException {
		
		Document doc1 = new Document();
		doc1.add(new Field("id", "doc1", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc1.add(new Field("text", "quick brown fox", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));

		Document doc2 = new Document();
		doc2.add(new Field("id", "doc2", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc2.add(new Field("text", "quick brown fox", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));

		Document doc3 = new Document();
		doc3.add(new Field("id", "doc3", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc3.add(new Field("text", "over lazy dog", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));

		Document doc4 = new Document();
		doc4.add(new Field("id", "doc4", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc4.add(new Field("text", "over lazy dog", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
		
		Document doc5 = new Document();
		doc5.add(new Field("id", "doc5", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc5.add(new Field("text", "over lazy dog", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
		
		Document doc6 = new Document();
		doc6.add(new Field("id", "doc6", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc6.add(new Field("text", "over lazy dog", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
		
		InstantiatedIndex index1 = new InstantiatedIndex();
		InstantiatedIndexWriter writer1 = index1.indexWriterFactory(new WhitespaceAnalyzer(Version.LUCENE_33), true);
		writer1.addDocument(doc1);
		writer1.addDocument(doc2);
		writer1.addDocument(doc3);
		writer1.close();
		
		InstantiatedIndex index2 = new InstantiatedIndex();
		InstantiatedIndexWriter writer2 = index2.indexWriterFactory(new WhitespaceAnalyzer(Version.LUCENE_33), true);
		writer2.addDocument(doc4);
		writer2.addDocument(doc5);
		writer2.addDocument(doc6);
		writer2.close();		
		
		IndexReader reader1 = index1.indexReaderFactory();
		reader1.deleteDocuments(new Term("id", "doc1"));

		IndexReader reader2 = index2.indexReaderFactory();
		reader2.deleteDocuments(new Term("id", "doc5"));

//		Assert.assertEquals(2, reader.numDocs());
//		Assert.assertEquals(4, reader.maxDoc());
//		Assert.assertEquals(2, reader.docFreq(new Term("text","fox")));
//		Assert.assertEquals(2, reader.docFreq(new Term("text","over")));
		
		index1 = new InstantiatedIndex(new MultiReader(reader1, reader2));
		reader1 = index1.indexReaderFactory();
		
//		Assert.assertEquals(2, reader.numDocs());
//		Assert.assertEquals(2, reader.maxDoc());
//		Assert.assertEquals(1, reader.docFreq(new Term("text","fox")));
//		Assert.assertEquals(1, reader.docFreq(new Term("text","dog")));
	}

	@Test
	public void test_simple_index_merge2() throws IOException {
		
		Document doc1 = new Document();
		doc1.add(new Field("id", "doc1", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc1.add(new Field("text", "quick brown fox", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
		
		Document doc2 = new Document();
		doc2.add(new Field("id", "doc2", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc2.add(new Field("text", "quick brown fox", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
		
		Document doc3 = new Document();
		doc3.add(new Field("id", "doc3", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc3.add(new Field("text", "over lazy dog", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
		
		Document doc4 = new Document();
		doc4.add(new Field("id", "doc4", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc4.add(new Field("text", "over lazy dog", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
		
		Document doc5 = new Document();
		doc5.add(new Field("id", "doc5", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc5.add(new Field("text", "over lazy dog", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
		
		Document doc6 = new Document();
		doc6.add(new Field("id", "doc6", Store.NO, Index.NOT_ANALYZED, TermVector.NO));
		doc6.add(new Field("text", "over lazy dog", Store.NO, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
		
		InstantiatedIndex index1 = new InstantiatedIndex();
		InstantiatedIndexWriter writer1 = index1.indexWriterFactory(new WhitespaceAnalyzer(Version.LUCENE_33), true);
		writer1.addDocument(doc1);
		writer1.addDocument(doc2);
		writer1.addDocument(doc3);
		writer1.close();
		
		InstantiatedIndex index2 = new InstantiatedIndex();
		InstantiatedIndexWriter writer2 = index2.indexWriterFactory(new WhitespaceAnalyzer(Version.LUCENE_33), true);
		writer2.addDocument(doc4);
		writer2.addDocument(doc5);
		writer2.addDocument(doc6);
		writer2.close();		
		
		IndexReader reader1 = index1.indexReaderFactory();
		reader1.deleteDocuments(new Term("id", "doc1"));
		
		IndexReader reader2 = index2.indexReaderFactory();
		reader2.deleteDocuments(new Term("id", "doc5"));
		
//		Assert.assertEquals(2, reader.numDocs());
//		Assert.assertEquals(4, reader.maxDoc());
//		Assert.assertEquals(2, reader.docFreq(new Term("text","fox")));
//		Assert.assertEquals(2, reader.docFreq(new Term("text","over")));
		
		index1 = new InstantiatedIndex(reader1);
		reader1 = index1.indexReaderFactory();
		
		index1 = new InstantiatedIndex(new MultiReader(reader1, reader2));
		reader1 = index1.indexReaderFactory();
		
//		Assert.assertEquals(2, reader.numDocs());
//		Assert.assertEquals(2, reader.maxDoc());
//		Assert.assertEquals(1, reader.docFreq(new Term("text","fox")));
//		Assert.assertEquals(1, reader.docFreq(new Term("text","dog")));
	}
	
//	@Test
//	public void test_simple_index_merge3() throws IOException {
//
//		InstantiatedIndex ii1 = new InstantiatedIndex();
//		InstantiatedIndex ii2 = new Insta
//		
//	}

}
