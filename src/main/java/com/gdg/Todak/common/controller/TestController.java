package com.gdg.Todak.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/")
    ResponseEntity<String> test() {
        return ResponseEntity.ok("하이");
    }
}
