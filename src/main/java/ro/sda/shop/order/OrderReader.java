package ro.sda.shop.order;

import ro.sda.shop.client.Client;
import ro.sda.shop.client.ClientDAO;
import ro.sda.shop.client.ClientWriter;
import ro.sda.shop.common.ConsoleReader;
import ro.sda.shop.common.ConsoleUtil;
import ro.sda.shop.product.Product;
import ro.sda.shop.product.ProductDAO;
import ro.sda.shop.product.ProductWriter;
import ro.sda.shop.stock.Stock;
import ro.sda.shop.stock.StockDAO;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderReader implements ConsoleReader<Order> {
    private ClientDAO clientDAO = new ClientDAO();
    private ProductDAO productDAO = new ProductDAO();
    private StockDAO stockDAO = new StockDAO();

    public Order read() {
        if (productDAO.findAll().isEmpty() || clientDAO.findAll().isEmpty()) {
            System.out.println("No products/clients available");
            return null;
        }
        Order order = new Order();
        new ClientWriter().writeAll(clientDAO.findAll());
        System.out.print("Select client: ");
        Client selectedClient = clientDAO.findById(ConsoleUtil.readLong());
        while (selectedClient == null) {
            System.out.print("Client not found. Select again: ");
            selectedClient = clientDAO.findById(ConsoleUtil.readLong());
        }
        new ProductWriter().writeAll(productDAO.findAll());
        System.out.print("Number of products: ");
        String invalidMessage = "Invalid number. Please retry: ";
        Long noOfProducts = ConsoleUtil.readLong(invalidMessage);
        Long maxProducts = 0L;
        for (Stock stock : stockDAO.getItems()) {
            maxProducts += stock.getQuantity();
        }
        while (noOfProducts <= 0 && noOfProducts > maxProducts) {
            noOfProducts = ConsoleUtil.readLong(invalidMessage);
        }
        List<Product> listOfProducts = getProducts(noOfProducts);
        System.out.print("Actual price: ");
        Double actualPrice = ConsoleUtil.getPrice(order);
        order.setClient(selectedClient);
        order.setOrderedProducts(listOfProducts);
        order.setFinalPrice(actualPrice);
        order.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
        setOrderToClient(order);
        return order;
    }

    private List<Product> getProducts(Long noOfItems) {
        List<Product> listOfProducts = new ArrayList<>();
        for (int i = 0; i < noOfItems; ) {
            //@sdatrainers - if you read this, beer is on us!!!!!!
            System.out.print("Product #" + (i + 1) + ": ");
            Product product = productDAO.findById(ConsoleUtil.readLong());
            if (product != null) {
                listOfProducts.add(product);
                i++;
            }
        }
        return listOfProducts;
    }

    public void setOrderToClient(Order order) {
        Client client = order.getClient();
        List<Order> orders = client.getOrders();
        orders.add(order);
        client.setOrders(orders);
    }
}
