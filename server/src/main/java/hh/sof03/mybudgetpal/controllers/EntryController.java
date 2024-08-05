package hh.sof03.mybudgetpal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import hh.sof03.mybudgetpal.domain.User;
import hh.sof03.mybudgetpal.domain.UserRepository;
import hh.sof03.mybudgetpal.security.jwt.JwtUtils;
import hh.sof03.mybudgetpal.domain.Entry;
import hh.sof03.mybudgetpal.domain.EntryRepository;
import hh.sof03.mybudgetpal.domain.Keyword;
import hh.sof03.mybudgetpal.domain.KeywordRepository;
import hh.sof03.mybudgetpal.domain.KeywordType;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class EntryController {

  private static final Logger log = LoggerFactory.getLogger(EntryController.class);

  @Autowired
  private EntryRepository entryRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private KeywordRepository keywordRepository;

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

  @GetMapping("/entries")
  public ResponseEntity<List<Entry>> getEntries(HttpServletRequest request) {
    User user = getUserFromRequest(request);
    if (user == null) {
      return ResponseEntity.badRequest().body(null);
    }

    List<Entry> entries = entryRepository.findAllByUser(user);
    return ResponseEntity.ok(entries);
  }

  @GetMapping("/entries/delete/{id}")
  public ResponseEntity<?> deleteEntry(@PathVariable Long id, HttpServletRequest request) {
    User user = getUserFromRequest(request);
    if (user == null) {
      return ResponseEntity.badRequest().body("User not found or invalid token");
    }

    entryRepository.deleteById(id);
    return ResponseEntity.ok("Entry deleted successfully");
  }

  @PostMapping("/entries/import-data")
  public ResponseEntity<?> importFiles(@RequestParam("file") MultipartFile[] files, HttpServletRequest request) {
    User user = getUserFromRequest(request);
    if (user == null) {
      return ResponseEntity.badRequest().body("User not found or invalid token");
    }

    if (files.length == 0) {
      return ResponseEntity.badRequest().body("Please select at least one file to upload.");
    }

    boolean hasInvalidFiles = false;

    for (MultipartFile file : files) {
      if (!file.getContentType().equals("text/csv")) {
        hasInvalidFiles = true;
        files = Arrays.stream(files).filter(f -> !f.equals(file)).toArray(MultipartFile[]::new);
      }
    }

    if (hasInvalidFiles) {
      return ResponseEntity.badRequest().body("Only CSV file allowed. Please try importing again.");
    }

    List<String> uploadedFiles = new ArrayList<>();
    List<String> failedFiles = new ArrayList<>();

    for (MultipartFile file : files) {
      try {
        boolean isStatementFile = processStatementFile(file, user);
        if (isStatementFile) {
          uploadedFiles.add(file.getOriginalFilename());
        } else {
          failedFiles.add(file.getOriginalFilename());
        }
      } catch (Exception e) {
        failedFiles.add(file.getOriginalFilename());
        e.printStackTrace();
      }
    }

    Map<String, List<String>> result = new HashMap<>();
    result.put("uploadedFiles", uploadedFiles);
    result.put("failedFiles", failedFiles);

    return ResponseEntity.ok(result);
  }

  public boolean processStatementFile(MultipartFile file, User user) {
    String bankName = determineBankName(file, user);
    log.info("Bank name is this: " + bankName);
    if (bankName.isEmpty()) {
      return false;
    }

    if (file.isEmpty()) {
      return false;
    }

    // Fetch excluded keywords for the user
    List<Keyword> excludedKeywords = keywordRepository.findAllByUserAndType(user, KeywordType.EXCLUDED);

    try (Scanner scanner = new Scanner(file.getInputStream())) {
      String[] headers = scanner.nextLine().split(";");
      Map<String, Integer> columnIndex = mapColumnIndexes(headers, bankName);
      log.info("Column indexes: " + columnIndex);
      while (scanner.hasNext()) {
        String line = scanner.nextLine();
        String[] columns = line.split(";");
        String date = columns[columnIndex.get("date")].replace(".", "-").replace("\"", "");
        LocalDate formattedDate = parseDate(date, bankName);
        BigDecimal amount = new BigDecimal(columns[columnIndex.get("amount")].replace(",", "."));
        String title = columns[columnIndex.get("title")].toLowerCase().replace("\"", "");
        String referenceId = columns[columnIndex.get("referenceId")].replace("\"", "");
        bankName = bankName.toLowerCase();

        title = title.replaceAll("\\s{2,}", " ");

        if (entryRepository.findByReferenceIdAndBankName(referenceId, bankName) != null) {
          continue;
        }

        if (containsExcludedKeyword(title, excludedKeywords)) {
          continue;
        }

        Entry newEntry = new Entry(formattedDate, amount, title, bankName, referenceId, user);
        entryRepository.save(newEntry);
      }

      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  private LocalDate parseDate(String date, String bankName) {
    switch (bankName) {
      case "s-pankki":
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
      case "op-pankki":
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      default:
        break;
    }
    return LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
  }

  private String determineBankName(MultipartFile file, User user) {
    String bankName = "";
    String filename = file.getOriginalFilename();
    bankName = getBankNameFromFileName(filename);

    if (bankName.isEmpty()) {
      try (Scanner scanner = new Scanner(file.getInputStream())) {
        String[] headers = scanner.nextLine().split(";");
        Map<String, Integer> columnIndex = mapColumnIndexes(headers, bankName);

        while (scanner.hasNext()) {
          String line = scanner.nextLine();
          String[] columns = line.split(";");

          String title = columns[columnIndex.get("title")].toLowerCase().replace("\"", "");
          BigDecimal amount = new BigDecimal(columns[columnIndex.get("amount")].replace(",", "."));

          if ((title.contains(user.getFirstName().toLowerCase() + " " + user.getLastName().toLowerCase())
              || title.contains(user.getLastName().toLowerCase() + " " + user.getFirstName().toLowerCase()))
              && amount.compareTo(BigDecimal.ZERO) >= 0) {
            log.info("Bank name found from BIC" + title);
            log.info("line" + line);
            bankName = BicToBankName(columns[columnIndex.get("bic")]);
            log.info("Bank name found from BIC is :" + bankName);
            if (!bankName.isEmpty()) {
              return bankName;
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return bankName;
  }

  private Map<String, Integer> mapColumnIndexes(String[] headers, String bankName) {
    Map<String, Integer> columnIndex = new HashMap<>();

    for (int i = 0; i < headers.length; i++) {
      String header = headers[i].trim().toLowerCase();
      if (header.contains("kirjauspäivä") || header.contains("päivämäärä")) {
        columnIndex.put("date", i);
      } else if (header.contains("summa") || header.contains("määrä")) {
        columnIndex.put("amount", i);
      } else if (header.contains("saaja/maksaja") || header.equals("saajan nimi")) {
        columnIndex.put("title", i);
      } else if (header.contains("arkistointitunnus")) {
        columnIndex.put("referenceId", i);
      } else if (header.contains("bic")) {
        log.info("BIC found at index: " + i);
        columnIndex.put("bic", i);
      }
    }

    return columnIndex;
  }

  private String BicToBankName(String bic) {
    switch (bic.toLowerCase()) {
      case "sbanfihh":
        return "s-pankki";
      case "okoyfihh":
        return "op-pankki";
      default:
        break;
    }
    return "";
  }

  private String getBankNameFromFileName(String filename) {
    if (filename.toLowerCase().contains("spankki") || filename.toLowerCase().contains("s-pankki")) {
      return "s-pankki";
    } else if (filename.toLowerCase().contains("oppankki") || filename.toLowerCase().contains("op")) {
      return "op-pankki";
    }
    return "";
  }

  public boolean containsExcludedKeyword(String title, List<Keyword> excludedKeywords) {
    if (title == null || excludedKeywords == null) {
      return false;
    }

    String lowerCaseTitle = title.toLowerCase();

    for (Keyword keyword : excludedKeywords) {
      String lowerCaseKeyword = keyword.getKeywords().get(0).toLowerCase();

      if (lowerCaseTitle.contains(lowerCaseKeyword)) {
        return true;
      }
    }

    return false;
  }
}
