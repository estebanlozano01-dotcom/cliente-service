package com.denkitronik.clienteservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Combina tres anotaciones:
// @Configuration         -> esta clase puede definir beans
// @EnableAutoConfiguration -> configura beans según las dependencias del classpath
// @ComponentScan         -> escanea el paquete buscando @Component, @Service, etc.
@SpringBootApplication
public class ClienteServiceApplication {

    public static void main(String[] args) {
        // Arranca el contexto de Spring y el servidor Tomcat embebido
        SpringApplication.run(ClienteServiceApplication.class, args);
    }
}