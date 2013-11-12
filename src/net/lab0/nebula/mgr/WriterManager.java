package net.lab0.nebula.mgr;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.exception.SerializationException;

public class WriterManager
{
    private Map<Path, FileOutputStream> mappings = new HashMap<>();
    /**
     * Lock: we want only 1 write operation at a time
     */
    private Lock                        lock     = new ReentrantLock();
    
    /**
     * Writes {@link PointsBlock} elements to a binary file. The structure is as follows:
     * 
     * <pre>
     * 4 bytes, int, the 'size' (net.lab0.nebula.data.PointsBlock.size).
     * double[size]  : the real array of the points block
     * double[size]  : the imag array of the points block
     * long[size]    : the iter array of the points block
     * </pre>
     * 
     * @param pointsBlock
     *            The block of points to serialize
     * @param output
     *            The location where the data must be written
     * @throws SerializationException
     *             if an error happens during this write operation.
     */
    public void write(PointsBlock pointsBlock, Path output)
    throws SerializationException
    {
        try
        {
            lock.lock();
            // need to check that all the array have the same size for the following operations
            if (!pointsBlock.isConsistent())
            {
                StringBuilder sb = new StringBuilder("The given points block is inconsistent: (size=");
                sb.append(pointsBlock.size);
                sb.append(", real.length=");
                sb.append(pointsBlock.real.length);
                sb.append(", imag.length=");
                sb.append(pointsBlock.imag.length);
                sb.append(", iter.length=");
                sb.append(pointsBlock.iter.length);
                sb.append(")");
                
                throw new IllegalArgumentException(sb.toString());
            }
            
            FileOutputStream out = getWriterFor(output);
            
            // size
            ByteBuffer sizeBuffer = ByteBuffer.wrap(new byte[Integer.SIZE / 8]);
            sizeBuffer.asIntBuffer().put(pointsBlock.size);
            sizeBuffer.flip();
            out.write(sizeBuffer.array());
            
            // arrays
            byte[] buffer = new byte[pointsBlock.size * (Double.SIZE / 8)];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            
            // real
            DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();
            doubleBuffer.clear();
            doubleBuffer.put(pointsBlock.real);
            out.write(buffer);
            
            // imag
            doubleBuffer.clear();
            doubleBuffer.put(pointsBlock.imag);
            byteBuffer.rewind();
            out.write(buffer);
            
            // iter
            LongBuffer longBuffer = byteBuffer.asLongBuffer();
            longBuffer.clear();
            longBuffer.put(pointsBlock.iter);
            out.write(buffer);
        }
        catch (FileNotFoundException e)
        {
            throw new SerializationException("Error when trying to get the data ouput stream", e);
        }
        catch (IOException e)
        {
            throw new SerializationException("Error while writing the data", e);
        }
        finally
        {
            lock.unlock();
        }
    }
    
    /**
     * Writes {@link RawMandelbrotData} elements to a binary file. The structure is as follows:
     * 
     * <pre>
     * 4 bytes, int, the 'width' (net.lab0.nebula.data.RawMandelbrotData.pixelWidth).
     * 4 bytes, int, the 'height' (net.lab0.nebula.data.RawMandelbrotData.pixelHeight).
     * int[width][height]: the data block, column by column
     * </pre>
     * 
     * @param pointsBlock
     *            The block of points to serialize
     * @param output
     *            The location where the data must be written
     * @throws SerializationException
     *             if an error happens during this write operation.
     */
    public void write(RawMandelbrotData data, Path output)
    throws SerializationException
    {
        try
        {
            lock.lock();
            
            FileOutputStream out = getWriterFor(output);
            
            // width / height
            ByteBuffer sizeBuffer = ByteBuffer.wrap(new byte[Integer.SIZE / 8 * 2]);
            IntBuffer intBuffer = sizeBuffer.asIntBuffer();
            intBuffer.put(data.getPixelWidth());
            intBuffer.put(data.getPixelHeight());
            sizeBuffer.flip();
            out.write(sizeBuffer.array());
            
            // data block
            final int width = data.getPixelWidth();
            byte[] buffer = new byte[data.getPixelHeight() * (Integer.SIZE / 8)];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            IntBuffer dataBuffer = byteBuffer.asIntBuffer();
            for (int i = 0; i < width; ++i)
            {
                dataBuffer.clear();
                dataBuffer.put(data.getData()[i]);
                dataBuffer.rewind();
                byteBuffer.rewind();
                out.write(buffer);
            }
        }
        catch (FileNotFoundException e)
        {
            throw new SerializationException("Error when trying to get the data ouput stream", e);
        }
        catch (IOException e)
        {
            throw new SerializationException("Error while writing the data", e);
        }
        finally
        {
            lock.unlock();
        }
    }
    
    /**
     * Use this method when there is nothing left ot write to that path. Releases the path (removes references to it,
     * allowing the GC to do its job.
     * 
     * @param path
     *            The path to release.
     * @return <code>true</code> if the release was successful, <code>false</code> otherwise.
     */
    public boolean release(Path path)
    {
        try
        {
            getWriterFor(path).close();
            mappings.remove(path);
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }
    
    /**
     * 
     * @param output
     * @return A {@link FileOutputStream} if it exists. Creates one if necessary.
     * @throws FileNotFoundException
     */
    private FileOutputStream getWriterFor(Path output)
    throws FileNotFoundException
    {
        if (output.toFile().isDirectory())
        {
            throw new IllegalArgumentException("The file " + output + " already exists and is a directory.");
        }
        FileOutputStream outputStream = mappings.get(output);
        if (outputStream == null)
        {
            outputStream = new FileOutputStream(output.toFile());
            mappings.put(output, outputStream);
        }
        return outputStream;
    }
}
