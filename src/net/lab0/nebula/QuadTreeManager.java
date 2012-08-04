
package net.lab0.nebula;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.lab0.nebula.data.PositionInParent;
import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.data.StatisticsData;
import net.lab0.nebula.data.Status;
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
    private QuadTreeNode root;
    private int          pointsPerSide      = 100;
    private int          maxIter            = 512;
    private int          diffIterLimit      = 5;
    private int          maxDepth           = 6;
    private long         totalComputingTime = 0;
    private long         computedNodes      = 0;
    
    public QuadTreeManager(QuadTreeNode root, int pointsPerSide, int maxIter, int diffIterLimit, int maxDepth)
    {
        super();
        this.root = root;
        this.pointsPerSide = pointsPerSide;
        this.maxIter = maxIter;
        this.diffIterLimit = diffIterLimit;
        this.maxDepth = maxDepth;
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
        this.computedNodes = Long.parseLong(index.getAttributeValue("computedNodes"));
        
        String mode = index.getAttributeValue("mode");
        if ("oneFile".equals(mode))
        {
            Builder dataParser = new Builder();
            File dataFile = new File(inputFolder.toFile(), index.getFirstChildElement("file").getAttributeValue("path"));
            System.out.println("Opening " + dataFile.getPath());
            Document dataDoc = dataParser.build(dataFile);
            Element mandelbrot = dataDoc.getRootElement();
            
            this.root = new QuadTreeNode(mandelbrot.getFirstChildElement("node"), null);
        }
        else if ("recursive".equals(mode))
        {
            Elements files = index.getChildElements("file");
            ArrayList<Pair<File, String>> filesAndParent = new ArrayList<>(files.size());
            for (int i = 0; i < files.size(); ++i)
            {
                Element file = files.get(i);
                File xmlFile = new File(inputFolder.toFile().getAbsolutePath() + file.getAttributeValue("path"));
                String quadTreeParentPath = file.getAttributeValue("parent");
                filesAndParent.add(new Pair<File, String>(xmlFile, quadTreeParentPath));
            }
            
            Pair<File, String> rootDataFile = filesAndParent.remove(0);
            Builder dataParser = new Builder();
            Document dataDoc = dataParser.build(rootDataFile.a);
            Element mandelbrot = dataDoc.getRootElement();
            this.root = new QuadTreeNode(mandelbrot.getFirstChildElement("node"), null);
            
            for (Pair<File, String> file : filesAndParent)
            {
                dataDoc = dataParser.build(file.a);
                mandelbrot = dataDoc.getRootElement();
                
                QuadTreeNode parent = this.root.getNodeByPath(file.b);
                QuadTreeNode node = new QuadTreeNode(mandelbrot.getFirstChildElement("node"), parent);
//                if (parent == null)
//                {
//                    System.out.println("parent null " + file.b);
//                }
                parent.ensureChildrenArray();
                parent.children[node.positionInParent.ordinal()] = node;
//                System.out.println("attached child node n" + node.positionInParent.ordinal() + " : "
//                + parent.children[node.positionInParent.ordinal()].getPath() + " to " + node.parent.getPath());
            }
            
        }
    }
    
    public QuadTreeNode getNextNodeToCompute(int maxComputationDepth)
    {
        return recursiveGetNextNodeToCompute(root, maxComputationDepth);
    }
    
    private QuadTreeNode recursiveGetNextNodeToCompute(QuadTreeNode node, int maxComputationDepth)
    {
        if (node.status.equals(Status.VOID))
        {
            return node;
        }
        else if (node.status.equals(Status.BROWSED))
        {
            // if the node has no children : create them
            if (node.children == null)
            {
                node.splitNode();
                QuadTreeNode nextNode = node.children[PositionInParent.TopLeft.ordinal()];
                if (nextNode.getDepth() > maxComputationDepth)
                {
                    return null;
                }
                else
                {
                    return nextNode;
                }
            }
            else
            {
                QuadTreeNode n1 = recursiveGetNextNodeToCompute(node.children[PositionInParent.TopLeft.ordinal()], maxComputationDepth);
                QuadTreeNode n2 = recursiveGetNextNodeToCompute(node.children[PositionInParent.TopRight.ordinal()], maxComputationDepth);
                QuadTreeNode n3 = recursiveGetNextNodeToCompute(node.children[PositionInParent.BottomLeft.ordinal()], maxComputationDepth);
                QuadTreeNode n4 = recursiveGetNextNodeToCompute(node.children[PositionInParent.BottomRight.ordinal()], maxComputationDepth);
                
                QuadTreeNode best = null;
                
                // try to assign at least 1 non NULL pointer
                if (n1 != null)
                {
                    best = n1;
                }
                else if (n2 != null)
                {
                    best = n2;
                }
                else if (n3 != null)
                {
                    best = n3;
                }
                else if (n4 != null)
                {
                    best = n4;
                }
                
                if (best == null)
                {
                    // if we didn't get any non NULL pointer, then all hope is lost :( --> return NULL
                    return best;
                }
                
                // get the least deep node : breadth first browsing
                // don't test for n1 because is best!=n1, then n1 is NULL
                if (n2 != null && n2.getDepth() < best.getDepth())
                {
                    best = n2;
                }
                if (n3 != null && n3.getDepth() < best.getDepth())
                {
                    best = n3;
                }
                if (n4 != null && n4.getDepth() < best.getDepth())
                {
                    best = n4;
                }
                
                if (best.getDepth() > maxComputationDepth)
                {
                    return null;
                }
                else
                {
                    return best;
                }
            }
        }
        else
        {
            return null;
        }
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
        index.addAttribute(new Attribute("computedNodes", "" + computedNodes));
        
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
                
                recursiveAppendChildren(docRoot, currentNode, currentNode.getDepth() + splitDepth, splittingNodes);
                
                List<String> relativeFilePath = new ArrayList<>();
                int value = dataIndex;
                if (value >= 64 * 64 * 64 * 64)
                {
                    // quad indirection
                    relativeFilePath.add("16M");
                    value -= (64 * 64 * 64 * 64);
                }
                if (value >= 64 * 64 * 64)
                {
                    // triple indirection
                    relativeFilePath.add("256k");
                    value -= (64 * 64 * 64);
                }
                if (value >= 64 * 64)
                {
                    // double indirection
                    relativeFilePath.add("4k");
                    value -= (64 * 64);
                }
                if (value >= 64)
                {
                    // simple indirection
                    relativeFilePath.add("64");
                }
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
            
            index.addAttribute(new Attribute("mode", "oneFile"));
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
    
    private void recursiveAppendChildren(Element containingXmlNode, QuadTreeNode quadTreeNode, int splitDepth, List<QuadTreeNode> splittingNodes)
    {
// System.out.println("depth " + quadTreeNode.getDepth());
        if (quadTreeNode.getDepth() < splitDepth)
        {
            Element childNode = quadTreeNode.asXML(false);
            containingXmlNode.appendChild(childNode);
            
            if (quadTreeNode.children != null)
            {
                for (QuadTreeNode childQuadTreeNode : quadTreeNode.children)
                {
                    recursiveAppendChildren(childNode, childQuadTreeNode, splitDepth, splittingNodes);
                }
            }
        }
        else
        {
            splittingNodes.add(quadTreeNode);
        }
    }
    
    public void compute(long maxNodesToCompute)
    {
        long startTime = System.currentTimeMillis();
        QuadTreeNode node = root;
        while (node != null && maxNodesToCompute > 0)
        {
            maxNodesToCompute--;
            
            node = this.getNextNodeToCompute(maxDepth);
            if (node != null)
            {
                node.computeStatus(pointsPerSide, maxIter, diffIterLimit);
                computedNodes++;
            }
            else
            {
                System.out.println("No next node");
            }
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
//        System.out.println(node.getPath());
        StatisticsData data = statistics.getStatisticsDataForDepth(node.getDepth());
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
    
}
