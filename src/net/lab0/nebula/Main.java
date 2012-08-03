
package net.lab0.nebula;


import net.lab0.nebula.data.QuadTreeNode;


public class Main
{
    private static QuadTreeNode root;
    
    public static void main(String[] args)
    {
        System.out.println("Start");
        
        long startTime = System.currentTimeMillis();
        
        root = new QuadTreeNode(-2.0, 2.0, -2.0, 2.0);
        int pointsPerSide = 100;
        int maxIter = 512;
        int diffIterLimit = 4;
        int maxDepth = 8;
        
        QuadTreeManager manager = new QuadTreeManager(root);
        QuadTreeNode node = root;
        while (node != null)
        {
            node = manager.getNextNodeToCompute(maxDepth);
            if (node != null)
            {
                node.computeStatus(pointsPerSide, maxIter, diffIterLimit);
            }
            else
            {
                System.out.println("No next node");
            }
        }
        
        System.out.println("End, time=" + (System.currentTimeMillis() - startTime));
    }
}
