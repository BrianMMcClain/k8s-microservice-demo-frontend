package com.github.brianmmcclain.k8smicroservicedemofrontend;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class FrontendController {
    
    @GetMapping("/")
    public String index(Model model) {
        Customization c = getCustomization();
        model.addAttribute("message", c.getMessage());
        model.addAttribute("bgcolor", c.getBGColor());
        return "index";
    }

    private Customization getCustomization() {
        RestTemplate restTemaplte = new RestTemplate();
        //TODO: Service lookup
        String serviceURI = "http://localhost:8083";
        Customization c = restTemaplte.getForObject(serviceURI, Customization.class);
        return c;
    }
}