
package net.lab0.nebula;


public enum SaveMode
{
    RECURSIVE("recursive"),
    ONE_FILE("oneFile"), ;
    
    public final String modeName;
    
    private SaveMode(String modeName)
    {
        this.modeName = modeName;
    }
    
}
