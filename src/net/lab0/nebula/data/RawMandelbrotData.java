package net.lab0.nebula.data;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import net.lab0.nebula.color.ColorationModel;
import net.lab0.nebula.color.GrayScaleColorModel;
import net.lab0.nebula.color.PointValues;
import net.lab0.nebula.exception.InvalidBinaryFileException;
import net.lab0.tools.MyString;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

/**
 * 
 * Contains the raw computation of a mandelbrot / nebulabrot set.
 * 
 * The values are stored as Java integers (int). The minimum values for pixelWidth and pixelHeight is 1.
 * 
 * @author 116
 * 
 */
public class RawMandelbrotData
{
    private int                   pixelWidth;
    private int                   pixelHeight;
    private int[/* X */][/* Y */] data;
    
    private int                   minIter;
    private int                   maxIter;
    private long                  pointsCount;
    private Map<String, String>   additional = new HashMap<>();
    
    public RawMandelbrotData(int pixelWidth, int pixelHeight, int minIter, int maxIter, long pointsCount)
    {
        super();
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        if (this.pixelWidth < 1 || this.pixelHeight < 1)
        {
            throw new IllegalArgumentException("The minimum allowed for pixel width / height is 1.");
        }
        this.minIter = minIter;
        this.maxIter = maxIter;
        if (this.minIter > this.maxIter)
        {
            throw new IllegalArgumentException("The minimum quantity of iterations can't be greater than the maximum quantity of iterations");
        }
        this.pointsCount = pointsCount;
        data = new int[pixelWidth][pixelHeight];
    }
    
    /**
     * Reads its data from the files in the <code>inputDirectoryPath</code> folder
     */
    public RawMandelbrotData(Path inputDirectoryPath)
    throws ValidityException, ParsingException, IOException, NoSuchAlgorithmException, InvalidBinaryFileException
    {
        // start by parsing the index file
        Builder parser = new Builder();
        Document doc = parser.build(new File(inputDirectoryPath.toFile(), "index.xml"));
        Element indexRoot = doc.getRootElement();
        
        Element serializedFileNode = indexRoot.getFirstChildElement("serializedFile");
        this.pixelWidth = Integer.parseInt(serializedFileNode.getAttributeValue("pixelWidth"));
        this.pixelHeight = Integer.parseInt(serializedFileNode.getAttributeValue("pixelHeight"));
        if (this.pixelWidth < 1 || this.pixelHeight < 1)
        {
            throw new IllegalArgumentException("The minimum allowed for pixel width / height is 1.");
        }
        this.data = new int[pixelWidth][pixelHeight];
        
        // check that this file contains data of int type
        String arrayType = serializedFileNode.getAttributeValue("arrayType");
        if (!arrayType.equals("int"))
        {
            throw new IllegalArgumentException("This class can only read raw data arrays of type int");
        }
        
        // loading infos
        Element informationNode = indexRoot.getFirstChildElement("information");
        this.minIter = Integer.parseInt(informationNode.getAttributeValue("minIter"));
        this.maxIter = Integer.parseInt(informationNode.getAttributeValue("maxIter"));
        if (this.minIter > this.maxIter)
        {
            throw new IllegalArgumentException("The minimum quantity of iterations can't be greater than the maximum quantity of iterations");
        }
        this.pointsCount = Long.parseLong(informationNode.getAttributeValue("pointsCount"));
        
        // loading misc infos
        Element additionalNode = informationNode.getFirstChildElement("additional");
        Elements entries = additionalNode.getChildElements("entry");
        for (int i = 0; i < entries.size(); ++i)
        {
            Element entry = entries.get(i);
            additional.put(entry.getAttributeValue("key"), entry.getAttributeValue("value"));
        }
        
        File rawData = FileSystems.getDefault().getPath(inputDirectoryPath.toString(), serializedFileNode.getAttributeValue("path")).toFile();
        
        Element checksum = serializedFileNode.getFirstChildElement("checksum");
        String algorithm = checksum.getAttributeValue("algorithm");
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        
        // actually read the data
        try (
            FileInputStream fileInputStream = new FileInputStream(rawData);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            DigestInputStream digestInputStream = new DigestInputStream(bufferedInputStream, messageDigest);)
        {
            byte[] buffer = new byte[this.pixelHeight * 4];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            for (int x = 0; x < this.pixelWidth; ++x)
            {
                byteBuffer.clear();
                digestInputStream.read(buffer, 0, pixelHeight * 4);
                for (int y = 0; y < this.pixelHeight; ++y)
                {
                    data[x][y] = byteBuffer.getInt();
                }
            }
            
            // check for file corruption
            String digest = MyString.getHexString(digestInputStream.getMessageDigest().digest());
            if (!digest.equals(checksum.getAttributeValue("value")))
            {
                System.out.println(digest + " VS " + MyString.getHexString(digestInputStream.getMessageDigest().digest()));
                throw new InvalidBinaryFileException("The checksum is incorrect.");
            }
        }
    }
    
    /**
     * Saves the raw data in the given directory
     * 
     * @param outputDirectoryPath
     *            the directory to save to
     * @throws IOException
     * 
     */
    public void save(Path outputDirectoryPath, boolean saveImagePreview)
    throws IOException
    {
        String algorithm = "SHA-512";
        MessageDigest messageDigest = null;
        try
        {
            messageDigest = MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException e)
        {
            // should not happen
            e.printStackTrace();
            return;
        }
        
        File outputFolder = outputDirectoryPath.toFile();
        if (!outputFolder.exists())
        {
            outputFolder.mkdirs();
        }
        
        File indexFile = FileSystems.getDefault().getPath(outputDirectoryPath.toString(), "index.xml").toFile();
        File dataFile = FileSystems.getDefault().getPath(outputDirectoryPath.toString(), "rawData.dat").toFile();
        File previewFile = FileSystems.getDefault().getPath(outputDirectoryPath.toString(), "preview.png").toFile();
        
        try (
            FileOutputStream fileOutputStream = new FileOutputStream(dataFile);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            DigestOutputStream digestOutputStream = new DigestOutputStream(bufferedOutputStream, messageDigest);)
        {
            byte[] buffer = new byte[this.pixelHeight * 4];
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            for (int x = 0; x < this.pixelWidth; ++x)
            {
                byteBuffer.clear();
                for (int y = 0; y < this.pixelHeight; ++y)
                {
                    byteBuffer.putInt(data[x][y]);
                }
                digestOutputStream.write(byteBuffer.array(), 0, byteBuffer.capacity());
            }
            
            Element indexRoot = new Element("index");
            Element serializedFileNode = new Element("serializedFile");
            serializedFileNode.addAttribute(new Attribute("path", "./rawData.dat"));
            serializedFileNode.addAttribute(new Attribute("pixelHeight", Integer.toString(pixelHeight)));
            serializedFileNode.addAttribute(new Attribute("pixelWidth", Integer.toString(pixelWidth)));
            serializedFileNode.addAttribute(new Attribute("arrayType", "int"));
            indexRoot.appendChild(serializedFileNode);
            
            Element information = new Element("information");
            information.addAttribute(new Attribute("minIter", Integer.toString(minIter)));
            information.addAttribute(new Attribute("maxIter", Integer.toString(maxIter)));
            information.addAttribute(new Attribute("pointsCount", Long.toString(pointsCount)));
            
            Element additionnalNode = new Element("additional");
            for (Entry<String, String> e : this.additional.entrySet())
            {
                Element entryNode = new Element("entry");
                entryNode.addAttribute(new Attribute("key", e.getKey()));
                entryNode.addAttribute(new Attribute("value", e.getValue()));
                additionnalNode.appendChild(entryNode);
            }
            
            information.appendChild(additionnalNode);
            indexRoot.appendChild(information);
            
            if (saveImagePreview)
            {
                BufferedImage bufferedImage = this.computeBufferedImage(new GrayScaleColorModel());
                ImageIO.write(bufferedImage, "PNG", previewFile);
                
                Element previewFileNode = new Element("previewFile");
                previewFileNode.addAttribute(new Attribute("path", "./preview.png"));
                indexRoot.appendChild(previewFileNode);
            }
            
            String digest = MyString.getHexString(digestOutputStream.getMessageDigest().digest());
            Element checksum = new Element("checksum");
            checksum.addAttribute(new Attribute("algorithm", algorithm));
            checksum.addAttribute(new Attribute("value", digest));
            serializedFileNode.appendChild(checksum);
            
            Document indexDocument = new Document(indexRoot);
            Serializer indexSerializer = new Serializer(new BufferedOutputStream(new FileOutputStream(indexFile)), "utf-8");
            indexSerializer.setIndent(2);
            indexSerializer.setMaxLength(0);
            indexSerializer.write(indexDocument);
        }
    }
    
    /**
     * 
     * @param colorationModel
     * @return a BufferedImage
     */
    public BufferedImage computeBufferedImage(ColorationModel colorationModel)
    {
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final BufferedImage bufferedImage = gc.createCompatibleImage(pixelWidth, pixelHeight, BufferedImage.TYPE_INT_RGB);
        
        // System.out.println("" + raw.getPixelWidth() + " * " + raw.getPixelHeight());
        int min = data[0][0];
        int max = data[0][0];
        for (int x = 0; x < pixelWidth; ++x)
        {
            for (int y = 0; y < pixelHeight; ++y)
            {
                if (data[x][y] > max)
                {
                    max = data[x][y];
                }
                if (data[x][y] < min)
                {
                    min = data[x][y];
                }
            }
        }
        
        WritableRaster raster = bufferedImage.getRaster();
        float[] fArray = new float[3];
        PointValues value = new PointValues();
        value.minIter = min;
        value.maxIter = max;
        for (int x = 0; x < pixelWidth; ++x)
        {
            for (int y = 0; y < pixelHeight; ++y)
            {
                value.value = data[x][y];
                colorationModel.computeColorForPoint(fArray, value);
                raster.setPixel(x, y, fArray);
            }
        }
        
        return bufferedImage;
    }
    
    public void addAdditionnalInformation(String key, String value)
    {
        this.additional.put(key, value);
    }
    
    /**
     * 
     * @param key
     *            the key to access a piece of information
     * @return the value if the key exists, <code>null</code> otherwise
     */
    public String getAdditionnalInformation(String key)
    {
        return this.additional.get(key);
    }
    
    public int getPixelWidth()
    {
        return pixelWidth;
    }
    
    public int getPixelHeight()
    {
        return pixelHeight;
    }
    
    public int[][] getData()
    {
        return data;
    }
    
    public int getMinIter()
    {
        return minIter;
    }
    
    public int getMaxIter()
    {
        return maxIter;
    }
    
    public long getPointsCount()
    {
        return pointsCount;
    }
    
    public Map<String, String> getComments()
    {
        return additional;
    }
}
