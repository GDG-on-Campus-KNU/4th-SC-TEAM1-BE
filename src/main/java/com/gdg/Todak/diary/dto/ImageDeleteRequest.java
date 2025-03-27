package com.gdg.Todak.diary.dto;

import jakarta.validation.constraints.NotBlank;

public record ImageDeleteRequest(
        @NotBlank
        String url
) {
}
