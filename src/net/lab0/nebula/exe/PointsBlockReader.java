package net.lab0.nebula.exe;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.LongBuffer;
import java.nio.file.Path;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.mgr.PointsBlockManager;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.MultipleOutputJob;
import net.lab0.tools.exec.PriorityExecutor;

/**
 * A job that reads points blocks from a file and output them one by one
 * 
 * @author 116
 * 
 */
public class PointsBlockReader
extends MultipleOutputJob<Void, PointsBlock>
{
    private FileInputStream    fileInputStream;
    // needs to contains at least 1 integer
    private byte[]             buffer = new byte[Integer.SIZE / 8];
    private PointsBlockManager pointsBlockManager;
    
    public PointsBlockReader(PriorityExecutor executor, int priority, JobBuilder<PointsBlock> jobBuilder,
    Path inputPath, PointsBlockManager pointsBlockManager)
    throws FileNotFoundException
    {
        super(executor, priority, jobBuilder);
        this.pointsBlockManager = pointsBlockManager;
        this.fileInputStream = new FileInputStream(inputPath.toFile());
    }
    
    @Override
    public PointsBlock nextStep()
    throws IOException
    {
        int read = fileInputStream.read(buffer, 0, Integer.SIZE / 8);
        if (read < 0)
        {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        int size = byteBuffer.asIntBuffer().get();
        pointsBlockManager.allocatePointsBlock(size);
        
        // check that the buffer is long enough
        int required = size * (Double.SIZE / 8);
        if (buffer.length < required)
        {
            buffer = new byte[required];
            byteBuffer = ByteBuffer.wrap(buffer);
        }
        
        PointsBlock pointsBlock = pointsBlockManager.allocatePointsBlock(size);
        
        // real
        fileInputStream.read(buffer, 0, required);
        DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();
        doubleBuffer.rewind();
        pointsBlock.copyReal(doubleBuffer);
        
        // imag
        fileInputStream.read(buffer, 0, required);
        doubleBuffer.rewind();
        pointsBlock.copyImag(doubleBuffer);
        
        // iter
        fileInputStream.read(buffer, 0, required);
        LongBuffer longBuffer = byteBuffer.asLongBuffer();
        longBuffer.rewind();
        pointsBlock.copyIter(longBuffer);
        
        return pointsBlock;
    }
}
