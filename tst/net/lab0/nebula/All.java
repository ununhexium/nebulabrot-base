package net.lab0.nebula;

import net.lab0.nebula.core.TestMandelbrotComputeRoutines;
import net.lab0.nebula.core.TestOpenClMandelbrotComputeRoutines;
import net.lab0.nebula.core.TestQuadTreeSaveLoad;
import net.lab0.nebula.core.TestRendering;
import net.lab0.nebula.data.TestQuadTreeNode;
import net.lab0.nebula.data.TestRawMandelbrotData;
import net.lab0.nebula.data.TestStatusQuadTreeNode;
import net.lab0.nebula.mgr.PointsBlockManager;
import net.lab0.nebula.mgr.TestPointsBlockManager;
import net.lab0.nebula.project.TestProject;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(value = { 
    TestMandelbrotComputeRoutines.class,
    TestOpenClMandelbrotComputeRoutines.class,
    TestPointsBlockManager.class,
    TestProject.class, 
    TestQuadTreeNode.class, 
    TestQuadTreeSaveLoad.class,
    TestRawMandelbrotData.class, 
    TestRendering.class,
    TestStatusQuadTreeNode.class,
})
public class All
{
    
}
