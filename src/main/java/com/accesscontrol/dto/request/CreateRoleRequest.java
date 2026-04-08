package com.accesscontrol.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateRoleRequest(
        @NotBlank @Size(max = 128)
        String name,

        @Size(max = 512)
        String description,

        UUID parentRoleId
) {
}
