package net.lab0.nebula.exe;

import java.nio.file.Path;

import net.lab0.tools.exec.CascadingJob;
import net.lab0.tools.exec.PriorityExecutor;

public abstract class Writer<T>
extends CascadingJob<T, Void>
{
    private T             data;
    private Path          outputPath;
    
    public Writer(PriorityExecutor executor, int priority, T data, Path outputPath)
    {
        super(executor, priority, null);
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
