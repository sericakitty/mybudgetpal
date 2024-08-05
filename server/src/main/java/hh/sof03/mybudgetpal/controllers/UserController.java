package hh.sof03.mybudgetpal.controllers;

import hh.sof03.mybudgetpal.domain.Keyword;
import hh.sof03.mybudgetpal.domain.KeywordRepository;
import hh.sof03.mybudgetpal.domain.KeywordType;
import hh.sof03.mybudgetpal.domain.User;
import hh.sof03.mybudgetpal.domain.UserRepository;
import hh.sof03.mybudgetpal.security.jwt.JwtUtils;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/user")
public class UserController {

  private static final Logger log = LoggerFactory.getLogger(UserController.class);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private KeywordRepository keywordRepository;

  @Autowired
  private JwtUtils jwtUtils;

  private User getUserFromRequest(HttpServletRequest request) {
    log.info("hello from getUserFromRequest");
    String token = jwtUtils.getJwtFromHeader(request);  // Get token from header
    log.info("getUserFromRequest: token: " + token);
    if (token != null && jwtUtils.validateJwtToken(token)) {

      String email = jwtUtils.getEmailFromJwtToken(token);
      User user = userRepository.findByEmail(email);
      if (user != null) {
        log.info("User found: " + user.getUsername());
      } else {
        log.info("User not found for username: " + email);
      }
      return user;
    }
    log.info("Token validation failed or token is null");
    return null;
  }



  @GetMapping("/keywords")
  public ResponseEntity<?> keywords(HttpServletRequest request) {
    User user = getUserFromRequest(request);
    if (user == null) {
      return ResponseEntity.badRequest().body("User not found or invalid token");
    }

    List<Keyword> includedKeywords = keywordRepository.findAllByUserAndType(user, KeywordType.INCLUDED);
    includedKeywords.forEach(keyword -> keyword.setUser(null));

    List<Keyword> excludedKeywords = keywordRepository.findAllByUserAndType(user, KeywordType.EXCLUDED);
    excludedKeywords.forEach(keyword -> keyword.setUser(null));

    Map<String, Object> keywords = new HashMap<>();
    keywords.put("includedKeywords", includedKeywords);
    keywords.put("excludedKeywords", excludedKeywords);

    return ResponseEntity.ok(keywords);
  }

  @GetMapping("/keywords/edit/{id}")
    public ResponseEntity<?> editKeyword(@PathVariable Long id, HttpServletRequest request) {
        User user = getUserFromRequest(request);
        log.info("User: " + user);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found or invalid token");
        }

        Optional<Keyword> keywordOptional = keywordRepository.findById(id);
        log.info("KeywordOptional: " + keywordOptional);
        if (!keywordOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Keyword not found");
        }

        Keyword keyword = keywordOptional.get();
        if (!keyword.getUser().getId().equals(user.getId()) && !user.getRoles().contains("ROLE_ADMIN")) {
            return ResponseEntity.status(403).body("You do not have permission to edit this keyword");
        }

        // Hide user information before sending response
        keyword.setUser(null);

        return ResponseEntity.ok(keyword);
    }


    @PostMapping("/keywords/update/{id}")
public ResponseEntity<?> updateKeyword(@PathVariable Long id, @RequestBody Keyword updatedKeyword, HttpServletRequest request) {
    User user = getUserFromRequest(request);
    if (user == null) {
        return ResponseEntity.badRequest().body("User not found or invalid token");
    }

    Optional<Keyword> keywordOptional = keywordRepository.findById(id);
    if (!keywordOptional.isPresent()) {
        return ResponseEntity.badRequest().body("Keyword not found");
    }

    Keyword existingKeyword = keywordOptional.get();

    if (!existingKeyword.getUser().getId().equals(user.getId()) && !user.getRoles().contains("ROLE_ADMIN")) {
        return ResponseEntity.status(403).body("You do not have permission to update this keyword");
    }

    // Update keyword details
    existingKeyword.setCategory(updatedKeyword.getCategory());
    existingKeyword.setKeywords(updatedKeyword.getKeywords());
    existingKeyword.setType(updatedKeyword.getType());

    keywordRepository.save(existingKeyword);

    return ResponseEntity.ok("Keyword updated successfully");
}



  // type = included or excluded requestbody
  @PostMapping("/keywords/add")
  public ResponseEntity<?> addKeyword(@RequestBody Keyword keyword, HttpServletRequest request) {
    User user = getUserFromRequest(request);
    if (user == null) {
      return ResponseEntity.badRequest().body("User not found or invalid token");
    }

    keyword.setUser(user);
    keywordRepository.save(keyword);
    return ResponseEntity.ok(keyword);
  }



  @GetMapping("/keywords/delete/{id}")
  public ResponseEntity<String> deleteKeyword(@PathVariable Long id, HttpServletRequest request) {
    User user = getUserFromRequest(request);
    if (user == null) {
      return ResponseEntity.badRequest().body("User not found or invalid token");
    }

    Optional<Keyword> keywordOptional = keywordRepository.findById(id);
    log.info("KeywordOptional: " + keywordOptional);
    if (!keywordOptional.isPresent()) {
      return ResponseEntity.badRequest().body("Keyword not found");
    }

    Keyword keyword = keywordOptional.get();
    log.info("Keyword: " + keyword);

    // Check if the authenticated user is the owner of the keyword or has the role of ADMIN
    if (keyword.getUser().getId().equals(user.getId()) || user.getRoles().contains("ROLE_ADMIN")) {
      keywordRepository.deleteById(id);
      return ResponseEntity.ok("Keyword deleted successfully");
    } else {
      return ResponseEntity.status(403).body("You do not have permission to delete this keyword");
    }
  }
}