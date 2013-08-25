package net.lab0.nebula.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.InvalidPropertiesFormatException;
import java.util.List;

import net.lab0.nebula.exception.NonEmptyFolderException;
import net.lab0.nebula.exception.ProjectException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Stores and loads the parameters of a project.
 * 
 * @see ProjectKey for the available properties.
 * 
 * @author 116@lab0.net
 * 
 */
public class Project
{
    /**
     * The path to the project's folder.
     */
    private Path                    projectFolder;
    
    /**
     * Holds the parameters of this project.
     */
    private PropertiesConfiguration propertiesConfiguration;
    
    /**
     * Tries to load a project if it exists or create a new one if it doesn't.
     * 
     * @param projectFolder
     *            The path where the project should be located.
     * 
     * @throws NonEmptyFolderException
     *             If the folder doesn't contain a <code>project.xml</code> file but is not empty.
     * @throws IOException
     *             If there was an error while reading the properties file.
     * @throws ProjectException
     *             If the given path doesn't point to a directory.
     * @throws ConfigurationException
     *             In case of error while loading the properties file
     */
    public Project(Path projectFolder)
    throws ProjectException, NonEmptyFolderException, IOException, ConfigurationException
    {
        super();
        this.projectFolder = projectFolder;
        File folder = projectFolder.toFile();
        
        // this would be a new project
        if (!folder.exists())
        {
            folder.mkdirs();
        }
        
        // if the folder is not a directory: error
        if (!folder.isDirectory())
        {
            throw new ProjectException("The given path does not point to a directory.");
        }
        
        // this is an attempt to load an existing project
        File projectFile = getProjectXmlFile();
        // there is no project here, is this a new project ?
        if (!projectFile.exists())
        {
            // there is no file -> new project
            if (folder.list().length == 0)
            {
                propertiesConfiguration = new PropertiesConfiguration(projectFile);
                propertiesConfiguration.setFile(projectFile);
            }
            // the is already something going on in this folder -> can't do anything
            else
            {
                throw new NonEmptyFolderException(
                "The given folder is not a valid project folder. The folder must be empty to create a new project.");
            }
        }
        // load the project's properties by overriding the default properties.
        else
        {
            propertiesConfiguration = new PropertiesConfiguration();
            propertiesConfiguration.setFile(projectFile);
            load();
        }
    }
    
    /**
     * Saves the parameters of this project in /path/pto/project/folder/<code>project.xml</code>.
     * 
     * @throws ConfigurationException
     * 
     * @throws IOException
     */
    public synchronized void save()
    throws ConfigurationException
    {
        propertiesConfiguration.save();
    }
    
    /**
     * Loads a parameters file.
     * 
     * @throws ConfigurationException
     * 
     * @throws ValidityException
     *             If the xml file is not valid.
     * @throws ParsingException
     *             If there is an error while parsing the xml.
     * @throws IOException
     *             If there is an error while reading the file.
     */
    private synchronized void load()
    throws ConfigurationException
    {
        propertiesConfiguration.load();
    }
    
    /**
     * @return a <code>String</code> of the absolute path to this project.
     */
    public String getProjectFolder()
    {
        return projectFolder.toFile().getAbsolutePath();
    }
    
    /**
     * 
     * @return The file that contains the parameters of this project.
     */
    private File getProjectXmlFile()
    {
        File folder = this.projectFolder.toFile();
        File file = new File(folder, "project.xml");
        return file;
    }
    
    public void enableOpenCL()
    {
        propertiesConfiguration.addProperty(ProjectKey.USE_OPENCL.toString(), Boolean.TRUE);
    }
    
    public void disableOpenCL()
    {
        propertiesConfiguration.addProperty(ProjectKey.USE_OPENCL.toString(), Boolean.FALSE);
    }
    
    public void enableMultithreading()
    {
        propertiesConfiguration.addProperty(ProjectKey.USE_MULTITHREADING.toString(), Boolean.TRUE);
    }
    
    public void disableMultithreading()
    {
        propertiesConfiguration.addProperty(ProjectKey.USE_MULTITHREADING.toString(), Boolean.FALSE);
    }
    
    /**
     * Retrieves the metadata information of the quadtrees in the
     * 
     * @return
     */
    // public List<QuadTreeMetadata> getQuadTreesMetadata()
    {
        
    }
}
