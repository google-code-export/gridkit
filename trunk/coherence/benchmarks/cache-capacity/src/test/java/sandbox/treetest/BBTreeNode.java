package sandbox.treetest;

import java.nio.ByteBuffer;

public class BBTreeNode implements TreeNode {

	private ByteBuffer freeSpace;
	private ByteBuffer record;
	private int pointer;
	
	public BBTreeNode(ByteBuffer buffer) {
		freeSpace = buffer;
		record = buffer.duplicate();
		pointer = freeSpace.position();
		
		// init four fields
		freeSpace.putInt(-1);
		freeSpace.putInt(-1);
		freeSpace.putInt(-1);
		freeSpace.putInt(0);
	}

	private BBTreeNode(ByteBuffer buffer, int pointer) {
	    freeSpace = buffer;
	    ByteBuffer r = buffer.duplicate();
	    r.position(pointer);
	    record = r.slice();
	}

    @Override
    public TreeNode parent() {
        int p = record.getInt(0);
        return deref(p);
    }

    protected TreeNode deref(int p) {
        if (p < 0) {
            return null;
        }
        else {
        	return new BBTreeNode(freeSpace, p);
        }
    }

    @Override
    public TreeNode left() {
        int p = record.getInt(4);        
        return deref(p);
    }

    @Override
    public TreeNode right() {
        int p = record.getInt(8);        
        return deref(p);
    }

    @Override
    public TreeNode addLeft() {
        int p = record.getInt(4);        
        if (p >= 0) {
            throw new IllegalArgumentException();
        }
        BBTreeNode left = new BBTreeNode(freeSpace);
        left.record.putInt(0, pointer);
        record.putInt(4, left.pointer);
        return left;
    }

    @Override
    public TreeNode addRight() {
        int p = record.getInt(8);        
        if (p >= 0) {
            throw new IllegalArgumentException();
        }
        BBTreeNode right = new BBTreeNode(freeSpace);
        right.record.putInt(0, pointer);
        record.putInt(8, right.pointer);
        return right;
    }

    @Override
    public int value() {
        return record.getInt(12);
    }

    @Override
    public void value(int val) {
        record.putInt(12, val);        
    }
}
