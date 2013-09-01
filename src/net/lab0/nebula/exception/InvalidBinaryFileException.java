package net.lab0.nebula.exception;

public class InvalidBinaryFileException
extends Exception
{
    
    /**
     * Beware you fools.
     */
    private static final long serialVersionUID = 1L;
    
    public InvalidBinaryFileException()
    {
        super();
    }
    
    public InvalidBinaryFileException(String message, Throwable cause, boolean enableSuppression,
    boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public InvalidBinaryFileException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public InvalidBinaryFileException(String message)
    {
        super(message);
    }
    
    public InvalidBinaryFileException(Throwable cause)
    {
        super(cause);
    }
    
}
