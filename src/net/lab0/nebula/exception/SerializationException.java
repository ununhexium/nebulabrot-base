package net.lab0.nebula.exception;

public class SerializationException
extends Exception
{

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 1L;

    public SerializationException()
    {
        super();
    }

    public SerializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SerializationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SerializationException(String message)
    {
        super(message);
    }

    public SerializationException(Throwable cause)
    {
        super(cause);
    }
    
}
