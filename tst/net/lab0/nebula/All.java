package net.lab0.nebula;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import net.lab0.nebula.color.AllColor;
import net.lab0.nebula.core.AllCore;
import net.lab0.nebula.data.AllData;
import net.lab0.nebula.example2.AllExamples;
import net.lab0.nebula.exe.AllExe;
import net.lab0.nebula.mgr.AllMrg;
import net.lab0.tools.FileUtils;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = { AllColor.class, AllCore.class, AllData.class, AllExamples.class, AllExe.class, AllMrg.class, })
public class All
{
    private static Path rootFolder = FileSystems.getDefault().getPath("R:", "dev", "nebula", "test");
    
    /**
     * The path to an empty and already created folder.
     * 
     * @param clazz
     * @return The test folder dedicated to that specific class.
     */
    public static Path getTestFolderPath(Class<?> clazz)
    {
        try
        {
            Path path = rootFolder.resolve(clazz.getCanonicalName());
            File file = path.toFile();
            if (file.exists())
            {
                if (file.isFile())
                {
                    file.delete();
                }
                if (file.isDirectory())
                {
                    FileUtils.removeRecursive(path, false);
                }
            }
            if (!file.exists())
            {
                file.mkdirs();
            }
            return path;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    @BeforeClass
    public static void cleanFolder()
    throws IOException
    {
        FileUtils.removeRecursive(rootFolder, true);
    }
}
