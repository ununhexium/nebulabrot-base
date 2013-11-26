package net.lab0.nebula.exe;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.BitSet;

import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.enums.Status;
import net.lab0.tools.exec.Generator;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;

public class MandelbrotQuadTreeNodeReader
extends Generator<DataInputStream, MandelbrotQuadTreeNode[]>
{
    private int blockSize;
    
    public MandelbrotQuadTreeNodeReader(PriorityExecutor executor,
    JobBuilder<MandelbrotQuadTreeNode[]> jobBuilder, Path inputPath, int blockSize)
    throws FileNotFoundException
    {
        super(executor, jobBuilder, buildDataInputStream(inputPath));
        buildDataInputStream(inputPath);
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
    public MandelbrotQuadTreeNode[] generate(DataInputStream dataInputStream)
    throws Exception
    {
        MandelbrotQuadTreeNode[] nodes = new MandelbrotQuadTreeNode[blockSize];
        
        int read = 0;
        try
        {
            // try to read the desired amount of nodes
            for (int i = 0; i < blockSize; ++i)
            {
                int depth = dataInputStream.readInt();
                int bitSetLength = dataInputStream.readInt();
                byte[] buffer = new byte[bitSetLength];
                dataInputStream.read(buffer);
                int statusOrdinal = dataInputStream.readByte();
                long min = dataInputStream.readLong();
                long max = dataInputStream.readLong();
                
                nodes[read] = new MandelbrotQuadTreeNode(depth, BitSet.valueOf(buffer), min, max);
                nodes[read].status = Status.values()[statusOrdinal];
                
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
                MandelbrotQuadTreeNode[] nodes2 = new MandelbrotQuadTreeNode[read];
                for (int i = 0; i < read; ++i)
                {
                    nodes2[i] = nodes[i];
                }
                nodes = nodes2;
            }
        }
        
        return nodes;
    }
    
}
