package net.lab0.nebula.exe;

import java.nio.file.Path;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.CascadingJob;

/**
 * A job that will aggregate the points block and serialize them into a file. This step is the last step of a
 * {@link PointsBlock}. The input blocks will be released.
 * 
 * @author 116
 * 
 */
public class PointsBlockWriter
extends Writer<PointsBlock>
{
    private long minimumIteration;
    private long maximumIteration;
    
    /**
     * Writes points if the number of iterations IT it has is such as <code>minimumIteration</code> <= IT <=
     * <code>maximumIteration</code>.
     * 
     * @param parentJob
     *            The parent job
     * @param pointsBlock
     *            The block to compute
     * @param ouputPath
     *            Where the block will be saved
     * 
     * @param minimumIteration
     *            The minimum number of iteration a point must have to be written
     * @param maximumIteration
     *            The maximum number of iteration a point must have to be written
     */
    public PointsBlockWriter(CascadingJob<?, PointsBlock> parentJob, PointsBlock pointsBlock, Path ouputPath,
    long minimumIteration, long maximumIteration)
    {
        super(parentJob, pointsBlock, ouputPath);
        this.minimumIteration = minimumIteration;
        this.maximumIteration = maximumIteration;
    }
    
    public PointsBlockWriter(CascadingJob<?, PointsBlock> parentJob, PointsBlock pointsBlock, Path ouputPath)
    {
        this(parentJob, pointsBlock, ouputPath, Long.MIN_VALUE, Long.MAX_VALUE);
    }
    
    @Override
    protected void save(PointsBlock data, Path outputPath)
    throws Exception
    {
        WriterManager.getInstance().write(data, outputPath, minimumIteration, maximumIteration);
    }
    
}
