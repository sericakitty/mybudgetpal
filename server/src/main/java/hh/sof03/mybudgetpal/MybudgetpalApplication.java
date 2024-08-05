package hh.sof03.mybudgetpal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import hh.sof03.mybudgetpal.domain.UserRepository;
import hh.sof03.mybudgetpal.domain.User;
import hh.sof03.mybudgetpal.domain.Keyword;
import hh.sof03.mybudgetpal.domain.KeywordRepository;
import hh.sof03.mybudgetpal.domain.KeywordType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class MybudgetpalApplication {

  private static final Logger log = LoggerFactory.getLogger(MybudgetpalApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(MybudgetpalApplication.class, args);
	}

  @Bean
  public CommandLineRunner demo(UserRepository userRepository, KeywordRepository keywordRepository) {

      // User user1 = new User("Sera", "Serica", "Suhonen", "serica.suhonen@myy.haaga-helia.fi", "$2a$06$3jYRJrg0ghaaypjZ/.g4SethoeA51ph3UD4kZi9oPkeMTpjKU5uo6");
      // Set<String> user1Roles = new HashSet<>();
      // user1Roles.add("ROLE_USER");
      // user1.setRoles(user1Roles);

      // userRepository.save(user1);

      /// Create keywords for user1
// Create keywords for user1
// keywordRepository.save(new Keyword(Arrays.asList("K-kauppa", "K-supermarket"), "Groceries", KeywordType.INCLUDED, user1));
// keywordRepository.save(new Keyword(Arrays.asList("Elli Keltto"), "Rent", KeywordType.INCLUDED, user1));
// keywordRepository.save(new Keyword(Arrays.asList("Lähivakuutus"), "Insurance", KeywordType.INCLUDED, user1));
// keywordRepository.save(new Keyword(Arrays.asList("Lähitapiola"), "Excluded", KeywordType.EXCLUDED, user1));

      
      return (args) -> {
          log.info("fetch all users");
          for (User user : userRepository.findAll()) {
              log.info(user.toString());
          }

          log.info("fetch all category keywords");
          for (Keyword keyword : keywordRepository.findAll()) {
              log.info(keyword.toString());
          }
      };


  }

}
