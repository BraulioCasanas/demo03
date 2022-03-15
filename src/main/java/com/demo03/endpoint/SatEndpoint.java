package com.demo03.endpoint;

import com.demo03.service.SatAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/sat")
public class SatEndpoint {

    private final SatAuthenticationService satAuthenticationService;

   @Autowired
    public SatEndpoint(SatAuthenticationService satAuthenticationService) {
        this.satAuthenticationService = satAuthenticationService;
    }

    @PostMapping(path = "authentication")
    public void authentication () {
        satAuthenticationService.authentication();
    }
}
