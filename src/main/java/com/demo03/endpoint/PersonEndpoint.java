package com.demo03.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/person")
public class PersonEndpoint {

    @GetMapping(path = "/hello")
    public String hello() {
        return "hi !";
    }
}
