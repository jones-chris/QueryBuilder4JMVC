package com.cj.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping(value = "/")
    public String getIndex() {
        return "index";
    }

    @GetMapping("/wdc")
    public String getWdcView() { return "wdc"; }

}
