package net.lab0.nebula.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.junit.After;
import org.junit.AfterClass;
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
public class TestBinarySaveFormats
{
    private static final Path      testFolder      = FileSystems.getDefault().getPath(".", "test_folder");
    
    private static QuadTreeManager quadTreeManager = null;
    private static Path            savePath        = null;
    
    @BeforeClass
    public static void generateQuadTree()
    throws InterruptedException, IOException
    {
        // init a basic tree to have something to save and compare with
        quadTreeManager = new QuadTreeManager(new QuadTreeNode(-2.0, 2.0, -2.0, 2.0), 64, 256, 3, 10);
        quadTreeManager.setThreads(Runtime.getRuntime().availableProcessors());
        quadTreeManager.compute(99999);
        
        for (File f : testFolder.toFile().listFiles())
        {
            if (f.exists())
            {
                Files.walkFileTree(f.toPath(), new SimpleFileVisitor<Path>()
                {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException
                    {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                    
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException
                    {
                        if (exc == null)
                        {
                            Files.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                        else
                        {
                            throw exc;
                        }
                    }
                    
                });
            }
        }
    }
    
    @Before
    public void initSavePath()
    {
        // find a non existing folder
        int i = 0;
        do
        {
            savePath = FileSystems.getDefault().getPath("./test_folder", Integer.toString(i));
            ++i;
        } while (savePath.toFile().exists());
    }
    
    @Test
    public void testXml()
    throws IOException, ValidityException, ClassNotFoundException, ParsingException, InvalidBinaryFileException
    {
        quadTreeManager.saveToXML(savePath);
        QuadTreeManager manager = new QuadTreeManager(savePath, null);
        QuadTreeNode root = manager.getQuadTreeRoot();
        assertTrue(root.testIsExactlyTheSameAs(quadTreeManager.getQuadTreeRoot(), false));
    }
    
    @Test
    public void testSerialized()
    throws IOException, ValidityException, ClassNotFoundException, ParsingException, InvalidBinaryFileException
    {
        quadTreeManager.saveToSearializedJavaObject(savePath);
        QuadTreeManager manager = new QuadTreeManager(savePath, null);
        QuadTreeNode root = manager.getQuadTreeRoot();
        assertTrue(root.testIsExactlyTheSameAs(quadTreeManager.getQuadTreeRoot(), false));
    }
    
    @Test
    public void testIndexedBinary()
    throws IOException, ValidityException, ClassNotFoundException, ParsingException, InvalidBinaryFileException
    {
        quadTreeManager.saveToBinaryFile(savePath, true);
        QuadTreeManager manager = new QuadTreeManager(savePath, null);
        QuadTreeNode root = manager.getQuadTreeRoot();
        assertTrue(root.testIsExactlyTheSameAs(quadTreeManager.getQuadTreeRoot(), false));
    }
    
    @Test
    public void testNonIndexedBinary()
    throws IOException, ValidityException, ClassNotFoundException, ParsingException, InvalidBinaryFileException
    {
        quadTreeManager.saveToBinaryFile(savePath, false);
        QuadTreeManager manager = new QuadTreeManager(savePath, null);
        QuadTreeNode root = manager.getQuadTreeRoot();
        assertTrue(root.testIsExactlyTheSameAs(quadTreeManager.getQuadTreeRoot(), false));
    }
}
