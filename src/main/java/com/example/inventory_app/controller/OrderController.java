package com.example.inventory_app.controller;

import com.example.inventory_app.dto.InventoryDTO;
import com.example.inventory_app.dto.OrderDTO;
import com.example.inventory_app.dto.PaginationDTO;
import com.example.inventory_app.model.Inventory;
import com.example.inventory_app.model.Item;
import com.example.inventory_app.model.Orders;
import com.example.inventory_app.repository.ItemRepository;
import com.example.inventory_app.repository.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/order")
public class OrderController {
    private final OrderRepository repo;
    private final ItemRepository itemRepo;

    public OrderController(OrderRepository repo, ItemRepository itemRepo) {
        this.repo = repo;
        this.itemRepo = itemRepo;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOne(@PathVariable String id){
        try {
            Optional<Orders> optionalItem = repo.findById(id.toUpperCase());

            return optionalItem.<ResponseEntity<Object>>map(item -> {
                OrderDTO dto = new OrderDTO(item.getOrderNo(), item.getItem().getId(), item.getQty(), item.getPrice());
                return ResponseEntity.status(HttpStatus.OK).body(dto);
            }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Item with ID " + id + " not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

    public Orders createOrder(Orders orders) {
        // Generate order number based on current count + 1
        if(orders.getOrderNo() == null){
            long count = repo.count() + 1;
            String orderNo = "O" + count;
            orders.setOrderNo(orderNo);
        }

        return repo.save(orders);
    }

    @PostMapping("/getAll")
    public ResponseEntity<Object> getAll(@RequestBody PaginationDTO dto){
        if (dto.page() == null) {
            return ResponseEntity.badRequest().body("Page is required");
        }
        if (dto.size() == null || dto.size() < 0) {
            return ResponseEntity.badRequest().body("Size is required and not 0");
        }

        try {
            Integer size = dto.size() == null? 10 : dto.size();
            Integer page = dto.page() == null? 0 : dto.page();

            Pageable pagination = PageRequest.of(page,size);

            List<OrderDTO> list = repo.findAll(pagination)
                    .stream()
                    .map(item -> new OrderDTO(
                            item.getOrderNo(),
                            item.getItem().getId(),
                            item.getPrice(),
                            item.getQty()
                    )).toList();
            return ResponseEntity.status(HttpStatus.OK).body(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/insertOrUpdate")
    public ResponseEntity<Object> insertOrUpdate(@RequestBody OrderDTO dto){
        if (dto.itemId() == null ) {
            return ResponseEntity.badRequest().body("Item id is required");
        }

        if (dto.qty() == null || dto.qty() < 0) {
            return ResponseEntity.badRequest().body("Quantity is required and must be non-negative");
        }

        if (dto.price() == null ) {
            return ResponseEntity.badRequest().body("Price is required");
        }

        try {
            Orders data;

            // Always fetch the Item from DB
            Optional<Item> itemOptional = itemRepo.findById(dto.itemId());
            if (itemOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Item with ID " + dto.itemId() + " not found.");
            }

            Item dataItem = itemOptional.get();

            if (dto.orderNo() != null) {
                Optional<Orders> optional = repo.findById(dto.orderNo());

                if (optional.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Order with Order no " + dto.orderNo() + " not found");
                }

                data = optional.get();

                Long diff = 0L;

                if(data.getQty() < dto.qty()){
                    diff = dto.qty() - data.getQty();
                    if(dataItem.getQty()-diff < 0){
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Item with name " + dataItem.getName() + " is insufficient to withdraw.");
                    }
                    dataItem.setQty(dataItem.getQty()-diff);
                }else{
                    diff = data.getQty() - dto.qty();
                    dataItem.setQty(dataItem.getQty()+diff);
                }

            } else {
                data = new Orders();

                if(dataItem.getQty() < dto.qty()){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Item with name " + dataItem.getName() + " is insufficient to withdraw.");
                }

                dataItem.setQty(dataItem.getQty()-dto.qty());
            }



            itemRepo.save(dataItem);

            data.setItem(dataItem);
            data.setQty(dto.qty());
            data.setPrice(dto.price());

            Orders saved = createOrder(data);

            OrderDTO result = new OrderDTO(saved.getOrderNo(), saved.getItem().getId(), saved.getQty(), saved.getPrice());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{orderNo}")
    public ResponseEntity<Object> delete(@PathVariable String orderNo){
        try {
            Optional<Orders> optionalItem = repo.findById(orderNo);

            if(optionalItem.isPresent()){
                Orders order = optionalItem.get();
                order.setDeleted(true);
                repo.save(order);
                return ResponseEntity.status(HttpStatus.OK).body("Data with id " + orderNo + " has successfully deleted");
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item with ID " + orderNo + " not found");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
