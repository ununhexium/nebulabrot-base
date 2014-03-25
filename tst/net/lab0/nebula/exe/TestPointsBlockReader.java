package net.lab0.nebula.exe;

import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.exe.builder.ToCoordinatesPointsBlockConverter;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.Dump;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;
import net.lab0.tools.exec.SingleOutputGenerator;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPointsBlockReader
{
    private static final Path         path          = FileSystems.getDefault().getPath("test", "points_block_reader",
                                                    "test_file.data");
    private static WriterManager      writerManager = WriterManager.getInstance();
    public static List<PointsBlock>   dumpList      = Collections.synchronizedList(new ArrayList<PointsBlock>());
    
    private static final class PointsBlockWriterCreator
    implements JobBuilder<PointsBlock>
    {
        @Override
        public CascadingJob<PointsBlock, ?> buildJob(CascadingJob<?, PointsBlock> parent, PointsBlock output)
        {
            return new PointsBlockWriter(parent, output, path);
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
        ToCoordinatesPointsBlockConverter toConverter = new ToCoordinatesPointsBlockConverter(
        new PointsBlockWriterCreator(), 16 * 16);
        SingleOutputGenerator<CoordinatesBlock> generator = new SingleOutputGenerator<CoordinatesBlock>(executor,
        toConverter, block);
        executor.registerShutdownHook(new Runnable()
        {
            @Override
            public void run()
            {
                writerManager.release(path);
            }
        });
        executor.execute(generator);
        try
        {
            executor.waitForFinish();
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
        PointsBlockReader pointsBlockReader = new PointsBlockReader(executor, new Dumper(), path, 250);
        executor.execute(pointsBlockReader);
        try
        {
            executor.waitForFinish();
        }
        catch (InterruptedException e)
        {
            Assert.fail();
            e.printStackTrace();
        }
        
        Assert.assertTrue(dumpList.size() == 5);
        int block250 = 0;
        int block24 = 0;
        for (PointsBlock block : dumpList)
        {
            switch (block.size)
            {
                case 24:
                    block24++;
                    break;
                
                case 250:
                    block250++;
                    break;
                
                default:
                    break;
            }
        }
        Assert.assertEquals(4, block250);
        Assert.assertEquals(1, block24);
    }
}
