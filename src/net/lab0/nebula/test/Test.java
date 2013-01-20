package net.lab0.nebula.test;

import java.io.IOException;
import java.nio.file.FileSystems;

import net.lab0.nebula.core.QuadTreeManager;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import net.lab0.nebula.listener.ConsoleQuadTreeManagerListener;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.commons.lang3.time.StopWatch;

public class Test
{
    public static void main(String[] args)
    throws ValidityException, ParsingException, IOException, ClassNotFoundException, InvalidBinaryFileException
    {
        int id = 44;
        
        StopWatch stopWatch = new StopWatch();
        
        //loading base quad tree from xml
        System.out.println("start");
        stopWatch.start();
        QuadTreeManager managerXml = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "p256i65536d5D16v" + id),
        new ConsoleQuadTreeManagerListener());
        stopWatch.stop();
        System.out.println("XML loading time: " + stopWatch.toString());
        
        // saving
        System.out.println();
        System.out.println("saving as serialized");
        stopWatch.reset();
        stopWatch.start();
        managerXml.saveToSearializedJavaObject(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "serial" + id));
        System.out.println("saved in " + stopWatch.toString());

        System.out.println();
        System.out.println("saving as non indexed custom binary");
        stopWatch.reset();
        stopWatch.start();
        managerXml.saveToBinaryFile(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "binaryNoIndex" + id), false);
        System.out.println("saved in " + stopWatch.toString());

        System.out.println();
        System.out.println("saving as indexed custom binary");
        stopWatch.reset();
        stopWatch.start();
        managerXml.saveToBinaryFile(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "binaryIndex" + id), true);
        System.out.println("saved in " + stopWatch.toString());
        
        //load
        System.out.println();
        System.out.println("reloading as serialized");
        stopWatch.reset();
        stopWatch.start();
        QuadTreeManager managerSerial = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "serial" + id),
        new ConsoleQuadTreeManagerListener());
        stopWatch.stop();
        System.out.println("reloaded in " + stopWatch.toString());

        System.out.println();
        System.out.println("reloading as non indexed custom binary");
        stopWatch.reset();
        stopWatch.start();
        QuadTreeManager managerNoIndex = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "binaryNoIndex" + id),
        new ConsoleQuadTreeManagerListener());
        stopWatch.stop();
        System.out.println("reloaded in " + stopWatch.toString());

        System.out.println();
        System.out.println("reloading as indexed custom binary");
        stopWatch.reset();
        stopWatch.start();
        QuadTreeManager managerIndexed = new QuadTreeManager(FileSystems.getDefault().getPath("F:", "dev", "nebula", "tree", "binaryIndex" + id),
        new ConsoleQuadTreeManagerListener());
        stopWatch.stop();
        System.out.println("reloaded in " + stopWatch.toString());

        System.out.println();
        //display
        System.out.println("root xml      : " + managerXml.getQuadTreeRoot());
        System.out.println("root serial   : " + managerSerial.getQuadTreeRoot());
        System.out.println("root no index : " + managerNoIndex.getQuadTreeRoot());
        System.out.println("root indexed  : " + managerIndexed.getQuadTreeRoot());

        //equivalence test
        boolean same;
        System.out.println();
        System.out.println("compare xml and serial");
        same = managerXml.getQuadTreeRoot().testIsExactlyTheSameAs(managerSerial.getQuadTreeRoot());
        System.out.println("same ? (no)" + same);

        System.out.println();
        System.out.println("compare xml and no index");
        same = managerXml.getQuadTreeRoot().testIsExactlyTheSameAs(managerNoIndex.getQuadTreeRoot());
        System.out.println("same ? (no)" + same);

        System.out.println();
        System.out.println("compare xml and indexed");
        same = managerXml.getQuadTreeRoot().testIsExactlyTheSameAs(managerIndexed.getQuadTreeRoot());
        System.out.println("same ? (no)" + same);

        System.out.println();
        System.out.println("compare serial and no index");
        same = managerSerial.getQuadTreeRoot().testIsExactlyTheSameAs(managerNoIndex.getQuadTreeRoot());
        System.out.println("same ? (yes)" + same);

        System.out.println();
        System.out.println("compare serial and indexed");
        same = managerSerial.getQuadTreeRoot().testIsExactlyTheSameAs(managerIndexed.getQuadTreeRoot());
        System.out.println("same ? (yes)" + same);
        
        //this should be enough to ensure that no index == indexed but we can still check
        System.out.println();
        System.out.println("compare no index and indexed");
        same = managerIndexed.getQuadTreeRoot().testIsExactlyTheSameAs(managerNoIndex.getQuadTreeRoot());
        System.out.println("same ? (yes)" + same);

        System.out.println();
        //the following 3 timer should be about the same duration
        stopWatch.reset();
        stopWatch.start();
        managerSerial.getQuadTreeRoot().updateFields();
        stopWatch.stop();
        System.out.println("updated root serial in " + stopWatch.toString());

        stopWatch.reset();
        stopWatch.start();
        managerNoIndex.getQuadTreeRoot().updateFields();
        stopWatch.stop();
        System.out.println("updated root no index in " + stopWatch.toString());
        
        stopWatch.reset();
        stopWatch.start();
        managerIndexed.getQuadTreeRoot().updateFields();
        stopWatch.stop();
        System.out.println("updated root indexed in " + stopWatch.toString());


        System.out.println();
        System.out.println("compare xml and serial");
        same = managerXml.getQuadTreeRoot().testIsExactlyTheSameAs(managerSerial.getQuadTreeRoot());
        System.out.println("same ? (yes)" + same);

        System.out.println();
        System.out.println("compare xml and no index");
        same = managerXml.getQuadTreeRoot().testIsExactlyTheSameAs(managerNoIndex.getQuadTreeRoot());
        System.out.println("same ? (yes)" + same);

        System.out.println();
        System.out.println("compare xml and indexed");
        same = managerXml.getQuadTreeRoot().testIsExactlyTheSameAs(managerIndexed.getQuadTreeRoot());
        System.out.println("same ? (yes)" + same);
    }
}
