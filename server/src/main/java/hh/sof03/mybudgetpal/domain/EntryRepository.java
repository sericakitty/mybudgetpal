package hh.sof03.mybudgetpal.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EntryRepository extends CrudRepository<Entry, Long> {
  
  List<Entry> findAllByUser(User user);
  
  Entry findByReferenceIdAndBankName(String referenceId, String bankName);
}
