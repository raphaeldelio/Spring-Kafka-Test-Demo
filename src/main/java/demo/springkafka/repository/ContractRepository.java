package demo.springkafka.repository;

import demo.springkafka.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, String> { }
