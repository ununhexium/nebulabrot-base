package net.lab0.nebula.project;

import java.io.File;
import java.io.IOException;

import javax.naming.ConfigurationException;
import javax.xml.bind.JAXBException;

import net.lab0.nebula.exception.NonEmptyFolderException;
import net.lab0.nebula.exception.ProjectException;
import net.lab0.nebula.project.Project;
import net.lab0.tools.FileUtils;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestProject
{
    private static File empty;   // empty folder
    private static File existing; // folder with existing project
    private static File nonEmpty; // folder not empty but without a project
    private static File file;    // a regular file
                                  
    @BeforeClass
    public static void beforeClass()
    {
        try
        {
            File baseFolder = new File("./test_folder");
            File projectFolder = new File(baseFolder, "project");
            projectFolder.mkdirs();
            
            // creates an empty directory
            empty = new File(projectFolder, "empty");
            if (!empty.exists())
            {
                empty.mkdirs();
            }
            else
            {
                FileUtils.removeRecursive(empty.toPath());
                empty.mkdirs();
            }
            
            // creates an existing project
            existing = new File(projectFolder, "existing");
            if (!existing.exists())
            {
                existing.mkdirs();
            }
            else
            {
                FileUtils.removeRecursive(existing.toPath());
                existing.mkdirs();
            }
            Project project = new Project(existing.toPath());
            project.saveProjectsParameters();
            String folder = project.getProjectFolder();
            Assert.assertEquals("Folders mismatch", existing.getAbsolutePath(), folder);
            
            // create a non empty non project folder
            nonEmpty = new File(projectFolder, "nonEmpty");
            if (!nonEmpty.exists())
            {
                nonEmpty.mkdirs();
            }
            else
            {
                FileUtils.removeRecursive(nonEmpty.toPath());
            }
            nonEmpty.mkdirs();
            File touch = new File(nonEmpty, "touch");
            touch.createNewFile();
            
            file = new File(projectFolder, "file");
            if (file.isDirectory())
            {
                FileUtils.removeRecursive(file.toPath());
            }
            file.createNewFile();
        }
        catch (ProjectException | NonEmptyFolderException | IOException | JAXBException e)
        {
            Assert.fail();
        }
    }
    
    @Test
    public void testProject1()
    throws ConfigurationException, ProjectException, NonEmptyFolderException, IOException, ValidityException, ParsingException, JAXBException
    {
        new Project(empty.toPath());
    }
    
    @Test
    public void testProject2()
    throws ConfigurationException, ProjectException, NonEmptyFolderException, IOException, ValidityException, ParsingException, JAXBException
    {
        new Project(existing.toPath());
    }
    
    @Test(expected = NonEmptyFolderException.class)
    public void testProject3()
    throws ConfigurationException, ProjectException, NonEmptyFolderException, IOException, ValidityException, ParsingException, JAXBException
    {
        new Project(nonEmpty.toPath());
    }
    
    @Test(expected = ProjectException.class)
    public void testProject4()
    throws ConfigurationException, ProjectException, NonEmptyFolderException, IOException, ValidityException, ParsingException, JAXBException
    {
        new Project(file.toPath());
    }
}
