package com.config;

import com.repository.Data_POI;
import com.service.POI_Service;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {
    @Bean
    public POI_Service poiService() {
        return new POI_Service(Data_POI.getInstance());
    }
}
