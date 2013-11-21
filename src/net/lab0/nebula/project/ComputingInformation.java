package net.lab0.nebula.project;

import javax.xml.bind.annotation.XmlAttribute;

public class ComputingInformation
{
    @XmlAttribute(name = "use_opencl")
    private boolean useOpenCL      = false;
    
    @XmlAttribute(name = "max_thread_count")
    private int     maxThreadCount = 1;
    
    /**
     * 
     * @return <code>true</code> if this project should use OpenCL for computation
     */
    public boolean useOpenCL()
    {
        return useOpenCL;
    }
    
    /**
     * Enables OpenCL in computation where available.
     */
    public void enableOpenCL()
    {
        this.useOpenCL = true;
    }
    
    /**
     * Disables OpenCL in computation where available.
     */
    public void disableOpenCL()
    {
        this.useOpenCL = false;
    }
    
    public int getMaxThreadCount()
    {
        return maxThreadCount;
    }
    
    public void setMaxThreadCount(int maxThreadCount)
    {
        this.maxThreadCount = maxThreadCount;
    }
    
}
