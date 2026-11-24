package com.todoratanasov.jobtracker.config;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "JobTracker API",
        version = "1.0",
        description = "Track job applications and interview notes. Log in via /api/auth/login, then use "
            + "the Authorize button with the access token."
    ),
    tags = {
        @Tag(name = OpenApiConfig.TAG_AUTH, description = "Public endpoints for accounts and tokens"),
        @Tag(
            name = OpenApiConfig.TAG_JOB_APPLICATIONS,
            description = "Your own applications. Every route needs a Bearer token."
        ),
        @Tag(name = OpenApiConfig.TAG_NOTES, description = "Notes attached to one of your job applications.")
    }
)
@SecurityScheme(
    name = OpenApiConfig.BEARER_AUTH,
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {

  public static final String BEARER_AUTH = "bearerAuth";

  public static final String TAG_AUTH = "Authentication";
  public static final String TAG_JOB_APPLICATIONS = "Job applications";
  public static final String TAG_NOTES = "Notes";

  // Names of the shared responses, as references the controllers can use in @ApiResponse(ref = ...)
  public static final String BAD_REQUEST = "#/components/responses/BadRequest";
  public static final String UNAUTHORIZED = "#/components/responses/Unauthorized";
  public static final String NOT_FOUND = "#/components/responses/NotFound";
  public static final String EMAIL_CONFLICT = "#/components/responses/EmailConflict";
  public static final String INVALID_CREDENTIALS = "#/components/responses/InvalidCredentials";
  public static final String INVALID_REFRESH_TOKEN = "#/components/responses/InvalidRefreshToken";

  private static final String PROBLEM_SCHEMA_REF = "#/components/schemas/Problem";
  private static final String FIELD_ERROR_SCHEMA_REF = "#/components/schemas/ProblemFieldError";

  private static final List<String> TAG_ORDER = List.of(TAG_AUTH, TAG_JOB_APPLICATIONS, TAG_NOTES);

  @Bean
  public OpenApiCustomizer sharedErrorResponses() {
    return openApi -> {
      Components components = openApi.getComponents() == null
          ? new Components()
          : openApi.getComponents();
      openApi.setComponents(components);

      components.addSchemas(
          "ProblemFieldError", new ObjectSchema()
              .addProperty("field", new StringSchema().example("company"))
              .addProperty("message", new StringSchema().example("must not be blank"))
      );

      components.addSchemas(
          "Problem", new ObjectSchema()
              .description("Error body following RFC 9457 (application/problem+json)")
              .addProperty("title", new StringSchema().example("Validation failed"))
              .addProperty("status", new IntegerSchema().example(400))
              .addProperty("detail", new StringSchema().example("Request validation failed"))
              .addProperty("instance", new StringSchema().example("/api/job-applications"))
              .addProperty(
                  "errors", new ArraySchema()
                      .items(new Schema<Object>().$ref(FIELD_ERROR_SCHEMA_REF))
                      .description("Only present on validation failures: one entry per invalid field")
              )
      );

      components.addResponses(
          "BadRequest", problem(
              "Invalid request. Body validation failures list each invalid field in `errors`; malformed JSON, "
                  + "an invalid id and an unknown sort field return a problem without `errors`."
          )
      );
      components.addResponses(
          "NotFound", problem(
              "No such resource, or it belongs to another user (the two are deliberately indistinguishable)."
          )
      );
      components.addResponses("EmailConflict", problem("The email is already registered."));
      components.addResponses("InvalidCredentials", problem("Wrong email or password (the message never says which)."));
      components.addResponses(
          "InvalidRefreshToken", problem(
              "The refresh token is unknown, expired, or was already used. Reusing a used token "
                  + "revokes the whole login session."
          )
      );

      // Produced by Spring Security's filter before our code runs, so it has no JSON body
      components.addResponses(
          "Unauthorized", new ApiResponse()
              .description(
                  "Missing, invalid or expired access token. The body is empty; see the "
                      + "WWW-Authenticate header."
              )
      );
    };
  }

  @Bean
  public OpenApiCustomizer orderedPaths() {
    return openApi -> {
      Paths paths = openApi.getPaths();
      Map<String, PathItem> copy = new LinkedHashMap<>(paths);
      paths.clear();
      copy.entrySet().stream()
          .sorted(
              Comparator
                  .comparingInt((Map.Entry<String, PathItem> entry) -> tagIndex(entry.getValue()))
                  .thenComparing(Map.Entry::getKey)
          )
          .forEach(entry -> paths.addPathItem(entry.getKey(), entry.getValue()));
    };
  }

  private static int tagIndex(PathItem pathItem) {
    return pathItem.readOperations().stream()
        .flatMap(operation -> operation.getTags() == null ? Stream.<String>empty() : operation.getTags().stream())
        .mapToInt(TAG_ORDER::indexOf)
        .filter(index -> index >= 0)
        .min()
        .orElse(TAG_ORDER.size());
  }

  private static ApiResponse problem(String description) {
    return new ApiResponse()
        .description(description)
        .content(
            new Content().addMediaType(
                org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                new MediaType().schema(new Schema<Object>().$ref(PROBLEM_SCHEMA_REF))
            )
        );
  }
}
