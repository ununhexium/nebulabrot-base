package net.lab0.nebula.mgr;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.BitSet;

import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.enums.PositionInParent;
import net.lab0.nebula.enums.Status;
import net.lab0.nebula.exception.SerializationException;
import net.lab0.tools.StaticRandom;

import org.junit.Assert;
import org.junit.Test;

public class TestWriterManager
{
    /**
     * Tests net.lab0.nebula.mgr.WriterManager#write(PointsBlock, Path)
     */
    @Test
    public void testWritePointsBlock()
    throws SerializationException, IOException
    {
        PointsBlock block = new PointsBlock(1024);
        
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
        
        WriterManager writerManager = WriterManager.getInstance();
        Path path = FileSystems.getDefault().getPath("test", "write_manager", "test_file1.data");
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
    
    /**
     * Tests net.lab0.nebula.mgr.WriterManager#write(PointsBlock, Path, long, long)
     */
    @Test
    public void testWritePointsBlock2()
    throws SerializationException, IOException
    {
        PointsBlock block = new PointsBlock(1024);
        
        double real = 0;
        double imag = 0;
        for (int i = 0; i < 1024; ++i)
        {
            block.real[i] = (double) i * Math.PI;
            block.imag[i] = (double) i * -Math.PI;
            block.iter[i] = i;
        }
        
        WriterManager writerManager = WriterManager.getInstance();
        Path path = FileSystems.getDefault().getPath("test", "write_manager", "test_file1.data");
        path.toFile().getParentFile().mkdirs();
        writerManager.write(block, path, 256, 512 + 256);
        writerManager.release(path);
        
        Assert.assertTrue("The file is empty", path.toFile().length() > 0);
        
        // check written data
        try (
            DataInputStream in = new DataInputStream(new FileInputStream(path.toFile())))
        {
            for (int i = 256; i <= 512 + 256; ++i)
            {
                real = i * Math.PI;
                imag = i * -Math.PI;
                Assert.assertEquals("Error at index " + i, real, in.readDouble(), 0.0d);
                Assert.assertEquals("Error at index " + i, imag, in.readDouble(), 0.0d);
                Assert.assertEquals("Error at index " + i, i, in.readLong());
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
        
        WriterManager writerManager = WriterManager.getInstance();
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
    
    @Test
    public void testWriteCoordinatesBlock()
    throws SerializationException, IOException
    {
        CoordinatesBlock[] blocks = new CoordinatesBlock[10];
        for (int i = 0; i < 10; ++i)
        {
            blocks[i] = new CoordinatesBlock(0, 0, 0, 0, 0, 0);
            blocks[i].minX = Math.PI * i;
            blocks[i].maxX = -Math.PI * i;
            blocks[i].minY = Math.E * i;
            blocks[i].maxY = -Math.E * i;
            blocks[i].stepX = 1.1 * i;
            blocks[i].stepY = -1.1 * i;
        }
        
        WriterManager writerManager = WriterManager.getInstance();
        Path path = FileSystems.getDefault().getPath("test", "write_manager", "test_file3.data");
        path.toFile().getParentFile().mkdirs();
        writerManager.write(blocks, path);
        writerManager.release(path);
        
        Assert.assertTrue("The file is empty", path.toFile().length() > 0);
        
        try (
            DataInputStream in = new DataInputStream(new FileInputStream(path.toFile())))
        {
            for (int i = 0; i < 10; ++i)
            {
                Assert.assertEquals(in.readDouble(), Math.PI * i, 0.0);
                Assert.assertEquals(in.readDouble(), -Math.PI * i, 0.0);
                Assert.assertEquals(in.readDouble(), Math.E * i, 0.0);
                Assert.assertEquals(in.readDouble(), -Math.E * i, 0.0);
                Assert.assertEquals(in.readDouble(), 1.1 * i, 0.0);
                Assert.assertEquals(in.readDouble(), -1.1 * i, 0.0);
            }
        }
    }
    
    @Test
    public void testWriteMandelbrotQuadTreeNodeBlock()
    throws SerializationException, FileNotFoundException, IOException
    {
        MandelbrotQuadTreeNode[] nodes = new MandelbrotQuadTreeNode[10];
        BitSet[] paths = new BitSet[10];
        for (int i = 0; i < 10; ++i)
        {
            int[] randoms = StaticRandom.randomArray(10, PositionInParent.TopLeft.ordinal(),
            PositionInParent.Root.ordinal());
            randoms[0] = PositionInParent.Root.ordinal();
            
            PositionInParent[] pathAsEnum = new PositionInParent[10];
            for (int j = 0; j < 10; ++j)
            {
                pathAsEnum[j] = PositionInParent.values()[randoms[j]];
            }
            
            paths[i] = MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(pathAsEnum).getPath();
            Status status = null;
            switch (i % 4)
            {
                case 0:
                    status = Status.BROWSED;
                    break;
                case 1:
                    status = Status.INSIDE;
                    break;
                case 2:
                    status = Status.OUTSIDE;
                    break;
                case 3:
                    status = Status.VOID;
                    break;
            }
            nodes[i] = new MandelbrotQuadTreeNode(i, paths[i], 100 * i, 200 * i, status);
        }
        
        WriterManager writerManager = WriterManager.getInstance();
        Path output = FileSystems.getDefault().getPath("test", "write_manager", "test_file2.data");
        output.toFile().getParentFile().mkdirs();
        writerManager.write(nodes, output);
        writerManager.release(output);
        
        try (
            DataInputStream in = new DataInputStream(new FileInputStream(output.toFile())))
        {
            for (int i = 0; i < 10; ++i)
            {
                Assert.assertEquals(i, in.readInt());
                int bitSetLength = in.readInt();
                // if (i < 8)
                // {
                // Assert.assertEquals(1, bitSetLength);
                // }
                // else
                // {
                // Assert.assertEquals(2, bitSetLength);
                // }
                byte[] buffer = new byte[bitSetLength];
                in.read(buffer);
                Assert.assertArrayEquals(buffer, paths[i].toByteArray());
                Assert.assertEquals(i % 4, in.readByte());
                Assert.assertEquals(100 * i, in.readLong());
                Assert.assertEquals(200 * i, in.readLong());
            }
        }
    }
}
