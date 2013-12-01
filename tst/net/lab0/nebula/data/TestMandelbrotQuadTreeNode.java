package net.lab0.nebula.data;

import java.util.BitSet;

import static net.lab0.nebula.enums.PositionInParent.*;
import net.lab0.nebula.data.MandelbrotQuadTreeNode.NodePath;
import net.lab0.nebula.enums.PositionInParent;
import net.lab0.tools.Pair;

import org.junit.Assert;
import org.junit.Test;

public class TestMandelbrotQuadTreeNode
{
    @Test
    public void testMandelbrotQuadTreeNodeDepth1()
    {
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(0);
        Assert.assertEquals(0, node.depth);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMandelbrotQuadTreeNodeDepth2()
    {
        new MandelbrotQuadTreeNode(-1);
        Assert.fail("Should raise IllegalArgumentException");
    }
    
    @Test
    public void testMandelbrotQuadTreeNodeDepth3()
    {
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(Short.MAX_VALUE);
        Assert.assertEquals(Short.MAX_VALUE, node.depth);
    }
    
    @Test
    public void testMandelbrotQuadTreeNodeDepthPath()
    {
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(0, new BitSet(2));
        Assert.assertEquals(0, node.depth);
        Assert.assertNotNull(node.path);
        Assert.assertTrue(node.path.size() >= 2);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMandelbrotQuadTreeNodeDepthPath2()
    {
        new MandelbrotQuadTreeNode(-1, new BitSet(0));
        Assert.fail("Should raise IllegalArgumentException");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMandelbrotQuadTreeNodeDepthPath3()
    {
        new MandelbrotQuadTreeNode(100, new BitSet(0));
        Assert.fail("Should raise IllegalArgumentException");
    }
    
    @Test
    public void testMandelbrotQuadTreeNodeDepthPath4()
    {
        BitSet path = new BitSet(4);
        path.set(0, 4, true);
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(1, path);
        Assert.assertNotNull(node.path);
        Assert.assertTrue(node.path.size() >= 2);
        Assert.assertEquals(path, node.path.get(0, 4));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPathCreationException1()
    {
        MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(new PositionInParent[0]);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testPathCreationException2()
    {
        MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(PositionInParent.BottomLeft, PositionInParent.Root);
    }
    
    @Test
    public void testPathCreation()
    {
        BitSet path = null;
        BitSet reference = null;
        // XX 00
        path = MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(Root, TopLeft).getPath();
        reference = new BitSet();
        path.xor(reference);
        Assert.assertTrue(path.isEmpty());
        
        // XX 01 10 11 00
        path = MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(Root, TopRight, BottomLeft, BottomRight, TopLeft)
        .getPath();
        reference = new BitSet(10);
        reference.set(3, true);
        reference.set(4, true);
        reference.set(6, true);
        reference.set(7, true);
        
        path.xor(reference);
        Assert.assertTrue(path.isEmpty());
    }
    
    @Test
    public void testGetXY1()
    {
        NodePath path = MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(Root, TopLeft);
        
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(path);
        // minX
        Assert.assertEquals(-2.0, node.getX().getMin(), 0.0);
        // maxX
        Assert.assertEquals(0.0, node.getX().getMax(), 0.0);
        // minY
        Assert.assertEquals(0.0, node.getY().getMin(), 0.0);
        // maxY
        Assert.assertEquals(2.0, node.getY().getMax(), 0.0);
    }
    
    @Test
    public void testGetXY2()
    {
        NodePath path = MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(Root, TopLeft, BottomRight);
        
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(path);
        // minX
        Assert.assertEquals(-1.0, node.getX().getMin(), 0.0);
        // maxX
        Assert.assertEquals(0.0, node.getX().getMax(), 0.0);
        // minY
        Assert.assertEquals(0.0, node.getY().getMin(), 0.0);
        // maxY
        Assert.assertEquals(1.0, node.getY().getMax(), 0.0);
    }
    
    @Test
    public void testGetXY3()
    {
        NodePath path = MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(Root, TopLeft, BottomRight, TopLeft);
        
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(path);
        // minX
        Assert.assertEquals(-1.0, node.getX().getMin(), 0.0);
        // maxX
        Assert.assertEquals(-0.5, node.getX().getMax(), 0.0);
        // minY
        Assert.assertEquals(0.5, node.getY().getMin(), 0.0);
        // maxY
        Assert.assertEquals(1.0, node.getY().getMax(), 0.0);
    }
    
    @Test
    public void testGetXY4()
    {
        NodePath path = MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(Root, TopRight, BottomLeft, TopRight);
        
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(path);
        // minX
        Assert.assertEquals(0.5, node.getX().getMin(), 0.0);
        // maxX
        Assert.assertEquals(1.0, node.getX().getMax(), 0.0);
        // minY
        Assert.assertEquals(0.5, node.getY().getMin(), 0.0);
        // maxY
        Assert.assertEquals(1.0, node.getY().getMax(), 0.0);
    }
    
    @Test
    public void testGetXY5()
    {
        NodePath path = MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(Root, BottomLeft, TopRight, BottomLeft);
        
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(path);
        // minX
        Assert.assertEquals(-1.0, node.getX().getMin(), 0.0);
        // maxX
        Assert.assertEquals(-0.5, node.getX().getMax(), 0.0);
        // minY
        Assert.assertEquals(-1.0, node.getY().getMin(), 0.0);
        // maxY
        Assert.assertEquals(-0.5, node.getY().getMax(), 0.0);
    }
    
    @Test
    public void testGetXY6()
    {
        NodePath path = MandelbrotQuadTreeNode.positionToDepthAndBitSetPath(Root, BottomRight, TopLeft, BottomRight);
        
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(path);
        // minX
        Assert.assertEquals(0.5, node.getX().getMin(), 0.0);
        // maxX
        Assert.assertEquals(1.0, node.getX().getMax(), 0.0);
        // minY
        Assert.assertEquals(-1.0, node.getY().getMin(), 0.0);
        // maxY
        Assert.assertEquals(-0.5, node.getY().getMax(), 0.0);
    }
}