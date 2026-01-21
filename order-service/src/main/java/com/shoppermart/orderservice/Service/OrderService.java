package com.shoppermart.orderservice.Service;

import com.shoppermart.orderservice.DTO.InventoryResponse;
import com.shoppermart.orderservice.FeignClients.InventoryClient;
import com.shoppermart.orderservice.FeignClients.UserClient;
import com.shoppermart.orderservice.Model.Order;
import com.shoppermart.orderservice.Model.OrderStatus;
import com.shoppermart.orderservice.Repo.OrderRepository;
import com.shoppermart.orderservice.event.OrderPlaceEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private  final InventoryClient inventoryClient;

    private final UserClient userClient;

    private final OrderRepository orderRepository;

    private final KafkaTemplate<String, OrderPlaceEvent> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, InventoryClient inventoryClient, UserClient userClient, KafkaTemplate kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.inventoryClient=inventoryClient;
        this.userClient = userClient;
        this.kafkaTemplate=kafkaTemplate;
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
    
    public List<Order> getOrdersByCustomerId(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }


    @CircuitBreaker(name = "inventoryService", fallbackMethod = "inventoryFallback")
    public InventoryResponse getInventoryResponse(String skuCode){

       return inventoryClient.getInventoryBySkuCode(skuCode);
    }

    @CircuitBreaker(name = "userService",fallbackMethod = "userFallBack")
    public String getUserEmailByCustomerId(String customerId){
        return userClient.getEmailByUserId(customerId);

    }


    public Order createOrder(Order order) {

        order.setStatus(OrderStatus.PENDING);

        String skuCode = order.getSkuCode();

        InventoryResponse inventory = this.getInventoryResponse(skuCode);


        if (inventory.getQuantity() < order.getQuantity()) {
            throw new RuntimeException("Out of stock");
        }

        // Step 1: decrement inventory
        inventoryClient.decrementInventory(skuCode, order.getQuantity());

        try {
            // Step 2: save order
            order.setStatus(OrderStatus.CONFIRMED);

            Order order1=orderRepository.save(order);

            String email=this.getUserEmailByCustomerId(order.getCustomerId());

            OrderPlaceEvent orderPlaceEvent=new OrderPlaceEvent(order.getId(),email);

            kafkaTemplate.send("order-placed",orderPlaceEvent);

            return order1;

        } catch (Exception ex) {
            // COMPENSATION
            inventoryClient.incrementInventory(skuCode, order.getQuantity());
            throw ex;
        }
    }



    @CircuitBreaker(name = "inventoryService", fallbackMethod = "inventoryFallback")
    public Order updateOrder(Long id, Order orderDetails) {

        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String skuCode = orderDetails.getSkuCode();

        // Step 1: restore old quantity
        inventoryClient.incrementInventory(
                skuCode,
                existingOrder.getQuantity()
        );

        try {
            // Step 2: check new quantity
            InventoryResponse inventory = this.getInventoryResponse(skuCode);

            if (inventory.getQuantity() < orderDetails.getQuantity()) {
                throw new RuntimeException("Insufficient stock");
            }

            // Step 3: apply new quantity
            inventoryClient.decrementInventory(
                    skuCode,
                    orderDetails.getQuantity()
            );

            // Step 4: update order
            existingOrder.setQuantity(orderDetails.getQuantity());
            existingOrder.setTotalPrice(orderDetails.getTotalPrice());
            existingOrder.setStatus(orderDetails.getStatus());

            return orderRepository.save(existingOrder);

        } catch (Exception ex) {
            // COMPENSATION
            inventoryClient.decrementInventory(
                    skuCode,
                    existingOrder.getQuantity()
            );
            throw ex;
        }
    }



    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "inventoryFallback")
    public void deleteOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Step 1: restore inventory
        inventoryClient.incrementInventory(
                order.getSkuCode(),
                order.getQuantity()
        );

        // Step 2: delete order
        orderRepository.delete(order);
    }

    public String userFallBack(Exception ex) {
        throw new RuntimeException(
                "Inventory service unavailable. Please try again later."
        );
    }
    public Order inventoryFallback(Order order, Exception ex) {
        order.setStatus(OrderStatus.FAILED);
        throw new RuntimeException(
                "Inventory service unavailable. Please try again later."
        );
    }


}
