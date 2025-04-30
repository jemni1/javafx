package edu.connexion3a41;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class RecaptchaServer {
    public static void main(String[] args) {
        SpringApplication.run(RecaptchaServer.class, args);
    }
}

@RestController
class RecaptchaController {
    @GetMapping("/recaptcha/token")
    public String getToken(@RequestParam String action) {
        // Mock token for testing
        return "{\"token\": \"mock-token-" + action + "\"}";
    }
}