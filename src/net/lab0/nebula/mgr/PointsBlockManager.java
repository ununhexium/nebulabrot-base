package net.lab0.nebula.mgr;

import java.util.ArrayList;
import java.util.List;

import net.lab0.nebula.data.PointsBlock;

public class PointsBlockManager
{
    // available blocks sorted by ascending size
    private List<PointsBlock> availableBlocksSize = new ArrayList<>();
    // available blocks sorted by ascending use date
    private List<PointsBlock> availableBlocksDate = new ArrayList<>();
    
    private List<PointsBlock> allocatedBlocks     = new ArrayList<>();
    private int               maxReserve;
    
    public PointsBlockManager(int maxReserve)
    {
        super();
        this.maxReserve = maxReserve;
    }
    
    /**
     * Request a {@link PointsBlock}. The data in the block can have any value.
     * 
     * @return an uninitialised {@link PointsBlock} with the requested size.
     */
    public PointsBlock getPointsBlock(int size)
    {
        PointsBlock block = sizeBinarySearch(availableBlocksSize, size);
        if (block == null)
        {
            block = new PointsBlock(size, this);
            allocatedBlocks.add(block);
        }
        else
        {
            availableBlocksDate.remove(block);
        }
        
        return block;
    }
    
    public static PointsBlock sizeBinarySearch(List<PointsBlock> list, int size)
    {
        int start = 0;
        int end = list.size() - 1;
        while (start <= end)
        {
            int middle = (start + end) / 2;
            PointsBlock middleBlock = list.get(middle);
            if (middleBlock.size < size)
            {
                start = middle + 1;
            }
            else if (middleBlock.size > size)
            {
                end = middle - 1;
            }
            else
            {
                return middleBlock;
            }
        }
        return null;
    }
    
    public static void sizeBinaryInsert(List<PointsBlock> list, PointsBlock block)
    {
        if (list.size() == 0)
        {
            list.add(block);
            return;
        }
        
        int start = 0;
        int end = list.size() - 1;
        int middle = 0;
        
        while (start <= end)
        {
            middle = (start + end) / 2;
            PointsBlock middleBlock = list.get(middle);
            if (middleBlock.size < block.size)
            {
                start = middle + 1;
            }
            else if (middleBlock.size > block.size)
            {
                end = middle - 1;
            }
            else
            {
                break; // got it :)
            }
        }
        
        if (list.get(middle).size < block.size)
        {
            list.add(middle + 1, block);
        }
        else
        {
            list.add(middle, block);
        }
        
    }
    
    public void free(PointsBlock block)
    {
        if (allocatedBlocks.remove(block))
        {
            while (allocatedBlocks.size() >= maxReserve)
            {
                PointsBlock toRemove = availableBlocksDate.remove(0);
                availableBlocksSize.remove(toRemove);
            }
            availableBlocksDate.add(block);
            sizeBinaryInsert(availableBlocksSize, block);
        }
        else
        {
            throw new IllegalArgumentException(
            "Trying to free a block that was not allocated to this PointsBlockManager");
        }
    }
}
