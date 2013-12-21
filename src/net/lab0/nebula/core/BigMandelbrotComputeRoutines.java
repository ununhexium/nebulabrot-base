package net.lab0.nebula.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This class contains the usual routines used when computing the Mandelbrot set. Uses {@link BigDecimal} to do the
 * computation.
 * 
 * @since 1.0
 * @author 116@lab0.net
 * 
 */
public class BigMandelbrotComputeRoutines
{
    
    /**
     * Computes the number of iterations needed to determine if the points is outside the mandelbrot set.
     * 
     * 
     * @param real
     *            The real part coordinate of the point to compute.
     * @param img
     *            The imaginary part coordinate of the point to compute.
     * @param maxIter
     *            The maximum number of iterations to do.
     * @param precision
     *            The precision used for the scale. (Example value: float32->7, float64->16, float128->34)
     * @return the number of computed iterations.
     */
    public static long computeIterationsCount(BigDecimal real, BigDecimal img, long maxIter, int precision)
    {
        BigDecimal limit = new BigDecimal("4.0");
        BigDecimal two = new BigDecimal("2.0");
        
        BigDecimal real1 = real;
        BigDecimal img1 = img;
        BigDecimal realSqr = real1.multiply(real1);
        BigDecimal imgSqr = img1.multiply(img1);
        BigDecimal real2, img2;
        
        long iter = 0;
        while ((iter < maxIter) && ((realSqr.add(imgSqr)).compareTo(limit) < 0))
        {
            // real2 = realSqr - imgSqr + real;
            real2 = realSqr.subtract(imgSqr).add(real);
            // img2 = 2 * real1 * img1 + img;
            img2 = two.multiply(real1).multiply(img1).add(img);
            // realSqr = real2 * real2
            realSqr = real2.multiply(real2);
            // imagSqr = img2 * img2
            imgSqr = img2.multiply(img2);
            
            real1 = real2.setScale(precision, RoundingMode.HALF_EVEN);
            img1 = img2.setScale(precision, RoundingMode.HALF_EVEN);
            
            iter++;
        }
        
        return iter;
    }
    
}
