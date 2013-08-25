package net.lab0.nebula.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.RootQuadTreeNode;
import net.lab0.nebula.enums.Indexation;
import net.lab0.nebula.exception.NonEmptyFolderException;
import net.lab0.nebula.exception.ProjectException;
import net.lab0.tools.quadtree.QuadTreeNode;
import net.lab0.tools.quadtree.QuadTreeRoot;
import nu.xom.ValidityException;

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
    private Path                       projectFolder;
    
    /**
     * Holds the parameters of this project.
     */
    private PropertiesConfiguration    propertiesConfiguration;
    
    private Map<QuadTreeManager, File> quadTreeFileMapping = new HashMap<QuadTreeManager, File>();
    
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
        File projectFile = getProjectConfigurationFile();
        // there is no project here, is this a new project ?
        if (!projectFile.exists())
        {
            // there is no file -> new project
            if (folder.list().length == 0)
            {
                initProperties(projectFile);
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
            initProperties(projectFile);
            load();
        }
    }
    
    /**
     * Creates and configures a properties object.
     * 
     * @param projectFile
     */
    private void initProperties(File projectFile)
    {
        propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.setFile(projectFile);
        propertiesConfiguration.setAutoSave(true);
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
    private File getProjectConfigurationFile()
    {
        File folder = this.projectFolder.toFile();
        File file = new File(folder, "project.conf");
        return file;
    }
    
    /**
     * Enables OpenCL in computation where available.
     */
    public void enableOpenCL()
    {
        propertiesConfiguration.addProperty(ProjectKey.USE_OPENCL.toString(), Boolean.TRUE);
    }
    
    /**
     * Disables OpenCL in computation where available.
     */
    public void disableOpenCL()
    {
        propertiesConfiguration.addProperty(ProjectKey.USE_OPENCL.toString(), Boolean.FALSE);
    }
    
    /**
     * Enables multithreading in computation where available.
     */
    public void enableMultithreading()
    {
        propertiesConfiguration.addProperty(ProjectKey.USE_MULTITHREADING.toString(), Boolean.TRUE);
    }
    
    /**
     * Disables multithreading in computation where available.
     */
    public void disableMultithreading()
    {
        propertiesConfiguration.addProperty(ProjectKey.USE_MULTITHREADING.toString(), Boolean.FALSE);
    }
    
    /**
     * Creates a new {@link QuadTreeRoot} with the given parameters.
     * 
     * @param minX
     *            the QuadTreeRoot minimum X value
     * @param maxX
     *            the QuadTreeRoot maximum X value
     * @param minY
     *            the QuadTreeRoot minimum Y value
     * @param maxY
     *            the QuadTreeRoot maximum Y value
     * @param pointsPerSide
     *            the number of points to test on a side of a {@link QuadTreeNode}
     * @param maxIteration
     *            The maximum number of iterations to do when computing the Mandelbrot formula.
     * @param diffIterLimit
     *            The maximum number of iteration allowed for a node to be still considered as not containing the
     *            Mandelbrot set.
     * @param maxDepth
     *            The maximum depth this QuadTree can have.
     * @return A {@link QuadTreeManager} linked to a new {@link QuadTreeRoot} created with the above parameters.
     * @throws IOException
     *             If an error happens when trying to create the folder containing this new quadtree.
     */
    public QuadTreeManager newQuadTree(double minX, double maxX, double minY, double maxY, int pointsPerSide,
    int maxIteration, int diffIterLimit, int maxDepth)
    throws IOException
    {
        RootQuadTreeNode root = new RootQuadTreeNode(minX, maxX, minY, maxY);
        QuadTreeManager quadTreeManager = new QuadTreeManager(root, pointsPerSide, maxIteration, diffIterLimit,
        maxDepth);
        this.addQuadTree(quadTreeManager);
        return quadTreeManager;
    }
    
    /**
     * Adds a new QuadTree to this project.
     * 
     * @throws IOException
     *             If an error happens when trying to create the folder containing this new quadtree.
     */
    private void addQuadTree(QuadTreeManager quadTreeManager)
    throws IOException
    {
        File tree = new File(projectFolder.toFile(), "tree");
        if (!tree.isDirectory())
        {
            tree.delete();
        }
        if (!tree.exists())
        {
            tree.mkdirs();
        }
        
        // find the folder for the tree we are going to add
        int index = 0;
        File newTree;
        do
        {
            newTree = new File(tree, Integer.toString(index));
        } while (newTree.exists());
        newTree.mkdir();
        quadTreeFileMapping.put(quadTreeManager, newTree);
    }
    
    /**
     * Computes at most <code>maxValue</code> nodes before returning.
     * 
     * @param manager
     *            The manager to use for the computation. Range: [0;Integer.MAX_VALUE]
     * @param maxValue
     *            The maximum amount of nodes to computes.
     */
    public void compute(QuadTreeManager manager, int maxValue)
    {
        manager.compute(maxValue);
    }
    
    /**
     * Saves the QuadTree managed by this <code>manager</code> in this project.
     * 
     * @param manager
     *            The manager holding the quadtree you want to save.
     * @throws IOException
     *             If here was an error while saving the file.
     */
    public void save(QuadTreeManager manager, Indexation indexation)
    throws IOException
    {
        File path = quadTreeFileMapping.get(manager);
        if (path == null)
        {
            throw new IllegalArgumentException("This manager was not associated to this project.");
        }
        manager.saveToBinaryFile(path.toPath(), indexation);
    }
}
