package com.cs.assignment.coursewaitlist;

import com.cs.assignment.coursewaitlist.config.CourseProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CourseProperties.class)
@OpenAPIDefinition(info = @Info(title = "Course Waitlist Service", version = "1.0.0", description = "Enrollment and waitlist workflow for a bounded course offering."))
public class CourseWaitlistApplication {
    public static void main(String[] args) {
        SpringApplication.run(CourseWaitlistApplication.class, args);
    }
}
