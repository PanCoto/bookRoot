package pl.studyshare.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.studyshare.domain.User;
import pl.studyshare.repository.UserRepository;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User appUser = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Użytkownik o loginie '" + login + "' nie został znaleziony"));

        return new org.springframework.security.core.userdetails.User(
                appUser.getLogin(),
                appUser.getPassword(),
                appUser.getEnabled(),
                true, true, true,
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()))
        );
    }
}
