package net.lab0.nebula;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.security.NoSuchAlgorithmException;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import net.lab0.nebula.listener.ConsoleQuadTreeManagerListener;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class CleanData
{
    public static void main(String[] args)
    throws ValidityException, ClassNotFoundException, NoSuchAlgorithmException, ParsingException, IOException, InvalidBinaryFileException
    {
        // QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "p256i65536d5D16v436"),
        // new ConsoleQuadTreeManagerListener());
        
//        QuadTreeManager manager2 = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "p256i65536d5D16binNoIndex"),
//        new ConsoleQuadTreeManagerListener());
//        QuadTreeManager manager3 = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "p256i65536d5D16binIndexed"),
//        new ConsoleQuadTreeManagerListener());
        QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "p256i65536d5D16XML"),
        new ConsoleQuadTreeManagerListener());
    }
}
