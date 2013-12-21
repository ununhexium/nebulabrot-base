package net.lab0.nebula.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.List;

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

import com.google.common.base.Stopwatch;

/**
 * This class computes Mandelbrot data using double precision floating point numbers on GPUs.
 * 
 * @author 116@lab0.net
 * 
 */
public class OpenClMandelbrotComputeRoutines
{
    // OpenCL variables
    private CLContext      context;
    private CLPlatform     platform;
    private List<CLDevice> devices;
    private CLCommandQueue queue;
    
    private Stopwatch      stopWatch;
    private CLProgram      mandelbrotProgram;
    private CLKernel       mandelbrotKernel;
    private CLMem          xMemory;
    private CLMem          yMemory;
    private CLMem          resultMemory;
    private PointerBuffer  globalWorkSize;
    
    // Used to determine how many units of work to do
    private final int      dimensions = 1;
    
    /**
     * Creates am OpenClMandelbrotComputeRoutines with a default kernel.
     * 
     * @throws LWJGLException
     *             If there is an error during the OpenCL initialization.
     */
    public OpenClMandelbrotComputeRoutines()
    throws LWJGLException
    {
        stopWatch = Stopwatch.createUnstarted();
        
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
    }
    
    /**
     * Finishes the openCL context
     */
    public void teardown()
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
     * @param x
     *            The x points coordinates. x and y must be the same size.
     * @param y
     *            The y points coordinates. x and y must be the same size.
     * @param maximumIteration
     *            The maximum number of iterations to do while computing.
     * @return An {@link IntBuffer} of capacity equal to x.length, containing the number of iterations for each of the given points.
     */
    public synchronized IntBuffer compute(double[] x, double[] y, long maximumIteration)
    {
        if (x.length != y.length)
        {
            throw new RuntimeException("Both x and y must have the same length.");
        }
        
        int blockSize = x.length;
        
        // Create the buffers needed for computation with the maximum allowed size
        DoubleBuffer xBuff = BufferUtils.createDoubleBuffer(blockSize);
        DoubleBuffer yBuff = BufferUtils.createDoubleBuffer(blockSize);
        
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
        mandelbrotKernel.setArg(4, maximumIteration);//TODO:return a long
        
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
     * 
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
                if (br != null)
                {
                    br.close();
                }
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
}