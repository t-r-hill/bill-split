package com.tomiscoding.billsplit;

import com.tomiscoding.billsplit.model.Authority;
import com.tomiscoding.billsplit.model.Currency;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import com.tomiscoding.billsplit.repository.AuthorityRepository;
import com.tomiscoding.billsplit.service.CurrencyConversionService;
import com.tomiscoding.billsplit.service.GroupService;
import com.tomiscoding.billsplit.service.MailerSendService;
import com.tomiscoding.billsplit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@SpringBootApplication
@EnableCaching
public class BillSplitApplication extends SpringBootServletInitializer {

	@Autowired
	AuthorityRepository authorityRepository;

	public static void main(String[] args) {
		SpringApplication.run(BillSplitApplication.class, args);
	}

//	@Override
//	public void run(String... args) throws Exception {
//
//		Authority userRole = new Authority(Authority.Roles.ROLE_USER);
//		Authority adminRole = new Authority(Authority.Roles.ROLE_ADMIN);
//
//		if (authorityRepository.findAll().isEmpty()){
//			authorityRepository.saveAll(Arrays.asList(userRole, adminRole));
//		}
//	}
}
