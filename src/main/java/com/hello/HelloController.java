package com.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController 
@RequestMapping ("/")
public class HelloController {
    
    @GetMapping("hello") 
    public String getGreeting(){
        return "Hello World!!!!";
    }

}
