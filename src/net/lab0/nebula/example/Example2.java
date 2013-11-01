package net.lab0.nebula.example;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import net.lab0.nebula.color.AquaColorModel;
import net.lab0.nebula.color.ColorationModel;
import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.RenderingParameters;
import net.lab0.nebula.enums.Indexing;
import net.lab0.nebula.enums.ComputingMethod;
import net.lab0.nebula.project.Project;
import net.lab0.tools.FileUtils;
import net.lab0.tools.geom.Point;
import net.lab0.tools.geom.Rectangle;
import net.lab0.tools.geom.RectangleInterface;


/**
 * This class' source code explains how to create a new project.
 * 
 * @author 116@lab0.net
 * 
 */
public class Example2
{
    public static void main(String[] args)
    throws Exception
    {
        /*
         * First, we need to choose an empty directory.
         */
        Path projectPath = FileSystems.getDefault().getPath("F:", "dev", "nebula", "new");
        System.out.println("Project path set to " + projectPath);
        
        /*
         * This class may be run several time so we need to make sure that the folder will always be empty.
         */
        if (projectPath.toFile().exists())
        {
            System.out.println("Deleting directory " + projectPath);
            FileUtils.removeRecursive(projectPath);
        }
        System.out.println("Creating project's directory");
        projectPath.toFile().mkdirs();
        
        /*
         * Create a new project
         */
        System.out.println("Creating a new project");
        Project project = new Project(projectPath);
        
        /*
         * Set some parameters
         */
        
        System.out.println("Setting project's parameters");
        // use OpenCL and multithreading because we like to make it fast
        project.enableOpenCL();
        project.enableMultithreading();
        project.saveProjectsParameters();
        
        // add a new quadtree to this project
        QuadTreeManager manager = project.newQuadTree(-2.0d, 2.0d, -2.0d, 2.0d, 256, 4096, 3, 6);
        
        // compute the tree
        System.out.println("Computing the quadtree");
        project.compute(manager, Integer.MAX_VALUE);
        
        // save it in the project's folder
        System.out.println("Saving the quadtree");
        project.save(manager, Indexing.NO_INDEXING);
        
        /*
         * compute some rendering
         */
        System.out.println("Rendering parameters");
        // set the resolution
        int resolution = 1024;
        // choose a rendering viewport: this is the part of the picture that you want to see
        RectangleInterface viewport = new Rectangle(new Point(-2.0, -2.0), new Point(2.0, 2.0));
        // choose the way to color the image
        ColorationModel colorationModel = new AquaColorModel();
        // create the rendering parameters
        RenderingParameters renderingParameters = new RenderingParameters(resolution, resolution, 1L << 24, 16, 4096,
        viewport, colorationModel);
        
        // render something
        System.out.println("Rendering");
        BufferedImage image = project.pictureRender(renderingParameters, ComputingMethod.QUADTREE);
        
        // save it
        File imageFile = new File("./test_folder/preview.png");
        System.out.println("Saving the image to " + imageFile.getAbsolutePath());
        ImageIO.write(image, "PNG", imageFile);
        
        System.out.println("Finished");
    }
}
