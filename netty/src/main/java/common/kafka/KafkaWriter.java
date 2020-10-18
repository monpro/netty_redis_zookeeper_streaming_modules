package common.kafka;

import com.google.common.base.Preconditions;
import common.PropertiesUtil;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Properties;
public class KafkaWriter {

    private Properties properties;
    private Producer<String, byte[]> producer;

    final static protected Properties DEFAULT_KAFKA_PROPERTIES = new Properties();

    static {
        DEFAULT_KAFKA_PROPERTIES.put("metadata.broker.list", "127.0.0.1:9092");
        DEFAULT_KAFKA_PROPERTIES.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
        //0, which means that the producer never waits for an acknowledgement from the broker (the same behavior as 0.7).
        //1, which means that the producer gets an acknowledgement after the leader replica has received the data.
        //-1, which means that the producer gets an acknowledgement after all in-sync replicas have received the data.
        DEFAULT_KAFKA_PROPERTIES.put("request.required.acks", "1");
        DEFAULT_KAFKA_PROPERTIES.put("partitioner.class", "common.kafka.WriterPartitioner");
        DEFAULT_KAFKA_PROPERTIES.put("key.serializer.class", "kafka.serializer.StringEncoder");
    }

    public KafkaWriter(final String kafkaBroker) {
        Preconditions.checkNotNull(kafkaBroker, "kafka broker is null");

        Properties kafkaProperties = new Properties();
        kafkaProperties.put("metadata.broker.list", kafkaBroker);
        this.properties = PropertiesUtil.newProperties(DEFAULT_KAFKA_PROPERTIES, kafkaProperties);
        this.producer = new Producer<>(new ProducerConfig(this.properties));
    }

    public KafkaWriter(final Properties properties) {
        Preconditions.checkNotNull(properties, "properties is null");

        this.properties = PropertiesUtil.newProperties(DEFAULT_KAFKA_PROPERTIES, properties);
        this.producer = new Producer<>(new ProducerConfig(this.properties));
    }

    public void send(String topic, String key, byte[] message) {
        producer.send(new KeyedMessage<>(topic, key, message));
    }

    public void send(String topic, byte[] message) {
        this.send(topic, null, message);
    }

}
