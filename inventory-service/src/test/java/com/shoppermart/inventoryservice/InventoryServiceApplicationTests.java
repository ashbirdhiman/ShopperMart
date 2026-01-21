package com.shoppermart.inventoryservice;

import com.shoppermart.inventoryservice.Model.Inventory;
import com.shoppermart.inventoryservice.Repo.InventoryRepository;
import com.shoppermart.inventoryservice.Service.InventoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;


//@SpringBootTest
@ExtendWith(MockitoExtension.class)
class InventoryServiceApplicationTests {

    @Mock
    InventoryRepository inventoryRepository;

    @InjectMocks
    InventoryService inventoryService;

    Inventory inventory;
    @BeforeEach
    public void setup(){
        inventory=new Inventory();
        inventory.setId(1L);
        inventory.setQuantity(20);
        inventory.setSkuCode("TEST-001");

    }

    @Test
    @DisplayName("ShouldCreateNewInventory")
    public void ShouldCreateNewInventory(){
        Mockito.when(inventoryRepository.save(inventory)).thenReturn(inventory);
        Assertions.assertEquals(inventory,inventoryService.createInventory(inventory));
    }

    @ParameterizedTest
    @DisplayName("shouldReturnInventoryBySkuCode")
    @ValueSource(strings = {"TEST-001","TEST-002","TEST-001"})
    public void shouldReturnInventoryBySkuCode(String skuCode){
        Mockito.when(inventoryRepository.findBySkuCode(skuCode)).thenReturn(Optional.ofNullable(inventory));

        Assertions.assertEquals(inventory.getSkuCode(),inventoryService.getInventoryBySkuCode("TEST-001").getSkuCode());
    }

    @Test
    void contextLoads() {
    }

}
