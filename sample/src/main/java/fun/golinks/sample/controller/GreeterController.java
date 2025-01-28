package fun.golinks.sample.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class GreeterController {

    @GetMapping
    public String get(@RequestParam("name") String name) {
        return String.format("Hello: %s", name);
    }
}
