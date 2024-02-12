package com.example.wefly_app;

import com.example.wefly_app.util.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
		FileStorageProperties.class
})
public class WeFlyApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeFlyApplication.class, args);
	}

}
