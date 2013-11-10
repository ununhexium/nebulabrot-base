package net.lab0.nebula.exe;

import java.nio.file.Path;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.exception.SerializationException;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.PriorityExecutor;

/**
 * A job that will aggregate the points block and serialize them into a file. This step is the last step of a
 * {@link PointsBlock}. The input blocks will be released.
 * 
 * @author 116
 * 
 */
public class PointsBlockWriter
extends CascadingJob<PointsBlock, Void>
{
    private PointsBlock   pointsBlock;
    private Path          outputPath;
    private WriterManager writerManager;
    
    public PointsBlockWriter(PriorityExecutor executor, int priority, PointsBlock pointsBlock, Path ouputPath,
    WriterManager writerManager)
    {
        super(executor, priority, null);
        this.pointsBlock = pointsBlock;
        this.outputPath = ouputPath;
        this.writerManager = writerManager;
    }
    
    @Override
    public void executeTask()
    throws SerializationException
    {
        writerManager.write(pointsBlock, outputPath);
        pointsBlock.release();
        System.out.println("out file");
    }
    
}
