package com.denkitronik.clienteservice.domain.services;

import com.denkitronik.clienteservice.domain.entities.Cliente;
import com.denkitronik.clienteservice.domain.exception.ClienteNotFoundException;
import com.denkitronik.clienteservice.domain.exception.ClienteServiceException;
import com.denkitronik.clienteservice.domain.repositories.IClienteDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTests {

    @Mock
    private IClienteDao clienteDao;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    @Test
    void findByIdThrowsWhenClientDoesNotExist() {
        when(clienteDao.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class, () -> clienteService.findById(999L));
    }

    @Test
    void updateThrowsWhenClientDoesNotExist() {
        when(clienteDao.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class,
                () -> clienteService.update(999L, new Cliente()));
    }

    @Test
    void deleteChecksExistenceAndThrowsWhenMissing() {
        when(clienteDao.existsById(999L)).thenReturn(false);

        assertThrows(ClienteNotFoundException.class, () -> clienteService.delete(999L));
        verify(clienteDao, never()).deleteById(anyLong());
    }

    @Test
    void saveWrapsDataIntegrityViolation() {
        Cliente cliente = new Cliente();
        when(clienteDao.saveAndFlush(cliente))
                .thenThrow(new DataIntegrityViolationException("duplicate email"));

        ClienteServiceException exception = assertThrows(ClienteServiceException.class,
                () -> clienteService.save(cliente));

        assertInstanceOf(DataIntegrityViolationException.class, exception.getCause());
    }
}
