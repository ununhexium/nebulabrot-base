package tries;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.enums.Status;
import net.lab0.nebula.exe.MandelbrotQuadTreeNodeReader;
import net.lab0.nebula.exe.PointsBlockOCLIterationComputing2;
import net.lab0.nebula.exe.PointsBlockOCLIterationComputing2.Parameters;
import net.lab0.nebula.exe.builder.BuilderFactory;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;

import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;

public class ComputePoints
{
    public static void main(String[] args)
    throws InterruptedException, IOException
    {
        System.out.println("Start");
        Stopwatch stopwatch = Stopwatch.createStarted();
        final int maxDepth = 9;
        final long maxIter = 65536;
        final long minIter = 64;
        
        Path base = FileSystems.getDefault().getPath("R:", "dev", "nebula", "tree", "serial");
        Path pointsBlocksBase = FileSystems.getDefault().getPath("R:", "dev", "nebula", "tries",
        ComputePoints.class.getName());
        if (!pointsBlocksBase.toFile().exists())
        {
            pointsBlocksBase.toFile().mkdirs();
        }
        Path pointBlocksOutput = FileSystems.getDefault().getPath(pointsBlocksBase.toString(), "pointsBlocks.data");
        JobBuilder<PointsBlock> toFile = BuilderFactory.toPointsBlocksFile(pointBlocksOutput, 64, Long.MAX_VALUE);
        System.out.println("outputing results to " + pointBlocksOutput);
        
        Predicate<Long> filterPoints = new Predicate<Long>()
        {
            @Override
            public boolean apply(Long input)
            {
                long i = input;
                return (i < maxIter && i>= minIter);
            }
        };
        PointsBlockOCLIterationComputing2.Parameters parameters = new Parameters(maxIter, filterPoints, 128, 1);
        double step = 4.0 / (double) (1L << (maxDepth)) / 16d;
        for (int i = 0; i <= maxDepth; ++i)
        {
            PriorityExecutor executor = new PriorityExecutor(Runtime.getRuntime().availableProcessors());
            Path file = FileSystems.getDefault().getPath(base.toString(),
            "p256i65536d5D16binNoIndex_depth" + i + ".data");
            Predicate<MandelbrotQuadTreeNode> filter = new Predicate<MandelbrotQuadTreeNode>()
            {
                @Override
                public boolean apply(MandelbrotQuadTreeNode node)
                {
                    if (node.nodePath.depth == maxDepth)
                    {
                        if (node.status == Status.BROWSED)
                        {
                            return true;
                        }
                        else
                        {
                            return node.status == Status.OUTSIDE && node.maximumIteration > minIter;
                        }
                    }
                    else
                    {
                        return node.status == Status.OUTSIDE && node.maximumIteration > minIter;
                    }
                }
            };
            System.out.println("read " + file + " " + new Date());
            JobBuilder<CoordinatesBlock> toConverter = BuilderFactory.toOCLCompute2(toFile, parameters);
            JobBuilder<MandelbrotQuadTreeNode[]> splitAndConvert = BuilderFactory.toNodeSplitterAndConverter(
            toConverter, step, filter);
            MandelbrotQuadTreeNodeReader reader = new MandelbrotQuadTreeNodeReader(executor, splitAndConvert, file,
            1024 * 1024);
            
            executor.execute(reader);
            executor.finishAndShutdown();
        }
        
        WriterManager.getInstance().release(pointBlocksOutput);
        System.out.println("Time: " + stopwatch.elapsed(TimeUnit.SECONDS));
    }
}
