package sandbox.treetest;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;

public class TreeTest {

    @Rule
    public BenchmarkRule benchmark = new BenchmarkRule();
    

    @Test
    @BenchmarkOptions(benchmarkRounds=5, callgc=true, warmupRounds = 2)
    public void test_heap_tree() throws Exception {
        test_tree(new HeapTreeNode(null));
    }

    @Test
    @BenchmarkOptions(benchmarkRounds=5, callgc=true, warmupRounds = 2)
    public void test_off_heap_tree() throws Exception {
        test_tree(new BBTreeNode(ByteBuffer.allocateDirect(100 << 20)));
    }

    @Test
    @BenchmarkOptions(benchmarkRounds=5, callgc=true, warmupRounds = 2)
    public void test_unsafe_tree() throws Exception {
        test_tree(new UnsafeTree(100 << 20).root());
    }

    @Test
    public void test_bb_direct_tree() throws Exception {
        test_tree(new BBTreeNode(ByteBuffer.allocateDirect(100 << 20)));
        System.gc();
        test_tree(new BBTreeNode(ByteBuffer.allocateDirect(100 << 20)));
        System.gc();
        test_tree(new BBTreeNode(ByteBuffer.allocateDirect(100 << 20)));
        System.gc();
        test_tree_naive(new BBTreeNode(ByteBuffer.allocateDirect(100 << 20)));
        System.gc();
        test_tree_naive(new BBTreeNode(ByteBuffer.allocateDirect(100 << 20)));
        System.gc();
        test_tree_naive(new BBTreeNode(ByteBuffer.allocateDirect(100 << 20)));
        System.gc();
    }

    @Test
    public void test_bb_heap_tree() throws Exception {
        test_tree(new BBTreeNode(ByteBuffer.allocate(100 << 20)));
    }

    @Test
    public void test_simple() throws Exception {
        TreeNode root = new UnsafeTree(20<<20).root();
        buildTree(root, 1000000);
        System.out.println(treeDepthNonRecursive(root));
        System.out.println(treeDepth(root));
    }
    
    public void test_tree_naive(final TreeNode root) throws Exception {
        time("Build heap tree: ", new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                buildTree(root, 1000000);
                return null;
            }
        });

        int depth = time("Calculating depth (naive): ", new Callable<Integer>() {
            
            @Override
            public Integer call() throws Exception {
                return treeDepth_naive(root);
            }
        });
        System.out.println("Depth: " + depth);
    }

    public void test_tree(final TreeNode root) throws Exception {
        time("Build heap tree: ", new Callable<Void>() {
            
            @Override
            public Void call() throws Exception {
                buildTree(root, 1000000);
                return null;
            }
        });
        
        int depth = time("Calculating depth (recursive): ", new Callable<Integer>() {
            
            @Override
            public Integer call() throws Exception {
                return treeDepth(root);
            }
        });
        System.out.println("Depth: " + depth);

        depth = time("Calculating depth (walker): ", new Callable<Integer>() {
            
            @Override
            public Integer call() throws Exception {
                return treeDepthNonRecursive(root);
            }
        });
        System.out.println("Depth: " + depth);
    }
    
    private <V> V time(String text, Callable<V> r) throws Exception {
        r.call();
        r.call();
        long start = System.nanoTime();
        try {
            return r.call();
        }
        finally {
            long time = System.nanoTime() - start;
            System.out.println(text + TimeUnit.NANOSECONDS.toMillis(time) + "ms");
        }
    }
    
    private int treeDepth_naive(TreeNode root) {
        int ld = root.left() == null ? 0 : treeDepth_naive(root.left());
        int rd = root.right() == null ? 0 : treeDepth_naive(root.right());
        return Math.max(ld, rd) + 1;
    }

    private int treeDepth(TreeNode root) {
        TreeNode left = root.left();
        TreeNode right = root.right();
        int ld = left == null ? 0 : treeDepth(left);
        int rd = right == null ? 0 : treeDepth(right);
        return Math.max(ld, rd) + 1;
    }

    private int treeDepthNonRecursive(TreeNode root) {
        int maxDepth = 1;
        int depth = 1;
        TreeNode node = root;
        boolean leftFirst = true;
        while(true) {
            if (leftFirst) {
                TreeNode left = node.left();
                if (left != null) {
                    node = left;
                    maxDepth = Math.max(maxDepth, ++depth);
                    continue;
                }
                leftFirst = false;
                continue;
            }
            else {
                TreeNode right = node.right();
                if (right != null) {
                    node = right;
                    leftFirst = true;
                    maxDepth = Math.max(maxDepth, ++depth);
                    continue;
                }
                else {
                    while(true) {
                        TreeNode par = node.parent();
                        if (par == null) {
                            return maxDepth;
                        }
                        TreeNode nextRight = par.right();
                        if (nextRight == null || node.same(nextRight)) {
                            node = par;
                            --depth;
                            continue;
                        }
                        else {
                            node = nextRight;
                            leftFirst = true;
                            break;
                        }
                    }
                }                
            }            
        }
    }
    
    private void buildTree(TreeNode root, int size) {
        Random rnd = new Random(1);
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
