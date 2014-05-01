package net.lab0.nebula.project;

/**
 * A subset of functionalities of a project.
 */
public class Module
{
    /**
     * The project associated to this module
     */
    protected Project project;
    
    /**
     * @param project
     *            The project this module belongs to.
     * 
     */
    public Module(Project project)
    {
        super();
        this.project = project;
    };
    
}
