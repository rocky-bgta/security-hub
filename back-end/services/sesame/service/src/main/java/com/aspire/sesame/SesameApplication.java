package com.aspire.sesame;

import com.aspire.asat.common.config.aws.SsmParameterInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SesameApplication {

	public static void main(String[] args) {
		// SpringApplication.run(SesameApplication.class, args);
		SpringApplication app = new SpringApplication(SesameApplication.class);
		app.addInitializers(new SsmParameterInitializer());
		app.run(args);
	}

}
