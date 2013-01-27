package net.lab0.nebula.exception;

public class InvalidXmlIndexFile
extends Exception
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public InvalidXmlIndexFile()
    {
        super();
    }
    
    public InvalidXmlIndexFile(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public InvalidXmlIndexFile(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public InvalidXmlIndexFile(String message)
    {
        super(message);
    }
    
    public InvalidXmlIndexFile(Throwable cause)
    {
        super(cause);
    }
    
}
