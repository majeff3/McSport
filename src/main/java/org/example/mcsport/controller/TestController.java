package org.example.mcsport.controller;

import jakarta.annotation.Resource;
import org.example.mcsport.service.FinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @PostMapping("/database_testing")
    public ResponseEntity<Object> databaseTesting(){
        return null;
    }

}
