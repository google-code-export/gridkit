package sandbox.treetest;

public interface TreeNode {
	
    public TreeNode parent();
    
    public boolean same(TreeNode node);
    
	public TreeNode left();
	
	public TreeNode right();
	
	public TreeNode addLeft();

	public TreeNode addRight();
	
	public int value();
	
	public void value(int val);

}
