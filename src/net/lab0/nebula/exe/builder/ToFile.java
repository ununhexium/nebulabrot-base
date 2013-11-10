package net.lab0.nebula.exe.builder;

import java.nio.file.Path;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.exe.PointsBlockWriter;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.JobBuilder;


/**
 * This class is used to indicate the job that will take points blocks as an input and create the job, the
 * points block writer, that will output the block to a file.
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
    
    public ToFile(WriterManager writerManager, Path outputPath)
    {
        super();
        this.writerManager = writerManager;
        this.outputPath = outputPath;
    }
    
    @Override
    public CascadingJob<PointsBlock, ?> buildJob(CascadingJob<?, PointsBlock> parent, PointsBlock output)
    {
        /**
         * The priority of the next will be high than the one of the parent job. This is to ensure that children
         * jobs will be executed before the parents and avoid overflows due to a lot of child processes that will be
         * executed only when the parent finishes.
         */
        return new PointsBlockWriter(parent.getExecutor(), parent.getPriority() + 1, output, outputPath,
        writerManager);
    }
    
}
