package net.lab0.nebula;

import net.lab0.nebula.color.AllColor;
import net.lab0.nebula.core.AllCore;
import net.lab0.nebula.data.AllData;
import net.lab0.nebula.exe.AllExe;
import net.lab0.nebula.mgr.AllMrg;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = {
    AllColor.class,
    AllCore.class,
    AllData.class,
    AllExe.class,
    AllMrg.class,
})
public class All
{
    
}
