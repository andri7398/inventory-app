package com.example.inventory_app.dto;

public record ItemDTO(
        Long id, String name, Long price, Long qty) {
}
