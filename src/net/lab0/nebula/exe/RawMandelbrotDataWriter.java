package net.lab0.nebula.exe;

import java.nio.file.Path;

import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.exception.SerializationException;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.JobBuilder;
import net.lab0.tools.exec.PriorityExecutor;

public class RawMandelbrotDataWriter
extends Writer<RawMandelbrotData>
{
    private WriterManager writerManager;
    
    public RawMandelbrotDataWriter(PriorityExecutor executor, int priority, JobBuilder<Void> jobBuilder,
    RawMandelbrotData rawMandelbrotData, Path ouputPath, WriterManager writerManager)
    {
        super(executor, priority, jobBuilder, rawMandelbrotData, ouputPath);
        this.writerManager = writerManager;
    }
    
    @Override
    protected void save(RawMandelbrotData data, Path outputPath)
    throws SerializationException
    {
        writerManager.write(data, outputPath);
    }
    
}
