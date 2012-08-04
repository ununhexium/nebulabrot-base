
package net.lab0.nebula;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lab0.nebula.data.StatisticsData;


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
