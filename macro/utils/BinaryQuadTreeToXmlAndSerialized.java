package utils;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.data.MandelbrotQuadTreeNode.NodePath;
import net.lab0.nebula.data.StatusQuadTreeNode;
import net.lab0.nebula.listener.ConsoleQuadTreeManagerListener;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.HumanReadable;
import net.lab0.tools.Pair;

public class BinaryQuadTreeToXmlAndSerialized
{
    public static void main(String[] args)
    throws Exception
    {
        Path input = FileSystems.getDefault()
        .getPath("R:", "dev", "nebula", "tree", "bin", "p256i65536d5D16binNoIndex");
        Path output = FileSystems.getDefault().getPath("R:", "dev", "nebula", "tree", "xml",
        "p256i65536d5D16binNoIndex");
        Path output2 = FileSystems.getDefault().getPath("R:", "dev", "nebula", "tree", "serial",
        "p256i65536d5D16binNoIndex.data");
        
        QuadTreeManager manager = new QuadTreeManager(input, new ConsoleQuadTreeManagerListener());
        System.out.println("loaded");
//        manager.exportToXML(output);
        
        int nodesCounts = manager.getQuadTreeRoot().getTotalNodesCount();
        List<StatusQuadTreeNode> nodes = new ArrayList<>(nodesCounts);
        manager.getQuadTreeRoot().getAllNodes(nodes);
        
        WriterManager writerManager = WriterManager.getInstance();
        int arraySize = 1024 * 1024;
        MandelbrotQuadTreeNode[] array = new MandelbrotQuadTreeNode[arraySize];
        int index = 0;
        System.out.println("total " + nodesCounts + " nodes.size() " + nodes.size());
        int sum = 0;
        for (StatusQuadTreeNode node : nodes)
        {
            NodePath path = MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(node.getPath());
            MandelbrotQuadTreeNode toWrite = new MandelbrotQuadTreeNode(path);
            toWrite.maximumIteration = node.getMax();
            toWrite.minimumIteration = node.getMin();
            toWrite.status = node.status;
            array[index] = toWrite;
            index++;
            if (index >= arraySize)
            {
                sum += arraySize;
                System.out.println("" + HumanReadable.humanReadableNumber(sum) + " / "
                + HumanReadable.humanReadableNumber(nodes.size()));
                writerManager.write(array, output2);
                index = 0;
            }
        }
        MandelbrotQuadTreeNode[] subArray = new MandelbrotQuadTreeNode[index];
        for (int i = 0; i < index; ++i)
        {
            subArray[i] = array[i];
        }
        writerManager.write(subArray, output2);
        writerManager.release(output2);
    }
}