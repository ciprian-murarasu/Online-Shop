package ro.sda.shop.order;

import ro.sda.shop.client.Client;
import ro.sda.shop.product.Product;
import ro.sda.shop.stock.StockService;
import ro.sda.shop.exceptions.ProductNotInStockException;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class OrderService {
    private OrderDAO orderDAO = new OrderDAO();
    private StockService stockService = new StockService();

    List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    Order getOrder(Long id) {
        return orderDAO.findById(id);
    }

    public Order save(Order order) {
        Order updatedOrder = null;
        if (order.getId() == null) {
            updatedOrder = orderDAO.add(order);
        } else {
            orderDAO.update(order);
            updatedOrder = order;
        }
        return updatedOrder;
    }

    private boolean areAllProductsInStock(List<Product> products) {
        for (Product product : products) {
            if (!stockService.isInStock(product)) {
                return false;
            }
        }
        return true;
    }

    private Double computePrice(List<Product> products) {
        Double total = 0D;
        for (Product product : products) {
            total += product.getPrice();
        }
        return total;
    }

    public void placeOrder(Client client, List<Product> products) {
        Order order = new Order();
        if (areAllProductsInStock(products)) {
            order.setClient(client);
            order.setOrderedProducts(products);
            order.setFinalPrice(computePrice(products));
            order.setTimestamp(new Timestamp(new Date().getTime() * 1000000));
            order.setStatus(OrderStatus.PLACED);
            save(order);
        } else {
            throw new ProductNotInStockException("Not all products are in stock");
        }
    }

    public void acceptOrder(Order order) {
        order.setStatus(OrderStatus.ACCEPTED);
        save(order);
    }

    public void deliverOrder(Order order) {
        order.setStatus(OrderStatus.DELIVERED);
        save(order);
    }

    public void payOrder(Order order) {
        order.setStatus(OrderStatus.PAYED);
        save(order);
    }

    public void cancelOrder(Order order) {
        switch (order.getStatus()) {
            case PLACED:
            case ACCEPTED:
            case DELIVERED:
                returnToStock(order);
                break;
            case PAYED:
                returnToStock(order);
                returnMoney(order);
                break;
        }
        order.setStatus(OrderStatus.CANCELED);
        save(order);
    }

    private void returnToStock(Order order) {
        for (Product product : order.getOrderedProducts()) {
            stockService.returnToStock(product);
        }
    }

    private void returnMoney(Order order) {
        // Restituim banii clientului
    }

    public List<Order> getOrdersForClient(Long clientId) {
        return orderDAO.findAllByClientId(clientId);
    }

    public List<Order> getOrdersBetweenDates(Timestamp start, Timestamp end) {
        return orderDAO.findAllBetweenDates(start, end);
    }
}
