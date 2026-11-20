package com.todoratanasov.jobtracker.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.todoratanasov.jobtracker.entity.JobApplication;

public class SortableFieldsTest {

  @Test
  void everySortableFieldExistsOnTheEntity() {
    Set<String> entityFields = Arrays.stream(JobApplication.class.getDeclaredFields())
        .map(Field::getName)
        .collect(Collectors.toSet());

    assertThat(entityFields).containsAll(JobApplicationResponse.SORTABLE_FIELDS);
  }
}
