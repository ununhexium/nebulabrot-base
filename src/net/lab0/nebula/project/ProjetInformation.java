package net.lab0.nebula.project;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Meta information about a project. This is the root of all the project's information.
 */
@XmlRootElement(name = "project")
public class ProjetInformation
{
    public static final int           LAST_VERSION         = 1;
    
    public int                        version              = LAST_VERSION;
    
    public String                     name                 = "";
    
    public Date                       creationDate;
    
    public final ComputingInformation computingInformation = new ComputingInformation();
    
    public final QuadTreesInformation quadTreesInformation = new QuadTreesInformation();
    
    public final PointsInformation    pointsInformation    = new PointsInformation();
}
