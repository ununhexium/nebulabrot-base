package net.lab0.nebula.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = { TestRawMandelbrotDataSave.class, TestQuadTreeNode.class, TestQuadTreeSaveLoad.class, })
public class All
{
    
}
