package com.sclera.syslogparser.syslog.model;

import com.sclera.syslogparser.syslog.Severity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Severity severity;

  @ElementCollection
  @CollectionTable(name = "category_keywords", joinColumns = @JoinColumn(name = "category_id"))
  @Column(name = "keyword")
  private List<String> keywords;

  @ManyToOne
  @JoinColumn(name = "asset_id")
  private Asset asset;
}