package net.lab0.nebula.data;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import net.lab0.nebula.core.NebulabrotRenderer;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import net.lab0.tools.geom.Point;
import net.lab0.tools.geom.Rectangle;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestRawMandelbrotData
{
    private static NebulabrotRenderer nebulabrotRenderer;
    private static int                resolution  = 128;
    private static int                minIter     = 64;
    private static int                maxIter     = 128;
    private static long               pointsCount = resolution * resolution * maxIter;
    private static Path               path        = FileSystems.getDefault()
                                                  .getPath(".", "test_folder", "rawRendering");
    private static RawMandelbrotData  originalData;
    
    @BeforeClass
    public static void generateRawData()
    throws IOException
    {
        nebulabrotRenderer = new NebulabrotRenderer(resolution, resolution, new Rectangle(new Point(-2.0, -2.0),
        new Point(2.0, 2.0)));
        // compute on a least 1 and at most N-1 CPUs to let other applications run smoothly
        originalData = nebulabrotRenderer.linearRender(pointsCount, minIter, maxIter, 1);
        originalData.addAdditionnalInformation("aKey", "aValue");
        originalData.save(path);
    }
    
    @Test
    public void testSave()
    throws IOException
    {
        originalData.addAdditionnalInformation("aKey", "aValue");
        originalData.save(path);
    }
    
    @Test
    public void testLoad()
    throws ValidityException, NoSuchAlgorithmException, ParsingException, IOException, InvalidBinaryFileException
    {
        Assert.assertTrue(true);
        RawMandelbrotData data = new RawMandelbrotData(path);
        Assert.assertEquals(originalData.getPixelHeight(), data.getPixelHeight());
        Assert.assertEquals(originalData.getPixelWidth(), data.getPixelWidth());
        Assert.assertEquals(originalData.getMinIter(), data.getMinIter());
        Assert.assertEquals(originalData.getMaxIter(), data.getMaxIter());
        Assert.assertEquals(originalData.getPointsCount(), data.getPointsCount());
        Assert.assertEquals("aValue", data.getAdditionnalInformation("aKey"));
        int[][] array = data.getData();
        int[][] originalArray = originalData.getData();
        
        for (int x = 0; x < data.getPixelWidth(); ++x)
        {
            for (int y = 0; y < data.getPixelWidth(); ++y)
            {
                Assert.assertEquals("Missmatch@(" + x + ";" + y + ")", originalArray[x][y], array[x][y]);
            }
        }
    }
}
