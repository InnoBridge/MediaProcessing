package io.github.innobridge.mediaprocesing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = {
	io.github.innobridge.mediaprocesing.controller.ApplicationSpecificSpringComponentScanMarker.class,
	io.github.innobridge.mediaprocesing.configuration.ApplicationSpecificSpringComponentScanMarker.class
})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
