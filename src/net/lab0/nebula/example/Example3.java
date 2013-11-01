package net.lab0.nebula.example;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.enums.Indexing;
import net.lab0.nebula.listener.ConsoleQuadTreeComputeListener;
import net.lab0.nebula.project.Project;
import net.lab0.tools.FileUtils;

/**
 * Computing a huge quad tree. Introduces the listeners.
 * 
 * @author 116@lab0.net
 * 
 */
public class Example3
{
    public static void main(String[] args)
    throws Exception
    {
        /*
         * The first part is the same as the beginning of the Example2 tutorial. First, we need to choose a directory.
         */
        Path projectPath = FileSystems.getDefault().getPath("F:", "dev", "nebula", "big1");
        System.out.println("Project path set to " + projectPath);
        
        /*
         * This class may be run several time so we need to make sure that the folder will always be empty.
         */
        if (projectPath.toFile().exists())
        {
            System.out.println("Deleting directory " + projectPath);
            FileUtils.removeRecursive(projectPath);
        }
        else
        {
            System.out.println("Creating project's directory");
            projectPath.toFile().mkdirs();
        }
        
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
        QuadTreeManager manager = project.newQuadTree(-2.0d, 2.0d, -2.0d, 2.0d, 256, 1 << 8, 5, 8);
        manager.addQuadTreeComputeListener(new ConsoleQuadTreeComputeListener());
        
        // compute the tree
        System.out.println("Computing the quadtree");
        boolean goOn = true;
        int pass = 0;
        while (goOn)
        {
            goOn = project.compute(manager, 1024);
            // prevently save every 1024 nodes computed
            project.save(manager, Indexing.NO_INDEXING);
            System.out.println("Pass" + ++pass);
        }
        
        // save it in the project's folder
        System.out.println("Saving the quadtree");
        project.save(manager, Indexing.NO_INDEXING);
        
    }
}
