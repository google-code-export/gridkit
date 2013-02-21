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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TreeNode right() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TreeNode addLeft() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TreeNode addRight() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int value() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void value(int val) {
        // TODO Auto-generated method stub
        
    }
}
