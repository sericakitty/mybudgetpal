package hh.sof03.mybudgetpal.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface KeywordRepository extends CrudRepository<Keyword, Long> {

  List<Keyword> findAllByUserAndType(User user, KeywordType type);

  Keyword findByIdAndUser(Long id, User user);

  List<Keyword> findAllByUser(User user);
}
