package com.sclera.syslogparser.syslog.dto;

import java.util.List;
import lombok.Data;

@Data
public class AssetDTO {

  private String type;
  private List<CategoryDTO> categories;
}
