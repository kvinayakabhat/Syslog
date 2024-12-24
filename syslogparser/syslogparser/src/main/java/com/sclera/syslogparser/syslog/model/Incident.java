package com.sclera.syslogparser.syslog.model;

import com.sclera.syslogparser.syslog.Severity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "incident")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Incident {

  @Id
  private String id;

  @Column(nullable = false)
  private Severity severity;

  @Column(nullable = false)
  private String category;

  @Column(nullable = false)
  private String message;

//  @OneToOne(mappedBy = "device", cascade = CascadeType.ALL,orphanRemoval = true)
//  private Device device;

  //  @OneToOne(mappedBy = "docker", cascade = CascadeType.ALL,orphanRemoval = true)
//  private Docker docker;

  @Column(nullable = false)
  private BigInteger timestamp;


}
