package com.shoppermart.inventoryservice.Service;

import com.shoppermart.inventoryservice.GlobalException.ResourceNotFoundException;
import com.shoppermart.inventoryservice.Model.Inventory;
import com.shoppermart.inventoryservice.Repo.InventoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }
    
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }
    
    public Inventory getInventoryById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
    }
    
    public Inventory getInventoryBySkuCode(String skuCode) {
        return inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with SKU code: " + skuCode));
    }
    
    public Inventory createInventory(Inventory inventory) {
        if (inventory.getSkuCode() == null || inventory.getSkuCode().trim().isEmpty()) {
            throw new IllegalArgumentException("SKU code cannot be empty");
        }
        if (inventory.getQuantity() == null || inventory.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity must be a positive number");
        }
        return inventoryRepository.save(inventory);
    }
    
    public Inventory updateInventory(Long id, Inventory inventoryDetails) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
        
        if (inventoryDetails.getSkuCode() != null && !inventoryDetails.getSkuCode().trim().isEmpty()) {
            inventory.setSkuCode(inventoryDetails.getSkuCode());
        }
        if (inventoryDetails.getQuantity() != null && inventoryDetails.getQuantity() >= 0) {
            inventory.setQuantity(inventoryDetails.getQuantity());
        }
        
        return inventoryRepository.save(inventory);
    }
    
    public Inventory updateQuantity(String skuCode, Integer quantity) {
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with SKU code: " + skuCode));
        
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Quantity must be a positive number");
        }
        
        inventory.setQuantity(quantity);
        return inventoryRepository.save(inventory);
    }
    
    public Inventory decrementQuantity(String skuCode, Integer amount) {
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with SKU code: " + skuCode));
        
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Decrement amount must be a positive number");
        }
        
        if (inventory.getQuantity() < amount) {
            throw new IllegalArgumentException("Insufficient inventory. Available: " + inventory.getQuantity() + ", Requested: " + amount);
        }
        
        inventory.setQuantity(inventory.getQuantity() - amount);
        return inventoryRepository.save(inventory);
    }
    
    public Inventory incrementQuantity(String skuCode, Integer amount) {
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with SKU code: " + skuCode));
        
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Increment amount must be a positive number");
        }
        
        inventory.setQuantity(inventory.getQuantity() + amount);
        return inventoryRepository.save(inventory);
    }
    
    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
        inventoryRepository.delete(inventory);
    }
}
