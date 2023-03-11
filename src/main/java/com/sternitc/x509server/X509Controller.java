package com.sternitc.x509server;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class X509Controller {

    @RequestMapping("/hello")
    public String hello() {
        return "Hello World\n";
    }

    @RequestMapping("/secured")
    public String protectedHello() {
        return "Protected content!\n";
    }

    @RequestMapping("/admin")
    public String admin() {
        return "Admin content!\n";
    }

}
