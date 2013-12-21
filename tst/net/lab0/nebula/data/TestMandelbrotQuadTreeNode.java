package net.lab0.nebula.data;

import static net.lab0.nebula.enums.PositionInParent.BottomLeft;
import static net.lab0.nebula.enums.PositionInParent.BottomRight;
import static net.lab0.nebula.enums.PositionInParent.Root;
import static net.lab0.nebula.enums.PositionInParent.TopLeft;
import static net.lab0.nebula.enums.PositionInParent.TopRight;

import java.util.BitSet;

import net.lab0.nebula.data.MandelbrotQuadTreeNode.NodePath;
import net.lab0.nebula.enums.PositionInParent;
import net.lab0.nebula.enums.Status;

import org.junit.Assert;
import org.junit.Test;

public class TestMandelbrotQuadTreeNode
{
    @Test
    public void testMandelbrotQuadTreeNodeDepth1()
    {
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(0);
        Assert.assertEquals(0, node.nodePath.depth);
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
        Assert.assertEquals(Short.MAX_VALUE, node.nodePath.depth);
    }
    
    @Test
    public void testMandelbrotQuadTreeNodeDepthPath()
    {
        MandelbrotQuadTreeNode node = new MandelbrotQuadTreeNode(0, new BitSet(2));
        Assert.assertEquals(0, node.nodePath.depth);
        Assert.assertNotNull(node.nodePath.path);
        Assert.assertTrue(node.nodePath.path.size() >= 2);
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
        Assert.assertNotNull(node.nodePath.path);
        Assert.assertTrue(node.nodePath.path.size() >= 2);
        Assert.assertEquals(path, node.nodePath.path.get(0, 4));
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
    
    @Test
    public void testGetPathAsEnum1()
    {
        MandelbrotQuadTreeNode node = MandelbrotQuadTreeNode.Factory.buildNode(PositionInParent.Root);
        PositionInParent[] path = node.getPathAsEnum();
        Assert.assertEquals(1, path.length);
        Assert.assertEquals(PositionInParent.Root, path[0]);
    }
    
    @Test
    public void testGetPathAsEnum2()
    {
        MandelbrotQuadTreeNode node = MandelbrotQuadTreeNode.Factory.buildNode(PositionInParent.Root,
        PositionInParent.TopLeft, PositionInParent.TopLeft, PositionInParent.TopLeft);
        PositionInParent[] path = node.getPathAsEnum();
        Assert.assertEquals(4, path.length);
        Assert.assertEquals(PositionInParent.Root, path[0]);
        Assert.assertEquals(PositionInParent.TopLeft, path[1]);
        Assert.assertEquals(PositionInParent.TopLeft, path[2]);
        Assert.assertEquals(PositionInParent.TopLeft, path[3]);
    }
    
    @Test
    public void testGetPathAsEnum3()
    {
        MandelbrotQuadTreeNode node = MandelbrotQuadTreeNode.Factory.buildNode(PositionInParent.Root,
        PositionInParent.BottomRight, PositionInParent.BottomRight, PositionInParent.BottomRight);
        PositionInParent[] path = node.getPathAsEnum();
        Assert.assertEquals(4, path.length);
        Assert.assertEquals(PositionInParent.Root, path[0]);
        Assert.assertEquals(PositionInParent.BottomRight, path[1]);
        Assert.assertEquals(PositionInParent.BottomRight, path[2]);
        Assert.assertEquals(PositionInParent.BottomRight, path[3]);
    }
    
    @Test
    public void testGetPathAsEnum4()
    {
        MandelbrotQuadTreeNode node = MandelbrotQuadTreeNode.Factory.buildNode(Root, TopRight, BottomLeft,
        BottomRight,TopLeft);
        System.out.println(node);
        PositionInParent[] path = node.getPathAsEnum();
        Assert.assertEquals(5, path.length);
        Assert.assertEquals(PositionInParent.Root, path[0]);
        Assert.assertEquals(PositionInParent.TopRight, path[1]);
        Assert.assertEquals(PositionInParent.BottomLeft, path[2]);
        Assert.assertEquals(PositionInParent.BottomRight, path[3]);
        Assert.assertEquals(PositionInParent.TopLeft, path[4]);
    }
    
    @Test
    public void testSplit()
    {
        MandelbrotQuadTreeNode node = MandelbrotQuadTreeNode.Factory.buildRoot();
        MandelbrotQuadTreeNode[] split = node.split();
        
        Assert.assertEquals(4, split.length);
        
        Assert.assertEquals(PositionInParent.TopLeft, split[0].getPathAsEnum()[1]);
        Assert.assertEquals(PositionInParent.TopRight, split[1].getPathAsEnum()[1]);
        Assert.assertEquals(PositionInParent.BottomLeft, split[2].getPathAsEnum()[1]);
        Assert.assertEquals(PositionInParent.BottomRight, split[3].getPathAsEnum()[1]);
        
        for (int i = 0; i < 4; ++i)
        {
            Assert.assertEquals(node.nodePath.depth+1, split[i].nodePath.depth);
            Assert.assertEquals(Status.VOID, split[i].status);
        }
    }
}
