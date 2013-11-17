package net.lab0.nebula.exe;

import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.mgr.PointsBlockManager;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.Dump;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPointsBlockReader
{
    private static final Path         path          = FileSystems.getDefault().getPath("test", "points_block_reader",
                                                    "test_file.data");
    private static WriterManager      writerManager = WriterManager.getInstance();
    private static PointsBlockManager manager       = new PointsBlockManager(10);
    public static List<PointsBlock>   dumpList      = Collections.synchronizedList(new ArrayList<PointsBlock>());
    
    private static final class PointsBlockWriterCreator
    implements JobBuilder<PointsBlock>
    {
        @Override
        public CascadingJob<PointsBlock, ?> buildJob(CascadingJob<?, PointsBlock> parent, PointsBlock output)
        {
            return new PointsBlockWriter(parent.getExecutor(), parent.getPriority() + 1, output, path, writerManager);
        }
    }
    
    private static final class Dumper
    implements JobBuilder<PointsBlock>
    {
        @Override
        public CascadingJob<PointsBlock, ?> buildJob(CascadingJob<?, PointsBlock> parent, PointsBlock output)
        {
            return new Dump<PointsBlock>(parent.getExecutor(), output, dumpList);
        }
    }
    
    @BeforeClass
    public static void create()
    {
        path.toFile().getParentFile().mkdirs();
        PriorityExecutor executor = new PriorityExecutor(Runtime.getRuntime().availableProcessors());
        // hopefully, there will be no rounding errors Powers of 2 are great :)
        CoordinatesBlock block = new CoordinatesBlock(-2.0, 2.0, -2.0, 2.0, 4.0 / 32d, 4.0 / 32d);
        CoordinatesToPointsBlockConverter generator = new CoordinatesToPointsBlockConverter(executor, 0,
        new PointsBlockWriterCreator(), block, 16 * 16, manager);
        executor.registerShutdownHook(new Runnable()
        {
            @Override
            public void run()
            {
                writerManager.release(path);
            }
        });
        executor.prestartAllCoreThreads();
        executor.submit(generator);
        try
        {
            executor.finishAndShutdown();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testReader()
    throws FileNotFoundException
    {
        PriorityExecutor executor = new PriorityExecutor(Runtime.getRuntime().availableProcessors());
        PointsBlockManager manager = new PointsBlockManager(10);
        PointsBlockReader pointsBlockReader = new PointsBlockReader(executor, 0, new Dumper(), path, manager, 250);
        executor.prestartAllCoreThreads();
        executor.submit(pointsBlockReader);
        try
        {
            executor.finishAndShutdown();
        }
        catch (InterruptedException e)
        {
            Assert.fail();
            e.printStackTrace();
        }
        
        Assert.assertTrue(dumpList.size() == 5);
        for (PointsBlock block : dumpList.subList(0, 4))
        {
            Assert.assertTrue(block.size == 250);
        }
        Assert.assertEquals(4 * 6, dumpList.get(4).size);
    }
}
