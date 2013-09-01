package net.lab0.nebula.exception;

public class NonEmptyFolderException
extends Exception
{
    
    /**
     * Serial killer UID
     */
    private static final long serialVersionUID = 1L;
    
    public NonEmptyFolderException()
    {
        super();
    }
    
    public NonEmptyFolderException(String message, Throwable cause, boolean enableSuppression,
    boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public NonEmptyFolderException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public NonEmptyFolderException(String message)
    {
        super(message);
    }
    
    public NonEmptyFolderException(Throwable cause)
    {
        super(cause);
    }
    
}
