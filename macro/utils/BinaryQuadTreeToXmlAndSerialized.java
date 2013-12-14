package utils;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.data.MandelbrotQuadTreeNode.NodePath;
import net.lab0.nebula.data.StatusQuadTreeNode;
import net.lab0.nebula.listener.ConsoleQuadTreeManagerListener;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.HumanReadable;

public class BinaryQuadTreeToXmlAndSerialized
{
    public static void main(String[] args)
    throws Exception
    {
        Path input = FileSystems.getDefault()
        .getPath("R:", "dev", "nebula", "tree", "bin", "p256i65536d5D16binNoIndex");
        Path output = FileSystems.getDefault().getPath("R:", "dev", "nebula", "tree", "xml",
        "p256i65536d5D16binNoIndex");
        
        Thread.sleep(10000);
        
        QuadTreeManager manager = new QuadTreeManager(input, new ConsoleQuadTreeManagerListener());
        System.out.println("loaded");
        // manager.exportToXML(output);
        
        int nodesCounts = manager.getQuadTreeRoot().getTotalNodesCount();
        List<StatusQuadTreeNode> nodes = new ArrayList<>(nodesCounts);
        manager.getQuadTreeRoot().getAllNodes(nodes);
        
        WriterManager writerManager = WriterManager.getInstance();
        int arraySize = 65536;
        int index = 0;
        System.out.println("total " + nodesCounts + " nodes.size() " + nodes.size());
        System.exit(0);
        int sum = 0;
        int maxDepth = manager.getQuadTreeRoot().getMaxNodeDepth();
        List<Path> paths = new ArrayList<>(maxDepth);
        for (int i = 0; i <= maxDepth; ++i)
        {
            paths.add(FileSystems.getDefault().getPath("R:", "dev", "nebula", "tree", "serial",
            "p256i65536d5D16binNoIndex_depth" + i + ".data"));
        }
        for (StatusQuadTreeNode node : nodes)
        {
            NodePath path = MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(node.getPath());
            MandelbrotQuadTreeNode toWrite = new MandelbrotQuadTreeNode(path);
            toWrite.maximumIteration = node.getMax();
            toWrite.minimumIteration = node.getMin();
            toWrite.status = node.status;
            sum += 1;
            writerManager.write(toWrite, paths.get(toWrite.depth));
            index++;
            if (index > arraySize)
            {
                System.out.println("" + HumanReadable.humanReadableNumber(sum) + " / "
                + HumanReadable.humanReadableNumber(nodes.size()) + " " + 100f * (float) sum / (float) nodes.size()
                + "%");
                index = 0;
            }
        }
        for (int i = 0; i <= maxDepth; ++i)
        {
            writerManager.release(paths.get(i));
        }
    }
}