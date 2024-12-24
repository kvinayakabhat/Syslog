package com.sclera.rule_engine.syslog.repository;

import com.sclera.rule_engine.syslog.model.Asset;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AssetRepository extends JpaRepository<Asset, String> {

  @Query("SELECT a FROM Asset a WHERE a.type IN :types")
  List<Asset> findByTypeIn(List<String> types);

  Optional<Asset> findByType(String type);
}
