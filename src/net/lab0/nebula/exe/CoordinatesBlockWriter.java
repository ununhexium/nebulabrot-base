package net.lab0.nebula.exe;

import java.nio.file.Path;

import net.lab0.nebula.data.CoordinatesBlock;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.CascadingJob;

public class CoordinatesBlockWriter
extends Writer<CoordinatesBlock[]>
{
    public CoordinatesBlockWriter(CascadingJob<?, CoordinatesBlock[]> parentJob, CoordinatesBlock[] data, Path outputPath,
    WriterManager writerManager)
    {
        super(parentJob, data, outputPath);
    }

    @Override
    protected void save(CoordinatesBlock[] data, Path outputPath)
    throws Exception
    {
        WriterManager.getInstance().write(data, outputPath);
    }
    
}
