package net.lab0.nebula.exe;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.tools.exec.Generator;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;

/**
 * A job that reads points from a file and output them as blocks
 * 
 * @author 116
 * 
 */
public class PointsBlockReader
extends Generator<DataInputStream, PointsBlock>
{
    private int                blockSize;
    
    public PointsBlockReader(PriorityExecutor executor, JobBuilder<PointsBlock> jobBuilder,
    Path inputPath, int blockSize)
    throws FileNotFoundException
    {
        super(executor, jobBuilder, buildDataInputStream(inputPath));
        this.blockSize = blockSize;
    }
    
    private static DataInputStream buildDataInputStream(Path inputPath)
    throws FileNotFoundException
    {
        File file = inputPath.toFile();
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        return new DataInputStream(bufferedInputStream);
    }
    
    @Override
    public PointsBlock generate(DataInputStream dataInputStream)
    throws IOException
    {
        // ignore the path: not needed
        PointsBlock pointsBlock = new PointsBlock(blockSize);
        
        int read = 0;
        try
        {
            // try to read the desired amount of points
            for (int i = 0; i < blockSize; ++i)
            {
                pointsBlock.real[i] = dataInputStream.readDouble();
                pointsBlock.imag[i] = dataInputStream.readDouble();
                pointsBlock.iter[i] = dataInputStream.readLong();
                read++;
            }
        }
        catch (EOFException e)
        {
            /*
             * We reached the end of the file. is it because we didn't read anything ? we would be at the end of the
             * input file ; or because there was not enough to fill a complete block ? we need to copy the data to a
             * buffer with the appropriate size.
             */
            if (read == 0)
            {
                return null;// job's finished, sir :)
            }
            else
            {
                /*
                 * reached the end of the file before the usual block size -> create a new block containing the points
                 * only that were read only.
                 */
                PointsBlock tmp = new PointsBlock(read);
                for (int i = 0; i < read; ++i)
                {
                    tmp.real[i] = pointsBlock.real[i];
                    tmp.imag[i] = pointsBlock.imag[i];
                    tmp.iter[i] = pointsBlock.iter[i];
                }
                pointsBlock = tmp;
            }
        }
        
        return pointsBlock;
    }
}
