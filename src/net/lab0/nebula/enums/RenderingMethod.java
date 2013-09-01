package net.lab0.nebula.enums;

import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.project.Project;

/**
 * Indicates which method the {@link RawMandelbrotData} of a {@link Project} has to use when doing a rendering.
 * 
 * @author 116@lab0.net
 * 
 */
public enum RenderingMethod
{
    LINEAR,
    QUADTREE,
    FILE,
}
