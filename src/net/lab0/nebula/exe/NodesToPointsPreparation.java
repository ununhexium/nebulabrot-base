package net.lab0.nebula.exe;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.SimpleJob;

import com.google.common.primitives.Doubles;

/**
 * Prepares the data that has to be computed. Uses the provided nodes, filters them to use the appropriate ones and
 * outputs the points that must be computed into points blocks.
 * 
 * @author 116
 * 
 */
public class NodesToPointsPreparation
extends SimpleJob<MandelbrotQuadTreeNode, PointsBlock>
{
    private BigDecimal         step;
    
    public NodesToPointsPreparation(CascadingJob<?, MandelbrotQuadTreeNode> parent, JobBuilder<PointsBlock> jobBuilder,
    MandelbrotQuadTreeNode input)
    {
        super(parent, jobBuilder, input);
    }
    
    @Override
    public PointsBlock singleStep(MandelbrotQuadTreeNode input)
    {
        // double xStart = Math.ceil(finalNode.getMinX() / stepX) * stepX;
        BigDecimal minX = new BigDecimal(String.valueOf(input.getX().getMin()));
        BigDecimal maxX = new BigDecimal(String.valueOf(input.getX().getMax()));
        BigDecimal divisionAndCeilX = minX.divide(step).setScale(0, RoundingMode.CEILING);
        BigDecimal xStart = divisionAndCeilX.multiply(step);
        xStart.setScale(34); // float 128 bits

        // double yStart = Math.ceil(finalNode.getMinY() / stepY) * stepY;
        BigDecimal minY = new BigDecimal(String.valueOf(input.getY().getMin()));
        BigDecimal maxY = new BigDecimal(String.valueOf(input.getY().getMax()));
        BigDecimal divisionAndCeilY = minY.divide(step).setScale(0, RoundingMode.CEILING);
        BigDecimal yStart = divisionAndCeilY.multiply(step);
        yStart.setScale(34); // float 128 bits
        
        BigDecimal real = xStart;
        
        List<Double> x = new ArrayList<>();
        List<Double> y = new ArrayList<>();
        while (real.compareTo(maxX) < 0)
        {
            BigDecimal img = yStart;
            while (img.compareTo(maxY) < 0)
            {
                x.add(real.doubleValue());
                y.add(img.doubleValue());
                
                img = img.add(step);
            }
            
            real = real.add(step);
        }
        
        PointsBlock block = new PointsBlock(x.size(), Doubles.toArray(x), Doubles.toArray(y));
        return block;
    }
}
