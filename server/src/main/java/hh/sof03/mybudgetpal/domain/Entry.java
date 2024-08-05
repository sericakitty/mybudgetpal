package hh.sof03.mybudgetpal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
public class Entry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private LocalDate date;

  private BigDecimal amount;

  private String title;

  @Column(name = "reference_id")
  private String referenceId;
  
  @Column(name = "bank_name")
  private String bankName;
  
  @ManyToOne
  @JsonIgnoreProperties("entries")
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
  
  public Entry() {
  }

  public Entry(LocalDate date, BigDecimal amount, String title, String bankname, String referenceid, User user) {
    this.date = date;
    this.amount = amount;
    this.title = title;
    this.bankName = bankname;
    this.referenceId = referenceid;
    this.user = user;
  }

  public Long getId() {
    return id;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getTitle() {

    String[] words = title.split(" ");
    for (int i = 0; i < words.length; i++) {
        if (words[i].length() > 0) {
            words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
        }
    }
    return String.join(" ", words);
  }

  public void setTitle(String title) {
    this.title = title.toLowerCase();
  }

  public String getBankName() {
    bankName = bankName.substring(0, 1).toUpperCase() + bankName.substring(1);
    return bankName;
  }

  public void setBankName(String bankName) {
    this.bankName = bankName.toLowerCase();
  }

  public String getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId.toLowerCase();
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
    
} 
