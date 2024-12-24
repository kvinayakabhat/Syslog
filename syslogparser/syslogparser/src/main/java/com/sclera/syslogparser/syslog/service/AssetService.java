package com.sclera.syslogparser.syslog.service;

import com.sclera.syslogparser.syslog.Severity;
import com.sclera.syslogparser.syslog.dto.AssetDTO;
import com.sclera.syslogparser.syslog.dto.CategoryDTO;
import com.sclera.syslogparser.syslog.dto.IncidentDTO;
import com.sclera.syslogparser.syslog.model.Asset;
import com.sclera.syslogparser.syslog.model.Category;
import com.sclera.syslogparser.syslog.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssetService {

  private final AssetRepository assetRepository;

  @Transactional
  public void saveAssets(List<AssetDTO> assetDTOs) {
    List<Asset> assets = assetDTOs.stream()
        .map(this::convertToEntity)
        .collect(Collectors.toList());

    assetRepository.saveAll(assets);
  }

  public List<AssetDTO> getAssetsByTypes(List<String> types) {
    List<Asset> assets = assetRepository.findByTypeIn(types);
    return assets.stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  public List<AssetDTO> getAssets() {
    List<Asset> assets = assetRepository.findAll();
    return assets.stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
  }

  public void upsertAssets(List<AssetDTO> assetDTOs) {
    for (AssetDTO assetDTO : assetDTOs) {
      upsertSingleAsset(assetDTO);
    }
  }

  private void upsertSingleAsset(AssetDTO assetDTO) {
    Asset existingAsset = assetRepository.findByType(assetDTO.getType())
        .orElse(null);

    if (existingAsset != null) {
      // Update existing asset
      updateExistingAsset(existingAsset, assetDTO);
    } else {
      // Create new asset
      Asset newAsset = convertToEntity(assetDTO);
      assetRepository.save(newAsset);
      log.info("Created new asset of type: {}", assetDTO.getType());
    }
  }

  private void updateExistingAsset(Asset existingAsset, AssetDTO assetDTO) {
    // Clear existing categories
    existingAsset.getCategories().clear();

    // Add new categories
    List<Category> newCategories = assetDTO.getCategories().stream()
        .map(categoryDTO -> {
          Category category = new Category();
          category.setTitle(categoryDTO.getTitle());
          category.setSeverity(Severity.valueOf(categoryDTO.getSeverity()));
          category.setKeywords(categoryDTO.getKeywords());
          category.setAsset(existingAsset);
          return category;
        }).toList();

    existingAsset.getCategories().addAll(newCategories);
    assetRepository.save(existingAsset);
    log.info("Updated existing asset of type: {}", assetDTO.getType());
  }

  private Asset convertToEntity(AssetDTO dto) {
    Asset asset = new Asset();
    asset.setType(dto.getType());

    List<Category> categories = dto.getCategories().stream()
        .map(categoryDTO -> {
          Category category = new Category();
          category.setTitle(categoryDTO.getTitle());
          category.setSeverity(Severity.valueOf(categoryDTO.getSeverity()));
          category.setKeywords(categoryDTO.getKeywords());
          category.setAsset(asset);
          return category;
        })
        .collect(Collectors.toList());

    asset.setCategories(categories);
    return asset;
  }

  private AssetDTO convertToDTO(Asset asset) {
    AssetDTO dto = new AssetDTO();
    dto.setType(asset.getType());

    List<CategoryDTO> categoryDTOs = asset.getCategories().stream()
        .map(category -> {
          CategoryDTO categoryDTO = new CategoryDTO();
          categoryDTO.setTitle(category.getTitle());
          categoryDTO.setSeverity(category.getSeverity().name());
          categoryDTO.setKeywords(category.getKeywords());
          return categoryDTO;
        })
        .collect(Collectors.toList());

    dto.setCategories(categoryDTOs);
    return dto;
  }


  public ResponseEntity<?> createIncident(IncidentDTO incidentDTO) {
    try {

      // Validation
      if (incidentDTO.getDeviceIp() == null) {
        return new ResponseEntity<>("Device ip address not found!", HttpStatus.BAD_REQUEST);
      }

      // Get device by ip address

      // Get network details by device id

      // Save incident

    } catch (Exception e) {
      e.printStackTrace();
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }

  public ResponseEntity<CategoryDTO> getCategoriesByType(String type) {
    try {

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}