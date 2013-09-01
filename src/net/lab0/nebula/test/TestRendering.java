package net.lab0.nebula.test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import net.lab0.nebula.color.PowerGrayScaleColorModel;
import net.lab0.nebula.core.NebulabrotRenderer;
import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.data.RootQuadTreeNode;
import net.lab0.nebula.exception.NonEmptyFolderException;
import net.lab0.nebula.exception.ProjectException;
import net.lab0.nebula.project.Project;
import net.lab0.tools.geom.Point;
import net.lab0.tools.geom.Rectangle;
import nu.xom.ParsingException;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the graphical rendering. The ouput must be checked by a human.
 * 
 * @author 116@lab0.net
 * 
 */
public class TestRendering
{
    public static QuadTreeManager  manager;
    public static Project          project;
    
    @BeforeClass
    public static void beforeClass()
    {
        Path path = FileSystems.getDefault().getPath("test_project");
        try
        {
            project = new Project(path);
            project.disableMultithreading();
            project.disableOpenCL();
            manager = project.newQuadTree(-2.0, 2.0, -2.0, 2.0, 128, 4096, 3, 7);
        }
        catch (ProjectException | NonEmptyFolderException | IOException | ParsingException e)
        {
            fail();
            e.printStackTrace();
        }
    }
    
    @Test
    public void testCPURendering()
    {
        Rectangle viewPort = new Rectangle(new Point(-2.0, -2.0), new Point(2.0, 2.0));
        NebulabrotRenderer renderer = new NebulabrotRenderer(1024, 1024, viewPort);
        long pointsCount = 1 << 20;
        int minIter = 0;
        int maxIter = 4000;
        RawMandelbrotData linearData = renderer.linearRender(pointsCount, minIter, maxIter, 1);
        RawMandelbrotData quadTreeData = renderer.quadTreeRender(pointsCount, minIter, maxIter,
        manager.getQuadTreeRoot(), 1);
        BufferedImage linearImage = linearData.computeBufferedImage(new PowerGrayScaleColorModel(0.5), 0);
        BufferedImage quadTreeImage = quadTreeData.computeBufferedImage(new PowerGrayScaleColorModel(0.5), 0);
        
    }
}
