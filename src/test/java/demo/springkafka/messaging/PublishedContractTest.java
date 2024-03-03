package demo.springkafka.messaging;

import demo.springkafka.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
class PublishedContractTest extends BaseKafkaIntegrationTest {

    private static final String INPUT_TOPIC = "contract-input";
    private static final String OUTPUT_TOPIC = "contract-output";

    @Test
    void shouldUpdateContract() {
        setUpConsumerTest();

        var contractEvent = createContractCreatedEvent("3", "contract1");
        kafkaTemplate.executeInTransaction(kafkaTemplate ->
                kafkaTemplate.send(INPUT_TOPIC, contractEvent.getEventId(), contractEvent)
        );

        ContractPublishedEvent[] resultContainer = new ContractPublishedEvent[1];
        await().atMost(Duration.ofSeconds(10)).until(() -> {
            ConsumerRecord<String, Object> record = kafkaTemplate.receive(OUTPUT_TOPIC, 0 , 0);
            if (record != null) {
                if (record.value() instanceof ContractPublishedEvent contractPublishedEvent) {
                    if (contractPublishedEvent.getEventId().equals("3")) {
                        resultContainer[0] = contractPublishedEvent;
                    }
                }
            }
            return resultContainer[0] != null;
        });

        assertThat(resultContainer[0]).isNotNull();
        assertThat(resultContainer[0].getEvent().getContractId()).isEqualTo("3");
        assertThat(resultContainer[0].getEvent().getContractName()).isEqualTo("contract1");
    }

    private ContractEvent createContractCreatedEvent(String id, String name) {
        ContractCreatedEvent contractCreatedEvent = ContractCreatedEvent.newBuilder()
                .setContractId(id)
                .setContractName(name)
                .build();

        return ContractEvent.newBuilder()
                .setEventId(id)
                .setContractEventType(ContractEventType.ADDED)
                .setEvent(contractCreatedEvent)
                .build();
    }
}