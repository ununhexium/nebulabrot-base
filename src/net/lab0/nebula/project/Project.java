package net.lab0.nebula.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.ConfigurationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.QuadTreePointsComputingSet;
import net.lab0.nebula.data.RootQuadTreeNode;
import net.lab0.nebula.enums.Indexing;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import net.lab0.nebula.exception.NonEmptyFolderException;
import net.lab0.nebula.exception.ProjectException;
import net.lab0.tools.quadtree.QuadTreeNode;
import net.lab0.tools.quadtree.QuadTreeRoot;
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
     * The path to the project's folder.
     */
    private Path                                    projectFolder;
    
    /**
     * The parameters of this project
     */
    private ProjetInformation                       projetInformation;
    
    private Map<String, QuadTreePointsComputingSet> computingSets     = new HashMap<>();
    private Map<QuadTreePointsComputingSet, Path>   computingSetPaths = new HashMap<>();
    
    private QuadTreeManager                         quadTreeManager;
    
    /**
     * Tries to load a project if it exists or create a new one if it doesn't.
     * 
     * @param projectFolder
     *            The path where the project should be located.
     * 
     * @throws NonEmptyFolderException
     *             If the folder doesn't contain a <code>project.xml</code> file but is not empty.
     * @throws ProjectException
     *             If the given path doesn't point to a directory.
     * @throws JAXBException
     */
    public Project(Path projectFolder)
    throws ProjectException, JAXBException, NonEmptyFolderException
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
                saveProjectsParameters();
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
            System.out.println("Load an existing project");
            load();
        }
    }
    
    /**
     * Saves the parameters of this project in /path/pto/project/folder/<code>project.xml</code>.
     * 
     * @throws JAXBException
     * 
     * @throws ConfigurationException
     */
    public synchronized void saveProjectsParameters()
    throws JAXBException
    {
        save();
    }
    
    /**
     * Saves this project with the version number 1 of the project.xml file.
     * 
     * @throws JAXBException
     */
    private void save()
    throws JAXBException
    {
        JAXBContext context = JAXBContext.newInstance(ProjetInformation.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(projetInformation, getProjectConfigurationFile());
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
    throws JAXBException
    {
        JAXBContext context = JAXBContext.newInstance(ProjetInformation.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        projetInformation = (ProjetInformation) unmarshaller.unmarshal(getProjectConfigurationFile());
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
        projetInformation.computingInformation.enableOpenCL();
    }
    
    /**
     * Disables OpenCL in computation where available.
     */
    public void disableOpenCL()
    {
        projetInformation.computingInformation.disableOpenCL();
    }
    
    /**
     * Enables multithreading in computation where available.
     */
    public void enableMultithreading(int threads)
    {
        projetInformation.computingInformation.setMaxThreadCount(threads);
    }
    
    /**
     * Disables multithreading in computation where available.
     */
    public void disableMultithreading()
    {
        projetInformation.computingInformation.setMaxThreadCount(1);
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
     * Saves the QuadTree managed by this <code>manager</code> in this project.
     * 
     * @param manager
     *            The manager holding the quadtree you want to save.
     * @throws IOException
     *             If here was an error while saving the file.
     */
    public void save(QuadTreeManager manager, Indexing indexation)
    throws IOException
    {
        File tree = new File(projectFolder.toFile(), "tree");
        manager.saveToBinaryFile(tree.toPath(), indexation);
    }
    
    /**
     * Loads the existing quad tree manager if it exists or create a new one if it doesn't.
     * 
     * @return a {@link QuadTreeManager}
     */
    public QuadTreeManager getQuadTreeManager()
    {
        if (quadTreeManager == null)
        {
            reloadQuadTree();
        }
        return quadTreeManager;
    }
    
    /**
     * tries to reload the quadtree from the disk
     */
    public void reloadQuadTree()
    {
        File tree = new File(projectFolder.toFile(), "tree");
        try
        {
            quadTreeManager = new QuadTreeManager(tree.toPath(), null);
        }
        catch (ClassNotFoundException | NoSuchAlgorithmException | ParsingException | IOException
        | InvalidBinaryFileException e)
        {
            throw new RuntimeException("No quadtree to load", e); // TODO: that's ugly
        }
    }
    
    public boolean useOpenCL()
    {
        return projetInformation.computingInformation.useOpenCL();
    }
        
    /**
     * Returns the output path associated to the given <code>quadTreePointsComputingSet</code>.
     * 
     * @param quadTreePointsComputingSet
     * @return The output folder in which the <code>quadTreePointsComputingSet</code> will put its results.
     */
    public Path getOutputFolder(QuadTreePointsComputingSet quadTreePointsComputingSet)
    {
        Path path = computingSetPaths.get(quadTreePointsComputingSet);
        return path;
    }
    
    public boolean useMultithreading()
    {
        return projetInformation.computingInformation.getMaxThreadCount() > 1;
    }
    
}
