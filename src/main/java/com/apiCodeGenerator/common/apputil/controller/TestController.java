package com.apiCodeGenerator.common.apputil.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/api/test/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Success! Backend is reachable and JSON is working.");
    }
}
