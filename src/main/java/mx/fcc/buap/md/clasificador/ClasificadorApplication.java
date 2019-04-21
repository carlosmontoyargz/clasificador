package mx.fcc.buap.md.clasificador;

import mx.fcc.buap.md.clasificador.storage.StorageProperties;
import mx.fcc.buap.md.clasificador.storage.StorageService;
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
			//storageService.deleteAll();
			storageService.init();
		};
	}
}
