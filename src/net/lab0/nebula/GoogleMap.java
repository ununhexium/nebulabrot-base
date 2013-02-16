package net.lab0.nebula;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.security.NoSuchAlgorithmException;

import net.lab0.nebula.color.PowerGrayScaleColorModel;
import net.lab0.nebula.data.RawMandelbrotData;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class GoogleMap
{
    public static void main(String[] args)
    throws ValidityException, NoSuchAlgorithmException, ParsingException, IOException, InvalidBinaryFileException
    {
        System.out.println("Load");
        RawMandelbrotData data = new RawMandelbrotData(FileSystems.getDefault().getPath("F:", "dev", "nebula", "render", "x32768", "p100000000000m256M65535",
        "quad14"));
        
        System.out.println("save");
        data.saveAsTiles(new PowerGrayScaleColorModel(0.5),
        FileSystems.getDefault().getPath("F:", "dev", "nebula", "render", "x32768", "p100000000000m256M65535", "quad14", "tiles").toFile(), 512);
        
        System.out.println("end");
    }
}
