package com.accesscontrol.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequest(
        @NotBlank @Size(max = 64)
        String action,

        @NotBlank @Size(max = 128)
        String resourceType,

        @Size(max = 512)
        String description
) {
}
