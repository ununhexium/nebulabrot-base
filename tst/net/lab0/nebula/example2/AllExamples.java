package net.lab0.nebula.example2;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Dummy test to run the example classes and check that they don't throw errors
 * 
 * @author 116
 * 
 */
@SuppressWarnings("javadoc")
public class AllExamples
{
    private static final String[] empty = {};
    
    @Test
    @Ignore
    public void testExample01()
    {
        Example01.main(empty);
    }
    
    @Test
    @Ignore
    public void testExample02()
    {
        Example02.main(empty);
    }
    
    @Test
    @Ignore
    public void testExample03()
    throws Exception
    {
        Example03.main(empty);
    }
    
    @Test
    @Ignore
    public void testExample04()
    throws Exception
    {
        Example04.main(empty);
    }
    
    @Test
    @Ignore
    public void testExample05()
    throws Exception
    {
        Example05.main(empty);
    }
}
