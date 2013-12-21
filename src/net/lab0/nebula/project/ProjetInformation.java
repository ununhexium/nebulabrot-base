package net.lab0.nebula.project;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "project")
public class ProjetInformation
{
    public static final int           LAST_VERSION         = 1;
    
    public int                        version              = LAST_VERSION;
    
    public String                     name                 = "";

    public Date                       creationDate;
    
    public final ComputingInformation computingInformation = new ComputingInformation();
    
    public final DataPathInformation dataPathInformation = new DataPathInformation();
}
