package com.fiap.snackapp.adapters.driven.infra.persistence.adapter;

import com.fiap.snackapp.adapters.driven.infra.persistence.entity.CustomerEntity;
import com.fiap.snackapp.adapters.driven.infra.persistence.mapper.CustomerPersistenceMapper;
import com.fiap.snackapp.adapters.driven.infra.persistence.repository.SpringDataCustomerJpaRepository;
import com.fiap.snackapp.core.application.repository.CustomerRepositoryPort;
import com.fiap.snackapp.core.domain.model.CustomerDefinition;
import com.fiap.snackapp.core.domain.vo.CPF;
import com.fiap.snackapp.core.domain.vo.Email;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(CustomerPersistenceMapper.class) // ajuste se seu mapper não for bean Spring
class CustomerPersistenceAdapterIntegrationTest {

    @Autowired
    private SpringDataCustomerJpaRepository jpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CustomerPersistenceMapper mapper;

    private CustomerRepositoryPort customerRepositoryPort;

    private String cpfValue;

    @BeforeEach
    void setUp() {
        customerRepositoryPort = new CustomerPersistenceAdapter(jpaRepository, mapper);

        jpaRepository.deleteAllInBatch();
        jpaRepository.flush();
        entityManager.clear();

        cpfValue = "12345678900";

        // Seed direto por JPA repository (entidade)
        // IMPORTANTE: id = null (deixa o @GeneratedValue cuidar)
        var entity = new CustomerEntity(
                null,
                "João",
                "joao@email.com",
                cpfValue
        );

        jpaRepository.saveAndFlush(entity);
    }

    @Test
    @DisplayName("save: deve persistir cliente e retornar domain com id")
    void save_ShouldPersistCustomer() {
        var customer = new CustomerDefinition(
                null,
                "Maria",
                new Email("maria@email.com"),
                new CPF("98765432100")
        );

        CustomerDefinition saved = customerRepositoryPort.save(customer);

        assertThat(saved).isNotNull();
        assertThat(saved.id()).isNotNull();
        assertThat(saved.name()).isEqualTo("Maria");
        assertThat(saved.email().toString()).contains("maria"); // depende do seu VO
        assertThat(saved.cpf().toString()).isEqualTo("98765432100");
    }

    @Test
    @DisplayName("findByCpf: deve retornar cliente quando existir")
    void findByCpf_ShouldReturnCustomer() {
        Optional<CustomerDefinition> result = customerRepositoryPort.findByCpf(new CPF(cpfValue));

        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("João");
        assertThat(result.get().cpf().toString()).isEqualTo(cpfValue);
    }

    @Test
    @DisplayName("findByCpf: deve retornar vazio quando não existir")
    void findByCpf_ShouldReturnEmpty() {
        Optional<CustomerDefinition> result = customerRepositoryPort.findByCpf(new CPF("00000000000"));

        assertThat(result).isEmpty();
    }
}
