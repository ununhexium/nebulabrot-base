package net.lab0.nebula.exe.builder;

import java.nio.file.Path;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.exe.PointsBlockWriter;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;

/**
 * This class is used to indicate the job that will take points blocks as an input and create the job, the points block
 * writer, that will output the block to a file.
 */
public class ToFile
implements JobBuilder<PointsBlock>
{
    /**
     * The writer manager is the class that will be used by several writers to gather the output to one unique file.
     */
    private final WriterManager writerManager;
    /**
     * The path where the file will be written.
     */
    private final Path          outputPath;
    
    private final long          minimumIteration;
    private final long          maximumIteration;
    
    public ToFile(WriterManager writerManager, Path outputPath, long minimumIteration, long maximumIteration)
    {
        super();
        this.writerManager = writerManager;
        this.outputPath = outputPath;
        this.minimumIteration = minimumIteration;
        this.maximumIteration = maximumIteration;
    }
    
    public ToFile(WriterManager writerManager, Path outputPath, long minimumIteration)
    {
        this(writerManager, outputPath, minimumIteration, Long.MAX_VALUE);
    }
    
    public ToFile(WriterManager writerManager, Path outputPath)
    {
        this(writerManager, outputPath, Long.MIN_VALUE, Long.MAX_VALUE);
    }
    
    @Override
    public CascadingJob<PointsBlock, ?> buildJob(CascadingJob<?, PointsBlock> parent, PointsBlock output)
    {
        /**
         * The priority of the next will be high than the one of the parent job. This is to ensure that children jobs
         * will be executed before the parents and avoid overflows due to a lot of child processes that will be executed
         * only when the parent finishes.
         */
        return new PointsBlockWriter(parent, output, outputPath, writerManager,
        minimumIteration, maximumIteration);
    }
    
}
