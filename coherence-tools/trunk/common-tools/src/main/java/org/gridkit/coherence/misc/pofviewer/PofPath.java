package org.gridkit.coherence.misc.pofviewer;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;

public abstract class PofPath implements Comparable<PofPath> {

	public static PofPath root() {
		return new Root();
	}
	
	protected PofPath parent;

	public PofPath a(int i) {
		return new AttrIndex(this, i);
	}

	public PofPath i(int i) {
		return new ArrayIndex(this, i);
	}
	
	public PofPath append(PofPath path) {
		if (path instanceof Root) {
			return this;
		}
		else {
			PofPath base = append(path.parent);
			if (path instanceof AttrIndex) {
				return base.a(((AttrIndex)path).index);
			}
			else {
				return base.i(((ArrayIndex)path).index);
			}
		}
	}
	
	@Override
	public int compareTo(PofPath o) {
		PofPath[] a = flatten();
		PofPath[] b = o.flatten();
		int n = 0;
		while(true) {
			if (n > a.length || n > b.length) {
				return a.length - b.length;
			}
			else {				
				int c = a[n].compare(b[n]);
				if (c != 0) {
					return c;
				}
				else {
					n++;
				}
			}
		}
	}
	
	@Override
	public int hashCode() {
		return parent.hashCode() << 7 ^ nodeHash();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PofPath)) {
			return false;
		}
		else {
			return compareTo((PofPath)obj) == 0;
		}
	}

	protected abstract int compare(PofPath matcher);
	
	protected abstract int nodeHash();
	
	protected abstract void append(StringBuilder buffer);
	
	private PofPath[] flatten() {
		int len = length();
		PofPath[] fp = new PofPath[len];
		PofPath p = parent;
		int n = 0;
		while(!(p instanceof Root)) {
			fp[fp.length - n] = p;
			p = p.parent;
		}
		return fp;
	}
	
	public int length() {
		int n = 0;
		PofPath p = parent;
		while(p != null) {
			p = p.parent;
			++n; 
		}
		return n;
	}
	
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		parent.append(buffer);
		append(buffer);
		return buffer.toString();
	}

	protected static class Root extends PofPath {
		
		public Root() {
			this.parent = null;
		}

		@Override
		protected int nodeHash() {
			return Integer.MAX_VALUE / 3;
		}

		@Override
		protected int compare(PofPath that) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void append(StringBuilder buffer) {
			// empty
		}

		@Override
		public String toString() {
			return "<root>";
		}
	}
	
	public static class AttrIndex extends PofPath {

		private int index;
		
		public AttrIndex(PofPath pofPath, int i) {
			index = i;
		}

		@Override
		protected int nodeHash() {
			return index;
		}

		@Override
		protected int compare(PofPath that) {
			if (that instanceof ArrayIndex) {
				return -1;
			}
			else {
				return index - ((AttrIndex)that).index;
			}
		}

		@Override
		protected void append(StringBuilder buffer) {
			parent.append(buffer);
			buffer.append('.').append(index);
		}
	}
	
	public static class ArrayIndex extends PofPath {

		private int index;

		public ArrayIndex(PofPath pofPath, int i) {
			index = i;
		}

		@Override
		protected int compare(PofPath that) {
			if (that instanceof AttrIndex) {
				return 1;
			}
			else {
				return index - ((ArrayIndex)that).index;
			}
		}

		@Override
		protected int nodeHash() {
			return -index;
		}
		
		@Override
		protected void append(StringBuilder buffer) {
			parent.append(buffer);
			buffer.append('[').append(index).append(']');
		}
	}
	
	public static class Serializer implements PofSerializer {

		@Override
		public void serialize(PofWriter out, Object that)	 throws IOException {
			PofPath[] fp = ((PofPath)that).flatten();
			int id = 1;
			out.writeInt(id++, fp.length);
			for(PofPath pe: fp) {
				int n;
				if (pe instanceof AttrIndex) {
					n = ((AttrIndex)pe).index + 1;
				}
				else {
					n = - (((ArrayIndex)pe).index + 1);
				}
				out.writeInt(id++, n);
			}
		}

		@Override
		public Object deserialize(PofReader in) throws IOException {
			int id = 1;
			int len = in.readInt(id++);
			PofPath path = root();
			for(int i = 0; i != len; ++i) {
				int n = in.readInt(id++);
				if (n > 0) {
					path = path.a(n - 1);
				}
				else if (n < 0) {
					n = -n;
					path = path.i(n - 1);
				}
				else {
					throw new IOException("Corrupted POF stream");
				}
			}
			return path;
		}
	}
}
