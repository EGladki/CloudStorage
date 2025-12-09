package com.gladkiei.cloudstorage.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    @GetMapping(value = {"/registration", "/login", "/files/**"})
    public String handleRefresh() {
        return "forward:/index.html";
    }

}
