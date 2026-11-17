package com.todoratanasov.jobtracker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.todoratanasov.jobtracker.controller.ApiPaths;

import java.util.Map;

@RestController
public class HealthController {

  @GetMapping(ApiPaths.HEALTH)
  public Map<String, String> health() {
    return Map.of("status", "ok");
  }
}