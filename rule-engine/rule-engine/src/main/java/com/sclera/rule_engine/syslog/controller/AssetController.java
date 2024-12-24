package com.sclera.rule_engine.syslog.controller;

import com.sclera.rule_engine.syslog.dto.AssetDTO;
import com.sclera.rule_engine.syslog.dto.CategoryDTO;
import com.sclera.rule_engine.syslog.service.AssetService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
@CrossOrigin(value = "*", origins = "*")
public class AssetController {

  private final AssetService assetService;

  @PostMapping
  public ResponseEntity<Void> createAsset(@RequestBody AssetDTO assetDTO) {
    assetService.createAsset(assetDTO);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/type/{type}")
  public ResponseEntity<Void> deleteAsset(@PathVariable String type) {
    assetService.deleteAsset(type);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/types")
  public ResponseEntity<List<AssetDTO>> getAssetByType(@RequestBody List<String> types) {
    return ResponseEntity.ok(assetService.getAssetsByTypes(types));
  }

  @GetMapping("/all")
  public ResponseEntity<List<AssetDTO>> getAssets() {
    return ResponseEntity.ok(assetService.getAssets());
  }

  @PostMapping("/type/{type}/category")
  public ResponseEntity<Void> createCategory(@PathVariable String type, @RequestBody CategoryDTO categoryDTO) {
    assetService.createCategory(type, categoryDTO);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/type/{type}/category/{categoryId}/keyword")
  public ResponseEntity<Void> createKeyword(@PathVariable String type, @PathVariable Long categoryId, @RequestBody List<String> keywords) {
    assetService.createKeyword(type, categoryId, keywords);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/type/{type}/category/{categoryId}")
  public ResponseEntity<Void> updateCategory(@PathVariable String type, @PathVariable Long categoryId, @RequestBody CategoryDTO categoryDTO) {
    assetService.updateCategory(type, categoryId, categoryDTO);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/type/{type}/category/{categoryId}/keyword")
  public ResponseEntity<Void> updateKeyword(@PathVariable String type, @PathVariable Long categoryId, @RequestBody List<String> keywords) {
    assetService.updateKeyword(type, categoryId, keywords);
    return ResponseEntity.ok().build();
  }

}