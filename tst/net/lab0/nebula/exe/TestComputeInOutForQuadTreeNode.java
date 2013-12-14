package net.lab0.nebula.exe;

import static net.lab0.nebula.enums.PositionInParent.BottomLeft;
import static net.lab0.nebula.enums.PositionInParent.BottomRight;
import static net.lab0.nebula.enums.PositionInParent.Root;
import static net.lab0.nebula.enums.PositionInParent.TopLeft;

import java.util.ArrayList;
import java.util.List;

import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.data.MandelbrotQuadTreeNode.NodePath;
import net.lab0.nebula.enums.PositionInParent;
import net.lab0.nebula.enums.Status;
import net.lab0.nebula.exe.builder.BuilderFactory;
import net.lab0.tools.exec.DevNull;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;
import net.lab0.tools.exec.SingleOutputGenerator;
import net.lab0.tools.exec.ToDump;

import org.junit.Assert;
import org.junit.Test;

public class TestComputeInOutForQuadTreeNode
{
    @Test
    public void testComputeInOutForQuadTreeNode1()
    throws InterruptedException
    {
        final PriorityExecutor executor = new PriorityExecutor();
        NodePath path = MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(PositionInParent.Root);
        List<MandelbrotQuadTreeNode> nodesList = new ArrayList<>();
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(path);
        
        Assert.assertEquals(Status.VOID, node.status);
        
        JobBuilder<MandelbrotQuadTreeNode> toComputeInOut = BuilderFactory.toComputeInOut(
        new DevNull<MandelbrotQuadTreeNode>(), 65536, 256, 4);
        SingleOutputGenerator<MandelbrotQuadTreeNode> generator = new SingleOutputGenerator<MandelbrotQuadTreeNode>(
        executor, toComputeInOut, node);
        ComputeInOutForQuadTreeNode job = new ComputeInOutForQuadTreeNode(generator, new ToDump<>(nodesList), node,
        65536, 256, 4);
        executor.submit(job);
        executor.finishAndShutdown();
        
        MandelbrotQuadTreeNode node2 = nodesList.get(0);
        
        Assert.assertNotNull(node2);
        Assert.assertTrue(node == node2);
        Assert.assertEquals(Status.BROWSED, node.status);
        Assert.assertEquals(-1, node.minimumIteration);
        Assert.assertEquals(-1, node.maximumIteration);
    }
    
    @Test
    public void testComputeInOutForQuadTreeNode2()
    throws InterruptedException
    {
        final PriorityExecutor executor = new PriorityExecutor();
        // reference node taken from former validated code
        NodePath path = MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(PositionInParent.Root,
        PositionInParent.TopLeft, PositionInParent.TopLeft);
        List<MandelbrotQuadTreeNode> nodesList = new ArrayList<>();
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(path);
        
        Assert.assertEquals(Status.VOID, node.status);
        
        JobBuilder<MandelbrotQuadTreeNode> toComputeInOut = BuilderFactory.toComputeInOut(
        new DevNull<MandelbrotQuadTreeNode>(), 65536, 256, 4);
        SingleOutputGenerator<MandelbrotQuadTreeNode> generator = new SingleOutputGenerator<MandelbrotQuadTreeNode>(
        executor, toComputeInOut, node);
        ComputeInOutForQuadTreeNode job = new ComputeInOutForQuadTreeNode(generator, new ToDump<>(nodesList), node,
        65536, 256, 4);
        executor.submit(job);
        executor.finishAndShutdown();
        
        MandelbrotQuadTreeNode node2 = nodesList.get(0);
        
        Assert.assertNotNull(node2);
        Assert.assertTrue(node == node2);
        Assert.assertEquals(Status.OUTSIDE, node2.status);
        Assert.assertEquals(0, node2.minimumIteration);
        Assert.assertEquals(4, node2.maximumIteration);
    }
    
    @Test
    public void testComputeInOutForQuadTreeNode3()
    throws InterruptedException
    {
        final PriorityExecutor executor = new PriorityExecutor();
        // reference node taken from former validated code
        NodePath path = MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(Root, TopLeft, BottomLeft, BottomRight,
        BottomLeft, BottomRight, BottomLeft, BottomRight);
        List<MandelbrotQuadTreeNode> nodesList = new ArrayList<>();
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(path);
        
        Assert.assertEquals(Status.VOID, node.status);
        
        JobBuilder<MandelbrotQuadTreeNode> toComputeInOut = BuilderFactory.toComputeInOut(
        new DevNull<MandelbrotQuadTreeNode>(), 65536, 256, 4);
        SingleOutputGenerator<MandelbrotQuadTreeNode> generator = new SingleOutputGenerator<MandelbrotQuadTreeNode>(
        executor, toComputeInOut, node);
        ComputeInOutForQuadTreeNode job = new ComputeInOutForQuadTreeNode(generator, new ToDump<>(nodesList), node,
        65536, 256, 4);
        executor.submit(job);
        executor.finishAndShutdown();
        
        MandelbrotQuadTreeNode node2 = nodesList.get(0);
        
        Assert.assertNotNull(node2);
        Assert.assertTrue(node == node2);
        Assert.assertEquals(Status.INSIDE, node2.status);
        Assert.assertEquals(-1, node2.minimumIteration);
        Assert.assertEquals(-1, node2.maximumIteration);
    }
}
