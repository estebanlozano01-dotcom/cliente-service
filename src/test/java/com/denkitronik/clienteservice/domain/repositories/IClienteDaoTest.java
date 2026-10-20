package com.denkitronik.clienteservice.domain.repositories;

import com.denkitronik.clienteservice.domain.entities.Cliente;
import com.denkitronik.clienteservice.domain.entities.Region;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cliente-dao-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class IClienteDaoTest {

    @Autowired
    private IClienteDao clienteDao;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savesAndFindsClientById() {
        Cliente saved = clienteDao.saveAndFlush(newCliente());
        entityManager.clear();

        Cliente found = clienteDao.findById(saved.getId()).orElseThrow();

        assertThat(found.getNombre()).isEqualTo("Ada");
        assertThat(found.getEmail()).isEqualTo(saved.getEmail());
    }

    @Test
    void findAllIncludesSavedClient() {
        Cliente saved = clienteDao.saveAndFlush(newCliente());

        assertThat(clienteDao.findAll())
                .extracting(Cliente::getId)
                .contains(saved.getId());
    }

    @Test
    void deleteByIdRemovesClient() {
        Cliente saved = clienteDao.saveAndFlush(newCliente());

        clienteDao.deleteById(saved.getId());
        clienteDao.flush();

        assertThat(clienteDao.existsById(saved.getId())).isFalse();
    }

    @Test
    void findAllRegionesIncludesPersistedRegion() {
        Region region = new Region();
        region.setNombre("Región de prueba");
        entityManager.persistAndFlush(region);

        assertThat(clienteDao.findAllRegiones())
                .extracting(Region::getNombre)
                .contains("Región de prueba");
    }

    private Cliente newCliente() {
        Region region = new Region();
        region.setNombre("Región de prueba");
        entityManager.persistAndFlush(region);

        Cliente cliente = new Cliente();
        cliente.setNombre("Ada");
        cliente.setApellido("Lovelace");
        cliente.setEmail(UUID.randomUUID() + "@example.com");
        cliente.setRegion(region);
        return cliente;
    }
}
