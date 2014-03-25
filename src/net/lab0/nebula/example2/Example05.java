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
import net.lab0.nebula.exe.builder.ToCoordinatesPointsBlockConverter;
import net.lab0.nebula.exe.builder.ToFilePointsBlock;
import net.lab0.nebula.exe.builder.ToOCLIterationComputing;
import net.lab0.nebula.exe.builder.ToPointsBlockAggregator;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;
import net.lab0.tools.exec.SingleOutputGenerator;
import net.lab0.tools.geom.Point;
import net.lab0.tools.geom.Rectangle;
import net.lab0.tools.geom.RectangleInterface;

/**
 * Using openCL for computation
 * 
 * @author 116
 * 
 */
public class Example05
{
    /**
     * @param args not used
     * @throws Exception because I don't care
     */
    public static void main(String[] args)
    throws Exception
    {
        /*
         * Same as example 4 except 3 lines
         */
        /*
         * 1: we want more points and bigger blocks
         */
        CoordinatesBlock coordinatesBlock = new CoordinatesBlock(-2.0, 2.0, -2.0, 2.0, 4.0 / 8192d, 4.0 / 8192d);
        int blockSize = 1024*1024;
        
        int threads = Runtime.getRuntime().availableProcessors();
        PriorityExecutor priorityExecutor = new PriorityExecutor(threads);
        /*
         * 2: Change the target folder
         */
        Path basePath = ExamplesGlobals.createClearDirectory(Example05.class);
        final Path outputPath = FileSystems.getDefault().getPath(basePath.toString(), "out.data");
        JobBuilder<PointsBlock> toFile = new ToFilePointsBlock(outputPath, -1);
        /*
         * 3: We want to use the openCL computation facility
         */
        JobBuilder<PointsBlock> toCPUComp = new ToOCLIterationComputing(toFile, 4096);
        JobBuilder<CoordinatesBlock> toConverter = new ToCoordinatesPointsBlockConverter(toCPUComp, blockSize);
        SingleOutputGenerator<CoordinatesBlock> generator = new SingleOutputGenerator<CoordinatesBlock>(priorityExecutor, toConverter, coordinatesBlock);
//        CoordinatesToPointsBlockConverter converter = new CoordinatesToPointsBlockConverter(priorityExecutor, 0,
//        toCPUComp, coordinatesBlock, blockSize, pointsBlockManager);
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
        
        priorityExecutor = new PriorityExecutor(threads);
        RawMandelbrotData aggregate = new RawMandelbrotData(1024, 1024, 0);
        RectangleInterface viewPort = new Rectangle(new Point(-2.0, -2.0), new Point(2.0, 2.0));
        ToPointsBlockAggregator toAggregator = new ToPointsBlockAggregator(aggregate, viewPort, -1, 1024);
        PointsBlockReader pointsBlockReader = new PointsBlockReader(priorityExecutor, toAggregator, outputPath, 1024 * 1024);
        priorityExecutor.execute(pointsBlockReader);
        priorityExecutor.waitForFinish();
        BufferedImage image = aggregate.computeBufferedImage(new GrayScaleColorModel(), 0);
        Path imageOutputPath = FileSystems.getDefault().getPath(basePath.toString(), "out.png");
        ImageIO.write(image, "png", imageOutputPath.toFile());
        System.out.println("The image is available at " + imageOutputPath);
    }
}
