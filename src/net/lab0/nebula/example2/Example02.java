package net.lab0.nebula.example2;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.exe.builder.ToCPUIterationComputing;
import net.lab0.nebula.exe.builder.ToCoordinatesPointsBlockConverter;
import net.lab0.nebula.exe.builder.ToFilePointsBlock;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;
import net.lab0.tools.exec.SingleOutputGenerator;

/**
 * Basic computing of an area of the Mandelbrot set.
 * 
 * <ul>
 * <li>Computing data</li>
 * <li>Multithread</li>
 * </ul>
 * 
 * @author 116
 * 
 */
public class Example02
{
    public static void main(String[] args)
    {
        /*
         * Same as example 1
         */
        CoordinatesBlock coordinatesBlock = new CoordinatesBlock(0.0, 1.0, 0.0, 1.0, 4.0 / 4096d, 4.0 / 4096d);
        int blockSize = 64 * 64;
        
        int threads = Runtime.getRuntime().availableProcessors();
        PriorityExecutor priorityExecutor = new PriorityExecutor(threads);
        
        Path basePath = ExamplesGlobals.createClearDirectory(Example02.class);
        final Path outputPath = FileSystems.getDefault().getPath(basePath.toString(), "out.data");
        JobBuilder<PointsBlock> toFile = new ToFilePointsBlock(outputPath);
        priorityExecutor.registerShutdownHook(new Runnable()
        {
            @Override
            public void run()
            {
                WriterManager.getInstance().release(outputPath);
            }
        });
        
        /*
         * This time, we redirect the points blocks to a computing job that will compute the iterations.
         */
        JobBuilder<PointsBlock> toCPUComp = new ToCPUIterationComputing(toFile, 65536);
        JobBuilder<CoordinatesBlock> toCoordinatesBlockConverter = new ToCoordinatesPointsBlockConverter(toCPUComp, blockSize);
        SingleOutputGenerator<CoordinatesBlock> generator = new SingleOutputGenerator<CoordinatesBlock>(priorityExecutor, toCoordinatesBlockConverter, coordinatesBlock);
        
        /*
         * Start the execution of the job. The call will start the job execution automatically.
         */
        priorityExecutor.prestartAllCoreThreads();
        priorityExecutor.submit(generator);
        /*
         * We now have to wait for the job to finish.
         */
        try
        {
            priorityExecutor.finishAndShutdown();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        /*
         * The file is now created.
         */
        System.out.println("The file is available at " + outputPath.toUri());
    }
}
