package net.lab0.nebula.exception;

public class NoMoreNodesToCompute
extends Exception
{

    /**
     * Who are you ?
     */
    private static final long serialVersionUID = 1L;

    public NoMoreNodesToCompute()
    {
        super();
    }

    public NoMoreNodesToCompute(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NoMoreNodesToCompute(String message, Throwable cause)
    {
        super(message, cause);
    }

    public NoMoreNodesToCompute(String message)
    {
        super(message);
    }

    public NoMoreNodesToCompute(Throwable cause)
    {
        super(cause);
    }
    
    
}
