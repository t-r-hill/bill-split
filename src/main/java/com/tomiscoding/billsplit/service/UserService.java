package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.exceptions.ValidationException;
import com.tomiscoding.billsplit.model.Authority;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import com.tomiscoding.billsplit.repository.AuthorityRepository;
import com.tomiscoding.billsplit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/** This is a service class for the user object which implements the loadByUsername() method
 *  as well as providing other functionality for registration, login and updating of user profiles
 */
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

    /** This method first validates the user object passed in as argument - unique, required fields and valid password
     - before encoding the password and persisting using the user repository
     */
    public User createNewUser(User user) throws ValidationException {

        validateUniqueUser(user);
        validateUserProperties(user);

        Authority userAuthority = authorityRepository.findByRole(Authority.Roles.ROLE_USER);

        user.setAuthorities(Collections.singletonList(userAuthority));
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);

    }

    public List<User> getUsersBySplitGroup(SplitGroup splitGroup){
        return userRepository.getByGroupMembers_SplitGroup(splitGroup);
    }

    // Helper function to prevent accounts with duplicate usernames
    private void validateUniqueUser(User user) throws ValidationException {
       if (userRepository.findByUsername(user.getUsername()).isPresent()){
           throw new ValidationException("A user already exists with email: " + user.getUsername());
       };
    }

    // Helper function to ensure password is of sufficient length
    private boolean isPasswordOk(String password){
        return password != null && password.length() > 8;
    }

    // Helper function to validate required fields on user object
    private void validateUserProperties(User user) throws ValidationException {
        if (user.getUsername() == null || user.getUsername().isBlank()){
            throw new ValidationException("Username must not be blank");
        } else if (!isPasswordOk(user.getPassword())) {
            throw new ValidationException("Password must be at least 8 characters");
        } else if (user.getFullName() == null || user.getFullName().isBlank()) {
            throw new ValidationException("Full name must not be blank");
        }
    }
}
