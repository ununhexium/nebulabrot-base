package net.lab0.nebula.exe;

import java.nio.file.Path;

import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.exception.SerializationException;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.CascadingJob;

public class RawMandelbrotDataWriter
extends Writer<RawMandelbrotData>
{
    public RawMandelbrotDataWriter(CascadingJob<?, RawMandelbrotData> parentJob, RawMandelbrotData rawMandelbrotData,
    Path ouputPath, WriterManager writerManager)
    {
        super(parentJob, rawMandelbrotData, ouputPath);
    }
    
    @Override
    protected void save(RawMandelbrotData data, Path outputPath)
    throws SerializationException
    {
        WriterManager.getInstance().write(data, outputPath);
    }
    
}
