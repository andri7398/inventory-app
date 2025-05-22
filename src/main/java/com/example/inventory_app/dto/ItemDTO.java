package com.example.inventory_app.dto;

import jakarta.validation.constraints.NotNull;

public record ItemDTO(
        Long id, String name, Long price, Long qty) {
}
