package net.lab0.nebula.example;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.project.Project;

/**
 * This class' source code explains how to load an existing project.
 * 
 * @author 116@lab0.net
 * 
 */
public class Example1
{
    public static void main(String[] args)
    throws Exception
    {
        Path path = FileSystems.getDefault().getPath("F:", "dev", "nebula", "new");
        /*
         * Create a new project item. This will store save and load the configuration of the project we are going to
         * work on.
         */
        System.out.println("Craetion of the project");
        Project project = new Project(path);
        
        /*
         * Now we want to load some precomputed quad tree
         */
        System.out.println("Loading the quadtree");
        QuadTreeManager quadTreeManager = project.getQuadTreeManager();
    }
}
