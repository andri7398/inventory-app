package com.example.inventory_app.dto;

import com.example.inventory_app.constant.InventoryType;
import com.example.inventory_app.model.Item;

public record InventoryDTO(
        Long id,
        Long itemId,
        Long qty,
        String type
) {

}
