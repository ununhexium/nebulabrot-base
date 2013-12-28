package tries;

import java.awt.image.BufferedImage;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import net.lab0.nebula.color.PowerGrayScaleColorModel;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.exe.PointsBlockReader;
import net.lab0.nebula.exe.builder.ToPointsBlockAggregator;
import net.lab0.tools.exec.PriorityExecutor;
import net.lab0.tools.geom.Point;
import net.lab0.tools.geom.Rectangle;
import net.lab0.tools.geom.RectangleInterface;

public class Renderer
{
    public static void main(String[] args)
    throws Exception
    {
        Path pointsBlocksBase = FileSystems.getDefault().getPath("R:","dev","nebula","project","point","18");
        Path pointBlocksOutput = FileSystems.getDefault().getPath(pointsBlocksBase.toString(), "c_17.data");

        RectangleInterface startViewPort = new Rectangle(new Point(-2.0, -2.0), new Point(2.0, 2.0));//0;0
        RectangleInterface endViewPort = new Rectangle(new Point(-0.2, 0.9), new Point(0, 1.1));//-0.1;1
        
        int frames = 1;
        for (int i = 0; i < frames; ++i)
        {
            // rendering
            System.out.println("Aggregating " + i + "/" + frames);
            PriorityExecutor priorityExecutor = new PriorityExecutor(Runtime.getRuntime().availableProcessors() - 1);
            RawMandelbrotData aggregate = new RawMandelbrotData(4096, 4096, 0);
            RectangleInterface viewPort = getViewPort(startViewPort, endViewPort, i, frames);
            ToPointsBlockAggregator toAggregator = new ToPointsBlockAggregator(aggregate, viewPort, -1, 1024);
            PointsBlockReader pointsBlockReader = new PointsBlockReader(priorityExecutor, toAggregator,
            pointBlocksOutput, 1024 * 1024);
            priorityExecutor.submit(pointsBlockReader);
            priorityExecutor.finishAndShutdown();
            
            System.out.println("Writing image");
            Path imageOutputPath = FileSystems.getDefault().getPath(pointsBlocksBase.toString(), "image"+String.format("%07d", i)+".png");
            /*
             * Graphic rendering
             */
            BufferedImage image = aggregate.computeBufferedImage(new PowerGrayScaleColorModel(0.5), 0);
            /*
             * Choose the path to the output image and save it.
             */
            ImageIO.write(image, "png", imageOutputPath.toFile());
            System.out.println("The image is available at " + imageOutputPath);
        }
        System.out.println("Finished");
    }

    private static RectangleInterface getViewPort(RectangleInterface startViewPort, RectangleInterface endViewPort,
    int current, int max)
    {
        double ratio = (double)current / max;
        double minX = startViewPort.getMinX() * (1-ratio) + endViewPort.getMinX() * ratio;
        double maxX = startViewPort.getMaxX() * (1-ratio) + endViewPort.getMaxX() * ratio;
        double minY = startViewPort.getMinY() * (1-ratio) + endViewPort.getMinY() * ratio;
        double maxY = startViewPort.getMaxY() * (1-ratio) + endViewPort.getMaxY() * ratio;
        
        return new Rectangle(new Point(minX, minY), new Point(maxX, maxY));
    }
}
