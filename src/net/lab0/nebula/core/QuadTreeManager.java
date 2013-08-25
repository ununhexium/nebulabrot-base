package net.lab0.nebula.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.event.EventListenerList;

import net.lab0.nebula.data.RootQuadTreeNode;
import net.lab0.nebula.data.Statistics;
import net.lab0.nebula.data.StatisticsData;
import net.lab0.nebula.data.StatusQuadTreeNode;
import net.lab0.nebula.data.SynchronizedCounter;
import net.lab0.nebula.enums.Indexation;
import net.lab0.nebula.enums.Status;
import net.lab0.nebula.enums.TreeSaveMode;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import net.lab0.nebula.exception.NoMoreNodesToCompute;
import net.lab0.nebula.listener.QuadTreeComputeListener;
import net.lab0.nebula.listener.QuadTreeManagerListener;
import net.lab0.tools.MyString;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * 
 * The quad tree manager
 * 
 * @author 116
 * 
 */
public class QuadTreeManager
{
    /**
     * The root that is managed by this QuadTreeManager
     */
    private RootQuadTreeNode                root;
    /**
     * the number of points per side for each node
     */
    private int                             pointsPerSide;
    /**
     * the maximum number of iterations to do
     */
    private int                             maxIter;
    /**
     * the maximum number of iterations difference to consider a node as {@link Status}.OUTSIDE
     */
    private int                             diffIterLimit;
    /**
     * the max depth of computation for this tree
     */
    private int                             maxDepth;
    /**
     * the cumulated computation time for the nodes of this quad tree
     */
    private long                            totalComputingTime;
    /**
     * the number of file needed to save this tree
     */
    private int                             filesCount;
    
    /**
     * the number of nodes which were computed
     */
    private SynchronizedCounter             computedNodes;
    /**
     * the queue of computation blocks
     */
    private Queue<List<StatusQuadTreeNode>> nodesList          = new LinkedList<>();
    /**
     * this value is set in order not to have too long queues
     */
    private int                             maxCapacity        = 1 << 20;                // 1M nodes max in the queue
                                                                                          
    /**
     * defaults to 1 and can't be <1
     */
    private int                             threads            = 1;
    /**
     * set to true when your need to stop all the threads
     */
    private boolean                         stop               = false;
    /**
     * counter for the quantity of nodes to be computed
     */
    private SynchronizedCounter             remainingNodesToCompute;
    
    /**
     * The event listener list
     */
    private EventListenerList               eventListenerList  = new EventListenerList();
    /**
     * the total quantity of computed nodes, includes current computation and the quantity indicated in the original
     * file which saw loaded if any.
     */
    private int                             totalComputedNodes;
    private int                             totalNodesToCompute;
    
    /**
     * The path to the file which was loaded if any.
     */
    private Path                            originalPath;
    /**
     * The desired split depth to use when saving the tree to xml files.
     */
    private int                             splitDepth         = 6;
    
    private TreeSaveMode                    treeSaveMode;
    
    /**
     * Uses openCL if true.
     */
    private boolean                         useOpenCL;
    
    /**
     * This value stores the size of the current read file
     */
    private long                            currentReadFileSize;
    
    /**
     * This value stores the amount of bytes read from an input stream
     */
    private long                            bytesRead;
    
    /**
     * This value stores the amount of bytes read from an input stream
     */
    private long                            previousBytesRead;
    
    /**
     * This value indicates every how many bytes we fire an event for bytes read
     */
    private long                            fireBytesReadEvery = 1024 * 1024;            // 1MiB
                                                                                          
    /**
     * Build a new {@link QuadTreeManager}
     * 
     * @param root
     *            the {@link StatusQuadTreeNode} to use as root
     * @param pointsPerSide
     *            the number of points for each node side
     * @param maxIter
     *            the maximum number of iterations to use for the computation
     * @param diffIterLimit
     *            the amount above which the node is not considerer {@link Status}.INSIDE anymore
     * @param maxDepth
     *            the maximum computation depth for this tree
     */
    public QuadTreeManager(RootQuadTreeNode root, int pointsPerSide, int maxIter, int diffIterLimit, int maxDepth)
    {
        super();
        this.root = root;
        this.pointsPerSide = pointsPerSide;
        this.maxIter = maxIter;
        this.diffIterLimit = diffIterLimit;
        this.maxDepth = maxDepth;
        
        this.computedNodes = new SynchronizedCounter(0);
    }
    
    public QuadTreeManager(Path inputFolder, QuadTreeManagerListener listener)
    throws ValidityException, ParsingException, IOException, ClassNotFoundException, InvalidBinaryFileException,
    NoSuchAlgorithmException
    {
        this(inputFolder, listener, Integer.MAX_VALUE);
    }
    
    public QuadTreeManager(Path inputFolder, QuadTreeManagerListener listener, int maxLoadDepth)
    throws ValidityException, ParsingException, IOException, ClassNotFoundException, InvalidBinaryFileException,
    NoSuchAlgorithmException
    {
        load(inputFolder, listener, maxLoadDepth);
    }
    
    /**
     * Loads a quadTree by reading it from files located in the given <code>inputFolder</code>
     * 
     * @param inputFolder
     *            the folder to read the data from
     * @param listener
     *            an optional listener to have information on the loading process
     * @param maxLoadDepth
     *            the maximum depth to load from the file, including maxLoadDepth. The root has a depth of 0.
     * 
     * @throws ValidityException
     * @throws ParsingException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InvalidBinaryFileException
     * @throws NoSuchAlgorithmException
     */
    private void load(Path inputFolder, QuadTreeManagerListener listener, int maxLoadDepth)
    throws ParsingException, ValidityException, IOException, ClassNotFoundException, InvalidBinaryFileException,
    NoSuchAlgorithmException
    {
        // Save the location of the original file. Useful for saveACopy()
        this.originalPath = inputFolder;
        
        if (listener != null)
        {
            this.eventListenerList.add(QuadTreeManagerListener.class, listener);
        }
        
        // start by parsing the index file
        Builder parser = new Builder();
        Document doc = parser.build(new File(inputFolder.toFile(), "index.xml"));
        Element index = doc.getRootElement();
        
        // parsing quad tree manager's parameters
        this.pointsPerSide = Integer.parseInt(index.getAttributeValue("pointsPerSide"));
        this.maxIter = Integer.parseInt(index.getAttributeValue("maxIter"));
        this.diffIterLimit = Integer.parseInt(index.getAttributeValue("diffIterLimit"));
        this.maxDepth = Integer.parseInt(index.getAttributeValue("maxDepth"));
        this.totalComputingTime = Long.parseLong(index.getAttributeValue("totalComputingTime"));
        this.computedNodes = new SynchronizedCounter(Long.parseLong(index.getAttributeValue("computedNodes")));
        this.treeSaveMode = TreeSaveMode.valueOf(index.getAttributeValue("saveMode"));
        
        switch (treeSaveMode)
        {
            case XML_TREE:
                throw new NotImplementedException();
                
            case CUSTOM_BINARY:
                loadAsCustomBinary(inputFolder, index);
                break;
            
            default:
                break;
        }
        
        this.root.updateFields();
        
        // TODO : better impl. Do not load over max depth
        this.root.strip(maxLoadDepth);
    }
    
    /**
     * Loads from a folder containing files saved in the custom binary format.
     * 
     * @param inputFolder
     *            The folder to read from.
     * @param index
     *            The xml index file.
     * 
     * @throws IOException
     * @throws InvalidBinaryFileException
     * @throws NoSuchAlgorithmException
     *             If the algorithm indicated in the index file is not a valid one.
     */
    private void loadAsCustomBinary(Path inputFolder, Element index)
    throws IOException, InvalidBinaryFileException, NoSuchAlgorithmException
    {
        Element serializedFile = index.getFirstChildElement("serializedFile");
        Element rootInformation = serializedFile.getFirstChildElement("rootInformation");
        Element checksum = serializedFile.getFirstChildElement("checksum");
        
        String algorithm = checksum.getAttributeValue("algorithm");
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        
        boolean indexed = Boolean.parseBoolean(serializedFile.getAttributeValue("indexed"));
        
        File inputFile = FileSystems.getDefault()
        .getPath(inputFolder.toString(), serializedFile.getAttributeValue("path")).toFile();
        FileInputStream fileInputStream = new FileInputStream(inputFile);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        DigestInputStream digestInputStream = new DigestInputStream(bufferedInputStream, messageDigest);
        fireLoadingFile(1, 1);
        bytesRead = 0;
        previousBytesRead = 0;
        currentReadFileSize = inputFile.length();
        StatusQuadTreeNode temporaryRoot = null;
        if (indexed)
        {
            temporaryRoot = recursivelyConvertToQuadTreeWithIndexes(digestInputStream);
        }
        else
        {
            temporaryRoot = recursivelyConvertToQuadTreeWithoutIndexes(digestInputStream);
        }
        
        String fileDigest = MyString.getHexString(digestInputStream.getMessageDigest().digest());
        if (!fileDigest.equalsIgnoreCase(checksum.getAttributeValue("value")))
        {
            throw new InvalidBinaryFileException("Checksums don't match");
        }
        
        double minX = Double.parseDouble(rootInformation.getAttributeValue("minX"));
        double maxX = Double.parseDouble(rootInformation.getAttributeValue("maxX"));
        double minY = Double.parseDouble(rootInformation.getAttributeValue("minY"));
        double maxY = Double.parseDouble(rootInformation.getAttributeValue("maxY"));
        
        this.root = new RootQuadTreeNode(temporaryRoot, minX, maxX, minY, maxY);
        
        bufferedInputStream.close();
        fileInputStream.close();
    }
    
    /**
     * Loads an indexed binary file.
     * 
     * @param inputStream
     * 
     * @return A node and its children.
     * 
     * @throws IOException
     * @throws InvalidBinaryFileException
     */
    private StatusQuadTreeNode recursivelyConvertToQuadTreeWithIndexes(InputStream inputStream)
    throws IOException, InvalidBinaryFileException
    {
        
        byte[] bytes = new byte[26];
        if (bytesRead > previousBytesRead + fireBytesReadEvery)
        {
            previousBytesRead += fireBytesReadEvery;
            fireLoadingOfCurrentFileProgress(bytesRead, currentReadFileSize);
        }
        
        if (inputStream.read(bytes, 0, 26) > 0)
        {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            byteBuffer.position(0);
            bytesRead += 26;
            
            byte statusValue = byteBuffer.get();
            Status status = Status.values()[statusValue];
            
            StatusQuadTreeNode node = new StatusQuadTreeNode(null);
            node.status = status;
            node.setMin(byteBuffer.getInt());
            node.setMax(byteBuffer.getInt());
            
            byte childrenValue = byteBuffer.get();
            
            // we don't care when we load everything
            // int[] pos = new int[4];
            // pos[0] = nodeIndex+1;
            // pos[1] = byteBuffer.getInt();
            // pos[2] = byteBuffer.getInt();
            // pos[3] = byteBuffer.getInt();
            
            if (childrenValue != 0) // has children
            {
                node.children = new StatusQuadTreeNode[4];
                for (int i = 0; i < 4; ++i)
                {
                    node.children[i] = recursivelyConvertToQuadTreeWithIndexes(inputStream);
                }
            }
            
            return node;
        }
        else
        {
            throw new InvalidBinaryFileException();
        }
    }
    
    /**
     * @see The save method for the file format specifications.
     * 
     * @param inputStream
     * 
     * @return A node and its children.
     * 
     * @throws IOException
     * @throws InvalidBinaryFileException
     */
    private StatusQuadTreeNode recursivelyConvertToQuadTreeWithoutIndexes(InputStream inputStream)
    throws IOException, InvalidBinaryFileException
    {
        byte[] bytes = new byte[26];
        if (bytesRead > previousBytesRead + fireBytesReadEvery)
        {
            previousBytesRead += fireBytesReadEvery;
            fireLoadingOfCurrentFileProgress(bytesRead, currentReadFileSize);
        }
        
        if (inputStream.read(bytes, 0, 1) > 0)
        {
            bytesRead += 1;
            StatusQuadTreeNode node = new StatusQuadTreeNode(null);
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            
            byte statusByte = byteBuffer.get(0);
            node.status = Status.values()[statusByte];
            
            if (node.status.equals(Status.OUTSIDE))
            {
                if (inputStream.read(bytes, 0, 8) > 0)
                {
                    bytesRead += 8;
                    node.setMin(byteBuffer.getInt(0));
                    node.setMax(byteBuffer.getInt(4));
                }
                else
                {
                    throw new InvalidBinaryFileException();
                }
            }
            
            if (inputStream.read(bytes, 0, 1) > 0)
            {
                bytesRead += 1;
                byte hasChildrenByte = byteBuffer.get();
                boolean hasChildren = (hasChildrenByte != 0);
                
                if (hasChildren)
                {
                    node.children = new StatusQuadTreeNode[4];
                    for (int i = 0; i < 4; ++i)
                    {
                        node.children[i] = recursivelyConvertToQuadTreeWithoutIndexes(inputStream);
                    }
                }
            }
            
            return node;
        }
        else
        {
            throw new InvalidBinaryFileException();
        }
    }
    
    /**
     * Saves the quad tree to the given folder. This algorithm divides the main tree in several subtrees.
     * 
     * @param outputDirectoryPath
     *            a folder to save the quad tree to
     * @throws IOException
     */
    public void exportToXML(Path outputDirectoryPath)
    throws IOException
    {
        File outputDirectoryFile = outputDirectoryPath.toFile();
        if (!outputDirectoryFile.exists())
        {
            outputDirectoryFile.mkdirs();
        }
        
        // file containing general information and information about other created files
        Element index = new Element("index");
        index.addAttribute(new Attribute("pointsPerSide", "" + pointsPerSide));
        index.addAttribute(new Attribute("maxIter", "" + maxIter));
        index.addAttribute(new Attribute("diffIterLimit", "" + diffIterLimit));
        index.addAttribute(new Attribute("maxDepth", "" + maxDepth));
        index.addAttribute(new Attribute("totalComputingTime", "" + totalComputingTime));
        index.addAttribute(new Attribute("computedNodes", "" + computedNodes.getValue()));
        index.addAttribute(new Attribute("method", "xmlTree"));
        index.addAttribute(new Attribute("saveMode", TreeSaveMode.XML_TREE.toString()));
        
        // counts the number of data files created
        int dataIndex = 0;
        
        // list containing the nodes which are splitting the tree for a given depth
        List<StatusQuadTreeNode> splittingNodes = new LinkedList<>();
        splittingNodes.add(this.root);
        
        /*
         * At the beginning, splittingNodes only contains the root node. After each pass, if the split depth is not high
         * enough to save the whole tree in 1 file, the nodes which are at the limit of the slip depth are added to
         * splittingNodes. The operation is repeated while there are splitting nodes in the list
         */
        while (!splittingNodes.isEmpty())
        {
            StatusQuadTreeNode currentNode = splittingNodes.remove(0);
            
            Element docRoot = new Element("mandelbrotQuadTree");
            docRoot.addAttribute(new Attribute("path", currentNode.getPath()));
            
            // adds all the necessary nodes and retrieves the next nodes to add
            recursiveAppendChildren(docRoot, currentNode, currentNode.getDepth() + splitDepth, currentNode.getDepth()
            + 2 * splitDepth, splittingNodes);
            
            List<String> relativeFilePath = new LinkedList<>();
            
            // this code computes in which folder the file should go.
            int value = dataIndex;
            value >>= 8; // the 8 lower bits are ignored. By doing this, each folder will contain 256 files
            int folder1 = value & 0xff; // the folder containing the data files
            int folder2 = (value & 0xff00) >> 8; // the folder containing the folder containing the data files
            int folder3 = (value & 0xff0000) >> 16; // the folder containing the folder containing the folder containing
                                                    // the data files
            // int folder4 = (value & 0xff000000)>>24;
            
            // final path : folder3/folder2/folder1/data###.xml
            
            // relativeFilePath.add("" + folder4);
            relativeFilePath.add("" + folder3);
            relativeFilePath.add("" + folder2);
            relativeFilePath.add("" + folder1);
            relativeFilePath.add("data" + dataIndex + ".xml");
            File baseFile = FileSystems.getDefault()
            .getPath(outputDirectoryPath.toString(), relativeFilePath.toArray(new String[0])).toFile();
            
            // ensures the existence of the directories
            if (!baseFile.getParentFile().exists())
            {
                baseFile.getParentFile().mkdirs();
            }
            
            // the data file path relatively to the index file
            String fileName = FileSystems.getDefault().getPath(".", relativeFilePath.toArray(new String[0])).toFile()
            .getPath();
            Element file = new Element("file");
            if (currentNode.parent == null)
            {
                file.addAttribute(new Attribute("parent", "null"));
            }
            else
            {
                file.addAttribute(new Attribute("parent", currentNode.parent.getPath()));
            }
            file.addAttribute(new Attribute("path", fileName));
            index.appendChild(file);
            
            // creates the document and saves it
            Document dataDocument = new Document(docRoot);
            Serializer dataSerializer = new Serializer(new BufferedOutputStream(new FileOutputStream(baseFile)),
            "utf-8");
            dataSerializer.setIndent(2);
            dataSerializer.setMaxLength(0);
            dataSerializer.write(dataDocument);
            
            dataIndex++;
        }
        
        // creates and saves the index file
        File indexFile = FileSystems.getDefault().getPath(outputDirectoryPath.toString(), "index.xml").toFile();
        Document indexDocument = new Document(index);
        Serializer indexSerializer = new Serializer(new BufferedOutputStream(new FileOutputStream(indexFile)), "utf-8");
        indexSerializer.setIndent(2);
        indexSerializer.setMaxLength(0);
        indexSerializer.write(indexDocument);
    }
    
    /**
     * Saves the quad tree in a custom binary format.
     * 
     * @see recursivelyConvertToBinaryFileWithIndexes and recursivelyConvertToBinaryFileWithoutIndexes methods for file
     *      specs
     * 
     * 
     * @param outputDirectoryPath
     * @throws IOException
     */
    public void saveToBinaryFile(Path outputDirectoryPath, Indexation indexation)
    throws IOException
    {
        String algorithm = "SHA-512";
        MessageDigest messageDigest = null;
        try
        {
            messageDigest = MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IOException(e);
        }
        
        File outputDirectoryFile = outputDirectoryPath.toFile();
        if (!outputDirectoryFile.exists())
        {
            outputDirectoryFile.mkdirs();
        }
        
        File serializedFile = FileSystems.getDefault().getPath(outputDirectoryPath.toString(), "tree.dat").toFile();
        
        try (
            FileOutputStream fileOutputStream = new FileOutputStream(serializedFile);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            DigestOutputStream digestOutputStream = new DigestOutputStream(bufferedOutputStream, messageDigest);)
        {
            boolean indexed = false;
            switch (indexation)
            {
                case NO_INDEXATION:
                    recursivelyConvertToBinaryFileWithoutIndexes(digestOutputStream, root);
                    indexed = false;
                    break;
                
                case USE_INDEXATION:
                    recursivelyConvertToBinaryFileWithIndexes(digestOutputStream, root, 0);
                    indexed = true;
                    break;
            }
            
            // file containing general information and information about other created files
            Element index = new Element("index");
            index.addAttribute(new Attribute("pointsPerSide", "" + pointsPerSide));
            index.addAttribute(new Attribute("maxIter", "" + maxIter));
            index.addAttribute(new Attribute("diffIterLimit", "" + diffIterLimit));
            index.addAttribute(new Attribute("maxDepth", "" + maxDepth));
            index.addAttribute(new Attribute("totalComputingTime", "" + totalComputingTime));
            index.addAttribute(new Attribute("computedNodes", "" + computedNodes.getValue()));
            index.addAttribute(new Attribute("saveMode", TreeSaveMode.CUSTOM_BINARY.toString()));
            
            Element serializedFileNode = new Element("serializedFile");
            serializedFileNode.addAttribute(new Attribute("path", "./tree.dat"));
            serializedFileNode.addAttribute(new Attribute("binaryVersion", "1"));
            serializedFileNode.addAttribute(new Attribute("indexed", Boolean.toString(indexed)));
            index.appendChild(serializedFileNode);
            
            Element rootInformations = new Element("rootInformation");
            rootInformations.addAttribute(new Attribute("minX", Double.toString(this.root.getMinX())));
            rootInformations.addAttribute(new Attribute("maxX", Double.toString(this.root.getMaxX())));
            rootInformations.addAttribute(new Attribute("minY", Double.toString(this.root.getMinY())));
            rootInformations.addAttribute(new Attribute("maxY", Double.toString(this.root.getMaxY())));
            serializedFileNode.appendChild(rootInformations);
            
            String digest = MyString.getHexString(digestOutputStream.getMessageDigest().digest());
            Element checksum = new Element("checksum");
            checksum.addAttribute(new Attribute("algorithm", algorithm));
            checksum.addAttribute(new Attribute("value", digest));
            serializedFileNode.appendChild(checksum);
            
            // creates and saves the index file
            File indexFile = FileSystems.getDefault().getPath(outputDirectoryPath.toString(), "index.xml").toFile();
            Document indexDocument = new Document(index);
            Serializer indexSerializer = new Serializer(new BufferedOutputStream(new FileOutputStream(indexFile)),
            "utf-8");
            indexSerializer.setIndent(2);
            indexSerializer.setMaxLength(0);
            indexSerializer.write(indexDocument);
        }
    }
    
    /**
     * Recursively translates the quad tree into a binary file
     * 
     * Each node contains 0 or 4 nodes.
     * 
     * <pre>
     * Node
     *  size: 144 bits
     *  infos:
     *      status: 8bits, enum {BROWSED: 0, INSIDE: 1, OUTSIDE: 2, VOID: 3} Theses values are decimal ones.
     *      min: 32 bits if status is OUTSIDE. Set to -1 otherwise.
     *      max: 32 bits if status is OUTSIDE. Set to -1 otherwise.
     *      hasChildren: 8 bit, enum {true: 1, false: 0}
     *      
     *      Positions :
     *      
     *      pos1: 32bits, the absolute position of the second child node.
     *      pos2: 32bits, the absolute position of the third child node.
     *      pos3: 32bits, the absolute position of the fourth child node.
     *      
     *      notes:
     *      RootNode has always index 0
     *      RootNode.firstChild has always index 1
     *      pos0 doesn't need to be written as it is the node right after the current one
     *      The position of subsequent nodes are always indicated even if they don't exist.
     *      This is useful to seek a node in the tree using the tree structure directly in the file instead of reading it all and then browsing it from memory.
     * </pre>
     * 
     * @param outputStream
     *            output file
     * @param node
     *            the node to serialize
     * @param nodesCount
     *            the amount of nodes already written in the file
     * @param messageDigest
     * @throws IOException
     */
    private void recursivelyConvertToBinaryFileWithIndexes(OutputStream outputStream, StatusQuadTreeNode node,
    int nodesCount)
    throws IOException
    {
        byte[] bytes = new byte[26];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        
        byteBuffer.put((byte) node.status.ordinal());
        byteBuffer.putInt(node.getMin());
        byteBuffer.putInt(node.getMax());
        
        int[] pos = new int[4];
        if (node.children != null)
        {
            pos[0] = nodesCount + 1;
            pos[1] = node.children[0].getTotalNodesCount() + pos[0];
            pos[2] = node.children[1].getTotalNodesCount() + pos[1];
            pos[3] = node.children[2].getTotalNodesCount() + pos[2];
            
            byteBuffer.put((byte) 1);
            byteBuffer.putInt(pos[1]);
            byteBuffer.putInt(pos[2]);
            byteBuffer.putInt(pos[3]);
            
        }
        else
        {
            byteBuffer.put((byte) 0);
            byteBuffer.putInt(-1);
            byteBuffer.putInt(-1);
            byteBuffer.putInt(-1);
        }
        
        outputStream.write(bytes, 0, 26);
        
        if (node.children != null)
        {
            for (int i = 0; i < 4; ++i)
            {
                recursivelyConvertToBinaryFileWithIndexes(outputStream, node.children[i], pos[i]);
            }
        }
    }
    
    /**
     * Recursively translates the quad tree into a binary file
     * 
     * Each node contains 0 or 4 nodes.
     * 
     * <pre>
     * Node
     *  size: 16-80 bits
     *  infos:
     *      status: 8bits, enum {BROWSED: 0, INSIDE: 1, OUTSIDE: 2, VOID: 3} Theses values are decimal ones.
     *      min: 32 bits if status is OUTSIDE. Doesn't exist if it is another status.
     *      max: 32 bits if status is OUTSIDE. Doesn't exist if it is another status.
     *      hasChildren: 8 bit, enum {true: 1, false: 0}
     * </pre>
     * 
     * @param outputStream
     *            output file
     * @param node
     *            the node to serialize
     * @param nodesCount
     *            the amount of nodes already written in the file
     * @throws IOException
     */
    private void recursivelyConvertToBinaryFileWithoutIndexes(OutputStream outputStream, StatusQuadTreeNode node)
    throws IOException
    {
        byte[] bytes = new byte[10];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        
        // put the fized size elements
        byteBuffer.put((byte) node.status.ordinal());
        int size = 1;
        
        if (node.status.equals(Status.OUTSIDE))
        {
            byteBuffer.putInt(node.getMin());
            byteBuffer.putInt(node.getMax());
            size += 8;
        }
        
        if (node.children == null)
        {
            byteBuffer.put((byte) 0);
        }
        else
        {
            byteBuffer.put((byte) 1);
        }
        size += 1;
        
        outputStream.write(bytes, 0, size);
        
        if (node.children != null)
        {
            for (int i = 0; i < 4; ++i)
            {
                recursivelyConvertToBinaryFileWithoutIndexes(outputStream, node.children[i]);
            }
        }
    }
    
    /**
     * Recursively appends the given {@link StatusQuadTreeNode} and its children to the <code>containingXmlNode</code>.
     * Adds systematically children which depth is inferior to <code>minSplitDepth</code>. If the
     * {@link StatusQuadTreeNode} has a maximum depth inferior to <code>maxSplitDepth</code>, the nodes at a depths
     * between <code>minSplitDepth</code> and strictly inferior to <code>maxSplitDepth</code> are also added. If the
     * maximum depth of the node is equal to, or over <code>maxSplitDepth</code>, the nodes at depth
     * <code>minSplitDepth</code> are added to the splitting nodes.
     * 
     * @param containingXmlNode
     *            the XML node which contains <code>quadTreeNode</code>
     * @param quadTreeNode
     *            the quad tree node to add recursively
     * @param minSplitDepth
     *            the minimum split depth
     * @param maxSplitDepth
     *            the maximum split depth
     * @param splittingNodes
     *            a list of node to store the splitting nodes
     */
    private void recursiveAppendChildren(Element containingXmlNode, StatusQuadTreeNode quadTreeNode, int minSplitDepth,
    int maxSplitDepth, List<StatusQuadTreeNode> splittingNodes)
    {
        // if the node can be saved in 1 file
        if (quadTreeNode.getDepth() < minSplitDepth || quadTreeNode.getMaxNodeDepth() < maxSplitDepth)
        {
            Element childNode = quadTreeNode.asXML(false);
            containingXmlNode.appendChild(childNode);
            
            if (quadTreeNode.children != null)
            {
                for (StatusQuadTreeNode childQuadTreeNode : quadTreeNode.children)
                {
                    recursiveAppendChildren(childNode, childQuadTreeNode, minSplitDepth, maxSplitDepth, splittingNodes);
                }
            }
        }
        else
        {
            splittingNodes.add(quadTreeNode);
        }
    }
    
    /**
     * Returns a list of nodes which need to be computed.
     * 
     * @param maxComputationDepth
     *            the maximum computation depth
     * @param blockSize
     *            the size of the block to retrieve
     * @return a list of {@link StatusQuadTreeNode}s, which may contains between 0 and <code>blockSize</code> elements.
     *         Never returns null.
     * @throws NoMoreNodesToCompute
     */
    public synchronized List<StatusQuadTreeNode> getNextNodeToCompute(int maxComputationDepth, int blockSize)
    throws NoMoreNodesToCompute
    {
        // if there is no list of nodes left : refill it
        if (nodesList.isEmpty())
        {
            List<StatusQuadTreeNode> tmpList = new ArrayList<>();
            root.getNodesByStatus(tmpList, Arrays.asList(Status.BROWSED));
            
            // split all the browsed node which depth is strictly inferior to maxComputationDepth
            for (StatusQuadTreeNode node : tmpList)
            {
                if (node.getDepth() < maxComputationDepth)
                {
                    node.splitNode();
                }
            }
            
            tmpList.clear();
            boolean nodesAvailable = false;
            root.getNodesByStatus(tmpList, Arrays.asList(Status.VOID));
            
            int currentSize = 0;
            int remaining = (int) (remainingNodesToCompute.getValue() > (long) maxCapacity ? maxCapacity
            : remainingNodesToCompute.getValue());
            List<StatusQuadTreeNode> nodes = new ArrayList<>(blockSize);
            for (StatusQuadTreeNode n : tmpList)
            {
                if (n.getDepth() <= maxComputationDepth)
                {
                    nodesAvailable = true;
                    if (!n.isFlagedForComputing())
                    {
                        nodes.add(n);
                        remaining--;
                        currentSize++;
                        if (currentSize >= blockSize)
                        {
                            nodesList.add(nodes);
                            nodes = new ArrayList<>(blockSize);
                            currentSize = 0;
                        }
                        if (remaining <= 0)
                        {
                            break;
                        }
                    }
                }
            }
            if (!nodesAvailable)
            {
                // signals that there are no more nodes to compute
                throw new NoMoreNodesToCompute();
            }
            nodesList.add(nodes);
        }
        
        List<StatusQuadTreeNode> nodes = nodesList.poll();
        if (nodes != null)
        {
            for (StatusQuadTreeNode node : nodes)
            {
                if (node != null)
                {
                    node.flagForComputing();
                }
            }
        }
        else
        {
            // do not return null because it would violate the contracts of the method.
            // do not return NoMoreNodesToCompute because there may be a node currently computed which may be assigned
            // the value BROWSED and then contain other
            // nodes to compute.
            nodes = new LinkedList<>();
        }
        
        return nodes;
    }
    
    /**
     * Computes up to <code>quantity</code> nodes and returns.
     * 
     * @param quantity
     *            The amount of nodes to compute.
     * @return <code>true</code> if there is more nodes to compute
     */
    public boolean compute(int quantity)
    {
        totalComputedNodes = 0;
        totalNodesToCompute = quantity;
        remainingNodesToCompute = new SynchronizedCounter(quantity);
        long startTime = System.currentTimeMillis();
        
        // creation of the computing thread(s)
        List<AbstractQuadTreeComputeThread> threadsList = new ArrayList<>(threads);
        
        for (int i = 0; i < threads; ++i)
        {
            CPUQuadTreeComputeThread thread = new CPUQuadTreeComputeThread(this, remainingNodesToCompute,
            computedNodes, 16);
            thread.setPriority(Thread.MIN_PRIORITY);
            threadsList.add(thread);
        }
        
        // add the listeners
        for (AbstractQuadTreeComputeThread thread : threadsList)
        {
            for (QuadTreeComputeListener listener : eventListenerList.getListeners(QuadTreeComputeListener.class))
            {
                thread.addQuadTreeComputeListener(listener);
            }
        }
        
        // start
        for (AbstractQuadTreeComputeThread thread : threadsList)
        {
            thread.start();
        }
        
        try
        {
            // waiting for the threads to finish
            for (Thread thread : threadsList)
            {
                thread.join();
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        
        long endTime = System.currentTimeMillis();
        
        totalComputingTime += (endTime - startTime);
        
        try
        {
            List<StatusQuadTreeNode> list = getNextNodeToCompute(getMaxDepth(), 1);
            if (!list.isEmpty())
            {
                list.get(0).unFlagForComputing();
            }
            fireComputationFinished(true);
            return true;
        }
        catch (NoMoreNodesToCompute e)
        {
            fireComputationFinished(false);
            return false;
        }
    }
    
    /**
     * compute statistics for the root node
     * 
     * @return {@link Statistics}
     */
    public Statistics computeStatistics()
    {
        return computeStatistics(root);
    }
    
    /**
     * compute statistics for the given node
     * 
     * @param node
     *            to compute statistics on
     * @return {@link Statistics}
     */
    public static Statistics computeStatistics(StatusQuadTreeNode node)
    {
        Statistics statistics = new Statistics();
        recursiveComputeStatistics(node, statistics);
        return statistics;
    }
    
    private static void recursiveComputeStatistics(StatusQuadTreeNode node, Statistics statistics)
    {
        // System.out.println(node.getPath());
        StatisticsData data = statistics.getStatisticsDataForDepth(node.getDepth());
        data.addStatusCount(node.status, 1);
        data.addSurface(node.status, node.getSurface());
        if (node.status == Status.OUTSIDE)
        {
            data.addIterations(node.getMin(), node.getMax());
            statistics.updateMaxKnownIter(node.getMax());
        }
        
        if (node.children != null)
        {
            for (StatusQuadTreeNode child : node.children)
            {
                recursiveComputeStatistics(child, statistics);
            }
        }
    }
    
    /**
     * 
     * @return the maximum depth parameter used for computing the quad tree
     */
    public int getMaxDepth()
    {
        return maxDepth;
    }
    
    public void setMaxDepth(int maxDepth)
    {
        this.maxDepth = maxDepth;
    }
    
    public int getPointsPerSide()
    {
        return pointsPerSide;
    }
    
    public int getMaxIter()
    {
        return maxIter;
    }
    
    public int getDiffIterLimit()
    {
        return diffIterLimit;
    }
    
    public long getTotalComputingTime()
    {
        return totalComputingTime;
    }
    
    public StatusQuadTreeNode getQuadTreeRoot()
    {
        return root;
    }
    
    public synchronized boolean stopRequired()
    {
        return stop;
    }
    
    public synchronized void stopNow()
    {
        stop = true;
    }
    
    public void resetStop()
    {
        stop = false;
    }
    
    public long getComputedNodesCount()
    {
        return computedNodes.getValue();
    }
    
    public int getFilesCount()
    {
        return filesCount;
    }
    
    public int getThreads()
    {
        return threads;
    }
    
    /**
     * Sets the thread count. Must be invoked before calling compute().
     * 
     * @param threads
     * @throws IllegalArgumentException
     *             if <code>threads</code> is not positive
     */
    public void setThreads(int threads)
    {
        if (threads < 1)
        {
            throw new IllegalArgumentException("threads must be > 0");
        }
        this.threads = threads;
    }
    
    /**
     * adds <code>computed</code> node to the toal of computed nodes and fires a compute progress
     * 
     * @param computed
     */
    public void computedNodes(int computed)
    {
        totalComputedNodes += computed;
        fireComputeProgress(totalComputedNodes, totalNodesToCompute);
    }
    
    public Path getOriginalPath()
    {
        return originalPath;
    }
    
    public int getSplitDepth()
    {
        return splitDepth;
    }
    
    public void setSplitDepth(int splitDepth)
    {
        this.splitDepth = splitDepth;
    }
    
    public boolean isUseOpenCL()
    {
        return useOpenCL;
    }
    
    public void setUseOpenCL(boolean useOpenCL)
    {
        this.useOpenCL = useOpenCL;
    }
    
    public void addQuadTreeManagerListener(QuadTreeManagerListener listener)
    {
        eventListenerList.add(QuadTreeManagerListener.class, listener);
    }
    
    public void addQuadTreeComputeListener(QuadTreeComputeListener listener)
    {
        eventListenerList.add(QuadTreeComputeListener.class, listener);
    }
    
    public void fireComputeProgress(int current, int total)
    {
        for (QuadTreeManagerListener listener : eventListenerList.getListeners(QuadTreeManagerListener.class))
        {
            listener.computeProgress(current, total);
        }
    }
    
    public void fireThreadSleeping(long threadId)
    {
        for (QuadTreeManagerListener listener : eventListenerList.getListeners(QuadTreeManagerListener.class))
        {
            listener.threadSleeping(threadId);
        }
    }
    
    public void fireThreadStarted(long threadId, String name)
    {
        for (QuadTreeManagerListener listener : eventListenerList.getListeners(QuadTreeManagerListener.class))
        {
            listener.threadStarted(threadId, name);
        }
    }
    
    public void fireThreadResumed(long threadId)
    {
        for (QuadTreeManagerListener listener : eventListenerList.getListeners(QuadTreeManagerListener.class))
        {
            listener.threadResumed(threadId);
        }
    }
    
    private void fireComputationFinished(boolean b)
    {
        for (QuadTreeManagerListener listener : eventListenerList.getListeners(QuadTreeManagerListener.class))
        {
            listener.computationFinished(b);
        }
    }
    
    private void fireLoadingFile(int current, int total)
    {
        for (QuadTreeManagerListener listener : eventListenerList.getListeners(QuadTreeManagerListener.class))
        {
            listener.loadingFile(current, total);
        }
    }
    
    private void fireLoadingOfCurrentFileProgress(long bytesRead2, long currentReadFileSize2)
    {
        for (QuadTreeManagerListener listener : eventListenerList.getListeners(QuadTreeManagerListener.class))
        {
            listener.loadingOfCurrentFileProgress(bytesRead2, currentReadFileSize2);
        }
    }
}
