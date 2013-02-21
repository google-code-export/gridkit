package sandbox.treetest;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TreeTest {

    @Test
    public void test_tree() throws Exception {
        final TreeNode root = new HeapTreeNode(null);
        time("Build heap tree: ", new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                buildTree(root, 1000000);
                return null;
            }
        });

        int depth = time("Calculating depth: ", new Callable<Integer>() {
            
            @Override
            public Integer call() throws Exception {
                return treeDepth(root);
            }
        });
        System.out.println("Depth:" + depth);
    }
    
    private <V> V time(String text, Callable<V> r) throws Exception {
        long start = System.nanoTime();
        try {
            r.call();
            r.call();
            return r.call();
        }
        finally {
            long time = System.nanoTime() - start;
            System.out.println(text + TimeUnit.NANOSECONDS.toMillis(time) + "ms");
        }
    }
    
    private int treeDepth(TreeNode root) {
        int ld = root.left() == null ? 0 : treeDepth(root.left());
        int rd = root.right() == null ? 0 : treeDepth(root.right());
        return Math.max(ld, rd) + 1;
    }
    
    private void buildTree(TreeNode root, int size) {
        Random rnd = new Random();
        int count = 0;
        for(int i = 0; i != size; ++i) {
            addNode(root, rnd);
        }
    }

    private void addNode(TreeNode root, Random rnd) {
        TreeNode c = root;
        while(true) {
            boolean left = rnd.nextBoolean();
            if (left) {
                if (c.left() == null) {
                    TreeNode n = c.addLeft();
                    n.value(rnd.nextInt());
                    return;
                }
                else {
                    c = c.left();
                    continue;
                }
            }
            else {
                if (c.right() == null) {
                    TreeNode n = c.addRight();
                    n.value(rnd.nextInt());
                    return;
                }
                else {
                    c = c.right();
                    continue;
                }
            }
        }        
    }
}
