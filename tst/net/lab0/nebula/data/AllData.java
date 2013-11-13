package net.lab0.nebula.data;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value={
    TestQuadTreeNode.class,
    TestRawMandelbrotData.class,
    TestStatusQuadTreeNode.class,
})
public class AllData
{
    
}
