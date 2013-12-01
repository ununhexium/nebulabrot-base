package net.lab0.nebula.exe;

import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.Splitter;

public class MandelbrotQuadTreeNodeSplitter
extends Splitter<MandelbrotQuadTreeNode[], MandelbrotQuadTreeNode>
{
    public interface SplittingCriterion
    {
        /**
         * @param node
         *            The node to test
         * @return <code>true</code> if this node must be split.
         */
        public boolean doWeSplitIt(MandelbrotQuadTreeNode node);
    }
    
    private SplittingCriterion       splittingCriteria;
    private MandelbrotQuadTreeNode[] input;
    private MandelbrotQuadTreeNode[] tmpSubNodes;
    private int                      currentNodeIndex;
    private int                      currentSubNodeIndex;
    
    public MandelbrotQuadTreeNodeSplitter(CascadingJob<?, MandelbrotQuadTreeNode[]> parent,
    JobBuilder<MandelbrotQuadTreeNode> jobBuilder, MandelbrotQuadTreeNode[] input, SplittingCriterion splittingCriteria)
    {
        super(parent, jobBuilder);
        if (input.length == 0)
        {
            throw new IllegalArgumentException("The length of the input must be positive");
        }
        this.input = input;
        this.splittingCriteria = splittingCriteria;
    }
    
    @Override
    public MandelbrotQuadTreeNode nextStep()
    throws Exception
    {
        if (currentSubNodeIndex == 4 || tmpSubNodes == null)
        {
            while (currentNodeIndex < input.length && !splittingCriteria.doWeSplitIt(input[currentNodeIndex]))
            {
                currentNodeIndex++;
                // skip until there is a node to split
            }
            if (currentNodeIndex < input.length)
            {
                currentSubNodeIndex = 0;
                tmpSubNodes = input[currentNodeIndex].split();
                currentNodeIndex++;
            }
            else
            {
                return null;
            }
        }
        
        MandelbrotQuadTreeNode returned = tmpSubNodes[currentSubNodeIndex];
        currentSubNodeIndex++;
        return returned;
    }
    
    @Override
    public boolean hasNext()
    {
        return currentNodeIndex != input.length || currentSubNodeIndex != 4;
    }
    
}
