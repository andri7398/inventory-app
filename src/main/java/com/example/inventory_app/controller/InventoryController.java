package com.example.inventory_app.controller;

import com.example.inventory_app.constant.InventoryType;
import com.example.inventory_app.dto.InventoryDTO;
import com.example.inventory_app.dto.ItemDTO;
import com.example.inventory_app.dto.PaginationDTO;
import com.example.inventory_app.model.Inventory;
import com.example.inventory_app.model.Item;
import com.example.inventory_app.repository.InventoryRepository;
import com.example.inventory_app.repository.ItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryRepository repo;

    private final ItemRepository itemRepo;

    public InventoryController(InventoryRepository repo, ItemRepository itemRepo) {
        this.repo = repo;
        this.itemRepo = itemRepo;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOne(@PathVariable Long id){
        try {
            Optional<Inventory> optionalItem = repo.findById(id);

            return optionalItem.<ResponseEntity<Object>>map(item -> {
                InventoryDTO dto = new InventoryDTO(item.getId(), item.getItem().getId(), item.getQty(), item.getType().name());
                return ResponseEntity.status(HttpStatus.OK).body(dto);
            }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Item with ID " + id + " not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

    @PostMapping("/getAll")
    public ResponseEntity<Object> getAll(PaginationDTO dto){
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

            List<InventoryDTO> list = repo.findAll(pagination)
                    .stream()
                    .map(inventory -> new InventoryDTO(
                            inventory.getId(),
                            inventory.getItem().getId(),
                            inventory.getQty(),
                            inventory.getType().name()
                    )).toList();
            return ResponseEntity.status(HttpStatus.OK).body(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/insertOrUpdate")
    public ResponseEntity<Object> insertOrUpdate(@RequestBody InventoryDTO dto){
        if (dto.itemId() == null ) {
            return ResponseEntity.badRequest().body("Item id is required");
        }

        if (dto.qty() == null || dto.qty() < 0) {
            return ResponseEntity.badRequest().body("Quantity is required and must be non-negative");
        }

        if (dto.type() == null || dto.type().isEmpty()) {
            return ResponseEntity.badRequest().body("Type is required");
        }

        try {
            Inventory data;

            if (dto.id() != null) {
                Optional<Inventory> optional = repo.findById(dto.id());
                data = optional.orElseGet(Inventory::new);
            } else {
                data = new Inventory();
            }

            // Always fetch the Item from DB
            Optional<Item> itemOptional = itemRepo.findById(dto.itemId());
            if (itemOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Item with ID " + dto.itemId() + " not found.");
            }
            Item dataItem = itemOptional.get();

            if(dto.type().equalsIgnoreCase(InventoryType.W.name())){
                if(dataItem.getQty() < dto.qty()){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Item with name" + dataItem.getName() + " is insufficient to withdraw.");
                }

                dataItem.setQty(dataItem.getQty()-dto.qty());
            }else{
                dataItem.setQty(dataItem.getQty()+dto.qty());
            }
            itemRepo.save(dataItem);

            data.setItem(dataItem);
            data.setQty(dto.qty());
            data.setType(InventoryType.valueOf(dto.type()));

            Inventory saved = repo.save(data);

            InventoryDTO result = new InventoryDTO(saved.getId(), saved.getItem().getId(), saved.getQty(), saved.getType().name());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id){
        try {
            Optional<Inventory> optionalItem = repo.findById(id);

            if(optionalItem.isPresent()){
                Inventory inventory = optionalItem.get();
                inventory.setDeleted(true);
                repo.save(inventory);
                return ResponseEntity.status(HttpStatus.OK).body("Data with id " + id + " has successfully deleted");
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item with ID " + id + " not found");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
