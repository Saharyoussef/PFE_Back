package com.sahar.discoveryservice.security;

import com.sahar.discoveryservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static org.springframework.security.core.authority.AuthorityUtils.commaSeparatedStringToAuthorityList;


@Service
@AllArgsConstructor
public class DiscoveryUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    // This method is called by Spring Security to load user details during authentication
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user in the database using the username
        var user = userRepository.getUserByUsername(username);
        return new User(user.getUsername(), user.getPassword(), user.isEnabled(), user.isAccountNonExpired(), user.isCredentialsNonExpired(), user.isAccountNonLocked(), commaSeparatedStringToAuthorityList(user.getRole() + "," + user.getAuthorities()));
        //we used here the Username and not the email because in application.yml in the default zone if we use email that will cause a problem
    }
}