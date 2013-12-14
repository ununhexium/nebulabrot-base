package net.lab0.nebula.exe;

import net.lab0.nebula.core.MandelbrotComputeRoutines;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.exe.builder.ToCPUIterationComputing;
import net.lab0.tools.exec.DevNull;
import net.lab0.tools.exec.PriorityExecutor;
import net.lab0.tools.exec.SingleOutputGenerator;

import org.junit.Assert;
import org.junit.Test;

public class TestPointsBlockCPUIterationComputing
{
    @Test
    public void testComputing()
    {
        PointsBlock pointsBlock = new PointsBlock(256 * 256);
        for (int i = 0; i < 256 * 256; ++i)
        {
            pointsBlock.real[i] = (Math.random() - 0.5) * 4d;
            pointsBlock.imag[i] = (Math.random() - 0.5) * 4d;
        }
        PriorityExecutor priorityExecutor = new PriorityExecutor();
        int maxIter = 65536;
        SingleOutputGenerator<PointsBlock> singleOutputGenerator = new SingleOutputGenerator<PointsBlock>(
        priorityExecutor, new ToCPUIterationComputing(new DevNull<PointsBlock>(), maxIter), pointsBlock);
        PointsBlockCPUIterationComputing job = new PointsBlockCPUIterationComputing(singleOutputGenerator,
        new DevNull<PointsBlock>(), pointsBlock, maxIter);
        priorityExecutor.execute(job);
        try
        {
            priorityExecutor.finishAndShutdown();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        
        for (int i = 0; i < 256 * 256; ++i)
        {
            long expected = MandelbrotComputeRoutines.computeIterationsCountOptim2(pointsBlock.real[i],
            pointsBlock.imag[i], maxIter);
            Assert.assertEquals("Error at index " + i, expected, pointsBlock.iter[i]);
        }
    }
}
