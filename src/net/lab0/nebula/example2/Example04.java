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
import net.lab0.nebula.exe.CoordinatesToPointsBlockConverter;
import net.lab0.nebula.exe.PointsBlockReader;
import net.lab0.nebula.exe.builder.ToCPUIterationComputating;
import net.lab0.nebula.exe.builder.ToFile;
import net.lab0.nebula.exe.builder.ToPointsBlockAggregator;
import net.lab0.nebula.mgr.PointsBlockManager;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;
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
        CoordinatesBlock coordinatesBlock = new CoordinatesBlock(-2.0, 2.0, -2.0, 2.0, 4.0 / 8192d, 4.0 / 8192d);
        int blockSize = 512 * 512;
        
        int threads = Runtime.getRuntime().availableProcessors();
        PriorityExecutor priorityExecutor = new PriorityExecutor(threads);
        
        final WriterManager writerManager = new WriterManager();
        /*
         * Change the target folder
         */
        Path basePath = ExamplesGlobals.createClearDirectory(Example04.class);
        final Path outputPath = FileSystems.getDefault().getPath(basePath.toString(), "out.data");
        JobBuilder<PointsBlock> toFile = new ToFile(writerManager, outputPath, 128);
        PointsBlockManager pointsBlockManager = new PointsBlockManager(10);
        JobBuilder<PointsBlock> toCPUComp = new ToCPUIterationComputating(toFile, 1024);
        CoordinatesToPointsBlockConverter converter = new CoordinatesToPointsBlockConverter(priorityExecutor, 0,
        toCPUComp, coordinatesBlock, blockSize, pointsBlockManager);
        priorityExecutor.registerShutdownHook(new Runnable()
        {
            @Override
            public void run()
            {
                writerManager.release(outputPath);
            }
        });
        priorityExecutor.prestartAllCoreThreads();
        priorityExecutor.submit(converter);
        priorityExecutor.finishAndShutdown();
        System.out.println("The file is available at " + outputPath.toUri());
        
        priorityExecutor = new PriorityExecutor(threads);
        RawMandelbrotData aggregate = new RawMandelbrotData(512, 512, 0);
        RectangleInterface viewPort = new Rectangle(new Point(-2.0, -2.0), new Point(2.0, 2.0));
        ToPointsBlockAggregator toAggregator = new ToPointsBlockAggregator(aggregate, viewPort, -1, 1024);
        PointsBlockReader pointsBlockReader = new PointsBlockReader(priorityExecutor, 0, toAggregator, outputPath,
        pointsBlockManager, 1024 * 1024);
        priorityExecutor.submit(pointsBlockReader);
        priorityExecutor.finishAndShutdown();
        BufferedImage image = aggregate.computeBufferedImage(new GrayScaleColorModel(), 0);
        Path imageOutputPath = FileSystems.getDefault().getPath(basePath.toString(), "out.png");
        ImageIO.write(image, "png", imageOutputPath.toFile());
        System.out.println("The image is available at " + imageOutputPath);
    }
}
