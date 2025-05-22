package com.example.inventory_app;

import com.example.inventory_app.constant.InventoryType;
import com.example.inventory_app.controller.InventoryController;
import com.example.inventory_app.controller.ItemController;
import com.example.inventory_app.dto.InventoryDTO;
import com.example.inventory_app.dto.ItemDTO;
import com.example.inventory_app.model.Inventory;
import com.example.inventory_app.model.Item;
import com.example.inventory_app.repository.InventoryRepository;
import com.example.inventory_app.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;


import java.util.Optional;

@AutoConfigureMockMvc
@SpringBootTest
class InventoryAppApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Mock
	private ItemRepository itemRepository;

	@InjectMocks
	private ItemController itemController;

	@Mock
	private InventoryRepository inventoryRepository;

	@InjectMocks
	private InventoryController inventoryController;

	@Test
	void getOne_WhenItemExists_ReturnsItemDTO() {
		Long itemId = 1L;
		Item mockItem = new Item();
		mockItem.setId(itemId);
		mockItem.setName("Test Item");
		mockItem.setPrice(10L);
		mockItem.setQty(100L);
		mockItem.setDeleted(false);
		when(itemRepository.findById(itemId)).thenReturn(Optional.of(mockItem));

		ResponseEntity<Object> response = itemController.getOne(itemId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody() instanceof ItemDTO);

		ItemDTO result = (ItemDTO) response.getBody();
		assertEquals(itemId, result.id());
		assertEquals("Test Item", result.name());
		assertEquals(10L, result.price());
		assertEquals(100L, result.qty());

		verify(itemRepository, times(1)).findById(itemId);
	}

	@Test
	void insertOrUpdate_WithValidItem_InsertsNewItem() {
		ItemDTO newItemDTO = new ItemDTO(null, "New Item", 15L, 50L);
		Item savedItem = new Item();
		savedItem.setId(1L);
		savedItem.setName("New Item");
		savedItem.setPrice(15L);
		savedItem.setQty(50L);

		when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

		ResponseEntity<Object> response = itemController.insertOrUpdate(newItemDTO);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertTrue(response.getBody() instanceof ItemDTO);

		ItemDTO result = (ItemDTO) response.getBody();
		assertEquals(1L, result.id());
		assertEquals("New Item", result.name());
		assertEquals(15L, result.price());
		assertEquals(50L, result.qty());

		verify(itemRepository, times(1)).save(any(Item.class));
	}

	@Test
	void getOne_WhenItemExists_ReturnsInventoryDTO() {
		Long inventoryId = 1L;
		Long itemId = 1L;

		Item mockItem = new Item();
		mockItem.setId(itemId);
		mockItem.setName("Test Item");
		mockItem.setPrice(10L);
		mockItem.setQty(100L);
		mockItem.setDeleted(false);

		Inventory mockInventory = new Inventory();
		mockInventory.setId(inventoryId);
		mockInventory.setItem(mockItem);
		mockInventory.setType(InventoryType.T);
		mockInventory.setQty(3L);
		mockInventory.setDeleted(false);

		when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(mockInventory));

		ResponseEntity<Object> response = inventoryController.getOne(inventoryId);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertTrue(response.getBody() instanceof InventoryDTO);

		InventoryDTO result = (InventoryDTO) response.getBody();
		assertEquals(itemId, result.id());
		assertEquals(1L, result.itemId());
		assertEquals(InventoryType.T.name(), result.type());
		assertEquals(3L, result.qty());

		verify(inventoryRepository, times(1)).findById(inventoryId);
	}


	@Test
	void insertOrUpdate_WithValidItem_InsertsNewInventory() {

		Long itemId = 1L;
		Item savedItem = new Item();
		savedItem.setId(1L);
		savedItem.setName("Test Item");
		savedItem.setPrice(10L);
		savedItem.setQty(100L);
		savedItem.setDeleted(false);

		when(itemRepository.findById(itemId)).thenReturn(Optional.of(savedItem));

		InventoryDTO newInventoryDTO = new InventoryDTO(null, 1L, 15L, InventoryType.T.name());
		Inventory savedInventory = new Inventory();

		savedInventory.setId(1L);
		savedInventory.setItem(savedItem);
		savedInventory.setType(InventoryType.T);
		savedInventory.setQty(50L);

		when(inventoryRepository.save(any(Inventory.class))).thenReturn(savedInventory);

		// Act
		ResponseEntity<Object> response = inventoryController.insertOrUpdate(newInventoryDTO);

		// Assert
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertTrue(response.getBody() instanceof InventoryDTO);

		InventoryDTO result = (InventoryDTO) response.getBody();
		assertEquals(1L, result.id());
		assertEquals(1L, result.itemId());
		assertEquals(InventoryType.T.name(), result.type());
		assertEquals(50L, result.qty());

		verify(inventoryRepository, times(1)).save(any(Inventory.class));
	}

}
