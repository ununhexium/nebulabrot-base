package net.lab0.nebula;

import java.io.IOException;
import java.nio.file.FileSystems;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.listener.ConsoleQuadTreeManagerListener;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class Test
{
    public static void main(String[] args) throws ValidityException, ParsingException, IOException
    {
        System.out.println("start");
        
        QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "p256i65536d5D16v44"),
        new ConsoleQuadTreeManagerListener());
        
        System.out.println("saving");
        
        manager.saveToSearializedJavaObject(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "serial0"));
        
        System.out.println("saved");
    }
}
