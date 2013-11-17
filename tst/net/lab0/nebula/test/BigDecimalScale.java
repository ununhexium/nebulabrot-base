package net.lab0.nebula.test;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalScale
{
    public static void main(String[] args)
    {
        for (int i = 0; i < 32; ++i)
        {
            System.out.println(new BigDecimal(String.valueOf(Math.PI)).setScale(i, RoundingMode.HALF_EVEN));
        }
    }
}
