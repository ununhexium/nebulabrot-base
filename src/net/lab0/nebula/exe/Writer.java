package net.lab0.nebula.exe;

import java.nio.file.Path;

import net.lab0.tools.exec.CascadingJob;

public abstract class Writer<T>
extends CascadingJob<T, Void>
{
    private T    data;
    private Path outputPath;
    
    public Writer(CascadingJob<?, T> parentJob, T data, Path outputPath)
    {
        super(parentJob.getExecutor(), parentJob.getPriority() + 1, null);
        this.data = data;
        this.outputPath = outputPath;
    }
    
    @Override
    public final void executeTask()
    throws Exception
    {
        save(data, outputPath);
    }
    
    protected abstract void save(T data, Path outputPath)
    throws Exception;
    
}
