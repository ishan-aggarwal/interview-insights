package com.interviewinsights.learn_java.records;

public record EmployeeRecord(String name, int age) {

    public EmployeeRecord {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }

        if (age < 18) {
            throw new IllegalArgumentException("Employee must be an adult");
        }
    }
}