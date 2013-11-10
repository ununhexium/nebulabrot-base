package net.lab0.nebula.exe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.mgr.PointsBlockManager;
import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.Dump;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestLinearPointsBlockGenerator
{
    public static List<PointsBlock> dumpList = Collections.synchronizedList(new ArrayList<PointsBlock>());
    
    private static final class TestJobBuilder
    implements JobBuilder<PointsBlock>
    {
        @Override
        public CascadingJob<PointsBlock, ?> buildJob(CascadingJob<?, PointsBlock> parent, PointsBlock output)
        {
            return new Dump<PointsBlock>(parent.getExecutor(), output, dumpList);
        }
    }
    
    @BeforeClass
    public static void beforeClass()
    {
        PriorityExecutor executor = new PriorityExecutor(Runtime.getRuntime().availableProcessors());
        //hopefully, there will be no rounding errors Powers of 2 are great :)
        CoordinatesBlock block = new CoordinatesBlock(-2.0, 2.0, -2.0, 2.0, 4.0 / 2048d, 4.0 / 2048d);
        PointsBlockManager manager = new PointsBlockManager(10);
        CoordinatesToPointsBlockConverter generator = new CoordinatesToPointsBlockConverter(executor, 0,
        new TestJobBuilder(), block, 1024 * 1024, manager);
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
    public void pointsOutput()
    {
        Assert.assertEquals(4, dumpList.size());
        for (PointsBlock pb : dumpList)
        {
            Assert.assertEquals(1024 * 1024, pb.size);
        }
    }
}
