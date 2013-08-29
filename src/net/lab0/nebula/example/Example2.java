package net.lab0.nebula.example;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.enums.Indexation;
import net.lab0.nebula.project.Project;
import net.lab0.nebula.project.QuadTreeMetadata;

import org.apache.commons.io.FileUtils;

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
        
        /*
         * This class may be run several time so we need to make sure that the folder will always be empty.
         */
        if (projectPath.toFile().exists())
        {
            FileUtils.deleteDirectory(projectPath.toFile());
        }
        projectPath.toFile().mkdirs();
        
        /*
         * Create a new project
         */
        Project project = new Project(projectPath);
        
        /*
         * Set some parameters
         */
        // use OpenCL and multithreading because we like to make it fast
        project.enableOpenCL();
        project.enableMultithreading();
        
        // add a new quadtree to this project
        QuadTreeManager manager = project.newQuadTree(-2.0d, 2.0d, -2.0d, 2.0d, 256, 1024, 3, 5);
        
        // compute the tree
        project.compute(manager, Integer.MAX_VALUE);
        
        // save it in the project's folder
        project.save(manager, Indexation.NO_INDEXATION);
        
        //compute some rendering
    }
}
