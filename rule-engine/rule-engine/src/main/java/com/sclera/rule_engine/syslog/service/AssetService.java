package com.sclera.rule_engine.syslog.service;

import com.sclera.rule_engine.syslog.Severity;
import com.sclera.rule_engine.syslog.dto.AssetDTO;
import com.sclera.rule_engine.syslog.dto.CategoryDTO;
import com.sclera.rule_engine.syslog.model.Asset;
import com.sclera.rule_engine.syslog.model.Category;
import com.sclera.rule_engine.syslog.repository.AssetRepository;
import com.sclera.rule_engine.syslog.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssetService {

  private final AssetRepository assetRepository;
  private final CategoryRepository categoryRepository;

  public void createAsset(AssetDTO assetDTO) {
    try {
      Asset asset = this.convertToEntity(assetDTO);
      assetRepository.save(asset);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void deleteAsset(String type) {
    try {
      Optional<Asset> asset = assetRepository.findByType(type);
      if (asset.isPresent()) {
        assetRepository.deleteById(type);
      } else {
        System.out.println("Asset type not found!");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
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

  private Asset convertToEntity(AssetDTO dto) {
    Asset asset = new Asset();
    asset.setType(dto.getType());

    if (dto.getCategories() != null) {
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
    }

    return asset;
  }

  private AssetDTO convertToDTO(Asset asset) {
    AssetDTO dto = new AssetDTO();
    dto.setType(asset.getType());

    if (asset.getCategories() != null) {
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
    }
    return dto;
  }

  public void createCategory(String type, CategoryDTO categoryDTO) {
    try {
      Category category = new Category();
      category.setTitle(categoryDTO.getTitle());
      category.setSeverity(Severity.valueOf(categoryDTO.getSeverity()));
      category.setKeywords(categoryDTO.getKeywords());
      category.setAsset(assetRepository.getReferenceById(type));
      categoryRepository.save(category);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void createKeyword(String type, Long categoryId, List<String> keywords) {
    Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
    if (categoryOpt.isPresent() && keywords != null) {
      Category category = categoryOpt.get();
      category.getKeywords().addAll(keywords);
      categoryRepository.save(category);
    } else {
      throw new IllegalArgumentException("Category not found");
    }
  }

  @Transactional
  public void updateCategory(String type, Long categoryId, CategoryDTO categoryDTO) {
    Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
    if (categoryOpt.isPresent()) {
      categoryRepository.updateCategory(categoryId, categoryDTO.getSeverity(), categoryDTO.getTitle());
    } else {
      throw new IllegalArgumentException("Category not found");
    }
  }

  @Transactional
  public void updateKeyword(String type, Long categoryId, List<String> keywords) {
    Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
    if (categoryOpt.isPresent()) {
      Category category = categoryOpt.get();
      category.getKeywords().clear();
      category.getKeywords().addAll(keywords);
      categoryRepository.save(category);
    } else {
      throw new IllegalArgumentException("Category not found");
    }
  }
}