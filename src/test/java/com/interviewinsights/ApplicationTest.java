package com.interviewinsights;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApplicationTest {

    @Test
    void applicationClassLoads() {
        assertNotNull(Application.class);
    }
}