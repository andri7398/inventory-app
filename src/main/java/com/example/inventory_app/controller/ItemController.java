package com.example.inventory_app.controller;

import com.example.inventory_app.dto.InventoryDTO;
import com.example.inventory_app.dto.ItemDTO;
import com.example.inventory_app.dto.PaginationDTO;
import com.example.inventory_app.model.Item;
import com.example.inventory_app.repository.ItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/item")
public class ItemController {

    private final ItemRepository repo;

    public ItemController(ItemRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOne(@PathVariable Long id){

        try {
            Optional<Item> optionalItem = repo.findById(id);

            return optionalItem.<ResponseEntity<Object>>map(item -> {
                ItemDTO dto = new ItemDTO(item.getId(), item.getName(), item.getPrice(), item.getQty());
                return ResponseEntity.status(HttpStatus.OK).body(dto);
            }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Item with ID " + id + " not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

    @PostMapping("/getAll")
    public ResponseEntity<Object> getAll(@RequestBody PaginationDTO dto){
        try {
            Integer size = dto.size() == null? 10 : dto.size();
            Integer page = dto.page() == null? 0 : dto.page();

            Pageable pagination = PageRequest.of(page,size);

            List<ItemDTO> list = repo.findAll(pagination)
                    .stream()
                    .map(item -> new ItemDTO(
                            item.getId(),
                            item.getName(),
                            item.getPrice(),
                            item.getQty()
                    )).toList();
            return ResponseEntity.status(HttpStatus.OK).body(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/insertOrUpdate")
    public ResponseEntity<Object> insertOrUpdate(@RequestBody ItemDTO dto){
        try {
            Item data = new Item();

            if(dto.id() != null){
                Optional<Item> optional = repo.findById(dto.id());

                if(optional.isPresent()){
                    data = optional.get();
                }
            }

            data.setName(dto.name());
            data.setPrice(dto.price());
            data.setQty(dto.qty());

            Item saved = repo.save(data);

            ItemDTO result = new ItemDTO(saved.getId(), saved.getName(), saved.getPrice(), saved.getQty());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id){
        try {
            Optional<Item> optionalItem = repo.findById(id);

            if(optionalItem.isPresent()){
                repo.delete(optionalItem.get());
                return ResponseEntity.status(HttpStatus.OK).body("Data with id " + id + " has successfully deleted");
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item with ID " + id + " not found");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
