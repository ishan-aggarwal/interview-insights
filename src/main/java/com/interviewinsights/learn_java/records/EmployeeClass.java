package com.interviewinsights.learn_java.records;

public class EmployeeClass {
    private final String name;
    private final int age;

    public EmployeeClass(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}