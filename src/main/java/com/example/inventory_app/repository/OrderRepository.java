package com.example.inventory_app.repository;

import com.example.inventory_app.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Orders, String> {
}
