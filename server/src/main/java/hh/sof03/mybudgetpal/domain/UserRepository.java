package hh.sof03.mybudgetpal.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  User findByEmail(String email);

  User findByUsername(String username);

  User findByEmailAndPasswordResetToken(String email, String passwordResetToken);

  User findByEmailVerificationToken(String emailVerificationToken);

  User findByEmailAndEmailVerificationToken(String email, String emailVerificationToken);
}
