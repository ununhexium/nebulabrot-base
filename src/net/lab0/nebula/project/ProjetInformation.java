package net.lab0.nebula.project;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "project")
public class ProjetInformation
{
    private static int LAST_VERSION = 1;
    
    @XmlAttribute(name = "version")
    private int        version      = LAST_VERSION;
    
    @XmlAttribute(name = "name")
    private String     name = "";
    
    public final ComputingInformation computingInformation = null;
    
    
}
