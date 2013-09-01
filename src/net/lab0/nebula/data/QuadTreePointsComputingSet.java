package net.lab0.nebula.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.IntBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.lab0.nebula.core.OpenClMandelbrotComputeRoutines;
import net.lab0.nebula.core.XZWriter;
import net.lab0.nebula.enums.Status;
import net.lab0.nebula.project.Project;

import org.lwjgl.LWJGLException;

public class QuadTreePointsComputingSet
{
    private Project             project;
    private int                 blocks;
    private RenderingParameters renderingParameters;
    
    public QuadTreePointsComputingSet(Project project, int blocks, RenderingParameters renderingParameters)
    {
        super();
        this.project = project;
        this.blocks = blocks;
        this.renderingParameters = renderingParameters;
    }
    
    public void computeBlock(int blockIndex)
    {
        if (blockIndex < 0)
        {
            throw new IllegalArgumentException("The block index (" + blockIndex + ") must be >=0");
        }
        if (blockIndex >= blocks)
        {
            throw new IllegalArgumentException("The block index (" + blockIndex + ") must be < this.blocks (" + blocks
            + ")");
        }
        
        if (project.useOpenCL())
        {
            try
            {
                computeWithOpenCL(blockIndex);
            }
            catch (LWJGLException | IOException e)
            {
                // TODO clean try catch
                e.printStackTrace();
            }
        }
    }
    
    private void computeWithOpenCL(int blockIndex)
    throws LWJGLException, IOException
    {
        int blockSize = 1024 * 1024;
        Path path = getPathForBlock(blockIndex);
        OpenClMandelbrotComputeRoutines ocl = new OpenClMandelbrotComputeRoutines();
        
        int threads = 1;
        if (project.useMultithreading())
        {
            threads = Runtime.getRuntime().availableProcessors() - 1;
        }
        
        try
        {
            XZWriter xzWriter = new XZWriter(path, threads, renderingParameters.getMinimumIteration(),
            renderingParameters.getMaximumIteration());
            Thread writerThread = new Thread(xzWriter, "XZ Writer");
            writerThread.start();
            
            double step = 4.0 / Math.sqrt(renderingParameters.getPointsCount());
            
            int depth = (int) (Math.log(4.0 / step) / Math.log(2)) - 4;
            int maxDepth = project.getQuadTreeManager().getQuadTreeRoot().getMaxNodeDepth();
            System.out.println("Depth " + depth);
            checkDepth(depth, maxDepth);
            
            double[] xCoordinates = new double[blockSize];
            double[] yCoordinates = new double[blockSize];
            
            System.gc();
            int currentNode = 0;
            int percentNode = 0;
            int index = 0;
            int maxIter = renderingParameters.getMaximumIteration();
            
            List<StatusQuadTreeNode> nodes = getNodesBlock(blockIndex);
            for (StatusQuadTreeNode node : nodes)
            {
                currentNode++;
                if ((currentNode * 10 / nodes.size()) > percentNode)
                {
                    percentNode = (currentNode * 10 / nodes.size());
                    System.out.println("" + ((float) percentNode * 10.0) + "% - " + currentNode + " - " + index);
                }
                
                // find the first point inside the node
                double xStart = Math.ceil(node.getMinX() / step) * step;
                double yStart = Math.ceil(node.getMinY() / step) * step;
                
                double real = xStart;
                double maxX = node.getMaxX();
                double maxY = node.getMaxY();
                
                while (real < maxX)
                {
                    double img = yStart;
                    while (img < maxY)
                    {
                        xCoordinates[index] = real;
                        yCoordinates[index] = img;
                        index++;
                        
                        if (index == blockSize)
                        {
                            // System.out.println("Block" + passes);
                            final double[] xCtmp = xCoordinates;
                            final double[] yCtmp = yCoordinates;
                            xCoordinates = new double[blockSize];
                            yCoordinates = new double[blockSize];
                            
                            IntBuffer result = ocl.compute(xCtmp, yCtmp, maxIter);
                            result.rewind();
                            
                            xzWriter.put(result, xCtmp, yCtmp);
                            
                            index = 0;
                        }
                        
                        img += step;
                    }
                    
                    real += step;
                }
            }
            
            // compute remaining points
            // optim:do not compute these points -> set out of range
            for (int i = index; i < blockSize; ++i)
            {
                xCoordinates[i] = Double.MAX_VALUE;
                yCoordinates[i] = Double.MAX_VALUE;
            }
            
            final double[] xCtmp = xCoordinates;
            final double[] yCtmp = yCoordinates;
            
            IntBuffer result = ocl.compute(xCtmp, yCtmp, maxIter);
            System.out.println("Ended computation");
            result.rewind();
            
            int[] array = new int[index];
            for (int i = 0; i < index; ++i)
            {
                array[i] = result.get();
            }
            
            xzWriter.put(IntBuffer.wrap(array), xCtmp, yCtmp);
            xzWriter.stopWriter();
            try
            {
                writerThread.join();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            
        }
        finally
        {
            ocl.teardown();
        }
    }
    
    private Path getPathForBlock(int blockIndex)
    {
        return FileSystems.getDefault().getPath(project.getOutputFolder(this).toString(), "blk" + blockIndex);
    }
    
    private void checkDepth(int depth, int maxDepth)
    {
        if (maxDepth > depth)
        {
            System.out.println("Stripping to " + depth);
            project.getQuadTreeManager().getQuadTreeRoot().strip(depth);
        }
        else if (maxDepth < depth)
        {
            System.out.println("Reloading " + depth);
            project.reloadQuadTree();
            System.out.println("Stripping to " + depth);
            project.getQuadTreeManager().getQuadTreeRoot().strip(depth);
        }
    }
    
    private List<StatusQuadTreeNode> getNodesBlock(int block)
    {
        StatusQuadTreeNode root = project.getQuadTreeManager().getQuadTreeRoot();
        
        final List<StatusQuadTreeNode> nodesList = new ArrayList<>();
        root.getLeafNodes(nodesList, Arrays.asList(Status.BROWSED, Status.OUTSIDE, Status.VOID),
        renderingParameters.getMinimumIteration(), renderingParameters.getMaximumIteration());
        
        System.out.println("Node: " + nodesList.size());
        
        long nodesCount = nodesList.size();
        int startNodeIndex = (int) (nodesCount * (long) block / (long) blocks);
        int endNodeIndex = (int) (nodesCount * (long) (block + 1) / (long) blocks);
        
        return nodesList.subList(startNodeIndex, endNodeIndex);
    }
    
    public int getBlocksCount()
    {
        return blocks;
    }
    
    /**
     * Call this method to finish the computing. This will concatenate the per-block computed files.
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void finish()
    throws FileNotFoundException, IOException
    {
        Path concatPath = FileSystems.getDefault().getPath(project.getOutputFolder(this).toString(), "concat.xz");
        OutputStream out = new FileOutputStream(concatPath.toFile());
        byte[] buf = new byte[1024 * 64];
        for (int i = 0; i < blocks; ++i)
        {
            Path inPath = FileSystems.getDefault().getPath(getPathForBlock(i).toString(), "concat.xz");
            InputStream in = new FileInputStream(inPath.toFile());
            int b = 0;
            while ((b = in.read(buf)) >= 0)
            {
                out.write(buf, 0, b);
            }
            in.close();
        }
        out.close();
    }
    
}
