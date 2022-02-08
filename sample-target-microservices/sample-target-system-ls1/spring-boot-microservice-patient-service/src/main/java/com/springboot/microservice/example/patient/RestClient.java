package com.springboot.microservice.example.patient;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestClient {
	private final RestTemplate restTemplate;

    public RestClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public String getPostsPlainJSON(String url) {
        return this.restTemplate.getForObject(url, String.class);
    }
}
