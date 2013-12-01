package net.lab0.nebula.example2;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import net.lab0.nebula.color.GrayScaleColorModel;
import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.data.PointsBlock;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.exe.PointsBlockReader;
import net.lab0.nebula.exe.builder.ToCPUIterationComputing;
import net.lab0.nebula.exe.builder.ToCoordinatesPointsBlockConverter;
import net.lab0.nebula.exe.builder.ToFilePointsBlock;
import net.lab0.nebula.exe.builder.ToPointsBlockAggregator;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;
import net.lab0.tools.exec.SingleOutputGenerator;
import net.lab0.tools.geom.Point;
import net.lab0.tools.geom.Rectangle;
import net.lab0.tools.geom.RectangleInterface;

/**
 * Rendering selected points
 * 
 * @author 116
 * 
 */
public class Example04
{
    public static void main(String[] args)
    throws InterruptedException, IOException
    {
        /*
         * Same as example 3 except 3 lines
         */
        /*
         * 1: we want more points
         */
        CoordinatesBlock coordinatesBlock = new CoordinatesBlock(-2.0, 2.0, -2.0, 2.0, 4.0 / 2048d, 4.0 / 2048d);
        int blockSize = 512 * 512;
        
        int threads = Runtime.getRuntime().availableProcessors();
        PriorityExecutor priorityExecutor = new PriorityExecutor(threads);
        /*
         * 2: Change the target folder
         */
        Path basePath = ExamplesGlobals.createClearDirectory(Example04.class);
        final Path outputPath = FileSystems.getDefault().getPath(basePath.toString(), "out.data");
        /*
         * 3: Set a minimum iteration count criteria
         */
        JobBuilder<PointsBlock> toFile = new ToFilePointsBlock(outputPath, 128);
        JobBuilder<PointsBlock> toCPUComp = new ToCPUIterationComputing(toFile, 1024);
        JobBuilder<CoordinatesBlock> toCoordinatesBlockConverter = new ToCoordinatesPointsBlockConverter(toCPUComp,
        blockSize);
        SingleOutputGenerator<CoordinatesBlock> generator = new SingleOutputGenerator<CoordinatesBlock>(
        priorityExecutor, toCoordinatesBlockConverter, coordinatesBlock);
        
        priorityExecutor.registerShutdownHook(new Runnable()
        {
            @Override
            public void run()
            {
                WriterManager.getInstance().release(outputPath);
            }
        });
        priorityExecutor.prestartAllCoreThreads();
        priorityExecutor.submit(generator);
        priorityExecutor.finishAndShutdown();
        System.out.println("The file is available at " + outputPath.toUri());
        
        priorityExecutor = new PriorityExecutor(threads);
        RawMandelbrotData aggregate = new RawMandelbrotData(512, 512, 0);
        RectangleInterface viewPort = new Rectangle(new Point(-2.0, -2.0), new Point(2.0, 2.0));
        ToPointsBlockAggregator toAggregator = new ToPointsBlockAggregator(aggregate, viewPort, -1, 1024);
        PointsBlockReader pointsBlockReader = new PointsBlockReader(priorityExecutor, toAggregator, outputPath, 1024 * 1024);
        priorityExecutor.submit(pointsBlockReader);
        priorityExecutor.finishAndShutdown();
        BufferedImage image = aggregate.computeBufferedImage(new GrayScaleColorModel(), 0);
        Path imageOutputPath = FileSystems.getDefault().getPath(basePath.toString(), "out.png");
        ImageIO.write(image, "png", imageOutputPath.toFile());
        System.out.println("The image is available at " + imageOutputPath);
    }
}
