package net.lab0.nebula.mgr;

import org.lwjgl.LWJGLException;

import net.lab0.nebula.core.OpenClMandelbrotComputeRoutines;

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
    
    public OpenCLManager()
    throws LWJGLException
    {
        super();
    }
}
