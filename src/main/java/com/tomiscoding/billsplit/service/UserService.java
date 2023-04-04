package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.exceptions.UserValidationException;
import com.tomiscoding.billsplit.model.Authority;
import com.tomiscoding.billsplit.model.User;
import com.tomiscoding.billsplit.repository.AuthorityRepository;
import com.tomiscoding.billsplit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthorityRepository authorityRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Could not find user with username: " + username));
    }

    public User createNewUser(User user) throws UserValidationException{
        validateUniqueUser(user);
        validateUserProperties(user);

        Authority userAuthority = authorityRepository.findByRole(Authority.Roles.ROLE_USER);

        user.setAuthorities(Collections.singletonList(userAuthority));
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);

    }

    // Helper function to prevent accounts with duplicate usernames
    private void validateUniqueUser(User user) throws UserValidationException{
       if (userRepository.findByUsername(user.getUsername()).isPresent()){
           throw new UserValidationException("A user already exists with email: " + user.getUsername());
       };
    }

    // Helper function to ensure password is of sufficient length
    private boolean isPasswordOk(String password){
        return password != null && password.length() > 8;
    }

    // Helper function to validate properties on user object
    private void validateUserProperties(User user) throws UserValidationException{
        if (user.getUsername() == null || user.getUsername().isBlank()){
            throw new UserValidationException("Username must not be blank");
        } else if (!isPasswordOk(user.getPassword())) {
            throw new UserValidationException("Password must be at least 8 characters");
        } else if (user.getFullName() == null || user.getFullName().isBlank()) {
            throw new UserValidationException("Full name must not be blank");
        }
    }
}
