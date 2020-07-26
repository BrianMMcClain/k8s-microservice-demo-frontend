package com.github.brianmmcclain.k8smicroservicedemofrontend;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class FrontendController {
    
    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/")
    public String index(Model model) {
        Customization c = getCustomization();
        List<Item> items = getItems();
        model.addAttribute("message", c.getMessage());
        model.addAttribute("bgcolor", c.getBGColor());
        model.addAttribute("items", items);
        return "index";
    }

    private Customization getCustomization() {
        RestTemplate restTemaplte = new RestTemplate();
        String serviceURI = this.lookupK8sService("k8s-demo-customization-service", "http://localhost:8083");
        Customization c = restTemaplte.getForObject(serviceURI, Customization.class);
        return c;
    }
    private List<Item> getItems() {
        RestTemplate restTemplate = new RestTemplate();
        String serviceURI = this.lookupK8sService("k8s-demo-backend-service", "http://localhost:8084");
        Item[] items = restTemplate.getForObject(serviceURI + "/item", Item[].class);
        return Arrays.asList(items);
    }

    private String lookupK8sService(String serviceName, String fallbackURI) {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
            if (!instances.isEmpty()) {
                return instances.get(0).getUri().toString();
            } else {
                return fallbackURI;
            }
        } catch (Exception e) {
            return fallbackURI;
        }
    }
}