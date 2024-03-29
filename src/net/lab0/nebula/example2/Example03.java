package net.lab0.nebula.example2;

import java.awt.image.BufferedImage;
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
 * Rendering with the nebulabrot coloration.
 * 
 * @author 116
 * 
 */
public class Example03
{
    /**
     * @param args not used
     * @throws Exception because I don't care
     */
    public static void main(String[] args)
    throws Exception
    {
        /*
         * Same as example 2
         */
        CoordinatesBlock coordinatesBlock = new CoordinatesBlock(-2.0, 2.0, -2.0, 2.0, 4.0 / 2048d, 4.0 / 2048d);
        int blockSize = 512*512;
        
        int threads = Runtime.getRuntime().availableProcessors();
        PriorityExecutor priorityExecutor = new PriorityExecutor(threads);
        
        Path basePath = ExamplesGlobals.createClearDirectory(Example03.class);
        final Path outputPath = FileSystems.getDefault().getPath(basePath.toString(), "out.data");
        JobBuilder<PointsBlock> toFile = new ToFilePointsBlock(outputPath);
        JobBuilder<PointsBlock> toCPUComp = new ToCPUIterationComputing(toFile, 1024);
        JobBuilder<CoordinatesBlock> toCoordinatesBlockConverter = new ToCoordinatesPointsBlockConverter(toCPUComp, blockSize);
        SingleOutputGenerator<CoordinatesBlock> generator = new SingleOutputGenerator<CoordinatesBlock>(priorityExecutor, toCoordinatesBlockConverter, coordinatesBlock);
        
        priorityExecutor.registerShutdownHook(new Runnable()
        {
            @Override
            public void run()
            {
                WriterManager.getInstance().release(outputPath);
            }
        });
        priorityExecutor.execute(generator);
        priorityExecutor.waitForFinish();
        System.out.println("The file is available at " + outputPath.toUri());
        
        // Example 03
        /*
         * Create a new execution chain with a new executor because the previous one ended.
         */
        priorityExecutor = new PriorityExecutor(threads);
        /*
         * This is the aggregate we want to create.
         */
        RawMandelbrotData aggregate = new RawMandelbrotData(512, 512, 0);
        /*
         * This rectangle describes the area we want to render.
         */
        RectangleInterface viewPort = new Rectangle(new Point(-2.0, -2.0), new Point(2.0, 2.0));
        /*
         * The job builder that will take points block as input and convert them to the raw Mandelbrot rendering.
         */
        ToPointsBlockAggregator toAggregator = new ToPointsBlockAggregator(aggregate, viewPort, -1, 1024);
        /*
         * The data input: the file we created in Example02. We don't care about the exception for this example.
         */
        PointsBlockReader pointsBlockReader = new PointsBlockReader(priorityExecutor, toAggregator, outputPath, 1024 * 1024);
        /*
         * Do the computation
         */
        priorityExecutor.execute(pointsBlockReader);
        priorityExecutor.waitForFinish();
        /*
         * Graphic rendering
         */
        BufferedImage image = aggregate.computeBufferedImage(new GrayScaleColorModel(), 0);
        /*
         * Choose the path to the output image and save it.
         */
        Path imageOutputPath = FileSystems.getDefault().getPath(basePath.toString(), "out.png");
        ImageIO.write(image, "png", imageOutputPath.toFile());
        System.out.println("The image is available at " + imageOutputPath);
    }
    
}
