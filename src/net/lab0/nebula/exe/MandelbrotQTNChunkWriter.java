package net.lab0.nebula.exe;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.CascadingJob;

/**
 * Writes only the given block of nodes to a file and then closes it.
 * 
 * @author 116
 * 
 */
public class MandelbrotQTNChunkWriter
extends Writer<MandelbrotQuadTreeNode[]>
{
    public static long chunkId = 0;
    
    public MandelbrotQTNChunkWriter(CascadingJob<?, MandelbrotQuadTreeNode[]> parentJob, MandelbrotQuadTreeNode[] data,
    Path baseOutputPath, String baseFileName)
    {
        super(parentJob, data, FileSystems.getDefault().getPath(baseOutputPath.toString(),
        baseFileName + "_" + chunkId++ + ".data"));
    }
    
    @Override
    protected void save(MandelbrotQuadTreeNode[] data, Path outputPath)
    throws Exception
    {
        WriterManager.getInstance().write(data, outputPath);
        WriterManager.getInstance().release(outputPath);
    }
    
}
