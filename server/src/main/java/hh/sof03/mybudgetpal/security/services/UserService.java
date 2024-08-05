package hh.sof03.mybudgetpal.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import hh.sof03.mybudgetpal.domain.User;
import hh.sof03.mybudgetpal.domain.UserRepository;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getEmail());
            return user;
        }
        return null;
    }
}
