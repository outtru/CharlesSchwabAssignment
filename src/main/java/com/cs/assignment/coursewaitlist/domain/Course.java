package com.cs.assignment.coursewaitlist.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_code", nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "prerequisite_code")
    private String prerequisiteCode;

    protected Course() {
    }

    public Course(String code, Integer capacity, String prerequisiteCode) {
        this.code = code;
        this.capacity = capacity;
        this.prerequisiteCode = prerequisiteCode;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getPrerequisiteCode() {
        return prerequisiteCode;
    }
}
