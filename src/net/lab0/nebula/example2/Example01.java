package net.lab0.nebula.example2;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.exe.CoordinatesToPointsBlockConverter;
import net.lab0.nebula.exe.builder.ToFile;
import net.lab0.nebula.mgr.PointsBlockManager;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;

/**
 * The basics.
 * 
 * <ul>
 * <li>Creation of a basic executable chain</li>
 * <li>Execution of the jobs</li>
 * <li>Output to a file</li>
 * </ul>
 * 
 * @author 116
 * 
 */
public class Example01
{
    public static void main(String[] args)
    {
        /*
         * First, the area we want to compute. This is the whole Mandelbrot set with 16 million points.
         */
        CoordinatesBlock coordinatesBlock = new CoordinatesBlock(-2.0, 2.0, -2.0, 2.0, 4.0 / 1024d, 4.0 / 1024d);
        
        /*
         * Then, the size of the computation blocks. This must be small enough to do a good parallelization on the
         * available CPUs. But the bigger, the less overhead due to processes management there will be. This will create
         * 64 block to compute.
         */
        int blockSize = 128 * 128;
        
        /*
         * We now need to define the job that will split the area to compute into 64 block of 512*512 points. This job
         * need several parameters.
         */
        CoordinatesToPointsBlockConverter converter = null;
        /*
         * The priority executor is the executor that will run this job. This must stay the same for all the execution
         * chain we are going to create. This is the priority executor that is in charge of the execution of the
         * different job we are going to create.
         */
        int threads = 1;
        PriorityExecutor priorityExecutor = new PriorityExecutor(threads);
        /*
         * The job builder is the class that will create the job that has to be executed for each of the created points
         * block. In this case, we want to output the result in a file. @see net.lab0.nebula.exe.builder.ToFile
         */
        WriterManager writerManager = new WriterManager();
        Path basePath = ExamplesGlobals.createClearDirectory(Example01.class);
        Path outputPath = FileSystems.getDefault().getPath(basePath.toString(), "out.data");
        JobBuilder<PointsBlock> toFile = new ToFile(writerManager, outputPath);
        
        /*
         * The points block manager is the class in charge of the allocation and management of the points blocks
         * objects. It avoid useless frequent allocation/free of the same memory array by allowing the points block to
         * be reused after their life cycle. The reserve is the amount of blocks that should be kept referenced by the
         * manager for a future use.
         */
        PointsBlockManager pointsBlockManager = new PointsBlockManager(10);
        
        /*
         * Effectively create the converter with the previously created parameters.
         */
        converter = new CoordinatesToPointsBlockConverter(priorityExecutor, 0, toFile, coordinatesBlock, blockSize,
        pointsBlockManager);
        /*
         * We just created a basic chain: the input is a coordinates block that will be split into points block by the
         * converter and then written to a file by the points block writer.
         */
        
        /*
         * Start the execution of the job. The call will start the job execution automatically.
         */
        priorityExecutor.prestartAllCoreThreads();
        priorityExecutor.submit(converter);
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