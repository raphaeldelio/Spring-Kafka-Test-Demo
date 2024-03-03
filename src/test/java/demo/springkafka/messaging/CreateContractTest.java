package demo.springkafka.messaging;

import demo.springkafka.BaseKafkaIntegrationTest;
import demo.springkafka.ContractCreatedEvent;
import demo.springkafka.ContractEvent;
import demo.springkafka.ContractEventType;
import demo.springkafka.repository.ContractRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
class CreateContractTest extends BaseKafkaIntegrationTest {

    @Autowired
    private ContractRepository contractRepository;

    private static final String INPUT_TOPIC = "contract-input";

    @Test
    void shouldCreateContract() {
        // Given : a valid contract event
        var contractEvent = createContractEvent();

        // When : the contract event is sent to the input topic
        kafkaTemplate.executeInTransaction(kafkaTemplate ->
                kafkaTemplate.send(INPUT_TOPIC, contractEvent.getEventId(), contractEvent)
        );

        // Then : the contract is created
        await().untilAsserted(() -> {
            var contract = contractRepository.findById("1");
            assertThat(contract).isPresent();
            assertThat(contract.get().getContractName()).isEqualTo("contract1");
        });
    }

    private ContractEvent createContractEvent() {
        ContractCreatedEvent contractCreatedEvent = ContractCreatedEvent.newBuilder()
                .setContractId("1")
                .setContractName("contract1")
                .build();

        return ContractEvent.newBuilder()
                .setEventId("1")
                .setContractEventType(ContractEventType.ADDED)
                .setEvent(contractCreatedEvent)
                .build();
    }
}