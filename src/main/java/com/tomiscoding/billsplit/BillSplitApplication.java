package com.tomiscoding.billsplit;

import com.tomiscoding.billsplit.model.Authority;
import com.tomiscoding.billsplit.repository.AuthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class BillSplitApplication implements CommandLineRunner {

	@Autowired
	AuthorityRepository authorityRepository;

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
