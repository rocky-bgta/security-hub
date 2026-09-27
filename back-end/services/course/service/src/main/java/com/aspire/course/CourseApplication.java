package com.aspire.course;

import com.aspire.asat.common.config.aws.SsmParameterInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class CourseApplication {

	public static void main(String[] args) {
		// SpringApplication.run(CourseApplication.class, args);
		SpringApplication app = new SpringApplication(CourseApplication.class);
		app.addInitializers(new SsmParameterInitializer());
		app.run(args);
	}

	@GetMapping("/api/v1/check")
	public String check()	{
		return "Response from course-service";
	}

}
