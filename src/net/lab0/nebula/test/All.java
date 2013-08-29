package net.lab0.nebula.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = { 
    TestMandelbrotComputeRoutines.class,
    TestProject.class, 
    TestQuadTreeNode.class, 
    TestQuadTreeSaveLoad.class,
    TestRawMandelbrotData.class, 
    TestStatusQuadTreeNode.class,
})
public class All
{
    
}
