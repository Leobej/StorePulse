package org.projects.security;

import org.projects.persistence.entity.AppUser;
import org.projects.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StorePulseUserDetailsService implements UserDetailsService {
    private final AppUserRepository appUserRepository;

    public StorePulseUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByUsername(username)
            .filter(AppUser::isActive)
            .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));

        return User.withUsername(user.getUsername())
            .password(user.getPasswordHash())
            .roles(user.getRole())
            .build();
    }
}
