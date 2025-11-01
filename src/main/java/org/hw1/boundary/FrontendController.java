package org.hw1.boundary;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/citizen")
    public String citizen() {
        return "citizen";
    }

    @GetMapping("/staff")
    public String staff() {
        return "staff";
    }
}