kernel void mandelbrot(global const double* a, global const double* b, global int* result, int const size, long const maxIter)
{
    const int itemId = get_global_id(0); 
    if(itemId < size)
    {
        long i = 0;
        double real = a[itemId];
        double real1 = real;
        double real2;
        
        double imag = b[itemId];
        double imag1 = imag;
        double imag2;
        
        double realSqr = real * real;
        double imagSqr = imag * imag;
        
        while ((i < maxIter) && ((realSqr + imagSqr) < 4))
        {
            real2 = real1 * real1 - imag1 * imag1 + real;
            imag2 = 2 * real1 * imag1 + imag;
            
            realSqr = real2 * real2;
            imagSqr = imag2 * imag2;
            real1 = realSqr - imagSqr + real;
            imag1 = 2 * real2 * imag2 + imag;
            
            i+=2;
        }
        result[itemId] = i;
    }
}
