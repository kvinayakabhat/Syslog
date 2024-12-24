package com.sclera.syslogparser.syslog.controller;

import com.sclera.syslogparser.syslog.dto.AssetDTO;
import com.sclera.syslogparser.syslog.dto.CategoryDTO;
import com.sclera.syslogparser.syslog.dto.IncidentDTO;
import com.sclera.syslogparser.syslog.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@CrossOrigin(value = "*", origins = "*")
public class AssetController {

  private final AssetService assetService;

  @PostMapping
  public ResponseEntity<Void> createAssets(@RequestBody List<AssetDTO> assets) {
    assetService.saveAssets(assets);
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

  @PostMapping("/createIncident")
  public ResponseEntity<?> createIncident(@RequestBody IncidentDTO incidentDTO) {
    return assetService.createIncident(incidentDTO);
  }

  @GetMapping("/type/{type}")
  public ResponseEntity<CategoryDTO> getCategoriesByType(@PathVariable String type) {
    return assetService.getCategoriesByType(type);
  }

}