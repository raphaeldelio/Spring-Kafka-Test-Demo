package demo.springkafka.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
@Table(name = "contract")
public class Contract {
    @Id
    private String contractId;
    private String contractName;
}
