package com.shoppermart.orderservice.Service;


import com.shoppermart.orderservice.GlobalException.ResourceNotFoundException;
import com.shoppermart.orderservice.Fullfillment.KafkaProducer.OrderProducer;
import com.shoppermart.orderservice.Model.Order;
import com.shoppermart.orderservice.Model.OrderStatus;
import com.shoppermart.orderservice.Repo.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private static final Logger log= LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;

    private final OrderProducer orderProducer;


    public OrderService(OrderRepository orderRepository, OrderProducer orderProducer) {
        this.orderRepository = orderRepository;
        this.orderProducer = orderProducer;
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




    public Order createOrder(Order order) {

        try {
            order.setStatus(OrderStatus.PENDING);
            Order createdOrder=orderRepository.save(order);

            log.info("Creating order with id:" + createdOrder.getId() + "details :"+createdOrder);



            return createdOrder;

        } catch (Exception ex) {
            // COMPENSATION
            throw new ResourceNotFoundException("Order Creating Failed");
        }
    }


    public Order updateOrder(Long id, Order orderDetails) {

        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        try {
            // Step 4: update order
            existingOrder.setSkuCodeQuantityMap(orderDetails.getSkuCodeQuantityMap());
            existingOrder.setTotalPrice(orderDetails.getTotalPrice());
            existingOrder.setStatus(orderDetails.getStatus());



            Order createdOrder= orderRepository.save(existingOrder);


            orderProducer.sendUpdateOrderToInventory(createdOrder,orderDetails.getSkuCodeQuantityMap());

            return createdOrder;

        } catch (Exception ex) {
            // COMPENSATION

            throw ex;
        }
    }


    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Step 2: delete order
        orderRepository.delete(order);
        orderProducer.sendOrderToInventory(order);

    }



}
