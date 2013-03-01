package sandbox.treetest;

public class UnsafeTree {

    private long pointer;
    private int used;
    private int limit;
    
    @Override
    protected void finalize() throws Throwable {
        TheUnsafe.UNSAFE.freeMemory(pointer);
    }
    
    public UnsafeTree(int size) { 
        pointer = TheUnsafe.UNSAFE.allocateMemory(size);
        used += 16;
        limit = size;
        TheUnsafe.UNSAFE.putInt(pointer, -1);
        TheUnsafe.UNSAFE.putInt(pointer + 4, -1);
        TheUnsafe.UNSAFE.putInt(pointer + 8, -1);
        
        // root element
    }
    
    public TreeNode root() {
        return deref(0);
    }
    
    Node deref(int offset) {
        return offset >= 0 ? new Node(pointer + offset) : null;
    }
    
    int newNode(Node parent) {
        int offs = used;
        used += 16;
        if (used > limit) {
            throw new IllegalStateException();
        }
        TheUnsafe.UNSAFE.putInt(pointer + offs, (int)(parent.pointer - pointer));
        TheUnsafe.UNSAFE.putInt(pointer + offs + 4, -1);
        TheUnsafe.UNSAFE.putInt(pointer + offs + 8, -1);
        return offs;
    }
    
    private class Node implements TreeNode {
        
        long pointer;
        
        Node(long pointer) {
            this.pointer = pointer;
        }

        int getField(int f) {
            return TheUnsafe.UNSAFE.getInt(pointer + 4 * f);
        }
        
        void setField(int f, int value) {
            TheUnsafe.UNSAFE.putInt(pointer + 4 * f, value);
        }
        
        @Override
        public TreeNode parent() {
            return deref(getField(0));
        }

        @Override
        public TreeNode left() {
            return deref(getField(1));
        }

        @Override
        public TreeNode right() {
            return deref(getField(2));
        }
        
        @Override
        public boolean same(TreeNode node) {
            return pointer == ((Node)node).pointer;
        }

        @Override
        public TreeNode addLeft() {
            if (getField(1) != -1) {
                throw new IllegalArgumentException();
            }
            else {
                setField(1, newNode(this));
            }
            return left();
        }

        @Override
        public TreeNode addRight() {
            if (getField(2) != -1) {
                throw new IllegalArgumentException();
            }
            else {
                setField(2, newNode(this));
            }
            return right();
        }

        @Override
        public int value() {
            return getField(3);
        }

        @Override
        public void value(int val) {
            setField(3, val);
        }
    }
}
