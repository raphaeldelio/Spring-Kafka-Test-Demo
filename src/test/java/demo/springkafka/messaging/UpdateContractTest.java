package demo.springkafka.messaging;

import demo.springkafka.*;
import demo.springkafka.repository.ContractRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
class UpdateContractTest extends BaseKafkaIntegrationTest {

    @Autowired
    private ContractRepository contractRepository;

    private static final String INPUT_TOPIC = "contract-input";

    private void background() {
        // Given : a contract already exists
        var contractEvent = createContractCreatedEvent("2", "contract1");

        kafkaTemplate.executeInTransaction(kafkaTemplate ->
                kafkaTemplate.send(INPUT_TOPIC, contractEvent.getEventId(), contractEvent)
        );

        await().untilAsserted(() -> {
            var contract = contractRepository.findById("2");
            assertThat(contract).isPresent();
            assertThat(contract.get().getContractName()).isEqualTo("contract1");
        });
    }

    @Test
    void shouldUpdateContract() {
        background();
        // Given : a contract update event
        var contractEvent = createContractUpdatedEvent("2", "contract2");

        // When : the contract update event is sent to the input topic
        kafkaTemplate.executeInTransaction(kafkaTemplate ->
                kafkaTemplate.send(INPUT_TOPIC, contractEvent.getEventId(), contractEvent)
        );

        // Then : the contract is updated
        await().untilAsserted(() -> {
            var contract = contractRepository.findById("2");
            assertThat(contract).isPresent();
            assertThat(contract.get().getContractName()).isEqualTo("contract2");
        });
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

    private ContractEvent createContractUpdatedEvent(String id, String name) {
        ContractUpdatedEvent contractUpdatedEvent = ContractUpdatedEvent.newBuilder()
                .setContractId(id)
                .setContractName(name)
                .build();

        return ContractEvent.newBuilder()
                .setEventId(id)
                .setContractEventType(ContractEventType.UPDATED)
                .setEvent(contractUpdatedEvent)
                .build();
    }
}