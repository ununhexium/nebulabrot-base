package net.lab0.nebula.exe;

import java.nio.file.Path;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.PriorityExecutor;

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
    private WriterManager writerManager;
    private long          minimumIteration;
    private long          maximumIteration;
    
    /**
     * Writes points if the number of iterations IT it has is such as <code>minimumIteration</code> <= IT <=
     * <code>maximumIteration</code>.
     * 
     * @param minimumIteration
     *            The minimum number of iteration a point must have to be written
     * @param maximumIteration
     *            The maximum number of iteration a point must have to be written
     */
    public PointsBlockWriter(PriorityExecutor executor, int priority, PointsBlock pointsBlock, Path ouputPath,
    WriterManager writerManager, long minimumIteration, long maximumIteration)
    {
        super(executor, priority, pointsBlock, ouputPath);
        this.writerManager = writerManager;
        this.minimumIteration = minimumIteration;
        this.maximumIteration = maximumIteration;
    }
    
    public PointsBlockWriter(PriorityExecutor executor, int priority, PointsBlock pointsBlock, Path ouputPath,
    WriterManager writerManager)
    {
        this(executor, priority, pointsBlock, ouputPath, writerManager, Long.MIN_VALUE, Long.MAX_VALUE);
    }
    
    @Override
    protected void save(PointsBlock data, Path outputPath)
    throws Exception
    {
        writerManager.write(data, outputPath, minimumIteration, maximumIteration);
        data.release();
    }
    
}
