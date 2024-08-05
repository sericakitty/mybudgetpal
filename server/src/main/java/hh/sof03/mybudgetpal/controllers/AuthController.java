package hh.sof03.mybudgetpal.controllers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.mail.internet.MimeMessage;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hh.sof03.mybudgetpal.payload.request.SignupRequest;
import hh.sof03.mybudgetpal.payload.request.LoginRequest;
import hh.sof03.mybudgetpal.payload.request.ResetPasswordRequest;
import hh.sof03.mybudgetpal.payload.response.UserInfoResponse;
import hh.sof03.mybudgetpal.payload.response.MessageResponse;
import hh.sof03.mybudgetpal.security.jwt.JwtUtils;
import hh.sof03.mybudgetpal.security.services.UserService;
import hh.sof03.mybudgetpal.domain.UserRepository;
import hh.sof03.mybudgetpal.security.services.CustomUserDetails;
import hh.sof03.mybudgetpal.security.services.UserNotFoundException;
import hh.sof03.mybudgetpal.MybudgetpalApplication;
import hh.sof03.mybudgetpal.domain.User;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(MybudgetpalApplication.class);

  @Autowired
  UserService userService;

  @Autowired
  UserRepository userRepository;

  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired
  JavaMailSender mailSender;

  @Autowired
  Dotenv dotenv;

  @Autowired
  JwtUtils jwtUtils;

  @PostMapping("/login")
  public ResponseEntity<UserInfoResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

    String role = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.joining(","));

    UserInfoResponse userInfoResponse = new UserInfoResponse(userDetails.getUsername(), userDetails.getEmail(), role);
    userInfoResponse.setEnabled(userDetails.isEnabled());
    userInfoResponse.setToken(jwtCookie.getValue());

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(userInfoResponse);
  }

  @PostMapping("/signup")
  public ResponseEntity<String> save(@Valid @RequestBody SignupRequest signupRequest, HttpServletRequest request)
      throws MessagingException {
    if (userRepository.findByEmail(signupRequest.getEmail()) != null) {
      return ResponseEntity.badRequest().body("Email already exists");
    }

    if (!signupRequest.getFirstName().matches("^[a-zA-Z]*$")) {
      return ResponseEntity.badRequest().body("First name can only contain letters");
    }

    if (!signupRequest.getLastName().matches("^[a-zA-Z]*$")) {
      return ResponseEntity.badRequest().body("Last name can only contain letters");
    }

    if (userRepository.findByUsername(signupRequest.getUsername()) != null) {
      return ResponseEntity.badRequest().body("Username already exists");
    }

    if (!signupRequest.getPassword().equals(signupRequest.getPasswordCheck())) {
      return ResponseEntity.badRequest().body("Passwords do not match");
    }

    String newUsername = signupRequest.getUsername();
    String newFirstName = signupRequest.getFirstName();
    String newLastName = signupRequest.getLastName();
    String newEmail = signupRequest.getEmail();
    String pwd = signupRequest.getPassword();

    String hashPwd = bCryptPasswordEncoder.encode(pwd);

    UUID uuid = UUID.randomUUID();
    String verificationToken = uuid.toString().replaceAll("-", "");

    User newUser = new User();
    newUser.setPasswordHash(hashPwd);
    newUser.setUsername(newUsername);
    newUser.setFirstName(newFirstName);
    newUser.setLastName(newLastName);
    newUser.setEmail(newEmail);
    newUser.setEmailVerificationToken(verificationToken);
    newUser.setEmailVerificationTokenExpiryDate(LocalDateTime.now().plusHours(2));

    Set<String> roles = new HashSet<>();
    roles.add("ROLE_USER");
    newUser.setRoles(roles);

    userRepository.save(newUser);

    String verificationLink = dotenv.get("CORS_ORIGIN") + "/verify-email?email=" + newEmail + "&token="
        + verificationToken;
    sendVerificationEmail(newUser.getEmail(), newUsername, verificationLink);

    return ResponseEntity
        .ok("User registered successfully! We have sent you a verification email. Please check your email.");
  }

  @PostMapping("/auth/logout")
  public ResponseEntity<?> logoutUser() {
    ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(new MessageResponse("You've been signed out!"));
  }

  @GetMapping("/auth/validate-token")
  public ResponseEntity<UserInfoResponse> verifyToken(HttpServletRequest request) {
    String token = jwtUtils.getJwtFromHeader(request);

    if (token != null && jwtUtils.validateJwtToken(token)) {

      String email = jwtUtils.getEmailFromJwtToken(token);
      logger.info("Email from token: " + email);
      User user = userRepository.findByEmail(email);

      if (user == null) {
        return ResponseEntity.badRequest().body(null);
      }

      UserInfoResponse userInfoResponse = new UserInfoResponse(user.getUsername(), user.getEmail(),
          user.getRoles().toString());
      userInfoResponse.setToken(token);
      userInfoResponse.setEnabled(user.isEnabled());

      return ResponseEntity.ok().body(userInfoResponse);
    }

    return ResponseEntity.badRequest().body(null);
  }

  @GetMapping("/auth/user")
  public ResponseEntity<UserInfoResponse> getUserInfo(HttpServletRequest request) {
    String token = jwtUtils.getJwtFromHeader(request);

    if (token != null && jwtUtils.validateJwtToken(token)) {
      String email = jwtUtils.getEmailFromJwtToken(token);
      User optionalUser = userRepository.findByEmail(email);

      if (optionalUser == null) {
        return ResponseEntity.badRequest().body(null);
      }

      UserInfoResponse userInfoResponse = new UserInfoResponse(optionalUser.getUsername(),
          optionalUser.getEmail(), optionalUser.getRoles().toString());
      userInfoResponse.setFirstName(optionalUser.getFirstName());
      userInfoResponse.setLastName(optionalUser.getLastName());
      userInfoResponse.setEnabled(optionalUser.isEnabled());

      return ResponseEntity.ok().body(userInfoResponse);
    }

    return ResponseEntity.badRequest().body(null);
  }

  @GetMapping("/auth/resend-verification-email")
  public ResponseEntity<String> requestEmailVerification(HttpServletRequest request) throws MessagingException {

    try {
      String token = jwtUtils.getJwtFromHeader(request);

      if (token != null && jwtUtils.validateJwtToken(token)) {
        String userEmail = jwtUtils.getEmailFromJwtToken(token);
        User user = userRepository.findByEmail(userEmail);

        if (user == null) {
          return ResponseEntity.badRequest().body("User not found");
        }

        if (user.isEnabled()) {
          return ResponseEntity.badRequest().body("User is already verified");
        }

        UUID uuid = UUID.randomUUID();
        String verificationToken = uuid.toString().replaceAll("-", "");

        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiryDate(LocalDateTime.now().plusHours(2));
        userRepository.save(user);

        String verificationLink = dotenv.get("CORS_ORIGIN") + "/verify-email?email=" + userEmail + "&token="
            + verificationToken;
        sendVerificationEmail(userEmail, user.getUsername(), verificationLink);

        return ResponseEntity.ok("Verification email sent successfully");
      }
      return ResponseEntity.badRequest().body("Invalid token");
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Error while sending email");
    }

  }

  // This works even if ResponseEntity returns a bad request
  @GetMapping("/verify-email")
public ResponseEntity<?> verifyEmail(@RequestParam("email") String email,
                                                       @RequestParam("token") String token) {

    User user = userRepository.findByEmail(email);

    if (user == null) {
        return ResponseEntity.badRequest().body("User not found");
    }

    if (!token.equals(user.getEmailVerificationToken())) {
        return ResponseEntity.badRequest().body("Invalid token");
    }

    // Check if the token has expired
    LocalDateTime tokenExpiryDate = user.getEmailVerificationTokenExpiryDate();
    if (tokenExpiryDate == null) {
        return ResponseEntity.badRequest().body("Token has expired. Please request a new verification email.");
    }

    if (tokenExpiryDate.isBefore(LocalDateTime.now())) {
        return ResponseEntity.badRequest().body("Token has expired. Please request a new verification email.");
    }

    user.setEnabled(true);
    user.setEmailVerificationToken(null);
    user.setEmailVerificationTokenExpiryDate(null);
    userRepository.save(user);

    return ResponseEntity.ok().body("Email verified successfully");
  }

  @GetMapping("/request-password-reset")
  public ResponseEntity<String> processForgotPassword(@RequestParam("email") String email, HttpServletRequest request)
      throws MessagingException {
    try {
      UUID uuid = UUID.randomUUID();
      String token = uuid.toString().replaceAll("-", "");

      User user = userRepository.findByEmail(email.toString());

      if (user == null) {
        throw new UserNotFoundException("Could not find the user with this email.");
      }

      user.setPasswordResetToken(token);
      user.setPasswordResetTokenExpiryDate(LocalDateTime.now().plusHours(2));
      userRepository.save(user);

      String userEmail = user.getEmail();

      String passwordResetLink = dotenv.get("CORS_ORIGIN") + "/reset-password?email=" + userEmail + "&token=" + token;
      System.out.println(passwordResetLink);

      sendResetEmail(email, user.getUsername(), passwordResetLink);

      return ResponseEntity.ok("We have sent you a reset link. Please check your email.");

    } catch (UserNotFoundException exception) {
      return ResponseEntity.badRequest().body(exception.getMessage());
    } catch (MessagingException exception) {
      return ResponseEntity.status(500).body("Error while sending email");
    }
  }

  @GetMapping("/reset-password")
  public ResponseEntity<Map<String, String>> showResetPasswordForm(@RequestParam(value = "email") String email,
      @RequestParam(value = "token") String token) {
    Map<String, String> response = new HashMap<>();
    User user = userRepository.findByEmailAndPasswordResetToken(email.toString(), token.toString());

    if (user == null) {
      response.put("message", "Invalid token");
      return ResponseEntity.badRequest().body(response);
    }

    if (user.getPasswordResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
      response.put("message", "Token has expired. Please request a new password reset.");
      return ResponseEntity.badRequest().body(response);
    }

    response.put("message", "Token is valid");
    return ResponseEntity.ok(response);
  }

  @PostMapping("/reset-password")
  public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {

    User user = userRepository
        .findByEmailAndPasswordResetToken(resetPasswordRequest.getEmail(), resetPasswordRequest.getToken());

    if (user == null) {
      return ResponseEntity.badRequest().body("Invalid token");
    }

    if (!resetPasswordRequest.getPassword().equals(resetPasswordRequest.getPasswordCheck())) {
      return ResponseEntity.badRequest().body("Passwords do not match");
    }

    String pwd = resetPasswordRequest.getPassword();
    BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
    String hashPwd = bc.encode(pwd);

    user.setPasswordHash(hashPwd);
    user.setPasswordResetToken(null);
    user.setPasswordResetTokenExpiryDate(null);

    userRepository.save(user);

    return ResponseEntity.ok("Password reset successfully");
  }

  private void sendVerificationEmail(String toEmail, String username, String confirmationUrl)
      throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message);

    String fromEmail = dotenv.get("SPRING_MAIL_EMAIL");
    helper.setFrom(fromEmail);
    helper.setTo(toEmail);

    String content = "<div style=\"font-family: Arial, sans-serif; line-height: 1.6;\">" +
        "  <h2>Email Confirmation Required</h2>" +
        "  <p>Dear " + username + ",</p>" +
        "  <p>Thank you for registering with our service. To complete your registration, please confirm your email address by clicking the button below:</p>"
        +
        "  <p><a href=\"" + confirmationUrl
        + "\" style=\"display: inline-block; padding: 10px 20px; font-size: 16px; color: white; background-color: blue; text-decoration: none; border-radius: 5px;\">Confirm Email</a></p>"
        +
        "  <p>If you did not request this email, please disregard it. No further action is required on your part.</p>" +
        "  <p>Best regards,<br>My budget Pal Team</p>" +
        "</div>";

    helper.setSubject("Email Verification");
    helper.setText(content, true);

    mailSender.send(message);
  }

  // // Send password reset email
  private void sendResetEmail(String toEmail, String username, String passwordResetLink) throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message);

    String fromEmail = dotenv.get("SPRING_MAIL_EMAIL");
    helper.setFrom(fromEmail);
    helper.setTo(toEmail);

    String content = "<div style=\"font-family: Arial, sans-serif; line-height: 1.6;\">" +
        "  <p>Hello, " + username + "</p>" +
        "  <p>You have requested to reset your password. To proceed with resetting your password, please click the link below:</p>"
        +
        "  <p><a href=\"" + passwordResetLink
        + "\" style=\"display: inline-block; padding: 10px 20px; font-size: 16px; color: white; background-color: blue; text-decoration: none; border-radius: 5px;\">Change My Password</a></p>"
        +
        "  <p>If you did not request a password reset, please disregard this email. No further action is required on your part.</p>"
        +
        "  <p>Best regards,<br>My Pudget Pal Team</p>" +
        "</div>";

    helper.setSubject("Password reset link");
    helper.setText(content, true);

    mailSender.send(message);
  }
}