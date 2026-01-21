package com.USWCicrcleLink.server.global.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocsController {

    @GetMapping("/docs")
    public String redoc() {
        return "redoc";
    }
}
