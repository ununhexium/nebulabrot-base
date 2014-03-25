package net.lab0.nebula.exe;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import net.lab0.nebula.All;
import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.enums.Status;
import net.lab0.nebula.exception.SerializationException;
import net.lab0.nebula.exe.builder.BuilderFactory;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.PriorityExecutor;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMandelbrotQuadTreeNodeReader
{
    private static Path   path  = All.getTestFolderPath(TestMandelbrotQuadTreeNodeReader.class);
    private static Path   path1 = path.resolve("node1");
    private static Path   path2 = path.resolve("node2");
    private static BitSet bitSet;
    
    @BeforeClass
    public static void beforeClass()
    throws SerializationException
    {
        bitSet = new BitSet();
        bitSet.flip(0, 64);
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(0, bitSet, 1, Long.MAX_VALUE, Status.VOID);
        
        WriterManager.getInstance().write(node, path1);
        WriterManager.getInstance().release(path1);
        
        int size = 100;
        MandelbrotQuadTreeNode[] nodes = new MandelbrotQuadTreeNode[size];
        for (int i = 0; i < size; ++i)
        {
            byte[] bytes = new byte[4];
            IntBuffer buffer = ByteBuffer.wrap(bytes).asIntBuffer();
            buffer.put(i);
            Status status = Status.values()[i % Status.values().length];
            nodes[i] = new MandelbrotQuadTreeNode(i, BitSet.valueOf(bytes), i, i + 116, status);
        }
        
        WriterManager.getInstance().write(nodes, path2);
        WriterManager.getInstance().release(path2);
    }
    
    /**
     * Tests the reading of a single element
     * @throws FileNotFoundException 
     * @throws InterruptedException 
     */
    @Test
    public void testSingleRead()
    throws FileNotFoundException, InterruptedException
    {
        PriorityExecutor executor = new PriorityExecutor();
        List<MandelbrotQuadTreeNode[]> dump = new ArrayList<>();
        MandelbrotQuadTreeNodeReader reader = new MandelbrotQuadTreeNodeReader(executor, BuilderFactory.toDumper(dump),
        path1, 10);
        
        executor.execute(reader);
        executor.waitForFinish();
        
        Assert.assertEquals(1, dump.size());
        MandelbrotQuadTreeNode[] array = dump.get(0);
        Assert.assertNotNull(array);
        Assert.assertEquals(1, array.length);
        
        MandelbrotQuadTreeNode node = array[0];
        
        Assert.assertEquals(0, node.nodePath.depth);
        Assert.assertEquals(bitSet, node.nodePath.path);
        Assert.assertEquals(1, node.minimumIteration);
        Assert.assertEquals(Long.MAX_VALUE, node.maximumIteration);
        Assert.assertEquals(Status.VOID, node.status);
    }
    
    /**
     * Tests The reading of multiples chunks of a file.
     * @throws FileNotFoundException 
     * @throws InterruptedException 
     */
    @Test
    public void testMultipleRead()
    throws FileNotFoundException, InterruptedException
    {
        PriorityExecutor executor = new PriorityExecutor();
        List<MandelbrotQuadTreeNode[]> dump = new ArrayList<>();
        MandelbrotQuadTreeNodeReader reader = new MandelbrotQuadTreeNodeReader(executor, BuilderFactory.toDumper(dump),
        path2, 10);
        
        executor.execute(reader);
        executor.waitForFinish();
        
        Assert.assertEquals(10, dump.size());
        for (int i = 0; i < 10; ++i)
        {
            MandelbrotQuadTreeNode[] array = dump.get(i);
            Assert.assertNotNull(array);
            Assert.assertEquals(10, array.length);
            
            for (int j = 0; j < 10; ++j)
            {
                MandelbrotQuadTreeNode node = array[j];
                
                int originalIndex = 10 * i + j;
                byte[] bytes = new byte[4];
                IntBuffer buffer = ByteBuffer.wrap(bytes).asIntBuffer();
                buffer.put(originalIndex);
                BitSet bitSet = BitSet.valueOf(bytes);
                Status status = Status.values()[originalIndex % Status.values().length];
                
                Assert.assertEquals(originalIndex, node.nodePath.depth);
                Assert.assertEquals(bitSet, node.nodePath.path);
                Assert.assertEquals(originalIndex, node.minimumIteration);
                Assert.assertEquals(originalIndex + 116, node.maximumIteration);
                Assert.assertEquals(status, node.status);
            }
        }
        
    }
}
