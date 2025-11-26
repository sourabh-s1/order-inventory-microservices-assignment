package com.assessment.inventory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerRedirectController {

    @GetMapping("/docs")
    public String redirectToSwagger() {
        return "redirect:/webjars/swagger-ui/index.html?configUrl=http://localhost:8071/v3/api-docs";
    }
}
