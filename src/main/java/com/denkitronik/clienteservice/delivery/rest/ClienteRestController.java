package com.denkitronik.clienteservice.delivery.rest;

import com.denkitronik.clienteservice.domain.entities.Cliente;
import com.denkitronik.clienteservice.domain.entities.Region;
import com.denkitronik.clienteservice.domain.services.IClienteService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/${api.version}/cliente-service")
@CrossOrigin(origins = {"http://localhost:4200"})
public class ClienteRestController {

    private final IClienteService clienteService;
    private final Validator validator;

    public ClienteRestController(IClienteService clienteService, Validator validator) {
        this.clienteService = clienteService;
        this.validator = validator;
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(clienteService.findAll());
    }

    @GetMapping("/clientes/page/{page}")
    public ResponseEntity<Page<Cliente>> listarClientesPaginado(@PathVariable Integer page) {
        return ResponseEntity.ok(clienteService.findAll(PageRequest.of(page, 4)));
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<Cliente> buscarCliente(@PathVariable Long id) {
        // Si no existe: ClienteNotFoundException -> GlobalExceptionHandler -> 404
        return ResponseEntity.ok(clienteService.findById(id));
    }

    @PostMapping("/clientes")
    public ResponseEntity<Cliente> crearCliente(@Valid @RequestBody Cliente cliente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.save(cliente));
    }

    @PutMapping("/clientes/{id}")
    public ResponseEntity<Cliente> actualizarCliente(@RequestBody Cliente cliente,
                                                    @PathVariable Long id) {
        clienteService.findById(id);
        Set<ConstraintViolation<Cliente>> violations = validator.validate(cliente);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.update(id, cliente));
    }

    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        // delete lanza ClienteNotFoundException si no existe -> 404 automático
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/clientes/regiones")
    public ResponseEntity<List<Region>> listarRegiones() {
        return ResponseEntity.ok(clienteService.findAllRegiones());
    }

}