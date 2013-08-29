package net.lab0.nebula.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.RootQuadTreeNode;
import net.lab0.nebula.enums.Indexation;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import net.lab0.nebula.exception.NonEmptyFolderException;
import net.lab0.nebula.exception.ProjectException;
import net.lab0.tools.quadtree.QuadTreeNode;
import net.lab0.tools.quadtree.QuadTreeRoot;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

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
     * The version of the project.xml file to use when saving a new project. Min version value: 1.
     */
    private static final int        VERSION    = 1;
    
    /**
     * The version of this project.xml file. If version <=0, then the version is not set.
     */
    private int                     version;
    
    /**
     * The path to the project's folder.
     */
    private Path                    projectFolder;
    
    private Map<ProjectKey, Object> parameters = new HashMap<>();
    
    private QuadTreeManager                 quadTreeManager;
    
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
     * @throws ParsingException
     * @throws ValidityException
     * @throws ConfigurationException
     *             In case of error while loading the properties file
     */
    public Project(Path projectFolder)
    throws ProjectException, NonEmptyFolderException, IOException, ValidityException, ParsingException
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
                save();
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
            load();
        }
    }
    
    /**
     * Saves the parameters of this project in /path/pto/project/folder/<code>project.xml</code>.
     * 
     * @throws FileNotFoundException
     * 
     * @throws ConfigurationException
     * 
     * @throws IOException
     */
    public synchronized void save()
    throws FileNotFoundException
    {
        int versionToUse = version == 0 ? VERSION : version;
        switch (versionToUse)
        {
            case 1:
                saveVersion1();
                break;
            
            default:
                break;
        }
    }
    
    /**
     * Saves this project with the version number 1 of the project.xml file.
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void saveVersion1()
    throws FileNotFoundException
    {
        Element root = new Element("project");
        root.addAttribute(new Attribute("version", Integer.toString(1)));
        
        Element parametersNode = new Element("parameters");
        root.appendChild(parametersNode);
        
        // opencl
        Boolean useOpenCLValue = (Boolean) parameters.get(ProjectKey.USE_OPENCL);
        if (useOpenCLValue != null)
        {
            Element node = new Element(ProjectKey.USE_OPENCL.name());
            node.addAttribute(new Attribute("boolean", Boolean.toString(useOpenCLValue)));
            parametersNode.appendChild(node);
        }
        
        Boolean useMultithreadingValue = (Boolean) parameters.get(ProjectKey.USE_MULTITHREADING);
        if (useMultithreadingValue != null)
        {
            Element node = new Element(ProjectKey.USE_MULTITHREADING.name());
            node.addAttribute(new Attribute("boolean", Boolean.toString(useMultithreadingValue)));
            parametersNode.appendChild(node);
        }
        
        Document document = new Document(root);
        String xml = document.toXML();
        File projectConfiguration = getProjectConfigurationFile();
        try (
            PrintWriter printWriter = new PrintWriter(projectConfiguration))
        {
            printWriter.write(xml);
        }
    }
    
    /**
     * Loads a parameters file.
     * 
     * @throws ParsingException
     * 
     * @throws ValidityException
     *             If the xml file is not valid.
     * @throws IOException
     *             If there is an error while reading the file.
     * @throws ProjectException
     */
    private synchronized void load()
    throws ValidityException, ParsingException, IOException, ProjectException
    {
        Builder builder = new Builder();
        Document document = builder.build(getProjectConfigurationFile());
        Element root = document.getRootElement();
        if (!root.getLocalName().equals("project"))
        {
            throw new ProjectException("Expected a project file but found a root named " + root.getLocalName());
        }
        
        Element parametersNode = root.getFirstChildElement("parameters");
        Elements elements = parametersNode.getChildElements();
        
        for (int i = 0; i < elements.size(); ++i)
        {
            Element e = elements.get(i);
            ProjectKey key = ProjectKey.valueOf(e.getLocalName());
            switch (key)
            {
            // boolean keys
                case USE_OPENCL:
                case USE_MULTITHREADING:
                    parameters.put(key, Boolean.parseBoolean(e.getAttributeValue("boolean")));
                    break;
                
                default:
                    break;
            }
        }
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
        File file = new File(folder, "project.xml");
        return file;
    }
    
    /**
     * Enables OpenCL in computation where available.
     */
    public void enableOpenCL()
    {
        parameters.put(ProjectKey.USE_OPENCL, Boolean.TRUE);
    }
    
    /**
     * Disables OpenCL in computation where available.
     */
    public void disableOpenCL()
    {
        parameters.put(ProjectKey.USE_OPENCL, Boolean.FALSE);
    }
    
    /**
     * Enables multithreading in computation where available.
     */
    public void enableMultithreading()
    {
        parameters.put(ProjectKey.USE_MULTITHREADING, Boolean.TRUE);
    }
    
    /**
     * Disables multithreading in computation where available.
     */
    public void disableMultithreading()
    {
        parameters.put(ProjectKey.USE_MULTITHREADING, Boolean.FALSE);
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
        this.quadTreeManager = quadTreeManager;
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
        if ((boolean) parameters.get(ProjectKey.USE_MULTITHREADING))
        {
            manager.setThreads(Runtime.getRuntime().availableProcessors() - 1);
        }
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
        File path = quadTreeManager.getOriginalPath().toFile();
        if (path == null)
        {
            throw new IllegalArgumentException("This manager was not associated to this project.");
        }
        manager.saveToBinaryFile(path.toPath(), indexation);
    }

    public QuadTreeManager getQuadTreeManager()
    {
        return quadTreeManager;
    }
    
    
}
