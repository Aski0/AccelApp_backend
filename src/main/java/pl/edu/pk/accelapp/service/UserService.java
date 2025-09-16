package pl.edu.pk.accelapp.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.pk.accelapp.dto.LoginDto;
import pl.edu.pk.accelapp.dto.UserDto;
import pl.edu.pk.accelapp.model.User;
import pl.edu.pk.accelapp.repository.UserRepository;
import pl.edu.pk.accelapp.security.JwtUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    public User register(UserDto dto){
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        return userRepository.save(user);
    }

    public String login(LoginDto dto){
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(()-> new UsernameNotFoundException("Nie znaleziono użytkownika"));
        if(!passwordEncoder.matches(dto.getPassword(),user.getPassword())){
            throw new BadCredentialsException("Nieprawidłowe dane logowania");
        }
        return jwtUtil.generateToken(user);
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("Nie znaleziono użytkownika: "+ email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                //new ArrayList<>()
        );
    }
}
