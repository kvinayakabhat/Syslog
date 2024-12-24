package com.sclera.syslogparser.syslog.scheduler;

import com.sclera.syslogparser.syslog.dto.AssetDTO;
import com.sclera.syslogparser.syslog.service.AssetService;
import com.sclera.syslogparser.syslog.websocket.WebsocketTrigger;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@EnableScheduling
@Component
@RequiredArgsConstructor
@Slf4j
public class AssetScheduler {

  private final AssetService assetService;
  private final RestTemplate restTemplate;
  private final WebsocketTrigger websocketTrigger;

  @Value("${external.api.url}")
  private String externalApiUrl;

  // Run every hour
  @Scheduled(fixedRate = 3600000) // 3600000 ms = 1 hour
  public void syncAssets() {
    log.info("Starting asset sync at {}", LocalDateTime.now());

    try {
      // Fetch assets from external API
      ResponseEntity<AssetDTO[]> response = restTemplate.getForEntity(
          externalApiUrl,
          AssetDTO[].class
      );

      if (response.getBody() != null) {
        List<AssetDTO> assets = Arrays.asList(response.getBody());
        log.info("Fetched {} assets from external API", assets.size());

        // Update assets in database
        assetService.upsertAssets(assets);
        log.info("Successfully synchronized assets");

        // Send updated logs to python code
        websocketTrigger.invoke("/topic/syslog", assetService.getAssets());

      } else {
        log.warn("Received empty response from external API");
      }
    } catch (Exception e) {
      log.error("Error during asset synchronization", e);
    }
  }

}
