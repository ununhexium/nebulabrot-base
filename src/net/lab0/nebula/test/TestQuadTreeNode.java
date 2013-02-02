package net.lab0.nebula.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.enums.PositionInParent;

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
    private QuadTreeNode emptyQuadTreeNode;
    
    @BeforeClass
    public static void setUpBeforeClass()
    {
        
    }
    
    /**
     * Perform pre-test initialization.
     * 
     * @throws Exception
     *             if the initialization fails for some reason
     * 
     * @generatedBy CodePro at 02/02/13 19:12
     */
    @Before
    public void setUp()
    throws Exception
    {
        emptyQuadTreeNode = new QuadTreeNode();
    }
    
    /**
     * Perform post-test clean-up.
     * 
     * @throws Exception
     *             if the clean-up fails for some reason
     * 
     * @generatedBy CodePro at 02/02/13 19:12
     */
    @After
    public void tearDown()
    throws Exception
    {
        // Add additional tear down code here
    }
    
    /**
     * Run the QuadTreeNode() constructor test.
     */
    @Test
    public void testQuadTreeNode_1()
    throws Exception
    {
        // add additional test code here
        assertNotNull(emptyQuadTreeNode);
        assertEquals("R", emptyQuadTreeNode.getPath());
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
    
    //TODO : go on
}