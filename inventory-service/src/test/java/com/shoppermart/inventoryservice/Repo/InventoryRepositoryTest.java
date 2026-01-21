package com.shoppermart.inventoryservice.Repo;

import com.shoppermart.inventoryservice.Model.Inventory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class InventoryRepositoryTest {

    @Autowired
    InventoryRepository repository;


    @Test
    void shouldSaveInventory() {
        Inventory inv = new Inventory("SKU1", 10);
        Inventory saved = repository.save(inv);

        assertNotNull(saved.getId());
    }
}