package net.lab0.nebula.exe.builder;

import java.nio.file.Path;

import com.google.common.base.Predicate;

import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.exe.ComputeInOutForQuadTreeNode;
import net.lab0.nebula.exe.CoordinatesToPointsBlockConverter;
import net.lab0.nebula.exe.MandelbrotQuadTreeNodeSplitter;
import net.lab0.nebula.exe.PointsBlockOCLIterationComputing;
import net.lab0.nebula.exe.PointsBlockWriter;
import net.lab0.nebula.exe.QuadTreeNodeArrayWriter;
import net.lab0.nebula.exe.QuadTreeNodeWriter;
import net.lab0.nebula.exe.SplitNodeAndConvertToCoordinatesBlock;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;

public class BuilderFactory
{
    public static synchronized JobBuilder<MandelbrotQuadTreeNode[]> arrayToFile(final Path outputPath)
    {
        return new JobBuilder<MandelbrotQuadTreeNode[]>()
        {
            @Override
            public CascadingJob<MandelbrotQuadTreeNode[], ?> buildJob(CascadingJob<?, MandelbrotQuadTreeNode[]> parent,
            MandelbrotQuadTreeNode[] output)
            {
                return new QuadTreeNodeArrayWriter(parent, output, outputPath);
            }
        };
    }
    
    public static synchronized JobBuilder<MandelbrotQuadTreeNode> toFile(final Path outputPath)
    {
        return new JobBuilder<MandelbrotQuadTreeNode>()
        {
            @Override
            public CascadingJob<MandelbrotQuadTreeNode, ?> buildJob(CascadingJob<?, MandelbrotQuadTreeNode> parent,
            MandelbrotQuadTreeNode output)
            {
                return new QuadTreeNodeWriter(parent, output, outputPath);
            }
        };
    }
    
    public static synchronized JobBuilder<PointsBlock> toPointsBlocksFile(final Path outputPath)
    {
        return new JobBuilder<PointsBlock>()
        {
            @Override
            public CascadingJob<PointsBlock, ?> buildJob(CascadingJob<?, PointsBlock> parent, PointsBlock output)
            {
                return new PointsBlockWriter(parent, output, outputPath);
            }
        };
    }
    
    public static synchronized JobBuilder<PointsBlock> toPointsBlocksFile(final Path outputPath,
    final long minimumIteration, final long maximumIteration)
    {
        return new JobBuilder<PointsBlock>()
        {
            @Override
            public CascadingJob<PointsBlock, ?> buildJob(CascadingJob<?, PointsBlock> parent, PointsBlock output)
            {
                return new PointsBlockWriter(parent, output, outputPath, minimumIteration, maximumIteration);
            }
        };
    }
    
    public static synchronized JobBuilder<MandelbrotQuadTreeNode[]> toMandelbrotQuadTreeNodeSplitter(
    final JobBuilder<MandelbrotQuadTreeNode> nextJob,
    final MandelbrotQuadTreeNodeSplitter.SplittingCriterion splittingCriterion)
    {
        return new JobBuilder<MandelbrotQuadTreeNode[]>()
        {
            @Override
            public CascadingJob<MandelbrotQuadTreeNode[], ?> buildJob(CascadingJob<?, MandelbrotQuadTreeNode[]> parent,
            MandelbrotQuadTreeNode[] output)
            {
                return new MandelbrotQuadTreeNodeSplitter(parent, nextJob, output, splittingCriterion);
            }
        };
    }
    
    public static JobBuilder<CoordinatesBlock> toCoordinatesToPointsBlockConverter(
    final JobBuilder<PointsBlock> jobBuilder, final int pointsBlockSize)
    {
        return new JobBuilder<CoordinatesBlock>()
        {
            @Override
            public CascadingJob<CoordinatesBlock, ?> buildJob(CascadingJob<?, CoordinatesBlock> parent,
            CoordinatesBlock output)
            {
                return new CoordinatesToPointsBlockConverter(parent, jobBuilder, output, pointsBlockSize);
            }
        };
    }
    
    public static JobBuilder<MandelbrotQuadTreeNode[]> toNodeSplitterAndConverter(
    final JobBuilder<CoordinatesBlock> jobBuilder, final double step, final Predicate<MandelbrotQuadTreeNode> filter)
    {
        return new JobBuilder<MandelbrotQuadTreeNode[]>()
        {
            @Override
            public CascadingJob<MandelbrotQuadTreeNode[], ?> buildJob(CascadingJob<?, MandelbrotQuadTreeNode[]> parent,
            MandelbrotQuadTreeNode[] output)
            {
                return new SplitNodeAndConvertToCoordinatesBlock(parent, jobBuilder, output, step, filter);
            }
        };
    }
    
    public static JobBuilder<PointsBlock> toOCLCompute(final JobBuilder<PointsBlock> jobBuilder,
    final long maximumIteration)
    {
        return new JobBuilder<PointsBlock>()
        {
            @Override
            public CascadingJob<PointsBlock, ?> buildJob(CascadingJob<?, PointsBlock> parent, PointsBlock output)
            {
                return new PointsBlockOCLIterationComputing(parent, jobBuilder, output, maximumIteration);
            }
        };
    }
    
    public static JobBuilder<MandelbrotQuadTreeNode> toComputeInOut(
    final JobBuilder<MandelbrotQuadTreeNode> jobBuilder, final long maximumIteration, final int sidePointsCount,
    final long iterationDifferenceLimit)
    {
        return new JobBuilder<MandelbrotQuadTreeNode>()
        {
            @Override
            public CascadingJob<MandelbrotQuadTreeNode, ?> buildJob(CascadingJob<?, MandelbrotQuadTreeNode> parent,
            MandelbrotQuadTreeNode output)
            {
                return new ComputeInOutForQuadTreeNode(parent, jobBuilder, output, maximumIteration, sidePointsCount,
                iterationDifferenceLimit);
            }
            
        };
    }
    
}
