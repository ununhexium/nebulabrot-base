package tries;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.enums.Indexing;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import net.lab0.nebula.listener.ConsoleQuadTreeManagerListener;
import net.lab0.nebula.project.Project;
import nu.xom.ParsingException;

public class CompleteComputation
{
    public static class Constants
    {
        public static int    treeDepth        = 5;
        public static String treeSourceFolder = "R:\\dev\\nebula\\tree\\bin\\p256i65536d5D16binNoIndex";
        public static String macroBaseFolder  = "R:\\dev\\nebula\\hardcoded";
    }
    
    public static void main(String[] args)
    throws Exception
    {
        QuadTreeManager manager = getQuadTree(Constants.treeDepth);
    }
    
    private static QuadTreeManager getQuadTree(int treeDepth)
    throws Exception
    {
        File f = FileSystems.getDefault().getPath(Constants.macroBaseFolder, "trees", "" + treeDepth).toFile();
        if (!f.exists())
        {
            Path p = FileSystems.getDefault().getPath(Constants.treeSourceFolder);
            QuadTreeManager m = new QuadTreeManager(p, new ConsoleQuadTreeManagerListener(), treeDepth);
            m.saveToBinaryFile(f.toPath(), Indexing.NO_INDEXING);
            return getQuadTree(treeDepth);
        }
        else
        {
            QuadTreeManager m = new QuadTreeManager(f.toPath(), new ConsoleQuadTreeManagerListener());
            return m;
        }
    }
}
