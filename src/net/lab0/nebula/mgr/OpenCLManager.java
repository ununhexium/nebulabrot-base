package net.lab0.nebula.mgr;

import net.lab0.nebula.core.OpenClMandelbrotComputeRoutines;

import org.lwjgl.LWJGLException;

public class OpenCLManager
extends OpenClMandelbrotComputeRoutines
{
    private static OpenCLManager instance;
    
    public static synchronized OpenCLManager getInstance()
    {
        if (instance == null)
        {
            try
            {
                instance = new OpenCLManager();
            }
            catch (LWJGLException e)
            {
                e.printStackTrace();
            }
        }
        return instance;
    }
    
    private OpenCLManager()
    throws LWJGLException
    {
        super();
    }
}
