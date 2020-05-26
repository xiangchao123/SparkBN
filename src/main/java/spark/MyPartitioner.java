package spark;

import org.apache.commons.collections.CollectionUtils;


import org.apache.spark.Partitioner;
import org.apache.spark.sql.catalyst.expressions.In;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiangchao on 2020/5/24.
 */
public class MyPartitioner extends Partitioner {
    private int partitions;
    private Map<Integer,Integer> map = new ConcurrentHashMap<>();

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }


    public void initMap(int partiton, List<Integer> list) {
        for(int i=0;i<partiton;i++){
            int index = list.get(i);
            map.put(index,index%partiton);
        }
    }

    @Override
    public int numPartitions() {
        return partitions;
    }

    @Override
    public int getPartition(Object key) {
        return map.get(key);
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MyPartitioner) {
            return ((MyPartitioner) obj).partitions == this.partitions;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.partitions;
    }
}
