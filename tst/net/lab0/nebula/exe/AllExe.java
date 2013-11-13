package net.lab0.nebula.exe;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value={
    TestLinearPointsBlockGenerator.class,
    TestPointsBlockCPUIterationComputing.class,
    TestPointsBlockReader.class,
})
public class AllExe
{
    
}
