package net.lab0.nebula.test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import junit.framework.Assert;
import net.lab0.nebula.core.NebulabrotRenderer;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import net.lab0.tools.geom.Point;
import net.lab0.tools.geom.Rectangle;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestRawMandelbrotDataSave
{
    private static NebulabrotRenderer nebulabrotRenderer;
    private static int                resolution  = 128;
    private static int                minIter     = 64;
    private static int                maxIter     = 128;
    private static long               pointsCount = resolution * resolution * maxIter;
    private static Path               path        = FileSystems.getDefault().getPath(".", "test_folder", "rawRendering");
    private static RawMandelbrotData         originalData;
    
    @BeforeClass
    public static void generateRawData()
    {
        nebulabrotRenderer = new NebulabrotRenderer(resolution, resolution, new Rectangle(new Point(-2.0, -2.0), new Point(2.0, 2.0)));
        originalData = nebulabrotRenderer.linearRender(pointsCount, minIter, maxIter);
    }
    
    @Test
    public void testSave()
    throws IOException
    {
        originalData.addAdditionnalInformation("aKey", "aValue");
        originalData.save(path, true);
    }
    
    @Test
    public void testLoad()
    throws ValidityException, NoSuchAlgorithmException, ParsingException, IOException, InvalidBinaryFileException
    {
        Assert.assertTrue(true);
        RawMandelbrotData data = new RawMandelbrotData(path);
        Assert.assertEquals(data.getPixelHeight(), originalData.getPixelHeight());
        Assert.assertEquals(data.getPixelWidth(), originalData.getPixelWidth());
        Assert.assertEquals(data.getMinIter(), originalData.getMinIter());
        Assert.assertEquals(data.getMaxIter(), originalData.getMaxIter());
        Assert.assertEquals(data.getPointsCount(), originalData.getPointsCount());
        Assert.assertEquals(data.getAdditionnalInformation("aKey"), "aValue");
        int[][] array = data.getData();
        int[][] originalArray = originalData.getData();
        
        for (int x = 0; x < data.getPixelWidth(); ++x)
        {
            for (int y = 0; y < data.getPixelWidth(); ++y)
            {
                Assert.assertEquals(array[x][y], originalArray[x][y]);
            }
        }
    }
}
