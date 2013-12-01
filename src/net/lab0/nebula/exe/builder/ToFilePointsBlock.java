package net.lab0.nebula.exe.builder;

import java.nio.file.Path;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.exe.PointsBlockWriter;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;

/**
 * This class is used to indicate the job that will take points blocks as an input and create the job, the points block
 * writer, that will output the block to a file.
 */
public class ToFilePointsBlock
implements JobBuilder<PointsBlock>
{
    /**
     * The path where the file will be written.
     */
    private final Path          outputPath;
    
    private final long          minimumIteration;
    private final long          maximumIteration;
    
    public ToFilePointsBlock(Path outputPath, long minimumIteration, long maximumIteration)
    {
        super();
        this.outputPath = outputPath;
        this.minimumIteration = minimumIteration;
        this.maximumIteration = maximumIteration;
    }
    
    public ToFilePointsBlock(Path outputPath, long minimumIteration)
    {
        this(outputPath, minimumIteration, Long.MAX_VALUE);
    }
    
    public ToFilePointsBlock(Path outputPath)
    {
        this(outputPath, Long.MIN_VALUE, Long.MAX_VALUE);
    }
    
    @Override
    public CascadingJob<PointsBlock, ?> buildJob(CascadingJob<?, PointsBlock> parent, PointsBlock output)
    {
        return new PointsBlockWriter(parent, output, outputPath, minimumIteration, maximumIteration);
    }
    
}
