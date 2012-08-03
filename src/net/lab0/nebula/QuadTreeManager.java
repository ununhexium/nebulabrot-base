
package net.lab0.nebula;


import net.lab0.nebula.data.PositionInParent;
import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.data.Status;


public class QuadTreeManager
{
    private QuadTreeNode root;
    
    public QuadTreeManager(QuadTreeNode root)
    {
        super();
        this.root = root;
    }
    
    public QuadTreeNode getNextNodeToCompute(int maxComputationDepth)
    {
        return recursiveGetNextNodeToCompute(root, maxComputationDepth);
    }
    
    private QuadTreeNode recursiveGetNextNodeToCompute(QuadTreeNode node, int maxComputationDepth)
    {
        if (node.status.equals(Status.VOID))
        {
            return node;
        }
        else if (node.status.equals(Status.BROWSED))
        {
            //if the node has no children : create them
            if (node.children == null)
            {
                node.splitNode();
                QuadTreeNode nextNode = node.children[PositionInParent.TopLeft.ordinal()];
                if (nextNode.getNodeDepth() > maxComputationDepth)
                {
                    return null;
                }
                else
                {
                    return nextNode;
                }
            }
            else
            {
                QuadTreeNode n1 = recursiveGetNextNodeToCompute(node.children[PositionInParent.TopLeft.ordinal()], maxComputationDepth);
                QuadTreeNode n2 = recursiveGetNextNodeToCompute(node.children[PositionInParent.TopRight.ordinal()], maxComputationDepth);
                QuadTreeNode n3 = recursiveGetNextNodeToCompute(node.children[PositionInParent.BottomLeft.ordinal()], maxComputationDepth);
                QuadTreeNode n4 = recursiveGetNextNodeToCompute(node.children[PositionInParent.BottomRight.ordinal()], maxComputationDepth);

                QuadTreeNode best = null;

                //try to assign at least 1 non NULL pointer
                if (n1!=null)
                {
                    best = n1;
                }
                else if (n2!=null)
                {
                    best = n2;
                }
                else if (n3!=null)
                {
                    best = n3;
                }
                else if (n4!=null)
                {
                    best = n4;
                }

                if (best==null)
                {
                    // if we didn't get any non NULL pointer, then all hope is lost :( --> return NULL
                    return best;
                }

                //get the least deep node : breadth first browsing
                //don't test for n1 because is best!=n1, then n1 is NULL
                if (n2!=null && n2.getNodeDepth() < best.getNodeDepth())
                {
                    best = n2;
                }
                if (n3!=null && n3.getNodeDepth() < best.getNodeDepth())
                {
                    best = n3;
                }
                if (n4!=null && n4.getNodeDepth() < best.getNodeDepth())
                {
                    best = n4;
                }

                if (best.getNodeDepth() > maxComputationDepth)
                {
                    return null;
                }
                else
                {
                    return best;
                }
            }
        }
        else
        {
            return null;
        }
    }
}
