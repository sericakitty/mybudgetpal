package hh.sof03.mybudgetpal.security.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import hh.sof03.mybudgetpal.domain.User;
import hh.sof03.mybudgetpal.domain.UserRepository;

@Service
@Configuration
public class UserDetailServiceImplement implements UserDetailsService {
  private final UserRepository userRepository;

    @Autowired
    public UserDetailServiceImplement(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User optionalUser = userRepository.findByEmail(email);

    if (optionalUser == null) {
      throw new UsernameNotFoundException("No user found with username: " + email);
    }

    User curruser = optionalUser;

    return UserDetailServiceImplement.build(curruser);
  }

  public static CustomUserDetails build(User user) {
    List<GrantedAuthority> authorities = user.getRoles().stream()
      .map(role -> new SimpleGrantedAuthority(role))
      .collect(Collectors.toList());

    return new CustomUserDetails(
      user.getId(),
      user.getUsername(),
      user.getEmail(),
      user.getPasswordHash(),
      authorities
    );
  }

}