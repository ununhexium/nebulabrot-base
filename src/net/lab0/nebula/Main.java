package net.lab0.nebula;
public class Main
{
    public static void main(String[] args)
    {
        System.out.println("Start");
        MandelbrotQuadTree tree = MandelbrotLibrary.INSTANCE.exec(100, 512, 5, 3, -2.0, 2.0, -2.0, 2.0);
        System.out.println(tree.children);
        System.out.println("End");
    }
}
