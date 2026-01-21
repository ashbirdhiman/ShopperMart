package com.shoppermart.orderservice.FeignClients;

import com.shoppermart.orderservice.DTO.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/api/inventory/sku/{skuCode}")
    InventoryResponse getInventoryBySkuCode(
            @PathVariable("skuCode") String skuCode
    );

    @PutMapping("/api/inventory/sku/{skuCode}/decrement")
    InventoryResponse decrementInventory(
            @PathVariable String skuCode,
            @RequestParam Integer amount
    );

    @PutMapping("/api/inventory/sku/{skuCode}/increment")
    InventoryResponse incrementInventory(
            @PathVariable String skuCode,
            @RequestParam Integer amount
    );
}

