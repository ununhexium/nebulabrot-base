package net.lab0.nebula.exe;

import java.util.BitSet;

import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.enums.PositionInParent;
import net.lab0.tools.exec.DevNull;
import net.lab0.tools.exec.PriorityExecutor;

import org.junit.Test;

public class TestComputeInOutForQuadTreeNode
{
    @Test
    public void testComputeInOutForQuadTreeNode()
    throws InterruptedException
    {
        final PriorityExecutor executor = new PriorityExecutor();
        BitSet path = MandelbrotQuadTreeNode.positionToBitSetPath(PositionInParent.Root);
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(0, path);
        ComputeInOutForQuadTreeNode job = new ComputeInOutForQuadTreeNode(executor, 0,
        new DevNull<MandelbrotQuadTreeNode>(), node, 65536, 256, 4);
        executor.submit(job);
        executor.finishAndShutdown();
    }
}
