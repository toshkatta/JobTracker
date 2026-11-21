package com.todoratanasov.jobtracker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.todoratanasov.jobtracker.config.OpenApiConfig;
import com.todoratanasov.jobtracker.controller.ApiPaths;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@Tag(name = OpenApiConfig.TAG_HEALTH)
@RestController
public class HealthController {

  @Operation(summary = "Liveness check")
  @ApiResponse(responseCode = "200", description = "The service is up")
  @GetMapping(ApiPaths.HEALTH)
  public Map<String, String> health() {
    return Map.of("status", "ok");
  }
}
