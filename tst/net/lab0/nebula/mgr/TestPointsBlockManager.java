package net.lab0.nebula.mgr;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.exception.NonEmptyFolderException;

public class TestPointsBlockManager
{
    @Test
    public void sizeBinaryInsertTest()
    {
        PointsBlockManager blockManager = new PointsBlockManager(10);
        List<PointsBlock> blocks = new ArrayList<>(100);
        for (int i = 0; i < 100; ++i)
        {
            PointsBlockManager.sizeBinaryInsert(blocks, new PointsBlock((int) (Math.random() * 10), blockManager));
            int previous = blocks.get(0).size;
            // check that at every point in the execution all the blocks are sorted
            for (int j = 0; j <= i; ++j)
            {
                Assert.assertTrue("An element is not ordered correctly", blocks.get(j).size >= previous);
                previous = blocks.get(j).size;
            }
        }
    }
    
    @Test
    public void sizeBinarySerachTest()
    {
        PointsBlockManager blockManager = new PointsBlockManager(10);
        List<PointsBlock> blocks = new ArrayList<>(100);
        for (int i = 0; i < 100; ++i)
        {
            PointsBlockManager.sizeBinaryInsert(blocks, new PointsBlock(i / 10, blockManager));
        }
        
        for (int i = 0; i < 10; ++i)
        {
            Assert.assertEquals(i, PointsBlockManager.sizeBinarySearch(blocks, i).size);
        }
        
        // these must not return anything
        Assert.assertNull(PointsBlockManager.sizeBinarySearch(blocks, 11));
        Assert.assertNull(PointsBlockManager.sizeBinarySearch(blocks, -1));
        Assert.assertNull(PointsBlockManager.sizeBinarySearch(blocks, Integer.MAX_VALUE));
        Assert.assertNull(PointsBlockManager.sizeBinarySearch(blocks, Integer.MIN_VALUE));
    }
    
    @Test
    public void allocation()
    {
        PointsBlockManager manager = new PointsBlockManager(10);
        PointsBlock block = manager.allocatePointsBlock(116);
        Assert.assertNotNull(block);
        Assert.assertEquals(116, block.size);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void wrongFree(){
        PointsBlockManager manager = new PointsBlockManager(10);
        PointsBlock block = new PointsBlock(116, manager);
        block.release();
    }
    
    @Test
    public void cacheTest()
    {
        PointsBlockManager manager = new PointsBlockManager(10);
        List<PointsBlock> blocks = new ArrayList<>(20);
        PointsBlock block;
        
        for (int i = 0; i < 9; ++i)
        {
            block = manager.allocatePointsBlock(64);
            Assert.assertNotNull(block);
            blocks.add(block);
        }
        
        for (int i = 0; i < 9; ++i)
        {
            block = manager.allocatePointsBlock(128);
            Assert.assertNotNull(block);
            blocks.add(block);
        }
        
        block = manager.allocatePointsBlock(64);
        Assert.assertNotNull(block);
        blocks.add(block);
        
        block = manager.allocatePointsBlock(128);
        Assert.assertNotNull(block);
        blocks.add(block);
        
        /*
         * we now have twice more than the manager can handle -> release everything and look at what stays in the cache.
         */
        for (int i = 0; i < 20; ++i)
        {
            blocks.get(i).release();
        }
        
        /*
         * Now we should have the 10 most recently used points blocks.
         */
        for (int i = 0; i < 9; ++i)
        {
            block = manager.allocatePointsBlock(128);
            Assert.assertTrue("Error while removing block " + i, blocks.remove(block));
        }
        // no more 128 block left
        Assert.assertTrue(!blocks.remove(manager.allocatePointsBlock(128)));
        // must be 1 64 block left
        Assert.assertTrue(blocks.remove(manager.allocatePointsBlock(64)));
        // no block remaining
        Assert.assertTrue(!blocks.remove(manager.allocatePointsBlock(64)));
        // there are 10 remaining blocks: those that were removed from the manager's cache.
        Assert.assertEquals(10, blocks.size());
    }
}
