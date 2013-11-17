package net.lab0.nebula.exe;

import java.nio.file.Path;

import net.lab0.nebula.data.MandelbrotQuadTreeNode;
import net.lab0.nebula.mgr.WriterManager;
import net.lab0.tools.exec.PriorityExecutor;

public class QuadTreeNodeWriter extends Writer<MandelbrotQuadTreeNode[]>
{
    public QuadTreeNodeWriter(PriorityExecutor executor, int priority, MandelbrotQuadTreeNode[] data, Path outputPath)
    {
        super(executor, priority, data, outputPath);
    }

    @Override
    protected void save(MandelbrotQuadTreeNode[] data, Path outputPath)
    throws Exception
    {
        WriterManager.getInstance().write(data, outputPath);
    }
    
}
