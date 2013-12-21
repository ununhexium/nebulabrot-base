package net.lab0.nebula.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.ConfigurationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.data.MandelbrotQuadTreeNode.NodePath;
import net.lab0.nebula.data.StatusQuadTreeNode;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import net.lab0.nebula.exception.NonEmptyFolderException;
import net.lab0.nebula.exception.ProjectException;
import net.lab0.nebula.exception.SerializationException;
import net.lab0.nebula.listener.GeneralListener;
import net.lab0.nebula.listener.QuadTreeManagerListener;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.FileUtils;
import net.lab0.tools.HumanReadable;
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
    private Path              projectFolder;
    
    /**
     * <code>true</code> if the created project is a new project (created from an empty folder)
     */
    private boolean           newProject;
    
    /**
     * The parameters of this project
     */
    private ProjetInformation projetInformation = new ProjetInformation();
    
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
     *             If there is an error while parsing the project's files.
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
                newProject = true;
                projetInformation.creationDate = new Date();
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
    
    public boolean useOpenCL()
    {
        return projetInformation.computingInformation.useOpenCL == true;
    }
    
    public boolean useMultithreading()
    {
        return projetInformation.computingInformation.maxThreadCount > 1;
    }
    
    public boolean isNewProject()
    {
        return newProject;
    }
    
    public ProjetInformation getProjetInformation()
    {
        return projetInformation;
    }
    
    /**
     * Imports the quad tree at the given location in this project.
     * 
     * @param input
     *            The index file of the quad tree to import
     * @param managerListener
     *            Optional. The listener to attach when reading the file.
     * @param generalListener
     *            Optional. A listener for the progress of the import.
     * @throws ProjectException
     */
    @SuppressWarnings("deprecation")
    public void importQuadTree(Path input, int maxDepth, QuadTreeManagerListener managerListener,
    GeneralListener generalListener)
    throws ProjectException
    {
        try
        {
            net.lab0.nebula.core.QuadTreeManager manager = new net.lab0.nebula.core.QuadTreeManager(input,
            managerListener);
            
            int nodesCounts = manager.getQuadTreeRoot().getTotalNodesCount();
            List<StatusQuadTreeNode> nodes = new ArrayList<>(nodesCounts);
            manager.getQuadTreeRoot().getAllNodes(nodes);
            
            maxDepth = Math.min(manager.getQuadTreeRoot().getMaxNodeDepth(), maxDepth);
            
            if (generalListener != null)
            {
                generalListener.print("Quad tree loaded. The maximum depth is " + maxDepth);
            }
            
            // prepare the paths
            List<Path> paths = new ArrayList<>(maxDepth);
            Path basePath = FileSystems.getDefault().getPath(getProjectFolder(), "quadtree");
            int suffix = FileUtils.getNextAvailablePath(basePath, "");
            Path folder = FileSystems.getDefault().getPath(basePath.toString(), "" + suffix);
            folder.toFile().mkdirs();
            for (int i = 0; i <= maxDepth; ++i)
            {
                paths.add(FileSystems.getDefault().getPath(folder.toString(), "depth_" + i + ".data"));
            }
            
            extractAndSave(nodes, maxDepth, paths, generalListener);
            
            QuadTreeInformation information = new QuadTreeInformation();
            information.id = suffix;
            information.maximumIterationCount = manager.getMaxIter();
            information.maximumIterationDifference = manager.getDiffIterLimit();
            information.pointsCountPerSide = manager.getPointsPerSide();
            projetInformation.dataPathInformation.quadTrees.add(information);
        }
        catch (ClassNotFoundException | NoSuchAlgorithmException | ParsingException | IOException
        | InvalidBinaryFileException | SerializationException e)
        {
            throw new ProjectException("Error while importing", e);
        }
    }
    
    /**
     * Extracts the node from the list and saves then at the appropriate path.
     * 
     * @param nodes
     *            The nodes to extract
     * @param maxDepth
     *            The maximum depth (inclusive) to consider for the extraction
     * @param paths
     *            The paths for the different depths.
     * @param generalListener
     *            The listener for general info output
     * @throws SerializationException
     *             If an error happens when writing the data.
     */
    private void extractAndSave(List<StatusQuadTreeNode> nodes, int maxDepth, List<Path> paths,
    GeneralListener generalListener)
    throws SerializationException
    {
        // for console listener
        int blockSize = 65536;
        int index = 0;
        
        int sum = 0;
        for (StatusQuadTreeNode node : nodes)
        {
            NodePath path = MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(node.getPath());
            MandelbrotQuadTreeNode toWrite = new MandelbrotQuadTreeNode(path);
            toWrite.maximumIteration = node.getMax();
            toWrite.minimumIteration = node.getMin();
            toWrite.status = node.status;
            sum += 1;
            WriterManager.getInstance().write(toWrite, paths.get(toWrite.nodePath.depth));
            index++;
            if (index > blockSize)
            {
                if (generalListener != null)
                {
                    generalListener.print("" + HumanReadable.humanReadableNumber(sum) + " / "
                    + HumanReadable.humanReadableNumber(nodes.size()) + " " + 100f * (float) sum / (float) nodes.size()
                    + "%");
                }
                index = 0;
            }
        }
        for (int i = 0; i <= maxDepth; ++i)
        {
            WriterManager.getInstance().release(paths.get(i));
        }
    }
}
