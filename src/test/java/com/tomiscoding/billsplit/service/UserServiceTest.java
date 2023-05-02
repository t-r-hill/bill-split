package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.Authority;
import com.tomiscoding.billsplit.model.User;
import com.tomiscoding.billsplit.repository.AuthorityRepository;
import com.tomiscoding.billsplit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = UserService.class)
class UserServiceTest {

    @MockBean
    UserRepository userRepository;

    @MockBean
    AuthorityRepository authorityRepository;

    @Autowired
    UserService userService;

    @MockBean
    PasswordEncoder passwordEncoder;

    @Test
    void createNewUserSuccess() throws ValidationException {
        User validUser = User.builder()
                .id(1L)
                .fullName("Valid user")
                .username("validUserName")
                .password("unEncodedPassword").build();

        when(userRepository.findByUsername("validUserName")).thenReturn(Optional.empty());
        when(authorityRepository.findByRole(Authority.Roles.ROLE_USER)).thenReturn(new Authority(Authority.Roles.ROLE_USER));
        when(passwordEncoder.encode("unEncodedPassword")).thenReturn("encodedPassword");
        when(userRepository.save(ArgumentMatchers.argThat(u ->
                u.getAuthorities().get(0).getAuthority().equals(Authority.Roles.ROLE_USER.name()) &&
                u.getPassword().equals("encodedPassword"))))
                .thenReturn(validUser);

        assertThat(userService.createNewUser(validUser).getFullName()).isEqualTo("Valid user");
    }
}