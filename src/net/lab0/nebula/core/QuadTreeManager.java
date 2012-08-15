package net.lab0.nebula.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.data.Statistics;
import net.lab0.nebula.data.StatisticsData;
import net.lab0.nebula.data.SynchronizedCounter;
import net.lab0.nebula.enums.SaveMode;
import net.lab0.nebula.enums.Status;
import net.lab0.nebula.exception.NoMoreNodesToCompute;
import net.lab0.nebula.listener.QuadTreeManagerListener;
import net.lab0.tools.Pair;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

public class QuadTreeManager
{
    private QuadTreeNode                 root;
    private int                          pointsPerSide;
    private int                          maxIter;
    private int                          diffIterLimit;
    private int                          maxDepth;
    private long                         totalComputingTime;
    private SaveMode                     saveMode;
    private int                          filesCount;
    
    private int                          searchCounter;
    private long                         searchTime;
    private SynchronizedCounter          computedNodes;
    private Queue<List<QuadTreeNode>>    nodesList         = new LinkedList<>();
    private int                          maxCapacity       = 1 << 20;           // 1M nodes max
                                                                                 
    private int                          threads           = 1;
    private boolean                      stop              = false;
    private SynchronizedCounter          remainingNodesToCompute;
    
    private Set<QuadTreeManagerListener> eventListenerList = new HashSet<>();
    private int                          totalComputedNodes;
    private int                          totalNodesToCompute;
    
    private Path                         originalPath;
    private int                          splitDepth        = 6;
    
    public QuadTreeManager(QuadTreeNode root, int pointsPerSide, int maxIter, int diffIterLimit, int maxDepth)
    {
        super();
        this.root = root;
        this.pointsPerSide = pointsPerSide;
        this.maxIter = maxIter;
        this.diffIterLimit = diffIterLimit;
        this.maxDepth = maxDepth;
        
        this.computedNodes = new SynchronizedCounter(0);
    }
    
    // TODO : change folder into index.xml
    public QuadTreeManager(Path inputFolder)
    throws ValidityException, ParsingException, IOException
    {
        this.originalPath = inputFolder;
        
        Builder parser = new Builder();
        Document doc = parser.build(new File(inputFolder.toFile(), "index.xml"));
        Element index = doc.getRootElement();
        
        this.pointsPerSide = Integer.parseInt(index.getAttributeValue("pointsPerSide"));
        this.maxIter = Integer.parseInt(index.getAttributeValue("maxIter"));
        this.diffIterLimit = Integer.parseInt(index.getAttributeValue("diffIterLimit"));
        this.maxDepth = Integer.parseInt(index.getAttributeValue("maxDepth"));
        this.totalComputingTime = Long.parseLong(index.getAttributeValue("totalComputingTime"));
        this.computedNodes = new SynchronizedCounter(Long.parseLong(index.getAttributeValue("computedNodes")));
        
        saveMode = SaveMode.RECURSIVE;
        Elements files = index.getChildElements("file");
        ArrayList<Pair<File, String>> filesAndParent = new ArrayList<>(files.size());
        for (int i = 0; i < files.size(); ++i)
        {
            Element file = files.get(i);
            File xmlFile = new File(inputFolder.toFile().getAbsolutePath() + file.getAttributeValue("path"));
            String quadTreeParentPath = file.getAttributeValue("parent");
            filesAndParent.add(new Pair<File, String>(xmlFile, quadTreeParentPath));
        }
        filesCount = filesAndParent.size() + 1;
        
        Pair<File, String> rootDataFile = filesAndParent.remove(0);
        Builder dataParser = new Builder();
        Document dataDoc = dataParser.build(rootDataFile.a);
        Element mandelbrot = dataDoc.getRootElement();
        this.root = new QuadTreeNode(mandelbrot.getFirstChildElement("node"), null);
        
        int currentFileIndex = 0;
        for (Pair<File, String> file : filesAndParent)
        {
            currentFileIndex++;
            System.out.println("reading file " + currentFileIndex + " out of " + filesAndParent.size());
            dataDoc = dataParser.build(file.a);
            mandelbrot = dataDoc.getRootElement();
            
            QuadTreeNode parent = this.root.getNodeByPath(file.b);
            QuadTreeNode node = new QuadTreeNode(mandelbrot.getFirstChildElement("node"), parent);
            // if (parent == null)
            // {
            // System.out.println("parent null " + file.b);
            // }
            parent.ensureChildrenArray();
            parent.children[node.positionInParent.ordinal()] = node;
            // System.out.println("attached child node n" + node.positionInParent.ordinal() + " : "
            // + parent.children[node.positionInParent.ordinal()].getPath() + " to " + node.parent.getPath());
        }
        
        this.root.updateDepth();
    }
    
    public void addQuadTreeManagerListener(QuadTreeManagerListener listener)
    {
        eventListenerList.add(listener);
    }
    
    public void fireComputeProgress(int current, int total)
    {
        for (QuadTreeManagerListener listener : eventListenerList)
        {
            listener.computeProgress(current, total);
        }
    }
    
    private void fireComputationFinished(boolean b)
    {
        for (QuadTreeManagerListener listener : eventListenerList)
        {
            listener.computationFinished(b);
        }
    }
    
    public void saveACopy(String prefix, String suffix)
    throws IOException
    {
        File originalFile = originalPath.toFile();
        if (prefix == null)
        {
            prefix = "";
        }
        if (suffix == null)
        {
            suffix = "";
        }
        File saveFile = new File(originalFile.getParentFile(), prefix + originalFile.getName() + suffix);
        
        saveToXML(saveFile.toPath());
    }
    
    public void saveToXML(Path outputDirectoryPath)
    throws IOException
    {
        File outputDirectoryFile = outputDirectoryPath.toFile();
        if (!outputDirectoryFile.exists())
        {
            outputDirectoryFile.mkdirs();
        }
        
        // file containing general information and information about other created files
        Element index = new Element("index");
        index.addAttribute(new Attribute("mode", "recursive"));
        index.addAttribute(new Attribute("pointsPerSide", "" + pointsPerSide));
        index.addAttribute(new Attribute("maxIter", "" + maxIter));
        index.addAttribute(new Attribute("diffIterLimit", "" + diffIterLimit));
        index.addAttribute(new Attribute("maxDepth", "" + maxDepth));
        index.addAttribute(new Attribute("totalComputingTime", "" + totalComputingTime));
        index.addAttribute(new Attribute("computedNodes", "" + computedNodes.getValue()));
        
        int dataIndex = 0;
        
        // list containing the nodes which are splitting the tree for a given depth
        List<QuadTreeNode> splittingNodes = new LinkedList<>();
        splittingNodes.add(this.root);
        
        while (!splittingNodes.isEmpty())
        {
            QuadTreeNode currentNode = splittingNodes.remove(0);
            
            Element docRoot = new Element("mandelbrotQuadTree");
            docRoot.addAttribute(new Attribute("path", currentNode.getPath()));
            
            recursiveAppendChildren(docRoot, currentNode, currentNode.depth + splitDepth, currentNode.depth + 2 * splitDepth, splittingNodes);
            
            List<String> relativeFilePath = new LinkedList<>();
            int value = dataIndex;
            
            value >>= 8;
            int folder1 = value & 0xff;
            int folder2 = (value & 0xff00) >> 8;
            int folder3 = (value & 0xff0000) >> 16;
            // int folder4 = (value & 0xff000000)>>24;
            
            // relativeFilePath.add("" + folder4);
            relativeFilePath.add("" + folder3);
            relativeFilePath.add("" + folder2);
            relativeFilePath.add("" + folder1);
            
            relativeFilePath.add("data" + dataIndex + ".xml");
            File baseFile = FileSystems.getDefault().getPath(outputDirectoryPath.toString(), relativeFilePath.toArray(new String[0])).toFile();
            
            if (!baseFile.getParentFile().exists())
            {
                baseFile.getParentFile().mkdirs();
            }
            
            String fileName = FileSystems.getDefault().getPath(".", relativeFilePath.toArray(new String[0])).toFile().getPath();
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
            
            // if (dataIndex == 0 || dataIndex == 64 || dataIndex == 4096 || dataIndex == 4096 * 64 || dataIndex == 4096 * 4096)
            // {
            // System.out.println("Generating file " + fileName + " for " + currentNode.getPath() + " datablock=" + dataIndex);
            // }
            
            Document dataDocument = new Document(docRoot);
            
            Serializer dataSerializer = new Serializer(new BufferedOutputStream(new FileOutputStream(baseFile)), "utf8");
            dataSerializer.setIndent(2);
            dataSerializer.setMaxLength(0);
            dataSerializer.write(dataDocument);
            
            dataIndex++;
        }
        
        File indexFile = FileSystems.getDefault().getPath(outputDirectoryPath.toString(), "index.xml").toFile();
        
        Serializer indexSerializer = new Serializer(new BufferedOutputStream(new FileOutputStream(indexFile)), "utf8");
        indexSerializer.setIndent(2);
        indexSerializer.setMaxLength(0);
        Document indexDocument = new Document(index);
        indexSerializer.write(indexDocument);
    }
    
    private void recursiveAppendChildren(Element containingXmlNode, QuadTreeNode quadTreeNode, int minSplitDepth, int maxSplitDepth,
    List<QuadTreeNode> splittingNodes)
    {
        // System.out.println("depth " + quadTreeNode.depth);
        if (quadTreeNode.depth < minSplitDepth || quadTreeNode.getMaxChildrenDepth() < maxSplitDepth)
        {
            Element childNode = quadTreeNode.asXML(false);
            containingXmlNode.appendChild(childNode);
            
            if (quadTreeNode.children != null)
            {
                for (QuadTreeNode childQuadTreeNode : quadTreeNode.children)
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
    
    public synchronized List<QuadTreeNode> getNextNodeToCompute(int maxComputationDepth)
    throws NoMoreNodesToCompute
    {
        if (nodesList.isEmpty())
        {
            long startTime = System.currentTimeMillis();
            searchCounter++;
            // System.out.println(Thread.currentThread().getName() + " searching nodes");
            List<QuadTreeNode> tmpList = new ArrayList<>();
            root.getNodesByStatus(tmpList, Arrays.asList(Status.BROWSED));
            
            for (QuadTreeNode node : tmpList)
            {
                if (node.depth < maxComputationDepth)
                {
                    node.splitNode();
                }
            }
            
            tmpList.clear();
            boolean nodesAvailable = false;
            root.getNodesByStatus(tmpList, Arrays.asList(Status.VOID));
            
            int blockSize = 16;
            int currentSize = 0;
            int remaining = (int) (remainingNodesToCompute.getValue() > (long) maxCapacity ? maxCapacity : remainingNodesToCompute.getValue());
            List<QuadTreeNode> nodes = new ArrayList<>(blockSize);
            for (QuadTreeNode n : tmpList)
            {
                if (n.depth <= maxComputationDepth)
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
                throw new NoMoreNodesToCompute();
            }
            nodesList.add(nodes);
            
            long endTime = System.currentTimeMillis();
            
            searchTime += (endTime - startTime);
        }
        
        List<QuadTreeNode> nodes = nodesList.poll();
        if (nodes != null)
        {
            for (QuadTreeNode node : nodes)
            {
                if (node != null)
                {
                    node.flagForComputing();
                }
            }
        }
        else
        {
            nodes = new ArrayList<>(0);
        }
        
        return nodes;
    }
    
    /**
     * 
     * @param quantity
     * @return true if there is more nodes to compute
     * @throws InterruptedException
     */
    public boolean compute(int quantity)
    throws InterruptedException
    {
        totalComputedNodes = 0;
        totalNodesToCompute = quantity;
        remainingNodesToCompute = new SynchronizedCounter(quantity);
        long startTime = System.currentTimeMillis();
        
        List<Thread> threadsList = new ArrayList<>(threads);
        for (int i = 0; i < threads; ++i)
        {
            QuadTreeComputeThread thread = new QuadTreeComputeThread(this, remainingNodesToCompute, computedNodes);
            thread.setPriority(Thread.MIN_PRIORITY);
            threadsList.add(thread);
            thread.start();
        }
        
        for (Thread thread : threadsList)
        {
            thread.join();
        }
        
        long endTime = System.currentTimeMillis();
        
        totalComputingTime += (endTime - startTime);
        
        try
        {
            List<QuadTreeNode> list = getNextNodeToCompute(getMaxDepth());
            System.out.println("More nodes : " + list.size());
            fireComputationFinished(true);
            return true;
        }
        catch (NoMoreNodesToCompute e)
        {
            System.out.println("No node left");
            fireComputationFinished(false);
            return false;
        }
    }
    
    public Statistics computeStatistics()
    {
        return computeStatistics(root);
    }
    
    public static Statistics computeStatistics(QuadTreeNode node)
    {
        Statistics statistics = new Statistics();
        recursiveComputeStatistics(node, statistics);
        return statistics;
    }
    
    private static void recursiveComputeStatistics(QuadTreeNode node, Statistics statistics)
    {
        // System.out.println(node.getPath());
        StatisticsData data = statistics.getStatisticsDataForDepth(node.depth);
        data.addStatusCount(node.status, 1);
        data.addSurface(node.status, node.getSurface());
        
        if (node.children != null)
        {
            for (QuadTreeNode child : node.children)
            {
                recursiveComputeStatistics(child, statistics);
            }
        }
    }
    
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
    
    public QuadTreeNode getQuadTreeRoot()
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
    
    public int getSearchCounter()
    {
        return searchCounter;
    }
    
    public long getSearchTime()
    {
        return searchTime;
    }
    
    public long getComputedNodesCount()
    {
        return computedNodes.getValue();
    }
    
    public SaveMode getSaveMode()
    {
        return saveMode;
    }
    
    public int getFilesCount()
    {
        return filesCount;
    }
    
    public int getThreads()
    {
        return threads;
    }
    
    public void setThreads(int threads)
    {
        this.threads = threads;
    }
    
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
    
}
