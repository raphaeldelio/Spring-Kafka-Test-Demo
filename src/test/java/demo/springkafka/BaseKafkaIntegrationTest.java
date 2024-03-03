package demo.springkafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test-kafka")
@EmbeddedKafka(partitions = 1, count = 3)
public class BaseKafkaIntegrationTest extends BaseIntegrationTest {
    @Autowired
    public KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public ConsumerFactory<String, Object> cf;

    public void setUpConsumerTest() {
        kafkaTemplate.setConsumerFactory(cf);
    }
}
