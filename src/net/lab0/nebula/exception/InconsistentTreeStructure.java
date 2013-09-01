package net.lab0.nebula.exception;

public class InconsistentTreeStructure
extends RuntimeException
{

    /**
     * Serial UID of the ancient times has come again !
     */
    private static final long serialVersionUID = 1L;

    public InconsistentTreeStructure()
    {
        super();
    }

    public InconsistentTreeStructure(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public InconsistentTreeStructure(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InconsistentTreeStructure(String message)
    {
        super(message);
    }

    public InconsistentTreeStructure(Throwable cause)
    {
        super(cause);
    }
    
}
