package com.shoppermart.inventoryservice.Controller;

import com.shoppermart.inventoryservice.Model.Inventory;
import com.shoppermart.inventoryservice.Service.InventoryService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }
    
    @GetMapping("/sku/{skuCode}")
    public ResponseEntity<Inventory> getInventoryBySkuCode(@PathVariable String skuCode) {
        return ResponseEntity.ok(inventoryService.getInventoryBySkuCode(skuCode));
    }
    
    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestBody Inventory inventory) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createInventory(inventory));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long id, @RequestBody Inventory inventoryDetails) {
        return ResponseEntity.ok(inventoryService.updateInventory(id, inventoryDetails));
    }
    
    @PutMapping("/sku/{skuCode}/quantity")
    public ResponseEntity<Inventory> updateQuantity(@PathVariable String skuCode, @RequestParam Integer quantity) {
        return ResponseEntity.ok(inventoryService.updateQuantity(skuCode, quantity));
    }
    
    @PutMapping("/sku/{skuCode}/decrement")
    public ResponseEntity<Inventory> decrementQuantity(@PathVariable String skuCode, @RequestParam Integer amount) {
        return ResponseEntity.ok(inventoryService.decrementQuantity(skuCode, amount));
    }
    
    @PutMapping("/sku/{skuCode}/increment")
    public ResponseEntity<Inventory> incrementQuantity(@PathVariable String skuCode, @RequestParam Integer amount) {
        return ResponseEntity.ok(inventoryService.incrementQuantity(skuCode, amount));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }
}
