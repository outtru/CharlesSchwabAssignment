package com.cs.assignment.coursewaitlist.config;

import com.cs.assignment.coursewaitlist.CourseOffering;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CourseProperties.class)
public class CourseConfiguration {

    @Bean
    @ConditionalOnMissingBean(CourseOffering.class)
    public CourseOffering defaultCourseOffering(CourseProperties properties) {
        return new CourseOffering(properties.getId(), properties.getCapacity(), properties.getPrerequisite());
    }
}
