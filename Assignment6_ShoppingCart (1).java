
/*
Assignment 6: Java Collection Framework Application (GUI)
Name: Adarsh Singh
Reg No: 24BCE0518
Real-time application: Shopping Cart Management System (Swing GUI)
Collections used: ArrayList, HashMap, Stack, Queue (LinkedList), TreeSet
Java version: 8+
To compile: javac Assignment6_ShoppingCart.java
To run: java Assignment6_ShoppingCart
*/
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.DecimalFormat;

public class Assignment6_ShoppingCart extends JFrame {
    private DefaultTableModel productTableModel;
    private DefaultTableModel cartTableModel;
    private java.util.List<Product> products; // ArrayList
    private Map<Integer, Product> productMap; // HashMap for quick search by ID
    private Queue<Order> orderQueue; // LinkedList as Queue
    private Stack<Action> actionStack; // Stack for undo
    private TreeSet<String> productNames; // TreeSet for sorted names
    private DecimalFormat df = new DecimalFormat("#0.00");

    // GUI components
    private JTable productTable;
    private JTable cartTable;
    private JTextField tfId, tfName, tfPrice, tfSearch;
    private JLabel lblStatus;

    // Shopping cart represented as Map<productId, quantity>
    private Map<Integer, Integer> cart;

    public Assignment6_ShoppingCart() {
        super("Shopping Cart Management System - Assignment 6");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Initialize collections
        products = new ArrayList<>();
        productMap = new HashMap<>();
        orderQueue = new LinkedList<>();
        actionStack = new Stack<>();
        productNames = new TreeSet<>();
        cart = new HashMap<>();

        // Sample products
        addSampleProducts();

        // Build GUI
        buildGUI();

        refreshProductTable();
        refreshCartTable();
    }

    private void addSampleProducts() {
        addProductInternal(new Product(101, "Apple iPhone 14", 799.00));
        addProductInternal(new Product(102, "Samsung Galaxy S22", 699.00));
        addProductInternal(new Product(103, "Sony Headphones", 129.99));
        addProductInternal(new Product(104, "Dell Laptop", 999.00));
        addProductInternal(new Product(105, "Logitech Mouse", 29.99));
    }

    private void addProductInternal(Product p) {
        products.add(p);
        productMap.put(p.getId(), p);
        productNames.add(p.getName());
    }

    private void buildGUI() {
        JPanel main = new JPanel(new BorderLayout(8,8));
        getContentPane().add(main);

        // Top: Controls to add/search products
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBorder(BorderFactory.createTitledBorder("Product Management"));
        tfId = new JTextField(5); tfName = new JTextField(15); tfPrice = new JTextField(7);
        JButton btnAdd = new JButton("Add Product");
        btnAdd.addActionListener(e -> onAddProduct());
        top.add(new JLabel("ID:")); top.add(tfId);
        top.add(new JLabel("Name:")); top.add(tfName);
        top.add(new JLabel("Price:")); top.add(tfPrice);
        top.add(btnAdd);

        top.add(new JLabel("  Search by ID:"));
        tfSearch = new JTextField(7);
        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> onSearch());
        top.add(tfSearch); top.add(btnSearch);

        JButton btnSort = new JButton("Sort by Name (TreeSet)");
        btnSort.addActionListener(e -> onSortByName());
        top.add(btnSort);

        JButton btnShowAll = new JButton("Show All");
        btnShowAll.addActionListener(e -> refreshProductTable());
        top.add(btnShowAll);

        main.add(top, BorderLayout.NORTH);

        // Center: tables for products and cart
        JPanel center = new JPanel(new GridLayout(1,2,8,8));

        // Product table
        productTableModel = new DefaultTableModel(new Object[]{"ID","Name","Price"},0) {
            public boolean isCellEditable(int r,int c){return false;}
        };
        productTable = new JTable(productTableModel);
        JScrollPane spProducts = new JScrollPane(productTable);
        JPanel pLeft = new JPanel(new BorderLayout());
        pLeft.setBorder(BorderFactory.createTitledBorder("Available Products"));
        pLeft.add(spProducts, BorderLayout.CENTER);

        // Buttons to add to cart / process order / undo
        JPanel pLeftButtons = new JPanel();
        JButton btnAddToCart = new JButton("Add to Cart >>");
        btnAddToCart.addActionListener(e -> onAddToCart());
        JButton btnProcessOrder = new JButton("Process Order (enqueue)");
        btnProcessOrder.addActionListener(e -> onProcessOrder());
        JButton btnUndo = new JButton("Undo Last Action");
        btnUndo.addActionListener(e -> onUndo());
        pLeftButtons.add(btnAddToCart);
        pLeftButtons.add(btnProcessOrder);
        pLeftButtons.add(btnUndo);
        pLeft.add(pLeftButtons, BorderLayout.SOUTH);

        // Cart table
        cartTableModel = new DefaultTableModel(new Object[]{"ID","Name","Qty","Price","Subtotal"},0) {
            public boolean isCellEditable(int r,int c){return false;}
        };
        cartTable = new JTable(cartTableModel);
        JScrollPane spCart = new JScrollPane(cartTable);
        JPanel pRight = new JPanel(new BorderLayout());
        pRight.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));
        pRight.add(spCart, BorderLayout.CENTER);

        // Cart controls
        JPanel pRightButtons = new JPanel();
        JButton btnRemoveFromCart = new JButton("Remove Selected");
        btnRemoveFromCart.addActionListener(e -> onRemoveFromCart());
        JButton btnCheckout = new JButton("Checkout (Create Order)");
        btnCheckout.addActionListener(e -> onCheckout());
        pRightButtons.add(btnRemoveFromCart);
        pRightButtons.add(btnCheckout);
        pRight.add(pRightButtons, BorderLayout.SOUTH);

        center.add(pLeft);
        center.add(pRight);

        main.add(center, BorderLayout.CENTER);

        // Bottom: status label
        lblStatus = new JLabel("Ready.");
        main.add(lblStatus, BorderLayout.SOUTH);
    }

    private void onAddProduct() {
        try {
            int id = Integer.parseInt(tfId.getText().trim());
            String name = tfName.getText().trim();
            double price = Double.parseDouble(tfPrice.getText().trim());
            if(productMap.containsKey(id)) {
                showStatus("Product ID already exists.");
                return;
            }
            Product p = new Product(id,name,price);
            // Add to collections
            products.add(p);
            productMap.put(id,p);
            productNames.add(name);
            // Record action for undo
            actionStack.push(new Action("addProduct", p));
            refreshProductTable();
            showStatus("Product added: " + name);
            tfId.setText(""); tfName.setText(""); tfPrice.setText("");
        } catch(Exception ex) {
            showStatus("Invalid input for product.");
        }
    }

    private void onSearch() {
        try {
            int id = Integer.parseInt(tfSearch.getText().trim());
            Product p = productMap.get(id);
            if(p!=null) {
                // highlight in table
                for(int i=0;i<productTableModel.getRowCount();i++) {
                    int pid = (int)productTableModel.getValueAt(i,0);
                    if(pid==id) {
                        productTable.getSelectionModel().setSelectionInterval(i,i);
                        productTable.scrollRectToVisible(productTable.getCellRect(i,0,true));
                        showStatus("Found product: " + p.getName());
                        return;
                    }
                }
            } else {
                showStatus("Product not found with ID: " + id);
            }
        } catch(Exception ex) {
            showStatus("Enter a valid integer ID to search.");
        }
    }

    private void onSortByName() {
        // TreeSet maintains sorted order of names; we'll sort products list by name
        Collections.sort(products, Comparator.comparing(Product::getName));
        refreshProductTable();
        showStatus("Products sorted by name.");
    }

    private void onAddToCart() {
        int row = productTable.getSelectedRow();
        if(row==-1) { showStatus("Select a product to add."); return; }
        int id = (int)productTableModel.getValueAt(row,0);
        Product p = productMap.get(id);
        if(p==null) { showStatus("Invalid product selection."); return; }
        // increment qty in cart
        int qty = cart.getOrDefault(id,0) + 1;
        cart.put(id, qty);
        // record action for undo
        actionStack.push(new Action("addToCart", p, 1));
        refreshCartTable();
        showStatus("Added to cart: " + p.getName());
    }

    private void onRemoveFromCart() {
        int row = cartTable.getSelectedRow();
        if(row==-1) { showStatus("Select cart row to remove."); return; }
        int id = (int)cartTableModel.getValueAt(row,0);
        Product p = productMap.get(id);
        if(p==null) { showStatus("Invalid cart selection."); return; }
        int qty = cart.getOrDefault(id,0);
        if(qty>0) {
            cart.remove(id);
            actionStack.push(new Action("removeFromCart", p, qty));
            refreshCartTable();
            showStatus("Removed from cart: " + p.getName());
        }
    }

    private void onProcessOrder() {
        if(cart.isEmpty()) { showStatus("Cart is empty. Add items before processing."); return; }
        // Create order object
        Order order = new Order(new HashMap<>(cart));
        orderQueue.offer(order);
        actionStack.push(new Action("processOrder", null));
        // Clear cart
        cart.clear();
        refreshCartTable();
        showStatus("Order queued. Queue size: " + orderQueue.size());
    }

    private void onCheckout() {
        if(orderQueue.isEmpty()) {
            showStatus("No orders in queue to checkout (process).");
            return;
        }
        Order o = orderQueue.poll();
        showStatus("Checkout processed for order. Remaining in queue: " + orderQueue.size());
        // For demo, show order details
        JOptionPane.showMessageDialog(this, o.summary(productMap), "Order Processed", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onUndo() {
        if(actionStack.isEmpty()) { showStatus("Nothing to undo."); return; }
        Action a = actionStack.pop();
        switch(a.type) {
            case "addProduct":
                Product p = a.product;
                products.removeIf(x -> x.getId() == p.getId());
                productMap.remove(p.getId());
                productNames.remove(p.getName());
                refreshProductTable();
                showStatus("Undo: removed product " + p.getName());
                break;
            case "addToCart":
                Product p2 = a.product;
                int cur = cart.getOrDefault(p2.getId(),0);
                cur -= a.qty;
                if(cur<=0) cart.remove(p2.getId()); else cart.put(p2.getId(), cur);
                refreshCartTable();
                showStatus("Undo: removed last add to cart for " + p2.getName());
                break;
            case "removeFromCart":
                Product p3 = a.product;
                cart.put(p3.getId(), a.qty);
                refreshCartTable();
                showStatus("Undo: restored removed cart item " + p3.getName());
                break;
            case "processOrder":
                // cannot fully undo queue easily, but for demonstration we'll do nothing
                showStatus("Undo: processOrder cannot be undone here.");
                break;
        }
    }

    private void refreshProductTable() {
        productTableModel.setRowCount(0);
        for(Product p : products) {
            productTableModel.addRow(new Object[]{p.getId(), p.getName(), df.format(p.getPrice())});
        }
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        double total = 0.0;
        for(Map.Entry<Integer,Integer> e : cart.entrySet()) {
            Product p = productMap.get(e.getKey());
            int q = e.getValue();
            double subtotal = p.getPrice() * q;
            cartTableModel.addRow(new Object[]{p.getId(), p.getName(), q, df.format(p.getPrice()), df.format(subtotal)});
            total += subtotal;
        }
        // add total row
        if(cart.size()>0) {
            cartTableModel.addRow(new Object[]{"", "TOTAL", "", "", df.format(total)});
        }
    }

    private void showStatus(String s) {
        lblStatus.setText(s);
    }

    // Entry point
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Assignment6_ShoppingCart app = new Assignment6_ShoppingCart();
            app.setVisible(true);
        });
    }
}

// Supporting classes
class Product {
    private int id;
    private String name;
    private double price;
    public Product(int id, String name, double price) {
        this.id = id; this.name = name; this.price = price;
    }
    public int getId(){return id;}
    public String getName(){return name;}
    public double getPrice(){return price;}
    public String toString(){ return id + \" - \" + name + \" : \" + price; }
}

class Order {
    private Map<Integer,Integer> items;
    private static int counter = 1;
    private int orderId;
    public Order(Map<Integer,Integer> items) {
        this.items = items;
        this.orderId = counter++;
    }
    public String summary(Map<Integer,Product> productMap) {
        StringBuilder sb = new StringBuilder();
        sb.append(\"Order ID: \").append(orderId).append(\"\\n\");
        double total = 0.0;
        for(Map.Entry<Integer,Integer> e: items.entrySet()) {
            Product p = productMap.get(e.getKey());
            int q = e.getValue();
            double subtotal = p.getPrice()*q;
            sb.append(p.getName()).append(\" x \").append(q).append(\" = \").append(String.format(\"%.2f\",subtotal)).append(\"\\n\");
            total += subtotal;
        }
        sb.append(\"Total: \").append(String.format(\"%.2f\", total));
        return sb.toString();
    }
}

class Action {
    String type;
    Product product;
    int qty;
    public Action(String type, Product product) {
        this(type, product, 0);
    }
    public Action(String type, Product product, int qty) {
        this.type = type; this.product = product; this.qty = qty;
    }
}
