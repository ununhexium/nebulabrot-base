package net.lab0.nebula.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.enums.PositionInParent;
import net.lab0.nebula.enums.Status;
import nu.xom.Element;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The class <code>QuadTreeNodeTest</code> contains tests for the class <code>{@link QuadTreeNode}</code>.
 * 
 * @author 116@lab0.net
 */
public class TestQuadTreeNode
{
    private static QuadTreeManager manager;
    private static final String    outsideNodePath = "R00";
    private static final String    insideNodePath  = "R033";
    private static final String    browsedNodePath = "R";
    private QuadTreeNode           emptyQuadTreeNode;
    private QuadTreeNode           standardRootQuadTreeNode;
    private QuadTreeNode           deep10QuadTreeNode;
    
    @BeforeClass
    public static void setUpBeforeClass()
    throws InterruptedException
    {
        manager = new QuadTreeManager(new QuadTreeNode(-2.0, 2.0, -2.0, 2.0), 256, 512, 5, 4);
        manager.compute(99999);
    }
    
    /**
     * Perform pre-test initialization.
     */
    @Before
    public void setUp()
    {
        emptyQuadTreeNode = new QuadTreeNode();
        standardRootQuadTreeNode = new QuadTreeNode(-2.0, 2.0, -2.0, 2.0);
        deep10QuadTreeNode = new QuadTreeNode(-2.0, 2.0, -2.0, 2.0);
        createDeepTree(deep10QuadTreeNode, 10);
    }
    
    private void createDeepTree(QuadTreeNode node, int depth)
    {
        if (depth == 0)
        {
            return;
        }
        else
        {
            node.splitNode();
            for (QuadTreeNode child : node.children)
            {
                createDeepTree(child, depth - 1);
            }
        }
    }
    
    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown()
    {
        // Add additional tear down code here
    }
    
    /**
     * Run the QuadTreeNode() constructor test.
     */
    @Test
    public void testQuadTreeNode_1()
    {
        // add additional test code here
        assertNotNull(emptyQuadTreeNode);
        assertEquals(browsedNodePath, emptyQuadTreeNode.getPath());
        assertEquals(true, emptyQuadTreeNode.isLeafNode());
        assertEquals(0.0, emptyQuadTreeNode.getSurface(), 0.0);
        assertEquals(false, emptyQuadTreeNode.isFlagedForComputing());
        assertEquals(1, emptyQuadTreeNode.getTotalNodesCount());
        assertEquals(false, emptyQuadTreeNode.hasComputedChildren());
        assertEquals(
        "QuadTreeNode [parent=null, children=null, minX=0.0, maxX=0.0, minY=0.0, maxY=0.0, depth=0, positionInParent=null, status=null, min=-1, max=-1, flagedForComputing=false]",
        emptyQuadTreeNode.completeToString());
        assertEquals(0, emptyQuadTreeNode.getMaxNodeDepth());
    }
    
    /**
     * Run the QuadTreeNode(double minX, double maxX, double minY, double maxY) constructor test. Check for default values.
     */
    @Test
    public void testQuadTreeNode_2()
    {
        assertNull(standardRootQuadTreeNode.children);
        assertEquals(0, standardRootQuadTreeNode.getDepth());
        assertEquals(-1, standardRootQuadTreeNode.getMax());
        assertEquals(-1, standardRootQuadTreeNode.getMin());
        assertEquals(-2.0, standardRootQuadTreeNode.getMinX(), 0.0);
        assertEquals(2.0, standardRootQuadTreeNode.getMaxX(), 0.0);
        assertEquals(-2.0, standardRootQuadTreeNode.getMinY(), 0.0);
        assertEquals(2.0, standardRootQuadTreeNode.getMaxY(), 0.0);
        assertNull(standardRootQuadTreeNode.parent);
        assertEquals(PositionInParent.Root, standardRootQuadTreeNode.positionInParent);
        assertEquals(Status.VOID, standardRootQuadTreeNode.status);
    }
    
    @Test
    public void testSplit()
    {
        standardRootQuadTreeNode.splitNode();
        assertNotNull(standardRootQuadTreeNode.children);
        
        // assert that nothing else was changed in the root node
        assertEquals(0, standardRootQuadTreeNode.getDepth());
        assertEquals(-1, standardRootQuadTreeNode.getMax());
        assertEquals(-1, standardRootQuadTreeNode.getMin());
        assertEquals(-2.0, standardRootQuadTreeNode.getMinX(), 0.0);
        assertEquals(2.0, standardRootQuadTreeNode.getMaxX(), 0.0);
        assertEquals(-2.0, standardRootQuadTreeNode.getMinY(), 0.0);
        assertEquals(2.0, standardRootQuadTreeNode.getMaxY(), 0.0);
        assertNull(standardRootQuadTreeNode.parent);
        assertEquals(PositionInParent.Root, standardRootQuadTreeNode.positionInParent);
        assertEquals(Status.VOID, standardRootQuadTreeNode.status);
        
        QuadTreeNode topLeftChild = standardRootQuadTreeNode.children[PositionInParent.TopLeft.ordinal()];
        assertEquals(1, topLeftChild.getDepth());
        assertEquals(-1, topLeftChild.getMax());
        assertEquals(-1, topLeftChild.getMin());
        assertEquals(-2.0, topLeftChild.getMinX(), 0.0);
        assertEquals(0.0, topLeftChild.getMaxX(), 0.0);
        assertEquals(0.0, topLeftChild.getMinY(), 0.0);
        assertEquals(2.0, topLeftChild.getMaxY(), 0.0);
        assertTrue(topLeftChild.parent == standardRootQuadTreeNode);
        assertEquals(PositionInParent.TopLeft, topLeftChild.positionInParent);
        assertEquals(Status.VOID, topLeftChild.status);
        
        QuadTreeNode topRightChild = standardRootQuadTreeNode.children[PositionInParent.TopRight.ordinal()];
        assertEquals(1, topRightChild.getDepth());
        assertEquals(-1, topRightChild.getMax());
        assertEquals(-1, topRightChild.getMin());
        assertEquals(0.0, topRightChild.getMinX(), 0.0);
        assertEquals(2.0, topRightChild.getMaxX(), 0.0);
        assertEquals(0.0, topRightChild.getMinY(), 0.0);
        assertEquals(2.0, topRightChild.getMaxY(), 0.0);
        assertTrue(topRightChild.parent == standardRootQuadTreeNode);
        assertEquals(PositionInParent.TopRight, topRightChild.positionInParent);
        assertEquals(Status.VOID, topRightChild.status);
        
        QuadTreeNode bottomLeftChild = standardRootQuadTreeNode.children[PositionInParent.BottomLeft.ordinal()];
        assertEquals(1, bottomLeftChild.getDepth());
        assertEquals(-1, bottomLeftChild.getMax());
        assertEquals(-1, bottomLeftChild.getMin());
        assertEquals(-2.0, bottomLeftChild.getMinX(), 0.0);
        assertEquals(0.0, bottomLeftChild.getMaxX(), 0.0);
        assertEquals(-2.0, bottomLeftChild.getMinY(), 0.0);
        assertEquals(0.0, bottomLeftChild.getMaxY(), 0.0);
        assertTrue(bottomLeftChild.parent == standardRootQuadTreeNode);
        assertEquals(PositionInParent.BottomLeft, bottomLeftChild.positionInParent);
        assertEquals(Status.VOID, bottomLeftChild.status);
        
        QuadTreeNode bottomRightChild = standardRootQuadTreeNode.children[PositionInParent.BottomRight.ordinal()];
        assertEquals(1, bottomRightChild.getDepth());
        assertEquals(-1, bottomRightChild.getMax());
        assertEquals(-1, bottomRightChild.getMin());
        assertEquals(0.0, bottomRightChild.getMinX(), 0.0);
        assertEquals(2.0, bottomRightChild.getMaxX(), 0.0);
        assertEquals(-2.0, bottomRightChild.getMinY(), 0.0);
        assertEquals(0.0, bottomRightChild.getMaxY(), 0.0);
        assertTrue(bottomRightChild.parent == standardRootQuadTreeNode);
        assertEquals(PositionInParent.BottomRight, bottomRightChild.positionInParent);
        assertEquals(Status.VOID, bottomRightChild.status);
    }
    
    @Test
    public void testComputeStatus()
    throws InterruptedException
    {
        QuadTreeNode root = manager.getQuadTreeRoot();
        assertEquals(Status.BROWSED, root.getNodeByAbsolutePath(browsedNodePath).status);
        assertEquals(Status.INSIDE, root.getNodeByAbsolutePath(insideNodePath).status);
        assertEquals(Status.OUTSIDE, root.getNodeByAbsolutePath(outsideNodePath).status);
    }
    
    @Test
    public void testGetPath()
    {
        String path = deep10QuadTreeNode.children[0].children[1].children[2].children[3].children[2].children[1].children[1].getPath();
        String reference = "R0123211";
        assertEquals(reference, path);
    }
    
    @Test
    public void testAsXML()
    {
        Element e = manager.getQuadTreeRoot().getNodeByAbsolutePath(outsideNodePath).asXML(false);
        assertNotNull(e);
        assertEquals("-2.0", e.getAttributeValue("minX"));
        assertEquals("-1.0", e.getAttributeValue("maxX"));
        assertEquals("1.0", e.getAttributeValue("minY"));
        assertEquals("2.0", e.getAttributeValue("maxY"));
        assertEquals("0", e.getAttributeValue("min"));
        assertEquals("4", e.getAttributeValue("max"));
        assertEquals("TopLeft", e.getAttributeValue("pos"));
        assertEquals("OUTSIDE", e.getAttributeValue("status"));
        assertEquals(0, e.getChildCount());
    }
    
    @Test
    public void testFlagForComputing()
    {
        assertEquals(false, emptyQuadTreeNode.isFlagedForComputing());
        emptyQuadTreeNode.flagForComputing();
        assertEquals(true, emptyQuadTreeNode.isFlagedForComputing());
    }
    
    @Test
    public void testGetDepth()
    {
        assertEquals(0, deep10QuadTreeNode.getDepth());
        assertEquals(2, deep10QuadTreeNode.getNodeByAbsolutePath("R00").getDepth());
        assertEquals(6, deep10QuadTreeNode.getNodeByAbsolutePath("R000000").getDepth());
        assertEquals(10, deep10QuadTreeNode.getNodeByAbsolutePath("R0000100002").getDepth());
    }
    
    @Test
    public void testGetSurface()
    {
        assertEquals(16.0, standardRootQuadTreeNode.getSurface(), 0.000001);
    }
    
    @Test
    public void testGetLeafNodes()
    {
        List<QuadTreeNode> list = new ArrayList<>(1 << 20);
        deep10QuadTreeNode.getLeafNodes(list);
        assertEquals(1 << 20, list.size());
    }
    
    @Test
    public void testGetLeafNodesByType()
    {
        // 44 browsed
        // 50 outside
        // 6 inside
        
        List<QuadTreeNode> list = new ArrayList<>();
        QuadTreeNode root = manager.getQuadTreeRoot();
        list.clear();
        root.getLeafNodes(list, Arrays.asList(Status.BROWSED));
        assertEquals(44, list.size());
        
        list.clear();
        root.getLeafNodes(list, Arrays.asList(Status.OUTSIDE));
        assertEquals(50, list.size());
        
        list.clear();
        root.getLeafNodes(list, Arrays.asList(Status.INSIDE));
        assertEquals(6, list.size());
        
        list.clear();
        root.getLeafNodes(list, Arrays.asList(Status.INSIDE, Status.OUTSIDE));
        assertEquals(56, list.size());
        
        list.clear();
        root.getLeafNodes(list, Arrays.asList(Status.INSIDE, Status.OUTSIDE, Status.BROWSED));
        assertEquals(100, list.size());
    }
    
    @Test
    public void testGetMaxNodeDepth()
    {
        assertEquals(0, standardRootQuadTreeNode.getMaxNodeDepth());
        assertEquals(4, manager.getQuadTreeRoot().getMaxNodeDepth());
        assertEquals(10, deep10QuadTreeNode.getMaxNodeDepth());
    }
    
    @Test
    public void getNodesByStatus()
    {
        // 65 browsed
        // 50 outside
        // 6 inside
        
        List<QuadTreeNode> list = new ArrayList<>();
        QuadTreeNode root = manager.getQuadTreeRoot();
        list.clear();
        root.getNodesByStatus(list, Arrays.asList(Status.BROWSED));
        assertEquals(77, list.size());
        
        list.clear();
        root.getNodesByStatus(list, Arrays.asList(Status.OUTSIDE));
        assertEquals(50, list.size());
        
        list.clear();
        root.getNodesByStatus(list, Arrays.asList(Status.INSIDE));
        assertEquals(6, list.size());
        
        list.clear();
        root.getNodesByStatus(list, Arrays.asList(Status.INSIDE, Status.OUTSIDE));
        assertEquals(56, list.size());
        
        list.clear();
        root.getNodesByStatus(list, Arrays.asList(Status.INSIDE, Status.OUTSIDE, Status.BROWSED));
        assertEquals(133, list.size());
        
        list.clear();
        root.getNodesByStatus(list, Arrays.asList(Status.INSIDE, Status.OUTSIDE, Status.BROWSED), 100);
        assertTrue(100 <= list.size());
    }
    
    @Test
    public void testGetNodesOverlappingRectangle()
    {
        List<QuadTreeNode> list = new ArrayList<>();
        QuadTreeNode root = manager.getQuadTreeRoot();
        
        Collection<QuadTreeNode> collection = root.getNodesOverlappingRectangle(new Point2D.Double(-2.0, -2.0), new Point2D.Double(-0.00001, -0.00001));
        root.getNodeByAbsolutePath("R2").getAllNodes(list);
        list.add(root);
        
        Set<QuadTreeNode> set1 = new HashSet<>(collection);
        Set<QuadTreeNode> set2 = new HashSet<>(list);
        assertEquals(collection.size(), set1.size());
        assertEquals(list.size(), set2.size());
        for (QuadTreeNode q1 : set1)
        {
            if (!set2.contains(q1))
            {
                System.out.println(q1);
                assertTrue(false);
            }
        }
        
        for (QuadTreeNode q2 : set2)
        {
            if (!set1.contains(q2))
            {
                assertTrue(false);
            }
        }
    }
    
    @Test
    public void testGetNodeByAbsolutePath()
    {
        QuadTreeNode node = deep10QuadTreeNode.children[0].children[1].children[2].children[3].children[2].children[1].children[1];
        String reference = "R0123211";
        assertEquals(node, deep10QuadTreeNode.getNodeByAbsolutePath(reference));
        
        assertNotNull(deep10QuadTreeNode.getNodeByAbsolutePath("R"));
        assertNotNull(deep10QuadTreeNode.getNodeByAbsolutePath("R0"));
        assertNotNull(deep10QuadTreeNode.getNodeByAbsolutePath("R00"));
        assertNotNull(deep10QuadTreeNode.getNodeByAbsolutePath("R0000100002"));
        assertNull(deep10QuadTreeNode.getNodeByAbsolutePath("R00001000023"));
        
        try
        {
            deep10QuadTreeNode.getNodeByAbsolutePath("A");
            assertTrue(false);
        }
        catch (IllegalArgumentException exception)
        {
            assertTrue(true);
        }
        
        try
        {
            deep10QuadTreeNode.getNodeByAbsolutePath("456798");
            assertTrue(false);
        }
        catch (IllegalArgumentException exception)
        {
            assertTrue(true);
        }
        
    }
    
    @Test
    public void testGetSubnodeByRelativePath()
    {
        QuadTreeNode node = deep10QuadTreeNode.children[0].children[1].children[2].children[3].children[2].children[1].children[1];
        QuadTreeNode node1 = deep10QuadTreeNode.children[0].children[1].children[2];
        QuadTreeNode node2 = node1.getSubnodeByRelativePath("3211");
        
        assertEquals(node, node2);
    }
    
    @Test
    public void testStrip()
    {
        assertEquals(deep10QuadTreeNode.getMaxNodeDepth(), 10);
        
        deep10QuadTreeNode.strip(5);
        
        assertEquals(5, deep10QuadTreeNode.getMaxNodeDepth());
    }
    
    // TODO : to be continued ...
}