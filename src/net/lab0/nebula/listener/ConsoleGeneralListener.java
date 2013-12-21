package net.lab0.nebula.listener;

public class ConsoleGeneralListener
implements GeneralListener
{
    @Override
    public void print(String s)
    {
        System.out.println(s);
    }
}
