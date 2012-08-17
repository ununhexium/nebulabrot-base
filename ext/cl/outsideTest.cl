
int testOutsideMandelbrotSet(double minX, double maxX, double minY, double maxY, int pointsPerSide, int maxIter, int diffIterLimit)
{
	int localMinIter = maxIter + 1;
	int localMaxIter = -1;
	
	double stepX = (maxX - minX) / (double) (pointsPerSide - 1);
	double stepY = (maxY - minY) / (double) (pointsPerSide - 1);
	
	for (int i = 0; i < pointsPerSide; i++)
	{
		double real = minX + (double) i * stepX;
		for (int j = 0; j < pointsPerSide; ++j)
		{
			double img = (minY + (double) j * stepY);
			
			double realsqr = real * real;
			double imgsqr = img * img;
			
			double real1 = real;
			double img1 = img;
			double real2, img2;
			
			int iter = 0;
			while ((iter < maxIter) && ((realsqr + imgsqr) < 4))
			{
				real2 = real1 * real1 - img1 * img1 + real;
				img2 = 2 * real1 * img1 + img;
				
				real1 = real2 * real2 - img2 * img2 + real;
				img1 = 2 * real2 * img2 + img;
				
				realsqr = real2 * real2;
				imgsqr = img2 * img2;
				real1 = realsqr - imgsqr + real;
				img1 = 2 * real2 * img2 + img;
				
				iter += 2;
			}
			
			if (iter < localMinIter)
			{
				localMinIter = iter;
			}
			else if (iter > localMaxIter)
			{
				localMaxIter = iter;
			}
			
			if ((localMaxIter - localMinIter + 1) > diffIterLimit)
			{
				return 1;
			}
		}
	}
	
	return 3;
}


/* for status array : 1 == BROWSED,  3 == OUTSIDE */

kernel void outsideTest(
	global const double *minX, 
	global const double *maxX, 
	global const double *minY, 
	global const double *maxY,
	global int *status,
	const int pointsPerSide,
	const int maxIter,
	const int diffIterLimit)
{
	int gid = get_global_id(0);
	
	status[gid] = testOutsideMandelbrotSet(minX[gid], maxX[gid], minY[gid], maxY[gid], pointsPerSide, maxIter, diffIterLimit); 
}