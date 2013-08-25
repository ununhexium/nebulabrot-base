package net.lab0.nebula;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.lab0.tools.Triplet;

import org.tukaani.xz.FilterOptions;
import org.tukaani.xz.LZMA2Options;

public class XZWriter
implements Runnable
{
    private BlockingQueue<Triplet<IntBuffer, double[], double[]>> blockingQueue = new ArrayBlockingQueue<>(16);
    private BlockingQueue<Runnable>                               tpeQueue      = new ArrayBlockingQueue<>(16);
    private boolean                                               stopWriter    = false;
    private long                                                  totalIterations;
    private int                                                   maxIteration;
    private int                                                   minIteration;
    private ThreadPoolExecutor                                    threadPoolExecutor;
    private List<OutputStream>                                    outputStreams = new ArrayList<>();
    private List<Boolean>                                         locks         = new ArrayList<>();
    
    public XZWriter(Path path, int threads, long pointsCount, int minIteration, int maxIteration)
    throws IOException
    {
        this.maxIteration = maxIteration;
        this.minIteration = minIteration;
        
        threadPoolExecutor = new ThreadPoolExecutor(threads, threads, 10, TimeUnit.SECONDS, tpeQueue);
        for (int i = 0; i < threads; ++i)
        {
            File file = new File(path.toFile(), "chunck" + i + ".xz");
            FilterOptions[] options = { new LZMA2Options(9) };
//            OutputStream outputStream = new XZOutputStream(new FileOutputStream(file), options, XZ.CHECK_CRC64);
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
            outputStreams.add(outputStream);
            locks.add(false);
            
//            if (i == 0)
//            {
//                byte[] buffer = new byte[8];
//                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
//                byteBuffer.putLong(pointsCount);
//                
//                outputStream.write(buffer);
//            }
        }
    }
    
    public void put(IntBuffer buffer, double[] x, double[] y)
    {
        try
        {
            blockingQueue.put(new Triplet<IntBuffer, double[], double[]>(buffer, x, y));
            System.out.println("Queue length: " + blockingQueue.size());
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void stopWriter()
    {
        stopWriter = true;
    }
    
    @Override
    public void run()
    {
        ArrayList<FutureTask<Long>> futureTasks = new ArrayList<>();
        while (!stopWriter || blockingQueue.size() != 0)
        {
            // get the result of the completed tasks
            Iterator<FutureTask<Long>> it = futureTasks.iterator();
            while (it.hasNext())
            {
                FutureTask<Long> task = it.next();
                if (task.isDone())
                {
                    try
                    {
                        totalIterations += task.get();
                        it.remove();
                    }
                    catch (InterruptedException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    catch (ExecutionException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            
            // do the compression
            try
            {
                // System.out.println("Queue length: " + blockingQueue.size());
                System.out
                .println("TPE Queue length: " + tpeQueue.size() + " - " + threadPoolExecutor.getActiveCount());
                final Triplet<IntBuffer, double[], double[]> triplet = blockingQueue.take();
                
                FutureTask<Long> task = new FutureTask<>(new Callable<Long>()
                {
                    @Override
                    public Long call()
                    throws Exception
                    {
                        byte[] buffer = new byte[4 + 8 + 8];
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        OutputStream writer = getOutputStream();
                        
                        // System.out.println("Outpout to " + writer);
                        
                        long total = 0;
                        for (int i = 0; i < triplet.a.capacity(); ++i)
                        {
                            int iterations = triplet.a.get();
                            totalIterations += iterations;
                            if (iterations > minIteration && iterations < maxIteration)
                            {
                                byteBuffer.clear();
                                byteBuffer.putInt(iterations);
                                byteBuffer.putDouble(triplet.b[i]);
                                byteBuffer.putDouble(triplet.c[i]);
                                writer.write(buffer);
                            }
                        }
                        
                        releaseOutputStream(writer);
                        
                        return total;
                    }
                });
                
                futureTasks.add(task);
                try
                {
                    System.out.println("Execute");
                    threadPoolExecutor.execute(task);
                }
                catch (RejectedExecutionException e)
                {
                    System.out.println("Put");
                    tpeQueue.put(task);
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                continue;
            }
        }
        
        threadPoolExecutor.shutdown();
        try
        {
            while (!threadPoolExecutor.isTerminated() && !threadPoolExecutor.awaitTermination(1, TimeUnit.SECONDS))
            {
                System.out.println("Awaiting termination of " + threadPoolExecutor.getActiveCount() + " threads");
            }
        }
        catch (InterruptedException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        System.out.println("closing streams");
        for (OutputStream outputStream : outputStreams)
        {
            try
            {
                outputStream.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    private synchronized OutputStream getOutputStream()
    {
        for (int i = 0; i < outputStreams.size(); ++i)
        {
            if (!locks.get(i))
            {
                locks.set(i, true);
                return outputStreams.get(i);
            }
        }
        
        return null;
    }
    
    private synchronized void releaseOutputStream(OutputStream outputStream)
    {
        for (int i = 0; i < outputStreams.size(); ++i)
        {
            if (outputStream == outputStreams.get(i))
            {
                locks.set(i, false);
            }
        }
    }
    
    public long getTotalIterations()
    {
        return totalIterations;
    }
    
}
