package mx.fcc.buap.clasificador;

import mx.fcc.buap.clasificador.storage.StorageProperties;
import mx.fcc.buap.clasificador.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class ClasificadorApplication
{

	public static void main(String[] args)
	{
		SpringApplication.run(ClasificadorApplication.class, args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			//storageService.removeAll();
			storageService.init();
		};
	}
}
