package tries;

import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.concurrent.ThreadFactory;

import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.enums.Status;
import net.lab0.nebula.exception.SerializationException;
import net.lab0.nebula.exe.MandelbrotQuadTreeNodeReader;
import net.lab0.nebula.exe.MandelbrotQuadTreeNodeSplitter;
import net.lab0.nebula.exe.MandelbrotQuadTreeNodeSplitter.SplittingCriterion;
import net.lab0.nebula.exe.builder.BuilderFactory;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;
import net.lab0.tools.exec.SingleOutputGenerator;

public class QuadTreeComputing
{
    private static final int maxIteration = 1 << 24;
    
    public static void main(String[] args)
    throws FileNotFoundException, InterruptedException, SerializationException
    {
        Path parent = FileSystems.getDefault().getPath("R:", "dev", "nebula", "tries",
        QuadTreeComputing.class.getName());
        final Path outputPath = FileSystems.getDefault().getPath(parent.toString(), "0.data");
        
        // case depth == 0
        PriorityExecutor executor = new PriorityExecutor(Runtime.getRuntime().availableProcessors() + 1);
        MandelbrotQuadTreeNode root = MandelbrotQuadTreeNode.buildRoot();
        JobBuilder<MandelbrotQuadTreeNode> toWriter = BuilderFactory.toFile(outputPath);
        JobBuilder<MandelbrotQuadTreeNode> toComputeInOut = BuilderFactory.toComputeInOut(toWriter, maxIteration, 128,
        5);
        SingleOutputGenerator<MandelbrotQuadTreeNode> rootNodeGenerator = new SingleOutputGenerator<MandelbrotQuadTreeNode>(
        executor, toComputeInOut, root);
        executor.submit(rootNodeGenerator);
        executor.registerShutdownHook(new Runnable()
        {
            @Override
            public void run()
            {
                WriterManager.getInstance().release(outputPath);
            }
        });
        executor.finishAndShutdown();
        
        Thread.sleep(1000);
        
        int maxDepth = 20;
        for (int depth = 1; depth < maxDepth; ++depth)
        {
            System.out.println("depth=" + depth);
            // executor
            executor = new PriorityExecutor(Runtime.getRuntime().availableProcessors() * 3 / 2);
            executor.setThreadFactory(new ThreadFactory()
            {
                @Override
                public Thread newThread(Runnable r)
                {
                    Thread thread = new Thread(r, "Priority TPE");
                    thread.setPriority(Thread.MIN_PRIORITY);
                    return thread;
                }
            });
            
            // source
            Path inputPath = FileSystems.getDefault().getPath(parent.toString(), String.valueOf(depth - 1) + ".data");
            
            // destination
            final Path outputPath2 = FileSystems.getDefault().getPath(parent.toString(),
            String.valueOf(depth) + ".data");
            
            // execs
            toWriter = BuilderFactory.toFile(outputPath2);
            toComputeInOut = BuilderFactory.toComputeInOut(toWriter, 256, 128, 5);
            SplittingCriterion splitBrowsedNodes = new MandelbrotQuadTreeNodeSplitter.SplittingCriterion()
            {
                @Override
                public boolean doWeSplitIt(MandelbrotQuadTreeNode node)
                {
                    return node.status == Status.BROWSED;
                }
            };
            JobBuilder<MandelbrotQuadTreeNode[]> toQTNSplitter = BuilderFactory.toMandelbrotQuadTreeNodeSplitter(
            toComputeInOut, splitBrowsedNodes);
            MandelbrotQuadTreeNodeReader reader = new MandelbrotQuadTreeNodeReader(executor, toQTNSplitter, inputPath,
            1024);
            executor.submit(reader);
            executor.registerShutdownHook(new Runnable()
            {
                @Override
                public void run()
                {
                    WriterManager.getInstance().release(outputPath2);
                }
            });
            executor.finishAndShutdown();
            
            // execution chain
            
        }
    }
}
