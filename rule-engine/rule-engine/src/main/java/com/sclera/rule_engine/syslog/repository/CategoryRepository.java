package com.sclera.rule_engine.syslog.repository;

import com.sclera.rule_engine.syslog.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  @Transactional
  @Modifying
  @Query(value = "UPDATE categories SET title=?3, severity=?2 WHERE id=?1", nativeQuery = true)
  void updateCategory(Long categoryId, String severity, String title);
}
