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
    
    public PointsBlockWriter(PriorityExecutor executor, int priority, PointsBlock pointsBlock, Path ouputPath,
    WriterManager writerManager)
    {
        super(executor, priority, pointsBlock, ouputPath);
        this.writerManager = writerManager;
    }

    @Override
    protected void save(PointsBlock data, Path outputPath)
    throws Exception
    {
        writerManager.write(data, outputPath);
        data.release();
    }
    
}
