package net.lab0.nebula.test;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import net.lab0.nebula.example2.ExamplesGlobals;
import net.lab0.tools.HumanReadable;

import com.google.common.base.Stopwatch;

public class BinaryWritePerformanceTest
{
    public static void main(String[] args)
    throws Exception
    {
        dataOutputStreamTest();
        bufferNIOTest();
    }
    
    private static void dataOutputStreamTest()
    throws FileNotFoundException, IOException
    {
        Path basePath = ExamplesGlobals.createClearDirectory(BinaryWritePerformanceTest.class);
        Path path = FileSystems.getDefault().getPath(basePath.toString(), "out1.data");
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(
        path.toFile())));
        
        Stopwatch stopwatch = Stopwatch.createStarted();
        int quantity = 1024 * 1024 * 128;
        for (int i = 0; i < quantity; ++i)
        {
            dataOutputStream.writeDouble(Math.PI);
        }
        dataOutputStream.close();
        stopwatch.stop();
        System.out.println(stopwatch);
        System.out.println("Write rate = "
        + HumanReadable.humanReadableSizeInBytes(quantity / stopwatch.elapsed(TimeUnit.SECONDS)) + "/s");
    }
    
    private static void bufferNIOTest()
    throws FileNotFoundException, IOException
    {
        Path basePath = ExamplesGlobals.createClearDirectory(BinaryWritePerformanceTest.class);
        Path path = FileSystems.getDefault().getPath(basePath.toString(), "out2.data");
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path.toFile()));
        
        Stopwatch stopwatch = Stopwatch.createStarted();
        int quantity = 1024 * 1024;
        byte[] buffer = new byte[Double.SIZE / 8 * quantity];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();
        for (int x = 0; x < 128; ++x)
        {
            doubleBuffer.rewind();
            for (int i = 0; i < quantity; ++i)
            {
                doubleBuffer.put(Math.PI);
                out.write(buffer);
            }
        }
        System.out.println(buffer[257]);
        out.close();
        stopwatch.stop();
        System.out.println(stopwatch);
        System.out.println("Write rate = "
        + HumanReadable.humanReadableSizeInBytes(quantity / stopwatch.elapsed(TimeUnit.SECONDS)) + "/s");
    }
}
