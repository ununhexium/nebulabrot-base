
package net.lab0.nebula.core;


import static org.lwjgl.opencl.CL10.CL_DEVICE_TYPE_GPU;
import static org.lwjgl.opencl.CL10.CL_MEM_COPY_HOST_PTR;
import static org.lwjgl.opencl.CL10.CL_MEM_READ_ONLY;
import static org.lwjgl.opencl.CL10.CL_MEM_WRITE_ONLY;
import static org.lwjgl.opencl.CL10.CL_QUEUE_PROFILING_ENABLE;
import static org.lwjgl.opencl.CL10.clBuildProgram;
import static org.lwjgl.opencl.CL10.clCreateBuffer;
import static org.lwjgl.opencl.CL10.clCreateCommandQueue;
import static org.lwjgl.opencl.CL10.clCreateKernel;
import static org.lwjgl.opencl.CL10.clCreateProgramWithSource;
import static org.lwjgl.opencl.CL10.clEnqueueNDRangeKernel;
import static org.lwjgl.opencl.CL10.clEnqueueReadBuffer;
import static org.lwjgl.opencl.CL10.clEnqueueWriteBuffer;
import static org.lwjgl.opencl.CL10.clFinish;
import static org.lwjgl.opencl.CL10.clReleaseCommandQueue;
import static org.lwjgl.opencl.CL10.clReleaseContext;
import static org.lwjgl.opencl.CL10.clReleaseKernel;
import static org.lwjgl.opencl.CL10.clReleaseProgram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.List;

import net.lab0.nebula.data.QuadTreeNode;
import net.lab0.nebula.data.SynchronizedCounter;
import net.lab0.nebula.enums.Status;
import net.lab0.nebula.exception.NoMoreNodesToCompute;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opencl.Util;


/**
 * 
 * Thread to compute the {@link QuadTreeNode}s' status of the given {@link QuadTreeManager} using OpenCL
 * 
 * @author 116
 * 
 */
public class OpenCLQuadTreeComputeThread
extends AbstractQuadTreeComputeThread
{
    /**
     * @see AbstractQuadTreeComputeThread
     */
    public OpenCLQuadTreeComputeThread(QuadTreeManager quadTreeManager, SynchronizedCounter maxNodesToCompute, SynchronizedCounter computedNodes,
    int computeBlockSize)
    {
        super(quadTreeManager, maxNodesToCompute, computedNodes, computeBlockSize);
    }
    
    @Override
    public void run()
    {
        try
        {
            while (!quadTreeManager.stopRequired() && maxNodesToCompute.isPositive())
            {
                try
                {
                    List<QuadTreeNode> nodes = quadTreeManager.getNextNodeToCompute(quadTreeManager.getMaxDepth(), computeBlockSize);
                    
                    System.out.println("Recieved " + nodes.size() + " nodes");
                    
                    if (!nodes.isEmpty())
                    {
                        long start = System.currentTimeMillis();
                        
                        // initialization
                        System.out.println("Setup");
                        CL.create();
                        CLPlatform platform = CLPlatform.getPlatforms().get(0);
                        List<CLDevice> devices = platform.getDevices(CL_DEVICE_TYPE_GPU);
                        CLContext context = CLContext.create(platform, devices, null, null, null);
                        CLCommandQueue queue = clCreateCommandQueue(context, devices.get(0), CL_QUEUE_PROFILING_ENABLE, null);
                        
                        // program and kernel creation
                        String insideTestSource = readSource(getClass().getResourceAsStream("/cl/insideTest.cl"));
                        CLProgram insideTestProgramm = clCreateProgramWithSource(context, insideTestSource, null);
                        Util.checkCLError(clBuildProgram(insideTestProgramm, devices.get(0), "", null));
                        CLKernel insideTestKernel = clCreateKernel(insideTestProgramm, "insideTest", null);
                        
                        DoubleBuffer minX = BufferUtils.createDoubleBuffer(computeBlockSize);
                        DoubleBuffer maxX = BufferUtils.createDoubleBuffer(computeBlockSize);
                        DoubleBuffer minY = BufferUtils.createDoubleBuffer(computeBlockSize);
                        DoubleBuffer maxY = BufferUtils.createDoubleBuffer(computeBlockSize);
                        IntBuffer statusResult = BufferUtils.createIntBuffer(computeBlockSize);
                        
                        // prepare data
                        minX.rewind();
                        maxX.rewind();
                        maxY.rewind();
                        minY.rewind();
                        
                        for (QuadTreeNode node : nodes)
                        {
                            minX.put(node.minX);
                            maxX.put(node.maxX);
                            minY.put(node.minY);
                            maxY.put(node.maxY);
                        }
                        
                        minX.rewind();
                        maxX.rewind();
                        maxY.rewind();
                        minY.rewind();
                        statusResult.rewind();
                        
                        // allocation
                        CLMem minXMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, minX, null);
                        clEnqueueWriteBuffer(queue, minXMem, 1, 0, minX, null, null);
                        CLMem maxXMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, maxX, null);
                        clEnqueueWriteBuffer(queue, maxXMem, 1, 0, maxX, null, null);
                        CLMem minYMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, minY, null);
                        clEnqueueWriteBuffer(queue, minYMem, 1, 0, minY, null, null);
                        CLMem maxYMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, maxY, null);
                        clEnqueueWriteBuffer(queue, maxYMem, 1, 0, maxY, null, null);
                        CLMem statusMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR, statusResult, null);
                        clFinish(queue);
                        
                        // execution for inside test
                        PointerBuffer kernel1DGlobalWorkSize1 = BufferUtils.createPointerBuffer(1);
                        kernel1DGlobalWorkSize1.put(0, nodes.size());
                        insideTestKernel.setArg(0, minXMem);
                        insideTestKernel.setArg(1, maxXMem);
                        insideTestKernel.setArg(2, minYMem);
                        insideTestKernel.setArg(3, maxYMem);
                        insideTestKernel.setArg(4, statusMem);
                        insideTestKernel.setArg(5, quadTreeManager.getPointsPerSide());
                        insideTestKernel.setArg(6, quadTreeManager.getMaxIter());
                        insideTestKernel.setArg(7, quadTreeManager.getDiffIterLimit());
                        clEnqueueNDRangeKernel(queue, insideTestKernel, 1, null, kernel1DGlobalWorkSize1, null, null, null);
                        
                        // read the results back
                        clEnqueueReadBuffer(queue, statusMem, 1, 0, statusResult, null, null);
                        clFinish(queue);
                        
                        // assigning status result
                        // TODO : optimization : compute only the ones which are not inside
                        for (QuadTreeNode node : nodes)
                        {
                            int status = statusResult.get();
                            if (status == 2)
                            {
                                node.status = Status.INSIDE;
                            }
                        }
                        
                        // program/kernel creation
                        String outsideTestSource = readSource(getClass().getResourceAsStream("/cl/outsideTestPerLine.cl"));
                        CLProgram outsideTestProgramm = clCreateProgramWithSource(context, outsideTestSource, null);
                        Util.checkCLError(clBuildProgram(outsideTestProgramm, devices.get(0), "", null));
                        CLKernel outsideTestKernel = clCreateKernel(outsideTestProgramm, "outsideTest", null);
                        
                        // prepare data
                        minX.rewind();
                        maxX.rewind();
                        minY.rewind();
                        maxY.rewind();
                        
                        for (QuadTreeNode node : nodes)
                        {
                            minX.put(node.minX);
                            maxX.put(node.maxX);
                            minY.put(node.minY);
                            maxY.put(node.maxY);
                        }
                        
                        minX.rewind();
                        maxX.rewind();
                        minY.rewind();
                        maxY.rewind();
                        statusResult.rewind();
                        
                        // allocation
                        minXMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, minX, null);
                        clEnqueueWriteBuffer(queue, minXMem, 1, 0, minX, null, null);
                        maxXMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, maxX, null);
                        clEnqueueWriteBuffer(queue, maxXMem, 1, 0, maxX, null, null);
                        minYMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, minY, null);
                        clEnqueueWriteBuffer(queue, minYMem, 1, 0, minY, null, null);
                        maxYMem = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, maxY, null);
                        clEnqueueWriteBuffer(queue, maxYMem, 1, 0, maxY, null, null);
                        statusMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY | CL_MEM_COPY_HOST_PTR, statusResult, null);
                        clFinish(queue);
                        
                        // execution for outside test
                        PointerBuffer kernel1DGlobalWorkSize2 = BufferUtils.createPointerBuffer(1);
                        kernel1DGlobalWorkSize2.put(0, nodes.size());
                        outsideTestKernel.setArg(0, minXMem);
                        outsideTestKernel.setArg(1, maxXMem);
                        outsideTestKernel.setArg(2, minYMem);
                        outsideTestKernel.setArg(3, maxYMem);
                        outsideTestKernel.setArg(4, statusMem);
                        outsideTestKernel.setArg(5, quadTreeManager.getPointsPerSide());
                        outsideTestKernel.setArg(6, quadTreeManager.getMaxIter());
                        outsideTestKernel.setArg(7, quadTreeManager.getDiffIterLimit());
                        clEnqueueNDRangeKernel(queue, outsideTestKernel, 1, null, kernel1DGlobalWorkSize2, null, null, null);
                        
                        // read the results back
                        clEnqueueReadBuffer(queue, statusMem, 1, 0, statusResult, null, null);
                        clFinish(queue);
                        
                        statusResult.rewind();
                        for (QuadTreeNode node : nodes)
                        {
                            int status = statusResult.get();
                            if (status == 1)
                            {
                                node.status = Status.BROWSED;
                            }
                            else if (status == 3)
                            {
                                node.status = Status.OUTSIDE;
                            }
                            else
                            {
                                System.out.println("Error");
                            }
                        }
                        
                        // teardown
                        System.out.println("Teardown");
                        clReleaseKernel(insideTestKernel);
                        clReleaseProgram(insideTestProgramm);
                        clReleaseKernel(outsideTestKernel);
                        clReleaseProgram(outsideTestProgramm);
                        clReleaseCommandQueue(queue);
                        clReleaseContext(context);
                        CL.destroy();
                        
                        quadTreeManager.computedNodes(nodes.size());
                        maxNodesToCompute.decrement(nodes.size());
                        computedNodes.increment(nodes.size());
                        
                        long end = System.currentTimeMillis();
                        System.out.println("Total time = " + (end - start) + ". Still " + maxNodesToCompute.getValue() + " nodes to compute");
                    }
                    else
                    {
                        try
                        {
                            // System.out.println(Thread.currentThread().getName() + " sleeping");
                            Thread.sleep(1000);
                            // System.out.println(Thread.currentThread().getName() + " resumed");
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                catch (NoMoreNodesToCompute e)
                {
                    System.out.println("Mo more nodes to compute");
                    break;
                }
            }
        }
        catch (LWJGLException | IOException e1)
        {
            e1.printStackTrace();
        }
    }
    
    /**
     * Read a file and returns it as a string
     * 
     * @param in
     *            the stream to read from
     * @return a conversion of this stream to a string
     * @throws IOException
     */
    private static String readSource(InputStream in) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = null;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null)
        {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }
    
    public int getComputeBlockSize()
    {
        return computeBlockSize;
    }
    
    public void setComputeBlockSize(int computeBlockSize)
    {
        this.computeBlockSize = computeBlockSize;
    }
    
    public static void main(String[] args) throws InterruptedException
    {
        long start = System.currentTimeMillis();
        QuadTreeManager manager = new QuadTreeManager(new QuadTreeNode(-2.0, 2.0, -2.0, 2.0), 256, 4096, 5, 10);
        manager.setUseOpenCL(true);
        manager.compute(10);
        long end = System.currentTimeMillis();
        System.out.println("Time : " + (end - start));
        
        start = System.currentTimeMillis();
        manager = new QuadTreeManager(new QuadTreeNode(-2.0, 2.0, -2.0, 2.0), 256, 4096, 5, 10);
        manager.setUseOpenCL(false);
        manager.compute(10);
        end = System.currentTimeMillis();
        System.out.println("Time : " + (end - start));
    }
}
