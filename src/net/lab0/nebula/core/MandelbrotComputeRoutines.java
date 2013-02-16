package net.lab0.nebula.core;

import org.apache.commons.lang3.time.StopWatch;

/**
 * This class contains the usual routines used when computing the Mandelbrot set. The optimX suffix indicate that the method does the computation by groups of X
 * iterations. This has for consequence that the maxIter parameter max be exceeded by at most X-1 iterations. For instance if a point is considered out after 3
 * iterations in a normal function, it will be considered as outside after 4 iterations by a optim4 or optim2 method. It will be considered out after 8
 * iterations by an optim8 method etc. These methods are here to speed up a bit the computation by testing less the outside conditions. The Reference methods
 * are non optimized methods to check the validity of the results.
 * 
 * @since 1.0
 * @author 116@lab0.net
 * 
 */
public class MandelbrotComputeRoutines
{
    /**
     * Method used as a reference to check if optimized methods are valid
     * 
     * @param real
     * @param img
     * @param maxIter
     * @return
     */
    public static boolean isOutsideMandelbrotSetReference(double real, double img, int maxIter)
    {
        double real1 = real;
        double img1 = img;
        double real2, img2;
        
        int iter = 0;
        while ((iter < maxIter) && ((real1 * real1 + img1 * img1) < 4))
        // there is no sqrt in the non optim methods to avoids failures due to rounding errors
        {
            real2 = real1 * real1 - img1 * img1 + real;
            img2 = 2 * real1 * img1 + img;
            
            real1 = real2;
            img1 = img2;
            
            iter++;
        }
        
        return iter < maxIter;
    }
    
    /**
     * Tells is a point can be considered outside of the Mandelbrot set with the given limit.
     * 
     * @param real
     *            The real part coordinate of the point to compute.
     * @param img
     *            The imaginary part coordinate of the point to compute.
     * @param maxIter
     *            The maximum number of iterations to do.
     * @return <code>true</code> if the point is outside the mandelbrot set, <code>false</code> otherwise.
     */
    public static boolean isOutsideMandelbrotSet(double real, double img, int maxIter)
    {
        // optim: compute it and then assign it: avoids 2 multiplications
        double realsqr = real * real;
        double imgsqr = img * img;
        
        double real1 = real;
        double img1 = img;
        double real2, img2;
        
        int iter = 0;
        // optim visible on non optim methods: single & avoids double branching
        while ((iter < maxIter) & ((realsqr + imgsqr) < 4))
        {
            real2 = real1 * real1 - img1 * img1 + real;
            img2 = 2 * real1 * img1 + img;
            
            realsqr = real2 * real2;
            imgsqr = img2 * img2;
            real1 = real2;
            img1 = img2;
            
            iter++;
        }
        
        return iter < maxIter;
    }
    
    /**
     * Tells is a point can be considered outside of the Mandelbrot set with the given limit. Computes by sets of 2 iterations.
     * 
     * @param real
     *            The real part coordinate of the point to compute.
     * @param img
     *            The imaginary part coordinate of the point to compute.
     * @param maxIter
     *            The maximum number of iterations to do.
     * @return <code>true</code> if the point is outside the mandelbrot set, <code>false</code> otherwise.
     */
    public static boolean isOutsideMandelbrotSetOptim2(double real, double img, int maxIter)
    {
        double realsqr = real * real;
        double imgsqr = img * img;
        
        double real1 = real;
        double img1 = img;
        double real2, img2;
        
        int iter = 0;
        while ((iter < maxIter) & ((realsqr + imgsqr) < 4))
        {
            real2 = real1 * real1 - img1 * img1 + real;
            img2 = 2 * real1 * img1 + img;
            
            realsqr = real2 * real2;
            imgsqr = img2 * img2;
            real1 = realsqr - imgsqr + real;
            img1 = 2 * real2 * img2 + img;
            
            iter += 2;
        }
        
        return iter < maxIter;
    }
    
    /**
     * Tells is a point can be considered outside of the Mandelbrot set with the given limit. Computes by sets of 4 iterations.
     * 
     * @param real
     *            The real part coordinate of the point to compute.
     * @param img
     *            The imaginary part coordinate of the point to compute.
     * @param maxIter
     *            The maximum number of iterations to do.
     * @return <code>true</code> if the point is outside the mandelbrot set, <code>false</code> otherwise.
     */
    public static boolean isOutsideMandelbrotSetOptim4(double real, double img, int maxIter)
    {
        double realsqr = real * real;
        double imgsqr = img * img;
        
        double real1 = real;
        double img1 = img;
        double real2, img2;
        
        int iter = 0;
        while ((iter < maxIter) & ((realsqr + imgsqr) < 4))
        {
            real2 = real1 * real1 - img1 * img1 + real;
            img2 = 2 * real1 * img1 + img;
            
            real1 = real2 * real2 - img2 * img2 + real;
            img1 = 2 * real2 * img2 + img;
            
            real2 = real1 * real1 - img1 * img1 + real;
            img2 = 2 * real1 * img1 + img;
            
            realsqr = real2 * real2;
            imgsqr = img2 * img2;
            real1 = realsqr - imgsqr + real;
            img1 = 2 * real2 * img2 + img;
            
            iter += 4;
        }
        
        return iter < maxIter;
    }
    
    public static void main(String[] args)
    {
        int maxIter = 2 << 10;
        int side = 2048;
        double step = 4.0 / side;
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int x = 0; x < side; ++x)
        {
            for (int y = 0; y < side; ++y)
            {
                double real = -2.0 + x * step;
                double img = -2.0 + y * step;
                MandelbrotComputeRoutines.isOutsideMandelbrotSet(real, img, maxIter);
            }
        }
        stopWatch.stop();
        System.out.println("1-Opt:" + stopWatch.toString());
        stopWatch.reset();
        
        stopWatch.start();
        for (int x = 0; x < side; ++x)
        {
            for (int y = 0; y < side; ++y)
            {
                double real = -2.0 + x * step;
                double img = -2.0 + y * step;
                MandelbrotComputeRoutines.isOutsideMandelbrotSetOptim2(real, img, maxIter);
            }
        }
        stopWatch.stop();
        System.out.println("2-Opt:" + stopWatch.toString());
        stopWatch.reset();
        
        stopWatch.start();
        for (int x = 0; x < side; ++x)
        {
            for (int y = 0; y < side; ++y)
            {
                double real = -2.0 + x * step;
                double img = -2.0 + y * step;
                MandelbrotComputeRoutines.isOutsideMandelbrotSetOptim4(real, img, maxIter);
            }
        }
        stopWatch.stop();
        System.out.println("4-Opt:" + stopWatch.toString());
        stopWatch.reset();
    }
}
