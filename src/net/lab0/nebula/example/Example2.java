package net.lab0.nebula.example;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import net.lab0.nebula.project.Project;

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
        // use OpenCL and multithreading because we like to live quickly
        project.enableOpenCL();
        project.enableMultithreading();
    }
}
