package net.lab0.nebula.mgr;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.exception.SerializationException;

import org.junit.Assert;
import org.junit.Test;

public class TestWriterManager
{
    private PointsBlockManager pointsBlockManager = new PointsBlockManager(1);
    
    @Test
    public void testWritePointsBlock()
    throws SerializationException, IOException
    {
        PointsBlock block = new PointsBlock(1024, pointsBlockManager);
        
        double real = 0;
        double imag = 0;
        for (int i = 0; i < 1024; ++i)
        {
            block.real[i] = real;
            block.imag[i] = imag;
            block.iter[i] = i;
            real += Math.PI;
            imag -= Math.PI;
        }
        
        WriterManager writerManager = new WriterManager();
        Path path = FileSystems.getDefault().getPath("test", "write_manager", "test_file.data");
        path.toFile().getParentFile().mkdirs();
        writerManager.write(block, path);
        writerManager.release(path);
        
        Assert.assertTrue("The file is empty", path.toFile().length() > 0);
        
        // check written data
        try (
            DataInputStream in = new DataInputStream(new FileInputStream(path.toFile())))
        {
            real = 0;
            imag = 0;
            for (int i = 0; i < 1024; ++i)
            {
                Assert.assertEquals("Error at index " + i, real, in.readDouble(), 0.0d);
                Assert.assertEquals("Error at index " + i, imag, in.readDouble(), 0.0d);
                Assert.assertEquals("Error at index " + i, i, in.readLong());
                real += Math.PI;
                imag -= Math.PI;
            }
        }
    }
    
    @Test
    public void testWriteRawMandelbrotData()
    throws SerializationException, IOException
    {
        int W = 64;
        int H = 128;
        RawMandelbrotData mandelbrotData = new RawMandelbrotData(W, H, 0);
        int[][] data = mandelbrotData.getData();
        for (int x = 0; x < W; ++x)
        {
            for (int y = 0; y < H; ++y)
            {
                data[x][y] = y + H * x;
            }
        }
        
        WriterManager writerManager = new WriterManager();
        Path output = FileSystems.getDefault().getPath("test", "write_manager", "test_file2.data");
        output.toFile().getParentFile().mkdirs();
        writerManager.write(mandelbrotData, output);
        writerManager.release(output);
        
        Assert.assertTrue("The file is empty", output.toFile().length() > 0);
        try (
            FileInputStream in = new FileInputStream(output.toFile()))
        {
            byte[] buffer = new byte[Integer.SIZE / 8 * H];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            in.read(buffer, 0, Integer.SIZE / 8 * 2);
            Assert.assertEquals(W, byteBuffer.asIntBuffer().get(0));
            Assert.assertEquals(H, byteBuffer.asIntBuffer().get(1));
            
            byteBuffer.rewind();
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            int current = 0;
            int read = 0;
            while ((read = in.read(buffer)) >= 0)
            {
                intBuffer.rewind();
                for (int i = 0; i < read * 8 / Integer.SIZE; ++i)
                {
                    int tested = intBuffer.get();
                    Assert.assertEquals("Error at " + i + " expected " + current + " but was " + tested, current,
                    tested);
                    current++;
                }
            }
        }
    }
}
