package com.pranav.merchant_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
@EnableCaching
public class MerchantServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MerchantServiceApplication.class, args);
	}

}
