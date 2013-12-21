package net.lab0.nebula.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value={
    TestMandelbrotComputeRoutines.class,
    TestOpenClMandelbrotComputeRoutines.class,
    TestQuadTreeSaveLoad.class,
})
public class AllCore
{
    
}
