package net.lab0.nebula.project;

import java.nio.file.Path;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class QuadTreeInformation
{
    @XmlElement(name = "maximum_iteration_count")
    private long   maximumIterationCount;
    
    /**
     * The location is a path relative to the project's root folder
     */
    @XmlElement(name = "location")
    private Path location;
}
