package net.lab0.nebula.example;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import net.lab0.nebula.data.QuadTreePointsComputingSet;
import net.lab0.nebula.data.RenderingParameters;
import net.lab0.nebula.enums.ComputingMethod;
import net.lab0.nebula.project.Project;
import net.lab0.nebula.project.ProjectKey;

/**
 * Computing and saving large sets of points.
 * 
 * @author 116@lab0.net
 * 
 */
public class Example4
{
    public static void main(String[] args)
    throws Exception
    {
        Stopwatch stopWatch = Stopwatch.createStarted();
        /*
         * First, we need to choose a directory.
         */
        Path projectPath = FileSystems.getDefault().getPath("F:", "dev", "nebula", "points");
        System.out.println("Project path set to " + projectPath);
        
        /*
         * This class may be run several time so we need to make sure that the folder will always be empty.
         */
        if (!projectPath.toFile().exists())
        {
            System.out.println("Creating project's directory");
            projectPath.toFile().mkdirs();
        }
        
        /*
         * Create a new project
         */
        System.out.println("Creating a new project");
        Project project = new Project(projectPath);
        // have a look at which parameters we use
        Map<ProjectKey, Object> param = project.getParameters();
        for (ProjectKey key : param.keySet())
        {
            System.out.println(key.name() + "=" + param.get(key));
        }
        
        
        
        // the amount of points on the whole mandelbrot set
        long pointsCount = 1L << 34;
        int blocks = (int) (pointsCount / (1L << 32));
        System.out.println("need to compute " + blocks + "blocks.");
        RenderingParameters renderingParameters = new RenderingParameters(0, 0, pointsCount, 15, 4096, null, null);
        renderingParameters.setPointsCount(pointsCount);
        QuadTreePointsComputingSet set = project.createNewPointComputingSet(blocks, ComputingMethod.QUADTREE,
        renderingParameters, "group0");
        
        for (int i = 0; i < set.getBlocksCount(); ++i)
        {
            System.out.println("Computing block " + i);
            set.computeBlock(i);
        }
        set.finish();
        
        stopWatch.stop();
        System.out.println(stopWatch.elapsed(TimeUnit.SECONDS) + "s");
    }
}
