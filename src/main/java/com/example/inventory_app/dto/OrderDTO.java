package com.example.inventory_app.dto;

public record OrderDTO(
        String orderNo,
        Long itemId,
        Long qty,
        Long price

) {
}
