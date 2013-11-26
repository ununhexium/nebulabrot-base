package tries;

import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import net.lab0.tools.exec.PriorityExecutor;

public class QuadTreeComputing
{
    public static void main(String[] args)
    throws FileNotFoundException
    {
        Path parent = FileSystems.getDefault().getPath("R:", "dev", "nebula", "tries", "t1");
        
        for (int depth = 0; depth < 5; ++depth)
        {
            //executor
            PriorityExecutor executor = new PriorityExecutor(Runtime.getRuntime().availableProcessors()-1);
            
            //source
            Path inputPath = FileSystems.getDefault().getPath(parent.toString(), String.valueOf(depth));
            
            //destination
            Path outputPath = FileSystems.getDefault().getPath(parent.toString(), String.valueOf(depth+1));

            // execs
//            ToComputeInOut toCompute = new ToComputeInOut(toWriter, 65536, 256, 5);
//            ToSplitter toSplitter = new ToSplitter(toCompute);
//            MandelbrotQuadTreeNodeReader nodeReader = new MandelbrotQuadTreeNodeReader(executor, 0, toSplitter, inputPath, 1024);
            
            //execution chain
            
        }
    }
}
