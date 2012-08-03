package net.lab0.nebula;

import com.ochafik.lang.jnaerator.runtime.Structure;

/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few
 * opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a
 * href="http://jna.dev.java.net/">JNA</a>.
 */
public class MandelbrotQuadTree extends Structure<MandelbrotQuadTree, MandelbrotQuadTree.ByValue, MandelbrotQuadTree.ByReference>
{
    // / C type : MandelbrotQuadTree*
    public MandelbrotQuadTree.ByReference parent;
    // / C type : MandelbrotQuadTree*
    public MandelbrotQuadTree.ByReference children;
    public double                         minX;
    public double                         maxX;
    public double                         minY;
    public double                         maxY;
    /**
     * @see PositionInParent<br>
     *      C type : PositionInParent
     */
    public int                            positionInParent;
    /**
     * @see NodeStatus<br>
     *      C type : NodeStatus
     */
    public int                            status;
    // / makes sense only in the case of "OUTSIDE" nodes
    public int                            minIter;
    public int                            maxIter;
    
    public MandelbrotQuadTree()
    {
        super();
        initFieldOrder();
    }
    
    protected void initFieldOrder()
    {
        setFieldOrder(new String[] { "parent", "children", "minX", "maxX", "minY", "maxY", "positionInParent", "status", "minIter", "maxIter" });
    }
    
    protected ByReference newByReference()
    {
        return new ByReference();
    }
    
    protected ByValue newByValue()
    {
        return new ByValue();
    }
    
    protected MandelbrotQuadTree newInstance()
    {
        return new MandelbrotQuadTree();
    }
    
    public static MandelbrotQuadTree[] newArray(int arrayLength)
    {
        return Structure.newArray(MandelbrotQuadTree.class, arrayLength);
    }
    
    public static class ByReference extends MandelbrotQuadTree implements Structure.ByReference
    {
        
    };
    
    public static class ByValue extends MandelbrotQuadTree implements Structure.ByValue
    {
        
    };
}
