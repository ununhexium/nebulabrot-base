package net.lab0.nebula.mgr;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.LongBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.exception.SerializationException;

import org.junit.Assert;
import org.junit.Test;

public class TestWriterManager
{
    private PointsBlockManager pointsBlockManager = new PointsBlockManager(1);
    
    @Test
    public void testWrite()
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
        
        Assert.assertTrue("The file is empty", path.toFile().length() > 0);
        
        // check written data
        try (
            FileInputStream in = new FileInputStream(path.toFile()))
        {
            byte[] buffer = new byte[1024 * (Double.SIZE/8)];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            in.read(buffer, 0, Integer.SIZE/8);
            Assert.assertEquals(1024, byteBuffer.asIntBuffer().get());
            
            in.read(buffer);
            byteBuffer.rewind();
            DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();
            real = 0;
            for (int i = 0; i < 1024; ++i)
            {
                Assert.assertEquals("Error at index " + i, real, doubleBuffer.get(), 0.0d);
                real += Math.PI;
            }
            
            in.read(buffer);
            doubleBuffer.rewind();
            imag = 0;
            for (int i = 0; i < 1024; ++i)
            {
                Assert.assertEquals("Error at index " + i, imag, doubleBuffer.get(), 0.0d);
                imag -= Math.PI;
            }
            
            in.read(buffer);
            LongBuffer longBuffer = byteBuffer.asLongBuffer();
            for (int i = 0; i < 1024; ++i)
            {
                Assert.assertEquals("Error at index " + i, i, longBuffer.get());
            }
        }
    }
}
