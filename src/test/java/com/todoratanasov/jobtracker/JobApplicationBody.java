package com.todoratanasov.jobtracker;

import java.util.UUID;

public record JobApplicationBody(UUID id, String company, String position, String status, Integer salary) {

}
