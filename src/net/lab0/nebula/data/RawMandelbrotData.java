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
    
    private long                  pointsCount;
    private Map<String, String>   additional = new HashMap<>();
    
    /**
     * Creates a raw mandelbrot data store with the given parameters
     * 
     * @param pixelWidth
     * @param pixelHeight
     * @param minIter
     * @param maxIter
     * @param pointsCount
     */
    public RawMandelbrotData(int pixelWidth, int pixelHeight, long pointsCount)
    {
        super();
        this.pixelWidth = pixelWidth;
        this.pixelHeight = pixelHeight;
        if (this.pixelWidth < 1 || this.pixelHeight < 1)
        {
            throw new IllegalArgumentException("The minimum allowed for pixel width / height is 1.");
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
    public void save(Path outputDirectoryPath)
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
        File previewFileFolder = FileSystems.getDefault().getPath(outputDirectoryPath.toString(), "preview.png").toFile();
        
        try (
            FileOutputStream fileOutputStream = new FileOutputStream(dataFile);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            DigestOutputStream digestOutputStream = new DigestOutputStream(bufferedOutputStream, messageDigest);)
        {
            // stream all the data
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
            
            // creation of the index file
            Element indexRoot = new Element("index");
            Element serializedFileNode = new Element("serializedFile");
            serializedFileNode.addAttribute(new Attribute("path", "./rawData.dat"));
            serializedFileNode.addAttribute(new Attribute("pixelHeight", Integer.toString(pixelHeight)));
            serializedFileNode.addAttribute(new Attribute("pixelWidth", Integer.toString(pixelWidth)));
            serializedFileNode.addAttribute(new Attribute("arrayType", "int"));
            indexRoot.appendChild(serializedFileNode);
            
            // information in the index
            Element information = new Element("information");
            information.addAttribute(new Attribute("pointsCount", Long.toString(pointsCount)));
            
            // additional information in the index
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
            
            // creation of the digest in the index
            String digest = MyString.getHexString(digestOutputStream.getMessageDigest().digest());
            Element checksum = new Element("checksum");
            checksum.addAttribute(new Attribute("algorithm", algorithm));
            checksum.addAttribute(new Attribute("value", digest));
            serializedFileNode.appendChild(checksum);
            
            // saving the index
            Document indexDocument = new Document(indexRoot);
            Serializer indexSerializer = new Serializer(new BufferedOutputStream(new FileOutputStream(indexFile)), "utf-8");
            indexSerializer.setIndent(2);
            indexSerializer.setMaxLength(0);
            indexSerializer.write(indexDocument);
        }
    }
    
    /**
     * Saves the <code>data</code> of this RawMandelbrotData to a folder hierarchy. These data will be saved as tiles to use in google maps API for instance.
     * 
     * @param colorationModel
     *            The coloration model to use in the conversion.
     * @param tilesFolder
     *            The folder to save the tiles to.
     * @param tilesSize
     *            The size of the tiles.
     * 
     * @throws IOException
     */
    public void saveAsTiles(ColorationModel colorationModel, File tilesFolder, int tilesSize)
    throws IOException
    {
        // find the number of zoom levels required
        int zoomLevels = 0;
        while (this.pixelHeight / Math.pow(2.0, zoomLevels) > tilesSize || this.pixelWidth / Math.pow(2.0, zoomLevels) > tilesSize)
        {
            zoomLevels++;
        }
        
        for (int zoom = zoomLevels; zoom >= 0; --zoom)
        {
            System.out.println("Tiles for zoom " + zoom);
            computeTileForZoom(colorationModel, new File(tilesFolder, Integer.toString(zoomLevels - zoom)), tilesSize, zoom);
        }
    }
    
    /**
     * Submethod for tiles computation.
     * 
     * @param colorationModel
     * @param tilesFolder
     * @param tilesSize
     * @param zoom
     * @throws IOException
     */
    private void computeTileForZoom(ColorationModel colorationModel, File tilesFolder, int tilesSize, int zoom)
    throws IOException
    {
        int zoomFactor = (1 << zoom);
        int xTilesCount = data.length / (tilesSize * zoomFactor);
        int yTilesCount = data[0].length / (tilesSize * zoomFactor);
        
        if (!tilesFolder.exists())
        {
            tilesFolder.mkdirs();
        }
        
        // if it requires a non integer amount of tiles
        if (data.length % (tilesSize * zoomFactor) != 0)
        {
            xTilesCount++;
        }
        if (data[0].length % (tilesSize * zoomFactor) != 0)
        {
            yTilesCount++;
        }
        
        long min = data[0][0];
        long max = data[0][0];
        for (int x = 0; x < pixelWidth; x += zoomFactor)
        {
            for (int y = 0; y < pixelHeight; y += zoomFactor)
            {
                long value = 0;
                
                int xLimit2 = Math.min(x + zoomFactor, pixelWidth);
                int yLimit2 = Math.min(y + zoomFactor, pixelHeight);
                for (int x2 = x; x2 < xLimit2; ++x2)
                {
                    for (int y2 = y; y2 < yLimit2; ++y2)
                    {
                        value += data[x2][y2];
                    }
                }
                
                if (value > max)
                {
                    max = value;
                }
                if (value < min)
                {
                    min = value;
                }
            }
        }
        
        for (int xTile = 0; xTile < xTilesCount; ++xTile)
        {
            for (int yTile = 0; yTile < yTilesCount; ++yTile)
            {
                System.out.println("Tile " + (xTile * yTilesCount + yTile) + "/" + (xTilesCount * yTilesCount));
                computeTile(colorationModel, tilesFolder, tilesSize, min, max, xTile, yTile, zoom);
            }
        }
    }
    
    /**
     * Sub sub methd for tiles computaion.
     * 
     * @param colorationModel
     * @param tilesFolder
     * @param tilesSize
     * @param min
     * @param max
     * @param xTile
     * @param yTile
     * @param zoom
     * @throws IOException
     */
    private void computeTile(ColorationModel colorationModel, File tilesFolder, int tilesSize, long min, long max, int xTile, int yTile, int zoom)
    throws IOException
    {
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final BufferedImage bufferedImage = gc.createCompatibleImage(tilesSize, tilesSize, BufferedImage.TYPE_INT_RGB);
        
        int zoomFactor = (1 << zoom);
        WritableRaster raster = bufferedImage.getRaster();
        float[] fArray = new float[3];
        PointValues value = new PointValues();
        value.minIter = min;
        value.maxIter = max;
        int xOrigin = xTile * tilesSize * zoomFactor;
        int yOrigin = yTile * tilesSize * zoomFactor;
        
        int xLimit1 = Math.min((xTile + 1) * tilesSize * zoomFactor, pixelWidth);
        int yLimit1 = Math.min((yTile + 1) * tilesSize * zoomFactor, pixelHeight);
        
        System.out.println("tileCoords " + xOrigin + "/" + yOrigin);
        for (int x = xOrigin; x < xLimit1; x += (zoomFactor))
        {
            for (int y = yOrigin; y < yLimit1; y += (zoomFactor))
            {
                value.value = 0;
                
                int xLimit2 = Math.min(x + zoomFactor, pixelWidth);
                int yLimit2 = Math.min(y + zoomFactor, pixelHeight);
                for (int subX = x; subX < xLimit2; subX++)
                {
                    for (int subY = y; subY < yLimit2; subY++)
                    {
                        value.value += data[subX][subY];
                    }
                }
                
                colorationModel.computeColorForPoint(fArray, value);
                raster.setPixel((x - xOrigin) / zoomFactor, (y - yOrigin) / zoomFactor, fArray);
            }
        }
        
        ImageIO.write(bufferedImage, "PNG", new File(tilesFolder, "tile-" + xTile + "-" + yTile + ".png"));
    }
    
    /**
     * Computes a {@link BufferedImage} from this RawMandelbrotData.
     * 
     * @param colorationModel
     *            The coloration model to use in the conversion.
     * 
     * @return a BufferedImage The data converted into a BufferedImage
     */
    public BufferedImage computeBufferedImage(ColorationModel colorationModel, int zoomOut)
    {
        if (zoomOut < 1)
        {
            throw new IllegalArgumentException("zoomOut must be positive.");
        }
        int zoomFactor = (1 << (zoomOut - 1));
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        final BufferedImage bufferedImage = gc.createCompatibleImage(pixelWidth / zoomFactor, pixelHeight / zoomFactor, BufferedImage.TYPE_INT_RGB);
        
        System.out.println("img size " + pixelWidth / zoomFactor + "/" + pixelHeight / zoomFactor);
        System.out.println("zoom factor " + zoomFactor + " zoomOut " + zoomOut);
        // System.out.println("" + raw.getPixelWidth() + " * " + raw.getPixelHeight());
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        
        for (int x = 0; x < pixelWidth; x += (zoomFactor))
        {
            for (int y = 0; y < pixelHeight; y += (zoomFactor))
            {
                long total = 0;
                
                int xLimit2 = Math.min(x + zoomFactor, pixelWidth);
                int yLimit2 = Math.min(y + zoomFactor, pixelHeight);
                for (int subX = x; subX < xLimit2; subX++)
                {
                    for (int subY = y; subY < yLimit2; subY++)
                    {
                        total += data[subX][subY];
                    }
                }
                
                if (total > max)
                {
                    max = total;
                }
                else if (total < min)
                {
                    min = total;
                }
            }
        }
        
        WritableRaster raster = bufferedImage.getRaster();
        float[] fArray = new float[3];
        PointValues value = new PointValues();
        value.minIter = min;
        value.maxIter = max;
        
        for (int x = 0; x < pixelWidth; x += (zoomFactor))
        {
            for (int y = 0; y < pixelHeight; y += (zoomFactor))
            {
                value.value = 0;
                
                int xLimit2 = Math.min(x + zoomFactor, pixelWidth);
                int yLimit2 = Math.min(y + zoomFactor, pixelHeight);
                for (int subX = x; subX < xLimit2; subX++)
                {
                    for (int subY = y; subY < yLimit2; subY++)
                    {
                        value.value += data[subX][subY];
                    }
                }
                
                colorationModel.computeColorForPoint(fArray, value);
                raster.setPixel(x / zoomFactor, y / zoomFactor, fArray);
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
        int minIter = Integer.MAX_VALUE;
        for (int x = 0; x < this.pixelWidth; ++x)
        {
            for (int y = 0; y < this.pixelHeight; ++y)
            {
                if (data[x][y] < minIter)
                {
                    minIter = data[x][y];
                }
            }
        }
        return minIter;
    }
    
    public int getMaxIter()
    {
        int maxIter = Integer.MIN_VALUE;
        for (int x = 0; x < this.pixelWidth; ++x)
        {
            for (int y = 0; y < this.pixelHeight; ++y)
            {
                if (data[x][y] > maxIter)
                {
                    maxIter = data[x][y];
                }
            }
        }
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
    
    public DiffReport diff(RawMandelbrotData raw2)
    {
        if (this.getPixelHeight() != raw2.getPixelHeight() || this.getPixelWidth() != raw2.getPixelWidth())
        {
            throw new IllegalArgumentException("The two raws must have the same size");
        }
        
        long total = 0;
        long total2 = 0;
        int maxDifference = 0;
        long totalDifference = 0;
        long differences = 0;
        int[][] data = this.getData();
        int[][] data2 = raw2.getData();
        for (int x = 0; x < this.getPixelWidth(); ++x)
        {
            for (int y = 0; y < this.getPixelHeight(); ++y)
            {
                total += data[x][y];
                total2 += data2[x][y];
                if (data[x][y] != data2[x][y])
                {
                    int diff = Math.abs(data[x][y] - data2[x][y]);
                    totalDifference += diff;
                    differences++;
                    if (diff > maxDifference)
                    {
                        maxDifference = diff;
                    }
                }
            }
        }
        
        return new DiffReport(total, total2, maxDifference, totalDifference, differences);
    }
}
