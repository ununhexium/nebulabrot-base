package net.lab0.nebula;

import java.nio.file.FileSystems;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.listener.ConsoleQuadTreeManagerListener;

public class Tmp
{
    public static void main(String[] args)
    throws Exception
    {
        QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "render",
        "x32768", "p100000000000m256M65535", "quad14", "tree"), new ConsoleQuadTreeManagerListener());
        
        System.out.println(manager.getQuadTreeRoot().getMaxNodeDepth());
    }
}
