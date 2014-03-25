package net.lab0.nebula.enums;

/**
 * @author 116 Enumerates the formats available for the tree saving.
 */
public enum TreeSaveMode
{
    /**
     * The XML format is strongly discouraged for performance issues. Only use it to export the data where you are not
     * able to read the binary format.
     */
    XML_TREE,
    /**
     * Standard way to save the file.
     */
    CUSTOM_BINARY, ;
    
}
