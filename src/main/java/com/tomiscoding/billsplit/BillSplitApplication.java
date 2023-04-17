package com.tomiscoding.billsplit;

import com.tomiscoding.billsplit.model.Authority;
import com.tomiscoding.billsplit.repository.AuthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@SpringBootApplication
public class BillSplitApplication implements CommandLineRunner {

	@Autowired
	AuthorityRepository authorityRepository;

	@Bean
	RestTemplate restTemplate(RestTemplateBuilder builder){
		return builder.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(BillSplitApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		Authority userRole = new Authority(Authority.Roles.ROLE_USER);
		Authority adminRole = new Authority(Authority.Roles.ROLE_ADMIN);

		if (authorityRepository.findAll().isEmpty()){
			authorityRepository.saveAll(Arrays.asList(userRole, adminRole));
		}
	}
}
