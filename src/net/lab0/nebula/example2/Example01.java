package net.lab0.nebula.example2;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.exe.builder.ToCoordinatesPointsBlockConverter;
import net.lab0.nebula.exe.builder.ToFilePointsBlock;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;
import net.lab0.tools.exec.SingleOutputGenerator;

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
         * First, the area we want to compute. This is the whole Mandelbrot set with 1 million points.
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
        Path basePath = ExamplesGlobals.createClearDirectory(Example01.class);
        final Path outputPath = FileSystems.getDefault().getPath(basePath.toString(), "out.data");
        JobBuilder<PointsBlock> toFile = new ToFilePointsBlock(outputPath);
        /*
         * After the computation, we need to tell to the write manager that we won't write anything in the above file
         * anymore. We can do that by registering a hook instead of doing it manually after the executor ended.
         */
        priorityExecutor.registerShutdownHook(new Runnable()
        {
            @Override
            public void run()
            {
                WriterManager.getInstance().release(outputPath);
            }
        });
        
        /*
         * Effectively create the converter with the previously created parameters.
         */
        ToCoordinatesPointsBlockConverter toCoordinatesConverter = new ToCoordinatesPointsBlockConverter(toFile,
        blockSize);
        
        /*
         * Create a single output source that will take place of the generator requested by the next step
         */
        SingleOutputGenerator<CoordinatesBlock> generator = new SingleOutputGenerator<CoordinatesBlock>(
        priorityExecutor, toCoordinatesConverter, coordinatesBlock);
        
        /*
         * We just created a basic chain: the input is a coordinates block that will be split into points block by the
         * converter and then written to a file by the points block writer.
         */
        
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
