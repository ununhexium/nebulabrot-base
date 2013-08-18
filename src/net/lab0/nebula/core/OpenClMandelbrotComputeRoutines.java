package net.lab0.nebula.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.List;

import net.lab0.tools.HumanReadable;

import org.apache.commons.lang3.time.StopWatch;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLMem;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opencl.Util;

/**
 * This class computes Mandelbrot data using double precision floating point numbers on GPUs.
 * 
 * @author 116@lab0.net
 * 
 */
public class OpenClMandelbrotComputeRoutines
{
    // OpenCL variables
    public CLContext      context;
    public CLPlatform     platform;
    public List<CLDevice> devices;
    public CLCommandQueue queue;
    
    private StopWatch     stopWatch;
    private CLProgram     mandelbrotProgram;
    private CLKernel      mandelbrotKernel;
    private DoubleBuffer  xBuff;
    private DoubleBuffer  yBuff;
    private CLMem         xMemory;
    private CLMem         yMemory;
    private CLMem         resultMemory;
    private PointerBuffer globalWorkSize;
    
    // Used to determine how many units of work to do
    private int           blockSize;
    private final int     dimensions = 1;
    
    /**
     * Creates am OpenClMandelbrotComputeRoutines with a default kernel.
     * 
     * @param blockSize
     *            The number of block to compute in each call to the <code>compute()</code> method.
     * @throws LWJGLException
     */
    public OpenClMandelbrotComputeRoutines(int blockSize)
    throws LWJGLException
    {
        this.blockSize = blockSize;
        
        stopWatch = new StopWatch();
        
        initializeCL();
        
        createKernel();
    }
    
    /**
     * Initializes the OpenCL context
     */
    private void initializeCL()
    throws LWJGLException
    {
        stopWatch.start();
        
        IntBuffer errorBuf = BufferUtils.createIntBuffer(1);
        // Create OpenCL
        CL.create();
        // Get the first available platform
        platform = CLPlatform.getPlatforms().get(0);
        // Run our program on the GPU
        devices = platform.getDevices(CL10.CL_DEVICE_TYPE_GPU);
        // Create an OpenCL context, this is where we could create an OpenCL-OpenGL compatible context
        context = CLContext.create(platform, devices, errorBuf);
        // Check for any errors
        Util.checkCLError(errorBuf.get(0));
        // Create a command queue
        queue = CL10.clCreateCommandQueue(context, devices.get(0), CL10.CL_QUEUE_PROFILING_ENABLE, errorBuf);
        // Check for any errors
        Util.checkCLError(errorBuf.get(0));
        
        stopWatch.stop();
        System.out.println("Initialization: " + stopWatch.toString());
        stopWatch.reset();
    }
    
    /**
     * Creates the kernel.
     */
    private void createKernel()
    {
        stopWatch.start();
        mandelbrotProgram = CL10.clCreateProgramWithSource(context, loadText("./cl/mandelbrotOptim2.cl"), null);
        // Build the OpenCL program, store it on the specified device
        int error = CL10.clBuildProgram(mandelbrotProgram, devices.get(0), "", null);
        // Check for any OpenCL errors
        Util.checkCLError(error);
        mandelbrotKernel = CL10.clCreateKernel(mandelbrotProgram, "mandelbrot", null);
        
        stopWatch.stop();
        System.out.println("Kernel creation: " + stopWatch.toString());
        stopWatch.reset();
        
        // Create the buffers needed for computation with the maximum allowed size
        
        xBuff = BufferUtils.createDoubleBuffer(blockSize);
        yBuff = BufferUtils.createDoubleBuffer(blockSize);
    }
    
    /**
     * Finishes the openCL 
     */
    private void teardown()
    {
        stopWatch.start();
        
        // Destroy our kernel and program
        CL10.clReleaseKernel(mandelbrotKernel);
        CL10.clReleaseProgram(mandelbrotProgram);
        
        // Destroy the OpenCL context
        
        // Finish destroying anything we created
        CL10.clReleaseCommandQueue(queue);
        CL10.clReleaseContext(context);
        // And release OpenCL, after this method call we cannot use OpenCL unless we re-initialize it
        CL.destroy();
        
        stopWatch.stop();
        System.out.println("Releasing: " + stopWatch.toString());
        stopWatch.reset();
    }
    
    /**
     * Computes the number of iterations for the given list of points.
     * 
     * @param x The x points coordinates.
     * @param y The y points coordinates.
     * @param maximumIteration The maximum number of iterations to do while computing.
     * @return An {@link IntBuffer} containing the number of iterations for each of the given points.
     */
    public synchronized IntBuffer compute(double[] x, double[] y, int maximumIteration)
    {
        xBuff.rewind();
        xBuff.put(x);
        xBuff.rewind();
        
        yBuff.rewind();
        yBuff.put(y);
        yBuff.rewind();
        
        IntBuffer errorBuff = BufferUtils.createIntBuffer(1);
        
        xMemory = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR, xBuff, errorBuff);
        // Check if the error buffer now contains an error
        Util.checkCLError(errorBuff.get(0));
        yMemory = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR, yBuff, errorBuff);
        
        // Check if the error buffer now contains an error
        Util.checkCLError(errorBuff.get(0));
        
        resultMemory = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY, blockSize * 4, errorBuff);
        // Check for any error creating the memory buffer
        Util.checkCLError(errorBuff.get(0));
        
        // Set the kernel parameters
        mandelbrotKernel.setArg(0, xMemory);
        mandelbrotKernel.setArg(1, yMemory);
        mandelbrotKernel.setArg(2, resultMemory);
        mandelbrotKernel.setArg(3, blockSize);
        mandelbrotKernel.setArg(4, maximumIteration);
        
        // Create a buffer of pointers defining the multi-dimensional size of the number of work units to execute
        globalWorkSize = BufferUtils.createPointerBuffer(dimensions);
        globalWorkSize.put(0, blockSize);
        
        // Run the specified number of work units using our OpenCL program kernel
        CL10.clEnqueueNDRangeKernel(queue, mandelbrotKernel, dimensions, null, globalWorkSize, null, null, null);
        CL10.clFinish(queue);
        // This reads the result memory buffer
        IntBuffer resultBuff = BufferUtils.createIntBuffer(blockSize);
        // We read the buffer in blocking mode so that when the method returns we know that the result buffer is full
        CL10.clEnqueueReadBuffer(queue, resultMemory, CL10.CL_TRUE, 0, resultBuff, null, null);
        
        // Destroy our memory objects
        CL10.clReleaseMemObject(xMemory);
        CL10.clReleaseMemObject(yMemory);
        CL10.clReleaseMemObject(resultMemory);
        
        return resultBuff;
    }
    
    /**
     * Reads a file an converts it to a string.
     * @param name
     * @return The content of the file as a <code>String</code>.
     */
    public String loadText(String name)
    {
        BufferedReader br = null;
        String resultString = null;
        try
        {
            // Get the file containing the OpenCL kernel source code
            File clSourceFile = new File(OpenClMandelbrotComputeRoutines.class.getClassLoader().getResource(name)
            .toURI());
            // Create a buffered file reader for the source file
            br = new BufferedReader(new FileReader(clSourceFile));
            // Read the file's source code line by line and store it in a string builder
            String line = null;
            StringBuilder result = new StringBuilder();
            while ((line = br.readLine()) != null)
            {
                result.append(line);
                result.append("\n");
            }
            // Convert the string builder into a string containing the source code to return
            resultString = result.toString();
        }
        catch (NullPointerException npe)
        {
            // If there is an error finding the file
            System.err.println("Error retrieving OpenCL source file: ");
            npe.printStackTrace();
        }
        catch (URISyntaxException urie)
        {
            // If there is an error converting the file name into a URI
            System.err.println("Error converting file name into URI: ");
            urie.printStackTrace();
        }
        catch (IOException ioe)
        {
            // If there is an IO error while reading the file
            System.err.println("Error reading OpenCL source file: ");
            ioe.printStackTrace();
        }
        finally
        {
            // Finally clean up any open resources
            try
            {
                br.close();
            }
            catch (IOException ex)
            {
                // If there is an error closing the file after we are done reading from it
                System.err.println("Error closing OpenCL source file");
                ex.printStackTrace();
            }
        }
        
        // Return the string read from the OpenCL kernel source code file
        return resultString;
    }
    
    /**
     * playground
     * @param args
     * @throws LWJGLException
     * @throws URISyntaxException
     */
    public static void main(String[] args)
    throws LWJGLException, URISyntaxException
    {
        int size = 4096;
        int blockSize = 1024 * 1024;
        
        OpenClMandelbrotComputeRoutines ocl = new OpenClMandelbrotComputeRoutines(blockSize);
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        long totalIterations = 0;
        double[] xCoordinates = new double[blockSize];
        double[] yCoordinates = new double[blockSize];
        double step = 1.0 / size;
        double yCurrent = -2.0;
        int index = 0;
        int passes = 0;
        int maxIter = 65536;
        for (int y = 0; y < size; ++y)
        {
            yCurrent = -0.5 + step * y;
            for (int x = 0; x < size; ++x)
            {
                yCoordinates[index] = yCurrent;
                xCoordinates[index] = -0.5 + step * x;
                index++;
                if (index == blockSize)
                {
                    System.out.println("Block" + passes);
                    final double[] xCtmp = xCoordinates;
                    final double[] yCtmp = yCoordinates;
                    
                    IntBuffer result = ocl.compute(xCtmp, yCtmp, maxIter);
                    // System.out.println("Ended computation");
                    result.rewind();
                    long total = 0;
                    for (int i = 0; i < result.capacity(); ++i)
                    {
                        total += result.get();
                    }
                    totalIterations += total;
                    
                    index = 0;
                    passes++;
                }
            }
        }
        
        System.out.println("Passes: " + passes);
        
        stopWatch.stop();
        System.out.println("OpenCL computation: " + stopWatch.toString());
        
        long speed = totalIterations / stopWatch.getTime() * 1000; // iterations per second
        System.out.println(HumanReadable.humanReadableNumber(speed, true) + " CL iteration per second");
        
        stopWatch.reset();
        stopWatch.start();
        totalIterations = 0;
        for (int y = 0; y < size; ++y)
        {
            yCurrent = -0.5 + step * y;
            for (int x = 0; x < size; ++x)
            {
                totalIterations += MandelbrotComputeRoutines.computeIterationsCountOptim2(-0.5 + step * x, yCurrent,
                maxIter);
            }
        }
        
        stopWatch.stop();
        System.out.println("CPU computation: " + stopWatch.toString());
        
        speed = totalIterations / stopWatch.getTime() * 1000; // points per second
        System.out.println(HumanReadable.humanReadableNumber(speed, true) + " CPU iteration per second");
        
        ocl.teardown(); // release the resources
    }
}