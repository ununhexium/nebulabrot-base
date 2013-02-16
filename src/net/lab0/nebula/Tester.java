package net.lab0.nebula;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.security.NoSuchAlgorithmException;

import net.lab0.nebula.core.NebulabrotRenderer;
import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.data.DiffReport;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import net.lab0.nebula.listener.ConsoleMandelbrotRendererListener;
import net.lab0.nebula.listener.ConsoleQuadTreeManagerListener;
import net.lab0.tools.geom.Point;
import net.lab0.tools.geom.Rectangle;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class Tester
{
    public static void main(String[] args)
    throws ValidityException, ClassNotFoundException, NoSuchAlgorithmException, ParsingException, IOException, InvalidBinaryFileException
    {
        System.out.println("start");
        NebulabrotRenderer renderer = new NebulabrotRenderer(2048, 2048, new Rectangle(new Point(-2.0, -2.0), new Point(2.0, 2.0)));
        renderer.addMandelbrotRendererListener(new ConsoleMandelbrotRendererListener());
        System.out.println("linear render 1 thread");
        long points = 10_000_000;
//        RawMandelbrotData raw = renderer.linearRender(points, 0, 256);
        int threads = Runtime.getRuntime().availableProcessors()-1;
        System.out.println("linear render " + threads + " threads");
        RawMandelbrotData raw2 = renderer.linearRender(points, 256, 65536, threads);
        
        int depth = 8;
        QuadTreeManager manager = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "bck",
        "p256i65536d5D" + depth + "binNoIndex"), new ConsoleQuadTreeManagerListener());
        
        System.out.println("quadTree render 1 thread");
//        RawMandelbrotData raw3 = renderer.quadTreeRender(points, 0, 256, manager.getQuadTreeRoot());
        
        System.out.println("quad tree render " + threads + " threads");
        RawMandelbrotData raw4 = renderer.quadTreeRender(points, 256, 65536, manager.getQuadTreeRoot(), threads);
        
//        DiffReport report12 = raw.diff(raw2);
//        DiffReport report34 = raw3.diff(raw4);
//        DiffReport report13 = raw.diff(raw3);
        DiffReport report24 = raw2.diff(raw4);
        
//        System.out.println("1-2 diff: " + report12);
//        System.out.println("3-4 diff: " + report34);
//        System.out.println("1-3 diff: " + report13);
        System.out.println("2-4 diff: " + report24);
    }
}
