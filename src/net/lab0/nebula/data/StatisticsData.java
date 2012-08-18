
package net.lab0.nebula.data;


import java.util.HashMap;
import java.util.Map;

import net.lab0.nebula.enums.Status;
import net.lab0.tools.NumberBox;


/**
 * Complement of the {@link Statistics} class. Contains the per-depth statistics.
 * 
 * @author 116
 * 
 */
public class StatisticsData
{
    private Map<Status, NumberBox<Double>>  surfaces    = new HashMap<>();
    private Map<Status, NumberBox<Integer>> statusCount = new HashMap<>();
    
    public void addSurface(Status status, double quantity)
    {
        NumberBox<Double> box = surfaces.get(status);
        if (box == null)
        {
            box = new NumberBox<>();
            box.value = 0d;
            surfaces.put(status, box);
        }
        box.value += quantity;
    }
    
    public void addStatusCount(Status status, int quantity)
    {
        NumberBox<Integer> box = statusCount.get(status);
        if (box == null)
        {
            box = new NumberBox<>();
            box.value = 0;
            statusCount.put(status, box);
        }
        box.value += quantity;
    }
    
    public int getCountFor(Status status)
    {
        NumberBox<Integer> box = statusCount.get(status);
        if (box != null)
        {
            return box.value;
        }
        else
        {
            return 0;
        }
    }
    
    public double getSurfaceFor(Status status)
    {
        NumberBox<Double> box = surfaces.get(status);
        if (box != null)
        {
            return box.value;
        }
        else
        {
            return 0d;
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\tSurfaces").append("\n");
        for (Status status : Status.values())
        {
            NumberBox<Double> box = surfaces.get(status);
            if (box != null)
            {
                sb.append("\t\t").append(status.toString()).append(": ").append(box.value).append("\n");
            }
        }
        sb.append("\tCounts").append("\n");
        for (Status status : Status.values())
        {
            NumberBox<Integer> box = statusCount.get(status);
            if (box != null)
            {
                sb.append("\t\t").append(status.toString()).append(": ").append(box.value).append("\n");
            }
        }
        
        return sb.toString();
    }
    
}
