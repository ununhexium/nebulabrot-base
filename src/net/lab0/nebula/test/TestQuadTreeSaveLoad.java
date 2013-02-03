package net.lab0.nebula.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import junit.framework.Assert;
import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for the binary file saving format
 * 
 * @author 116@lab0.net
 * 
 */
@RunWith(JUnit4.class)
public class TestQuadTreeSaveLoad
{
    private static final Path      testFolder      = FileSystems.getDefault().getPath(".", "test_folder");
    private static int             i               = 0;
    
    private static QuadTreeManager quadTreeManager = null;
    private static Path            savePath        = null;
    
    @BeforeClass
    public static void generateQuadTree()
    throws InterruptedException, IOException
    {
        // ensure the existence of the test directory in order to avoid NullPointerException at the listing of the files of this directory
        if (!testFolder.toFile().exists())
        {
            if (!testFolder.toFile().mkdirs())
            {
                throw new IOException("Could not create the test folder");
            }
        }
        
        // init a basic tree to have something to save and compare with
        quadTreeManager = new QuadTreeManager(new QuadTreeNode(-2.0, 2.0, -2.0, 2.0), 64, 256, 3, 10);
        quadTreeManager.setThreads(Runtime.getRuntime().availableProcessors());
        quadTreeManager.compute(99999);
    }
    
    @Before
    public void initSavePath()
    {
        // find a non existing subfolder
        do
        {
            savePath = FileSystems.getDefault().getPath(".", "test_folder", Integer.toString(i));
            ++i;
        } while (savePath.toFile().exists());
        savePath.toFile().mkdirs();
    }
    
    @Test
    public void testXml()
    throws IOException, ValidityException, ClassNotFoundException, ParsingException, InvalidBinaryFileException, NoSuchAlgorithmException
    {
        quadTreeManager.saveToXML(savePath);
        QuadTreeManager manager = new QuadTreeManager(savePath, null);
        QuadTreeNode root = manager.getQuadTreeRoot();
        assertTrue(root.testIsExactlyTheSameAs(quadTreeManager.getQuadTreeRoot(), false));
    }
    
    @Test
    public void testIndexedBinary()
    throws IOException, ValidityException, ClassNotFoundException, ParsingException, InvalidBinaryFileException, NoSuchAlgorithmException
    {
        quadTreeManager.saveToBinaryFile(savePath, true);
        QuadTreeManager manager = new QuadTreeManager(savePath, null);
        QuadTreeNode root = manager.getQuadTreeRoot();
        assertTrue(root.testIsExactlyTheSameAs(quadTreeManager.getQuadTreeRoot(), false));
    }
    
    @Test
    public void testNonIndexedBinary()
    throws IOException, ValidityException, ClassNotFoundException, ParsingException, InvalidBinaryFileException, NoSuchAlgorithmException
    {
        quadTreeManager.saveToBinaryFile(savePath, false);
        QuadTreeManager manager = new QuadTreeManager(savePath, null);
        QuadTreeNode root = manager.getQuadTreeRoot();
        assertTrue(root.testIsExactlyTheSameAs(quadTreeManager.getQuadTreeRoot(), false));
    }
    
    @Test
    public void testXmlLoadDepthLimit()
    throws IOException, ValidityException, ClassNotFoundException, NoSuchAlgorithmException, ParsingException, InvalidBinaryFileException
    {
        quadTreeManager.saveToXML(savePath);
        int maxDepth = 5;
        QuadTreeManager manager = new QuadTreeManager(savePath, null, maxDepth);
        QuadTreeNode root = manager.getQuadTreeRoot();
        Assert.assertEquals(maxDepth, root.getMaxNodeDepth());
    }
    
    @Test
    public void testBinaryNoIndexLoadDepthLimit()
    throws IOException, ValidityException, ClassNotFoundException, NoSuchAlgorithmException, ParsingException, InvalidBinaryFileException
    {
        quadTreeManager.saveToBinaryFile(savePath, false);
        int maxDepth = 5;
        QuadTreeManager manager = new QuadTreeManager(savePath, null, maxDepth);
        QuadTreeNode root = manager.getQuadTreeRoot();
        Assert.assertEquals(root.getMaxNodeDepth(), maxDepth);
    }
    
    @Test
    public void testBinaryIndexedLoadDepthLimit()
    throws IOException, ValidityException, ClassNotFoundException, NoSuchAlgorithmException, ParsingException, InvalidBinaryFileException
    {
        quadTreeManager.saveToBinaryFile(savePath, true);
        int maxDepth = 5;
        QuadTreeManager manager = new QuadTreeManager(savePath, null, maxDepth);
        QuadTreeNode root = manager.getQuadTreeRoot();
        Assert.assertEquals(root.getMaxNodeDepth(), maxDepth);
    }
    
    // TODO : file corruption tests
}
