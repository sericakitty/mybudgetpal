package hh.sof03.mybudgetpal.domain;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@Entity
public class Keyword {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @ElementCollection
  @CollectionTable(name = "keyword_keywords", joinColumns = @JoinColumn(name = "keyword_id"))
  @Column(name = "keyword", nullable = false)
  private List<String> keywords;

  @Column(name = "category", nullable = false)
  private String category;

  @Column(name = "type", nullable = false)
  @Enumerated(EnumType.STRING)
  private KeywordType type;

  @ManyToOne
  @JsonIgnoreProperties("keywords")
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  public Keyword() {}

  public Keyword(List<String> keywords, String category, KeywordType type, User user) {
    this.keywords = keywords;
    this.category = category;
    this.type = type;
    this.user = user;
  }

  public Long getId() {
    return id;
  }

  public List<String> getKeywords() {
    return keywords;
  }

  public void setKeywords(List<String> keywords) {
    this.keywords = keywords;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public KeywordType getType() {
    return type;
  }

  public void setType(KeywordType type) {
    this.type = type;
  }

  public String getKeywordsAsString() {
    return String.join(", ", keywords);
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public boolean doesKeywordExist(String keyword) {
    return this.keywords.contains(keyword.toLowerCase());
  }

}
