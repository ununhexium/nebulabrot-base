package net.lab0.nebula.project;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import com.google.common.io.ByteStreams;

public class LzmaCompressorThread
extends Thread
{
    private static int id = 0;
    
    private Path       input;
    private Path       output;
    private boolean    deleteOriginalFile;
    
    public LzmaCompressorThread(Path input, Path output, boolean deleteOriginalFile)
    {
        super("LZMA Compressor " + id++);
        this.input = input;
        this.output = output;
        this.deleteOriginalFile = deleteOriginalFile;
    }
    
    @Override
    public void run()
    {
        if (input.toFile().length() != 0)
        {
            try (
                XZOutputStream outputStream = new XZOutputStream(new FileOutputStream(output.toFile()),
                new LZMA2Options(4));
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(input.toFile()));)
            {
                ByteStreams.copy(inputStream, outputStream);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Crap :(", e);
            }
        }
        
        if (deleteOriginalFile)
        {
            input.toFile().delete();
        }
    }
    
}
