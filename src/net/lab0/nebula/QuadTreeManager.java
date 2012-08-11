
package net.lab0.nebula;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.data.Statistics;
import net.lab0.nebula.data.StatisticsData;
import net.lab0.nebula.data.Status;
import net.lab0.nebula.data.SynchronizedCounter;
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
    private QuadTreeNode              root;
    private int                       pointsPerSide      = 100;
    private int                       maxIter            = 512;
    private int                       diffIterLimit      = 5;
    private int                       maxDepth           = 6;
    private long                      totalComputingTime = 0;
    private SaveMode                  saveMode;
    private int                       filesCount;
    
    private int                       searchCounter      = 0;
    private long                      searchTime         = 0;
    private SynchronizedCounter       computedNodes;
    private Queue<List<QuadTreeNode>> nodesList          = new LinkedList<>();
    
    private int                       threads;
    private boolean                   stop               = false;
    private Semaphore                 waitingThreads;
    
    public QuadTreeManager(QuadTreeNode root, int pointsPerSide, int maxIter, int diffIterLimit, int maxDepth, int threads)
    {
        super();
        this.root = root;
        this.pointsPerSide = pointsPerSide;
        this.maxIter = maxIter;
        this.diffIterLimit = diffIterLimit;
        this.maxDepth = maxDepth;
        
        this.threads = threads;
        this.computedNodes = new SynchronizedCounter(0);
        this.waitingThreads = new Semaphore(threads);
    }
    
    public QuadTreeManager(Path inputFolder)
    throws ValidityException, ParsingException, IOException
    {
        Builder parser = new Builder();
        Document doc = parser.build(new File(inputFolder.toFile(), "index.xml"));
        Element index = doc.getRootElement();
        
        this.pointsPerSide = Integer.parseInt(index.getAttributeValue("pointsPerSide"));
        this.maxIter = Integer.parseInt(index.getAttributeValue("maxIter"));
        this.diffIterLimit = Integer.parseInt(index.getAttributeValue("diffIterLimit"));
        this.maxDepth = Integer.parseInt(index.getAttributeValue("maxDepth"));
        this.totalComputingTime = Long.parseLong(index.getAttributeValue("totalComputingTime"));
        this.computedNodes = new SynchronizedCounter(Long.parseLong(index.getAttributeValue("computedNodes")));
        
        String mode = index.getAttributeValue("mode");
        if (SaveMode.ONE_FILE.modeName.equals(mode))
        {
            saveMode = SaveMode.ONE_FILE;
            filesCount = 2;
            Builder dataParser = new Builder();
            File dataFile = new File(inputFolder.toFile(), index.getFirstChildElement("file").getAttributeValue("path"));
            System.out.println("Opening " + dataFile.getPath());
            Document dataDoc = dataParser.build(dataFile);
            Element mandelbrot = dataDoc.getRootElement();
            
            this.root = new QuadTreeNode(mandelbrot.getFirstChildElement("node"), null);
            this.root.updateDepth();
        }
        else if (SaveMode.RECURSIVE.modeName.equals(mode))
        {
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
    }
    
    public synchronized List<QuadTreeNode> getNextNodeToCompute(int maxComputationDepth) throws NoMoreNodesToCompute
    {
        if (nodesList.isEmpty())
        {
            long startTime = System.currentTimeMillis();
            searchCounter++;
            System.out.println(Thread.currentThread().getName() + " searching nodes");
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
            
            int blockSize = 1024;
            int currentSize = 0;
            List<QuadTreeNode> nodes = new ArrayList<>(blockSize);
            nodesList.add(nodes);
            for (QuadTreeNode n : tmpList)
            {
                if (n.depth <= maxComputationDepth)
                {
                    nodesAvailable = true;
                    if (!n.isFlagedForComputing())
                    {
                        nodes.add(n);
                        currentSize++;
                        if (currentSize >= blockSize)
                        {
                            nodes = new ArrayList<>(blockSize);
                            currentSize = 0;
                            nodesList.add(nodes);
                        }
                    }
                }
            }
            if (!nodesAvailable)
            {
                throw new NoMoreNodesToCompute();
            }
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
    
    public void saveToXML(Path outputDirectoryPath, boolean splitIntoMultipleFiles, int splitDepth) throws IOException
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
        
        if (splitIntoMultipleFiles)
        {
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
        else
        {
            Element docRoot = new Element("mandelbrotQuadTree");
            docRoot.appendChild(this.root.asXML(true));
            
            Document dataDocument = new Document(docRoot);
            
            File baseFile = FileSystems.getDefault().getPath(outputDirectoryPath.toString(), "data0.xml").toFile();
            Serializer serializer = new Serializer(new BufferedOutputStream(new FileOutputStream(baseFile)), "utf8");
            serializer.setIndent(2);
            serializer.setMaxLength(0);
            serializer.write(dataDocument);
            
            index.addAttribute(new Attribute("mode", SaveMode.ONE_FILE.modeName));
            Element file = new Element("file");
            file.addAttribute(new Attribute("parent", "null"));
            file.addAttribute(new Attribute("path", "data0.xml"));
            index.appendChild(file);
            
            File indexFile = FileSystems.getDefault().getPath(outputDirectoryPath.toString(), "index.xml").toFile();
            
            serializer = new Serializer(new BufferedOutputStream(new FileOutputStream(indexFile)), "utf8");
            serializer.setIndent(2);
            serializer.setMaxLength(0);
            Document indexDocument = new Document(index);
            serializer.write(indexDocument);
        }
    }
    
    private void recursiveAppendChildren(Element containingXmlNode, QuadTreeNode quadTreeNode, int splitDepth, int maxSplitDepth,
    List<QuadTreeNode> splittingNodes)
    {
        // System.out.println("depth " + quadTreeNode.depth);
        if (quadTreeNode.depth < splitDepth || quadTreeNode.getMaxChildrenDepth() < maxSplitDepth)
        {
            Element childNode = quadTreeNode.asXML(false);
            containingXmlNode.appendChild(childNode);
            
            if (quadTreeNode.children != null)
            {
                for (QuadTreeNode childQuadTreeNode : quadTreeNode.children)
                {
                    recursiveAppendChildren(childNode, childQuadTreeNode, splitDepth, maxSplitDepth, splittingNodes);
                }
            }
        }
        else
        {
            splittingNodes.add(quadTreeNode);
        }
    }
    
    public void compute(long quantity) throws InterruptedException
    {
        SynchronizedCounter maxNodesToCompute = new SynchronizedCounter(quantity);
        long startTime = System.currentTimeMillis();
        
        List<Thread> threadsList = new ArrayList<>(threads);
        for (int i = 0; i < threads; ++i)
        {
            QuadTreeComputeThread thread = new QuadTreeComputeThread(this, maxNodesToCompute, computedNodes);
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
    
}
