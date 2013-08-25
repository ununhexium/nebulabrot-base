package net.lab0.nebula.enums;

public enum TreeSaveMode
{
    /**
     * The XML format is strongly discouraged for performance issues. Only use it to export the data where you are not
     * able to read the binary formet.
     */
    XML_TREE,
    /**
     * Standard way to save the file.
     */
    CUSTOM_BINARY, ;
    
}
