package com.sclera.syslogparser.syslog.dto;

import java.util.List;
import lombok.Data;

@Data
public class CategoryDTO {
  private String title;
  private String severity;
  private List<String> keywords;
}
