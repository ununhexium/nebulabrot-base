package net.lab0.nebula;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.enums.Indexing;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import net.lab0.nebula.listener.ConsoleQuadTreeManagerListener;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class GenerateQuadTrees
{
    public static void main(String[] args)
    throws ValidityException, ClassNotFoundException, NoSuchAlgorithmException, ParsingException, IOException, InvalidBinaryFileException
    {
        Path path = FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "bck", "p256i65536d5D" + 16 + "binNoIndex");
        QuadTreeManager manager = new QuadTreeManager(path, new ConsoleQuadTreeManagerListener());
        
        for (int i = 15; i >= 0; --i)
        {
            System.out.println(i);
            Path p = FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "bck",
            "p256i65536d5D" + i + "binNoIndex");
            manager.getQuadTreeRoot().strip(i);
            manager.saveToBinaryFile(p, Indexing.NO_INDEXING);
        }
    }
}
