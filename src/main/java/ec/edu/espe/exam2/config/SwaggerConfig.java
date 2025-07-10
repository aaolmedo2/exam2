package ec.edu.espe.exam2.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Ventanilla Bancaria API")
                                                .version("1.0.0")
                                                .description("API REST para gestión de efectivo en ventanillas bancarias. "
                                                                +
                                                                "Permite a los cajeros iniciar turnos, procesar transacciones de depósitos y retiros, "
                                                                +
                                                                "cerrar turnos y obtener resúmenes de actividades.")
                                                .contact(new Contact()
                                                                .name("Angelo Olmedo Camacho")
                                                                .email("-")
                                                                .url("-"))
                                                .license(new License()
                                                                .name("-")
                                                                .url("-")))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:8080")
                                                                .description("Servidor de Desarrollo"),
                                                new Server()
                                                                .url("-")
                                                                .description("-")));
        }
}
