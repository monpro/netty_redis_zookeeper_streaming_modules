package common.kafka;


import com.google.common.base.Preconditions;
import common.PropertiesUtil;
import kafka.consumer.*;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class KafkaReader {
    private static final Logger logger = LoggerFactory.getLogger(KafkaReader.class);

    protected Properties properties;
    protected String topic;
    protected ConsumerConnector consumerConnector;

    private ConsumerIterator<byte[], byte[]> consumerIterator;
    final static protected Properties DEFAULT_KAFKA_PROPERTIES = new Properties();

    static {
        DEFAULT_KAFKA_PROPERTIES.put("zookeeper.connect", "127.0.0.1:2181");
        DEFAULT_KAFKA_PROPERTIES.put("zookeeper.session.timeout.ms", "4000");
        DEFAULT_KAFKA_PROPERTIES.put("zookeeper.sync.time.ms", "2000");
        DEFAULT_KAFKA_PROPERTIES.put("auto.commit.interval.ms", "1000");
        DEFAULT_KAFKA_PROPERTIES.put("auto.offset.reset", "largest");
    }

    public KafkaReader(final String zookeeperConnect, String groupId, String offsetReset, String topic) {
        Preconditions.checkNotNull(zookeeperConnect, "zookeeper connection string is null");
        Preconditions.checkNotNull(groupId, "group id is null");
        Preconditions.checkNotNull(topic, "topic is null");
        Preconditions.checkNotNull(offsetReset, "offset reset is null");

        Properties kafkaProperties = new Properties();
        kafkaProperties.setProperty("zookeeper.connect", zookeeperConnect);
        kafkaProperties.setProperty("group.id", groupId);
        kafkaProperties.setProperty("auto.offset.reset", offsetReset);

        this.properties = PropertiesUtil.newProperties(DEFAULT_KAFKA_PROPERTIES, kafkaProperties);
        this.topic = topic;
        this.consumerConnector = Consumer.createJavaConsumerConnector(new ConsumerConfig(this.properties));
        createKafkaStream();
    }

    public KafkaReader(Properties properties, String topic) {
        Preconditions.checkNotNull(properties, "properties is null");
        Preconditions.checkNotNull(topic, "topic is null");

        this.properties = PropertiesUtil.newProperties(DEFAULT_KAFKA_PROPERTIES, properties);
        this.topic = topic;
        this.consumerConnector = Consumer.createJavaConsumerConnector(new ConsumerConfig(this.properties));
        createKafkaStream();
    }

    private void createKafkaStream() {
        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, 1);
        // use one thread to preserve event order.
        logger.info(String.format("init kafkaStream with topic[%s]", topic));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector.createMessageStreams(topicCountMap);
        KafkaStream<byte[], byte[]> kafkaStream = consumerMap.get(topic).get(0);
        this.consumerIterator = kafkaStream.iterator();
    }

    public boolean hasNext() {
        try {
            consumerIterator.hasNext();
            return true;
        } catch (ConsumerTimeoutException e) {
            return false;
        } catch (Exception e) {
            logger.error("kafka receiver exception: " + e);
            throw e;
        }
    }

    public byte[] next() {
        MessageAndMetadata<byte[], byte[]> msg = consumerIterator.next();
        return msg.message();
    }
}