package sandbox.treetest;

public class HeapTreeNode implements TreeNode {

    private HeapTreeNode parent;
	private HeapTreeNode left;
	private HeapTreeNode right;
	private int value;
	
    public HeapTreeNode(HeapTreeNode parent) {
        this.parent = parent;
    }

    @Override
    public TreeNode parent() {
        return parent;
    }

    @Override
    public TreeNode left() {
        return left;
    }

    @Override
    public TreeNode right() {
        return right;
    }

    @Override
    public TreeNode addLeft() {
    	if (left == null) {
    		left = new HeapTreeNode(this);
    		return left;
    	}
    	else {
    		throw new IllegalStateException();
    	}
    }

    @Override
    public TreeNode addRight() {
        if (right == null) {
        	right = new HeapTreeNode(this);
            return right;
        }
        else {
            throw new IllegalStateException();
        }
    }

    @Override
    public int value() {
        return value;
    }

    @Override
    public void value(int val) {
    	value = val;
    }
}
