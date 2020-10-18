package common.kafka;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

public class WriterPartitioner implements Partitioner {
    /**
     * constructor, avoid java.lang.NoSuchMethodException...(kafka.utils.VerifiableProperties)
     */
    public WriterPartitioner(VerifiableProperties verifiableProperties) {}

    @Override
    public int partition(Object key, int numPartitions) {
        if (key == null) {
            throw new NullPointerException("key is null");
        }
        return Math.abs(hash(key.toString())) % numPartitions;
    }

    /**
     *
     * @param key
     * @return flip to make an even distribution
     */
    private int hash(String key) {
        int hashCode = 0;
        hashCode ^= key.hashCode();
        hashCode ^= (hashCode >>> 20) ^ (hashCode >>> 12);
        return hashCode ^ (hashCode >>> 7) ^ (hashCode >>> 4);
    }
}
