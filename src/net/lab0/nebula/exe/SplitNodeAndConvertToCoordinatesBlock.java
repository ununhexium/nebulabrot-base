package net.lab0.nebula.exe;

import com.google.common.base.Predicate;

import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.data.MandelbrotQuadTreeNode.Coords;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.Splitter;

public class SplitNodeAndConvertToCoordinatesBlock
extends Splitter<MandelbrotQuadTreeNode[], CoordinatesBlock>
{
    private double                            step;
    private MandelbrotQuadTreeNode[]          input;
    private int                               currentNodeIndex = -1;
    private Predicate<MandelbrotQuadTreeNode> filter;
    
    public SplitNodeAndConvertToCoordinatesBlock(CascadingJob<?, MandelbrotQuadTreeNode[]> parent,
    JobBuilder<CoordinatesBlock> jobBuilder, MandelbrotQuadTreeNode[] input, double step,
    Predicate<MandelbrotQuadTreeNode> filter)
    {
        super(parent, jobBuilder);
        this.step = step;
        this.input = input;
        this.filter = filter;
    }
    
    @Override
    public CoordinatesBlock nextStep()
    throws Exception
    {
        do
        {
            currentNodeIndex++;
        } while (currentNodeIndex < input.length && !filter.apply(input[currentNodeIndex]));
        
        if (currentNodeIndex < input.length)
        {
            MandelbrotQuadTreeNode node = input[currentNodeIndex];
            Coords x = node.getX();
            Coords y = node.getY();
            CoordinatesBlock block = new CoordinatesBlock(x.getMin(), x.getMax(), y.getMin(), y.getMax(), step, step);
            return block;
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public boolean hasNext()
    {
        return currentNodeIndex < input.length;
    }
}
