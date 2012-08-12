
package net.lab0.nebula.data;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Statistics
{
    private Map<Integer, StatisticsData> statisticsDepth = new HashMap<>();
    
    public StatisticsData getStatisticsDataForDepth(int depth)
    {
        StatisticsData data = this.statisticsDepth.get(depth);
        if (data == null)
        {
            data = new StatisticsData();
            this.statisticsDepth.put(depth, data);
        }
        
        return data;
    }
    
    public int getMaxDepth()
    {
        int max = -1;
        for (Integer i : statisticsDepth.keySet())
        {
//            System.out.println(i);
            if (i > max)
            {
                max = i;
            }
        }
        return max;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        List<Integer> depths = new ArrayList<>(statisticsDepth.keySet());
        Collections.sort(depths);
        
        sb.append("Statitics").append("\n");
        for (Integer i : depths)
        {
            sb.append("Depth ").append(i).append("\n");
            sb.append(statisticsDepth.get(i).toString()).append("\n");
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
}
