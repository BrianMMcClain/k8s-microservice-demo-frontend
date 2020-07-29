package com.github.brianmmcclain.k8smicroservicedemofrontend;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class FrontendController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    Environment env;

    @GetMapping("/")
    public String index(Model model) {
        Customization c = getCustomization();
        List<Item> items = getItems();
        Map<String, String> backendInfo = getBackendInfo();
        Map<String, String> customizationInfo = getCustomizationInfo();
        String frontendProfiles = getProfiles();
        
        model.addAttribute("message", c.getMessage());
        model.addAttribute("bgcolor", c.getBGColor());
        model.addAttribute("items", items);
        model.addAttribute("backendInfo", backendInfo);
        model.addAttribute("customizationInfo", customizationInfo);
        model.addAttribute("frontendProfiles", frontendProfiles);
        return "index";
    }

    private Customization getCustomization() {
        RestTemplate restTemaplte = new RestTemplate();
        String serviceURI = this.lookupK8sService("k8s-microservice-demo-customize-service", "http://localhost:8083");
        Customization c = restTemaplte.getForObject(serviceURI, Customization.class);
        return c;
    }

    private Map<String, String>  getCustomizationInfo() {
        RestTemplate restTemplate = new RestTemplate();
        String serviceURI = this.lookupK8sService("k8s-microservice-demo-backend-service", "http://localhost:8083");
        String customizationInfo = restTemplate.getForObject(serviceURI + "/info", String.class);
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, String> info = mapper.readValue(customizationInfo, Map.class);
            info.put("connected", "Yes");
            return info;
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Map<String, String> info = new HashMap<String, String>();
        info.put("connected", "No");
        return info;
    }

    private List<Item> getItems() {
        RestTemplate restTemplate = new RestTemplate();
        String serviceURI = this.lookupK8sService("k8s-microservice-demo-backend-service", "http://localhost:8084");
        Item[] items = restTemplate.getForObject(serviceURI + "/item", Item[].class);
        return Arrays.asList(items);
    }

    private Map<String, String>  getBackendInfo() {
        RestTemplate restTemplate = new RestTemplate();
        String serviceURI = this.lookupK8sService("k8s-microservice-demo-backend-service", "http://localhost:8084");
        String backendInfo = restTemplate.getForObject(serviceURI + "/info", String.class);
        
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, String> info = mapper.readValue(backendInfo, Map.class);
            info.put("connected", "Yes");
            return info;
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Map<String, String> info = new HashMap<String, String>();
        info.put("connected", "No");
        return info;
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

    private String getProfiles() {
        Map<String,String> payload = new HashMap<>();
        return "[" + String.join(", ", this.env.getActiveProfiles()) + "]"; 
    }
}