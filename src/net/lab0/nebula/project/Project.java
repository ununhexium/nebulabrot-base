package net.lab0.nebula.project;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.lab0.nebula.color.PowerGrayScaleColorModel;
import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.data.MandelbrotQuadTreeNode.NodePath;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.data.StatusQuadTreeNode;
import net.lab0.nebula.enums.Status;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import net.lab0.nebula.exception.NonEmptyFolderException;
import net.lab0.nebula.exception.ProjectException;
import net.lab0.nebula.exception.SerializationException;
import net.lab0.nebula.exe.MandelbrotQuadTreeNodeReader;
import net.lab0.nebula.exe.PointsBlockReader;
import net.lab0.nebula.exe.builder.BuilderFactory;
import net.lab0.nebula.exe.builder.ToPointsBlockAggregator;
import net.lab0.nebula.listener.GeneralListener;
import net.lab0.nebula.listener.QuadTreeManagerListener;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.FileUtils;
import net.lab0.tools.HumanReadable;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;
import net.lab0.tools.geom.RectangleInterface;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.io.PatternFilenameFilter;

/**
 * Stores and loads the parameters of a project.
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
     * @return <code>true</code> if this project was newly created (If there was no project in the project's folder
     *         before a call to the constructor of project)
     */
    public boolean isNewProject()
    {
        return newProject;
    }
    
    /**
     * 
     * @return The project's information root node.
     */
    public ProjetInformation getProjetInformation()
    {
        return projetInformation;
    }
    
    /**
     * Imports the quad tree at the given location in this project.
     * 
     * @param input
     *            The index file of the quad tree to import
     * @param maxDepth
     *            The maximum depth to use when loading the quadtree
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
            managerListener, maxDepth);
            
            int nodesCounts = manager.getQuadTreeRoot().getTotalNodesCount();
            List<StatusQuadTreeNode> nodes = new ArrayList<>(nodesCounts);
            manager.getQuadTreeRoot().getAllNodes(nodes);
            
            // real max depth can be lower than the given parameter: checking that here
            maxDepth = Math.min(manager.getQuadTreeRoot().getMaxNodeDepth(), maxDepth);
            
            if (generalListener != null)
            {
                generalListener.print("Quad tree loaded. The maximum depth is " + maxDepth + ". It has " + nodes.size()
                + " nodes.");
            }
            
            // prepare the paths
            List<Path> paths = new ArrayList<>(maxDepth);
            Path basePath = getQuadTreeFolderPath();
            int suffix = FileUtils.getNextAvailablePath(basePath, "");
            Path folder = basePath.resolve("" + suffix);
            folder.toFile().mkdirs();
            for (int i = 0; i <= maxDepth; ++i)
            {
                paths.add(folder.resolve("d_" + i + ".data"));
            }
            
            extractAndSave(nodes, maxDepth, paths, generalListener);
            
            QuadTreeInformation information = new QuadTreeInformation();
            information.id = suffix;
            information.maximumIterationCount = manager.getMaxIter();
            information.maximumIterationDifference = manager.getDiffIterLimit();
            information.pointsCountPerSide = manager.getPointsPerSide();
            information.maxDepth = manager.getQuadTreeRoot().getMaxNodeDepth();
            information.nodesCount = manager.getQuadTreeRoot().getTotalNodesCount();
            projetInformation.quadTreesInformation.quadTrees.add(information);
        }
        catch (ClassNotFoundException | NoSuchAlgorithmException | ParsingException | IOException
        | InvalidBinaryFileException | SerializationException e)
        {
            throw new ProjectException("Error while importing", e);
        }
    }
    
    /**
     * @return The path to the folder containing the quad trees.
     */
    private Path getQuadTreeFolderPath()
    {
        return projectFolder.resolve("quadtree");
    }
    
    /**
     * @return The path to the folder containing the quad tree with the given id.
     */
    private Path getQuadTreeFolderPath(int treeId)
    {
        return getQuadTreeFolderPath().resolve("" + treeId);
    }
    
    /**
     * @return The path to the folder containing the chunks of the given <code>chunckSize</code> size of the quad tree
     *         with the given id.
     */
    private Path getQuadTreeFolderSplitPath(int treeId, int blockSize)
    {
        return getQuadTreeFolderPath(treeId).resolve("s_" + blockSize);
    }
    
    /**
     * @return The path to the folder that contains all the point data
     */
    private Path getPointsFolderPath()
    {
        return projectFolder.resolve("point");
    }
    
    /**
     * @param pointSetId
     * @return The path to the folder containing the points' set with the given id.
     */
    private Path getPointsFolderPath(int pointSetId)
    {
        return getPointsFolderPath().resolve("" + pointSetId);
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
        int blockSize = 1024;
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
    
    /**
     * Split the node of the quad tree with the id <code>id</code> into blocks of node with at most
     * <code>blockSize</code> nodes per file.
     * 
     * @param treeId
     *            The id of the tree that will be used
     * @param blockSize
     *            The size of the split blocks
     * @throws ProjectException
     *             If it doesn't pass the tree id validation. If the blocks of that size already exist.
     * @throws FileNotFoundException
     */
    public void splitNodes(int treeId, int blockSize)
    throws ProjectException, FileNotFoundException
    {
        QuadTreeInformation tree = getProjetInformation().quadTreesInformation.getById(treeId);
        
        if (tree == null)
        {
            throw new ProjectException("The tree id " + treeId + " is invalid.");
        }
        if (tree.blockSizes.contains(blockSize))
        {
            throw new ProjectException("The tree already has a split by " + blockSize + ".");
        }
        
        Path source = getQuadTreeFolderPath().resolve("" + treeId);
        Path dest = getQuadTreeFolderSplitPath(treeId, blockSize);
        if (dest.toFile().exists())
        {
            if (dest.toFile().isDirectory())
            {
                throw new ProjectException("A split already exists at " + dest.toFile().getAbsolutePath());
            }
            if (dest.toFile().isFile())
            {
                throw new ProjectException("A is already a file there: " + dest.toFile().getAbsolutePath());
            }
        }
        
        for (File f : source.toFile().listFiles(new PatternFilenameFilter("d_[0-9]+\\.data")))
        {
            PriorityExecutor executor = new PriorityExecutor(1);
            String fileNameWithoutExtension = f.getName().split("\\.")[0];
            Path outputPath = dest.resolve(fileNameWithoutExtension);
            MandelbrotQuadTreeNodeReader reader = new MandelbrotQuadTreeNodeReader(executor,
            BuilderFactory.toSingleOutputMandelbrotQTNArray(outputPath, "c"), f.toPath(), blockSize);
            executor.execute(reader);
            try
            {
                executor.waitForFinish();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
        
        tree.blockSizes.add(blockSize);
    }
    
    /**
     * Computes the points to render using a quad tree.
     * 
     * @param parameters
     *            {@link PointsComputingParameters}
     * @throws FileNotFoundException
     * @throws ProjectException
     *             If the specified tree id is invalid.
     * @throws InterruptedException
     */
    public void computePoints(final PointsComputingParameters parameters)
    throws FileNotFoundException, ProjectException, InterruptedException
    {
        checkValidity(parameters);
        
        Map<Integer, List<Path>> fileAtDepth = createFilesListByDepth(parameters);
        
        // get the next available output folder
        int nextIndex = FileUtils.getNextAvailablePath(getPointsFolderPath(), "");
        Path pointsBaseOutputFolder = getPointsFolderPath(nextIndex);
        pointsBaseOutputFolder.toFile().mkdirs();
        
        int treeId = parameters.getTreeId();
        QuadTreeInformation tree = getProjetInformation().quadTreesInformation.getById(treeId);
        int maxDepth = Math.min(tree.maxDepth, parameters.getMaxDepth());
        
        // keep useful nodes only
        Predicate<MandelbrotQuadTreeNode> filter = new Predicate<MandelbrotQuadTreeNode>()
        {
            @Override
            public boolean apply(MandelbrotQuadTreeNode node)
            {
                if (node.nodePath.depth == parameters.getMaxDepth())
                {
                    if (node.status == Status.BROWSED)
                    {
                        return true;
                    }
                    else
                    {
                        return node.status == Status.OUTSIDE
                        && node.maximumIteration > parameters.getMinimumIteration();
                    }
                }
                else
                {
                    return node.status == Status.OUTSIDE && node.maximumIteration > parameters.getMinimumIteration();
                }
            }
        };
        
        // process the input files one by one and compress the result
        int chunkId = 0;
        List<LzmaCompressorThread> compressors = new ArrayList<>();
        for (int depth = 0; depth <= maxDepth; ++depth)
        {
            List<Path> filesToUse = fileAtDepth.get(depth);
            for (Path p : filesToUse)
            {
                double step = parameters.getStep(depth);
                Path pointBlocksOutput = pointsBaseOutputFolder.resolve("c_" + chunkId + ".data");
                Path compressedPointBlocksOutput = pointsBaseOutputFolder.resolve("c_" + chunkId + ".data.xz");
                System.out.println("outputing results to " + pointBlocksOutput);
                
                LzmaCompressorThread c = compute(parameters, filter, p, step, pointBlocksOutput,
                compressedPointBlocksOutput);
                compressors.add(c);
                
                chunkId++;
            }
        }
        
        for (LzmaCompressorThread c : compressors)
        {
            if (c.isAlive())
            {
                System.out.println("Waiting " + c.getName());
                c.join();
            }
        }
        
        getProjetInformation().pointsInformation.pointsComputingParameters.add(new PointInformation(nextIndex,
        parameters));
    }
    
    /**
     * Computes the points for each of the quad tree nodes of the given file and start the compression of the output.
     * 
     * @param parameters
     * @param filter
     * @param input
     * @param step
     * @param output
     * @param compressedPointBlocksOutput
     * @return The {@link LzmaCompressorThread} that is compressing the ouput in order to be able to wait for it to
     *         finish its work before finishing the program
     * @throws FileNotFoundException
     * @throws InterruptedException
     */
    private LzmaCompressorThread compute(final PointsComputingParameters parameters,
    Predicate<MandelbrotQuadTreeNode> filter, Path input, double step, final Path output,
    Path compressedPointBlocksOutput)
    throws FileNotFoundException, InterruptedException
    {
        int threads = Runtime.getRuntime().availableProcessors();
        if (parameters.getRoutine() == ComputingRoutine.CPU)
        {
            threads = Runtime.getRuntime().availableProcessors();
        }
        else if (parameters.getRoutine() == ComputingRoutine.OCL)
        {
            threads = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        }
        PriorityExecutor executor = new PriorityExecutor(threads);
        
        JobBuilder<PointsBlock> toFile = //
        BuilderFactory.toPointsBlocksFile(output, 64, Long.MAX_VALUE);
        
        JobBuilder<CoordinatesBlock> toConverter = //
        BuilderFactory.toOCLCompute2(toFile, parameters);
        
        JobBuilder<MandelbrotQuadTreeNode[]> splitAndConvert = //
        BuilderFactory.toNodeSplitterAndConverter(toConverter, step, filter);
        
        MandelbrotQuadTreeNodeReader reader = //
        new MandelbrotQuadTreeNodeReader(executor, splitAndConvert, input, parameters.getBlockSize());
        
        executor.execute(reader);
        executor.registerShutdownHook(new Runnable()
        {
            @Override
            public void run()
            {
                WriterManager.getInstance().release(output);
            }
        });
        executor.waitForFinish();
        
        // release this path as we are sure that we won't write on this file again
        WriterManager.getInstance().release(output);
        
        // make sure to release the memory consumed by the call to native libs
        System.gc();
        
        // in background: compress the file we just created
        LzmaCompressorThread compressorThread = new LzmaCompressorThread(output, compressedPointBlocksOutput, true);
        compressorThread.start();
        
        return compressorThread;
    }
    
    /**
     * Checks that the given parameters are valid for computing
     * 
     * @param parameters
     * @throws ProjectException
     *             If the specified tree id is invalid.
     */
    private void checkValidity(PointsComputingParameters parameters)
    throws ProjectException
    {
        int treeId = parameters.getTreeId();
        QuadTreeInformation tree = getProjetInformation().quadTreesInformation.getById(treeId);
        
        if (tree == null)
        {
            throw new ProjectException("There is no tree with the id " + treeId);
        }
        
        /*
         * ensuring that the max depth parameter doesn't exceed the maximum possible value because it would result in no
         * computation.
         */
        int maxDepth = Math.min(tree.maxDepth, parameters.getMaxDepth());
        parameters.setMaxDepth(maxDepth);
        
        if (parameters.isBlockSizeSpecified())
        {
            int blockSize = parameters.getBlockSize();
            if (!tree.blockSizes.contains(blockSize))
            {
                throw new ProjectException("The requested block size (" + blockSize + ") is not available.");
            }
        }
    }
    
    /**
     * Browses the file system and lists the files to use by depth
     * 
     * @return The list of files that will be used for each depth.
     */
    private Map<Integer, List<Path>> createFilesListByDepth(PointsComputingParameters parameters)
    throws ProjectException
    {
        int treeId = parameters.getTreeId();
        QuadTreeInformation tree = getProjetInformation().quadTreesInformation.getById(treeId);
        int maxDepth = Math.min(tree.maxDepth, parameters.getMaxDepth());
        
        Map<Integer, List<Path>> filesByDepth = new HashMap<>();
        
        if (!parameters.isBlockSizeSpecified())
        {
            for (int depth = 0; depth <= maxDepth; ++depth)
            {
                Path file = getQuadTreeFolderPath(treeId).resolve("d_" + depth + ".data");
                filesByDepth.put(depth, Lists.newArrayList(file));
            }
        }
        else
        {
            int blockSize = parameters.getBlockSize();
            
            Path blocksFolder = getQuadTreeFolderSplitPath(treeId, blockSize);
            for (int depth = 0; depth <= maxDepth; ++depth)
            {
                Path depthFolder = blocksFolder.resolve("d_" + depth);
                List<Path> paths = new ArrayList<>();
                for (File f : depthFolder.toFile().listFiles(new PatternFilenameFilter("c_[0-9]+\\.data")))
                {
                    paths.add(f.toPath());
                }
                filesByDepth.put(depth, paths);
            }
        }
        
        return filesByDepth;
    }
    
    /**
     * Computes a nebula from a given set of files and returns several {@link RawMandelbrotData} files.
     * 
     * @param pointsId
     *            The points to use
     * @param viewPort
     *            The viewport of the rendering
     * @param xRes
     *            The resolution in X
     * @param yRes
     *            The resolution in Y
     * @param minIter
     * @param maxIter
     * @param imageName
     * @return A list of path containing the generated {@link RawMandelbrotData} files.
     * @throws IOException
     * @throws InterruptedException
     * @throws ValidityException
     * @throws ParsingException
     */
    public List<Path> computeNebula(int pointsId, RectangleInterface viewPort, int xRes, int yRes, long minIter,
    long maxIter, String imageName)
    throws IOException, InterruptedException, ValidityException, ParsingException
    {
        Path pointsBlocksBase = FileSystems.getDefault().getPath(getProjectFolder(), "point", "" + pointsId);
        Path nebulaPartsMainOutputFolder = FileSystems.getDefault().getPath(getProjectFolder(), "nebula");
        int nextAvailable = FileUtils.getNextAvailablePath(nebulaPartsMainOutputFolder, "");
        Path nebulaPartsOutputFolder = nebulaPartsMainOutputFolder.resolve("" + nextAvailable);
        
        // quickly check that the files listed are only those we want to use
        Pattern format = Pattern.compile("c_[0-9]+\\.data(.xz)?");
        
        int outputIndex = -1;
        List<Path> partsList = new ArrayList<>();
        File[] filesList = pointsBlocksBase.toFile().listFiles();
        for (File sourceFile : filesList)
        {
            outputIndex++;
            Matcher matcher = format.matcher(sourceFile.getName());
            boolean compressed;
            if (!matcher.matches())
            {
                continue;
            }
            
            if (sourceFile.getName().endsWith("xz"))
            {
                compressed = true;
            }
            else
            {
                compressed = false;
            }
            
            // rendering
            PriorityExecutor priorityExecutor = new PriorityExecutor(Runtime.getRuntime().availableProcessors() - 1);
            RawMandelbrotData aggregate = new RawMandelbrotData(xRes, yRes, 0);
            ToPointsBlockAggregator toAggregator = new ToPointsBlockAggregator(aggregate, viewPort, minIter, maxIter);
            PointsBlockReader pointsBlockReader = new PointsBlockReader(priorityExecutor, toAggregator,
            sourceFile.toPath(), 1024 * 1024, compressed);
            priorityExecutor.execute(pointsBlockReader);
            priorityExecutor.waitForFinish();
            
            System.out.println("Write data " + outputIndex + "/" + filesList.length);
            Path dataPath = nebulaPartsOutputFolder.resolve("" + outputIndex);
            partsList.add(dataPath);
            aggregate.save(dataPath);
            
            /*
             * Graphic rendering
             */
            // System.out.println("Writing image");
            // Path imagePath = imageOutputPath.resolve("i_" + outputIndex + ".png");
            // BufferedImage image = aggregate.computeBufferedImage(new PowerGrayScaleColorModel(0.5), 0);
            // ImageIO.write(image, "png", imagePath.toFile());
            // System.out.println("The image is available at " + imageOutputPath);
        }
        System.out.println("Finished");
        
        return partsList;
    }
    
    /**
     * Sums a list of nebula renderings
     * 
     * @param id
     *            The id of the set of nebula to sum
     * @throws ValidityException
     * @throws ParsingException
     * @throws IOException
     */
    public void sumNebulas(Integer id)
    throws ValidityException, ParsingException, IOException
    {
        List<Path> partsList = new ArrayList<>();
        Path base = FileSystems.getDefault().getPath(getProjectFolder(), "nebula");
        
        for (File p : base.resolve("" + id).toFile().listFiles())
        {
            partsList.add(p.toPath());
        }
        
        RawMandelbrotData concat = RawMandelbrotData.sum(partsList);
        concat.save(base.resolve("" + id).resolve("final"));
    }
    
    /**
     * Does a graphical rendering of a given nebula
     * 
     * @param id
     * @param subid
     * @param name
     *            The name of the output image file. If <code>null</code>, will be the concatenation of the id and the
     *            sub id.
     * @throws IOException
     * @throws ValidityException
     * @throws NoSuchAlgorithmException
     * @throws ParsingException
     * @throws InvalidBinaryFileException
     */
    public void renderNebula(int id, String subid, String name)
    throws IOException, ValidityException, NoSuchAlgorithmException, ParsingException, InvalidBinaryFileException
    {
        if (name == null)
        {
            name = "" + id + "_" + subid + ".png";
        }
        Path imageOutputPath = FileSystems.getDefault().getPath(getProjectFolder(), "image", "nebula", "" + id);
        if (!imageOutputPath.toFile().exists())
        {
            imageOutputPath.toFile().mkdirs();
        }
        Path nebulaPath = FileSystems.getDefault().getPath(this.getProjectFolder(), "nebula", "" + id, subid);
        RawMandelbrotData data = new RawMandelbrotData(nebulaPath);
        BufferedImage image = data.computeBufferedImage(new PowerGrayScaleColorModel(0.5), 0);
        ImageIO.write(image, "png", imageOutputPath.resolve(name).toFile());
    }
    
    /**
     * Concatenates the points of the points set <code>id</code> to make sets of points as close as possible to the
     * <code>size</code>.
     * 
     * @param id
     * @param size
     *            The size in Bytes
     */
    public void concatenatePoints(int id, long size)
    {
        Path pointsPath = FileSystems.getDefault().getPath(this.getProjectFolder(), "point", "" + id);
        
        // the groups of points that will be concatenated together
        List<List<File>> groups = new ArrayList<List<File>>();
        
        // the link between the files' sizes and the group
        Multimap<Long, File> map = ArrayListMultimap.create();
        System.out.println(pointsPath);
        for (File f : pointsPath.toFile().listFiles())
        {
            long l = f.length();
            if (l < size)
            {
                map.put(f.length(), f);
            }
            /*
             * else: we don't create a group of 1 file: useless
             */
        }
        
        List<Long> sizes = new ArrayList<Long>(map.keys());
        Collections.sort(sizes);
        
        // as long as there is something to group
        while (sizes.size() != 0)
        {
            Iterator<Long> it = sizes.iterator();
            List<File> group = new ArrayList<>();
            long currentTotal = 0;
            // we will browse all the sizes available
            while (it.hasNext())
            {
                long l = it.next();
                // adding the corresponding files to the current group if it doesn't get bigger than the max size
                if (l + currentTotal <= size)
                {
                    Collection<File> c = map.get(l);
                    File f = c.iterator().next();
                    // deleting the used element
                    map.remove(l, f);
                    group.add(f);
                    // remembering the new total size
                    currentTotal += l;
                }
            }
            
            // and finally saving the created group to the list of files to group
            groups.add(group);
            group = new ArrayList<>();
        }
        
        for (List<File> list : groups)
        {
            long total = 0;
            System.out.print("[");
            for (File file : list)
            {
                System.out.print(file.getName() + ", ");
                total += file.length();
            }
            System.out.println("] " + HumanReadable.humanReadableSizeInBytes(total));
        }
    }
}
