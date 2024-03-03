package demo.springkafka.messaging;

import demo.springkafka.*;
import demo.springkafka.model.Contract;
import demo.springkafka.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ContractMessagingService {

    private final ContractRepository contractRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "contract-input")
    @Transactional("transactionManager")
    public void processContract(@Payload ContractEvent contractEvent, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.info("Processing contract: {}", contractEvent);

        switch (contractEvent.getContractEventType()) {
            case ADDED: processCreateContract(contractEvent);
            case UPDATED: processUpdateContract(contractEvent);
            default: log.error("Unknown contract event type: {}", contractEvent.getContractEventType());
        }
    }

    private void processCreateContract(ContractEvent contractEvent) {
        log.info("Processing create contract: {}", contractEvent);
        if (contractEvent.getEvent() instanceof ContractCreatedEvent updatedEvent) {
            Contract contract = new Contract();
            contract.setContractId(updatedEvent.getContractId());
            contract.setContractName(updatedEvent.getContractName());
            Contract persistedContract = contractRepository.save(contract);
            publishContractEvent(persistedContract);
        }
    }

    private void processUpdateContract(ContractEvent contractEvent) {
        log.info("Processing update contract: {}", contractEvent);
        if (contractEvent.getEvent() instanceof ContractUpdatedEvent updatedEvent) {
            Contract contract = contractRepository.findById(updatedEvent.getContractId()).orElseThrow();
            contract.setContractName(updatedEvent.getContractName());
            Contract persistedContract = contractRepository.save(contract);
            publishContractEvent(persistedContract);
        }
    }

    private void publishContractEvent(Contract contract) {
        log.info("Publishing contract event: {}", contract);

        ContractPublished contractPublished = ContractPublished.newBuilder()
                .setContractId(contract.getContractId())
                .setContractName(contract.getContractName())
                .build();

        ContractPublishedEvent contractEvent = ContractPublishedEvent.newBuilder()
                .setEventId(contract.getContractId())
                .setEvent(contractPublished)
                .build();

        kafkaTemplate.send("contract-output", contractEvent);
    }
}
