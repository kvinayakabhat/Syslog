package com.sclera.syslogparser.syslog.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebsocketTrigger {

  private SimpMessagingTemplate simpMessagingTemplate;

  public <T> void invoke(String endpoint, T payload) {
    this.simpMessagingTemplate.convertAndSend(endpoint, payload);
  }

}
