package com.sclera.syslogparser.syslog.dto;

import com.sclera.syslogparser.syslog.Severity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@ToString
@Getter
@Setter
public class IncidentDTO {

  private Severity level;
  private String category;
  private String message;
  private String deviceIp;
  private String deviceType;
}
