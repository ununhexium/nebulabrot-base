package net.lab0.nebula.example2;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import net.lab0.tools.FileUtils;

/**
 * This class defines constants that will be used in the example files.
 * 
 * @author 116
 * 
 */
public class ExamplesGlobals
{
    public static final Path baseDirectory = FileSystems.getDefault().getPath("R:", "dev", "nebula", "example");
    
    /**
     * Creates a directory with an appropriate path and an empty content. Exits if the creation failed.
     * 
     * @param clazz
     *            The class of the example file.
     * @return The path to the folder where the example can write its data.
     */
    public static Path createClearDirectory(Class<?> clazz)
    {
        Path path = FileSystems.getDefault().getPath(baseDirectory.toString(), clazz.getCanonicalName());
        
        try
        {
            File dir = path.toFile();
            if (!dir.exists())
            {
                System.out.println("Creating " + dir.getAbsolutePath());
                dir.mkdirs();
            }
            else if (!dir.isDirectory())
            {
                System.out.println("Deleting the file " + dir.getAbsolutePath());
                dir.delete();
                dir.mkdir();
            }
            else if (dir.list().length != 0)
            {
                System.out.println("Deleting the content of " + dir.getAbsolutePath());
                FileUtils.removeRecursive(path, false);
                dir.mkdir();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        
        return path;
    }
}
