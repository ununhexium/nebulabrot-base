package net.lab0.nebula.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.List;

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
 * 
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
    private IntBuffer     errorBuff;
    private DoubleBuffer  aBuff;
    private DoubleBuffer  bBuff;
    private CLMem         aMemory;
    private CLMem         bMemory;
    private CLMem         resultMemory;
    private PointerBuffer globalWorkSize;
    
    public class WorkBlock
    {
        public int     size;
        public int     maximumIteration;
        public float[] xCoordinates;
        public float[] yCoordinates;
        
        public WorkBlock(int size, int maximumIteration, float[] xCoordinates, float[] yCoordinates)
        {
            super();
            this.size = size;
            this.maximumIteration = maximumIteration;
            this.xCoordinates = xCoordinates;
            this.yCoordinates = yCoordinates;
        }        
    }
    
    // Used to determine how many units of work to do
    private final int size       = 1024;
    private final int points     = size * size;
    private final int maxIter    = 65536;
    private final int dimensions = 1;
    
    public OpenClMandelbrotComputeRoutines()
    throws LWJGLException
    {
        stopWatch = new StopWatch();
        
        initializeCL();
        
        createKernel();
    }
    
    private void initializeCL()
    throws LWJGLException
    {
        // Create our OpenCL context to run commands
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
        // Create a command queue
        queue = CL10.clCreateCommandQueue(context, devices.get(0), CL10.CL_QUEUE_PROFILING_ENABLE, errorBuf);
        // Check for any errors
        Util.checkCLError(errorBuf.get(0));
        
        stopWatch.stop();
        System.out.println("Initialization: " + stopWatch.toString());
        stopWatch.reset();
    }
    
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
    
    private void createAndFillBuffers()
    {
        errorBuff = BufferUtils.createIntBuffer(1);
        
        // Create our first array of numbers to add to a second array of numbers
        
        aBuff = BufferUtils.createDoubleBuffer(points);
        bBuff = BufferUtils.createDoubleBuffer(points);
        
        double step = 4.0f / size;
        double x = -2.0f;
        for (int i = 0; i < size; ++i)
        {
            double y = -2.0f;
            for (int j = 0; j < size; ++j)
            {
                aBuff.put(x);
                bBuff.put(y);
                y += step;
            }
            x += step;
        }
        
        aBuff.rewind();
        aMemory = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR, aBuff, errorBuff);
        // Check if the error buffer now contains an error
        Util.checkCLError(errorBuff.get(0));
        
        bBuff.rewind();
        bMemory = CL10.clCreateBuffer(context, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR, bBuff, errorBuff);
        // Check if the error buffer now contains an error
        Util.checkCLError(errorBuff.get(0));
        
        resultMemory = CL10.clCreateBuffer(context, CL10.CL_MEM_READ_ONLY, points * 4, errorBuff);
        // Check for any error creating the memory buffer
        Util.checkCLError(errorBuff.get(0));
        
        // Set the kernel parameters
        mandelbrotKernel.setArg(0, aMemory);
        mandelbrotKernel.setArg(1, bMemory);
        mandelbrotKernel.setArg(2, resultMemory);
        mandelbrotKernel.setArg(3, points);
        mandelbrotKernel.setArg(4, maxIter);
        
        // Create a buffer of pointers defining the multi-dimensional size of the number of work units to execute
        globalWorkSize = BufferUtils.createPointerBuffer(dimensions);
        globalWorkSize.put(0, points);
    }
    
    private void compute()
    {
        // Run the specified number of work units using our OpenCL program kernel
        CL10.clEnqueueNDRangeKernel(queue, mandelbrotKernel, dimensions, null, globalWorkSize, null, null, null);
        CL10.clFinish(queue);
    }
    
    private void readResult()
    {
        // This reads the result memory buffer
        IntBuffer resultBuff = BufferUtils.createIntBuffer(points);
        // We read the buffer in blocking mode so that when the method returns we know that the result buffer is full
        CL10.clEnqueueReadBuffer(queue, resultMemory, CL10.CL_TRUE, 0, resultBuff, null, null);
        
        // Print the values in the result buffer
        // for (int i = 0; i < resultBuff.capacity(); i++)
        // {
        // double real = aBuff.get(i);
        // double img = bBuff.get(i);
        // int result = resultBuff.get(i);
        // int expected = MandelbrotComputeRoutines.computeIterationsCountOptim2(real, img, maxIter);
        // if (result != expected)
        // {
        // System.out.println("result for(" + real + ";" + img + "): " + i + " = " + result + " (" + expected + ")");
        // }
        // }
        // Destroy our memory objects
        CL10.clReleaseMemObject(aMemory);
        CL10.clReleaseMemObject(bMemory);
        CL10.clReleaseMemObject(resultMemory);
    }
    
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
    
    public String loadText(String name)
    {
        if (!name.endsWith(".cl"))
        {
            name += ".cl";
        }
        BufferedReader br = null;
        String resultString = null;
        try
        {
            // Get the file containing the OpenCL kernel source code
            File clSourceFile = new File(OpenClMandelbrotComputeRoutines.class.getClassLoader().getResource(name).toURI());
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
    
    public static void main(String[] args)
    throws LWJGLException, URISyntaxException
    {
        OpenClMandelbrotComputeRoutines ocl = new OpenClMandelbrotComputeRoutines();
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        for (int i = 0; i < 1000; ++i)
        {
            ocl.createAndFillBuffers();
            ocl.compute();
            ocl.readResult();
        }
        
        stopWatch.stop();
        System.out.println("Computation: " + stopWatch.toString());
        stopWatch.reset();
        
        ocl.teardown(); // release the resources
    }
}