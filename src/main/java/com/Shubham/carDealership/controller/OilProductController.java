package com.Shubham.carDealership.controller;

import com.Shubham.carDealership.config.JwtUtil;
import com.Shubham.carDealership.model.OilEnquiry;
import com.Shubham.carDealership.model.OilOrder;
import com.Shubham.carDealership.model.OilProduct;
import com.Shubham.carDealership.model.User;
import com.Shubham.carDealership.repository.OilEnquiryRepository;
import com.Shubham.carDealership.repository.OilOrderRepository;
import com.Shubham.carDealership.repository.OilProductRepository;
import com.Shubham.carDealership.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/oil")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "https://bhavishya-frontend.onrender.com"})
public class OilProductController {

    @Autowired
    private OilProductRepository productRepository;

    @Autowired
    private OilOrderRepository orderRepository;

    @Autowired
    private OilEnquiryRepository enquiryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // ── Seed products on startup ───────────────────────────────────────────
    @PostConstruct
    public void seedProducts() {
        if (productRepository.count() == 0) {
            List<OilProduct> products = List.of(
                    createProduct("Bhavishya Kachi Ghani Mustard Oil", "200ml", "182g", 75.00, "0.35 Paise/g", "retail", "Perfect for trying our premium mustard oil. Ideal for small families and daily cooking."),
                    createProduct("Bhavishya Kachi Ghani Mustard Oil", "500ml", "455g", 150.00, "0.33 Paise/g", "retail", "Our most popular retail size. Great value for regular home cooking."),
                    createProduct("Bhavishya Kachi Ghani Mustard Oil", "1 Litre", "910g", 250.00, "0.274 Paise/g", "retail", "Best value for everyday cooking. The preferred choice for most households."),
                    createProduct("Bhavishya Kachi Ghani Mustard Oil", "2 Litre", "1820g", 500.00, "0.274 Paise/g", "retail", "Ideal for large families. Stock up and save with our family pack."),
                    createProduct("Bhavishya Kachi Ghani Mustard Oil", "15 Litre", "15kg", 3750.00, "250 Paise/Litre", "wholesale", "Perfect for restaurants, hotels and bulk buyers. Best price per litre."),
                    createProduct("Bhavishya Kachi Ghani Mustard Oil", "15 Kg Tin", "15kg", 3900.00, "260 Paise/Kg", "wholesale", "Industrial tin pack for distributors and wholesale buyers.")
            );
            productRepository.saveAll(products);
            System.out.println("✅ Oil products seeded successfully!");
        }
    }

    private OilProduct createProduct(String name, String size, String weight, double price, String usp, String category, String description) {
        OilProduct p = new OilProduct();
        p.setName(name);
        p.setSize(size);
        p.setWeight(weight);
        p.setPrice(BigDecimal.valueOf(price));
        p.setUsp(usp);
        p.setCategory(category);
        p.setDescription(description);
        p.setAvailable(true);
        return p;
    }

    // ── Helper ────────────────────────────────────────────────────────────
    private User getAuthenticatedUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.extractUserId(token);
                return userRepository.findById(userId).orElse(null);
            }
        }
        return null;
    }

    private boolean isAdmin(User user) {
        return user != null && ("ADMIN".equals(user.getRole()) || "SUPER_ADMIN".equals(user.getRole()));
    }

    // ── GET all products ──────────────────────────────────────────────────
    @GetMapping("/products")
    public ResponseEntity<?> getProducts() {
        List<OilProduct> products = productRepository.findByAvailable(true);
        return ResponseEntity.ok(Map.of("success", true, "products", products));
    }

    // ── GET single product ────────────────────────────────────────────────
    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        OilProduct product = productRepository.findById(id).orElse(null);
        if (product == null) return ResponseEntity.ok(Map.of("success", false, "message", "Product not found"));
        return ResponseEntity.ok(Map.of("success", true, "product", product));
    }

    // ── UPDATE price (Admin only) ─────────────────────────────────────────
    @PutMapping("/products/{id}/price")
    public ResponseEntity<?> updatePrice(@PathVariable Long id,
                                         @RequestBody Map<String, Object> body,
                                         HttpServletRequest request) {
        User admin = getAuthenticatedUser(request);
        if (!isAdmin(admin)) return ResponseEntity.ok(Map.of("success", false, "message", "Admin access required"));

        OilProduct product = productRepository.findById(id).orElse(null);
        if (product == null) return ResponseEntity.ok(Map.of("success", false, "message", "Product not found"));

        double newPrice = Double.parseDouble(body.get("price").toString());
        product.setPrice(BigDecimal.valueOf(newPrice));
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        return ResponseEntity.ok(Map.of("success", true, "message", "Price updated successfully", "product", product));
    }

    // ── PLACE ORDER ───────────────────────────────────────────────────────
    @PostMapping("/orders")
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            User user = getAuthenticatedUser(request);

            Long productId = Long.parseLong(body.get("productId").toString());
            Integer quantity = Integer.parseInt(body.get("quantity").toString());

            OilProduct product = productRepository.findById(productId).orElse(null);
            if (product == null) return ResponseEntity.ok(Map.of("success", false, "message", "Product not found"));

            BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));

            OilOrder order = new OilOrder();
            if (user != null) order.setUserId(user.getId());
            order.setProductId(productId);
            order.setQuantity(quantity);
            order.setTotalPrice(totalPrice);
            order.setCustomerName((String) body.get("name"));
            order.setCustomerPhone((String) body.get("phone"));
            order.setCustomerEmail((String) body.get("email"));
            order.setDeliveryAddress((String) body.get("address"));
            order.setCity((String) body.get("city"));
            order.setState((String) body.get("state"));
            order.setNotes((String) body.get("notes"));
            order.setStatus("PENDING");

            OilOrder saved = orderRepository.save(order);
            return ResponseEntity.ok(Map.of("success", true, "message", "Order placed successfully", "orderId", saved.getId(), "totalPrice", totalPrice));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ── GET user orders ───────────────────────────────────────────────────
    @GetMapping("/orders/my")
    public ResponseEntity<?> getMyOrders(HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) return ResponseEntity.ok(Map.of("success", false, "message", "Please login"));
        List<OilOrder> orders = orderRepository.findByUserId(user.getId());
        return ResponseEntity.ok(Map.of("success", true, "orders", orders));
    }

    // ── SUBMIT ENQUIRY ────────────────────────────────────────────────────
    @PostMapping("/enquiry")
    public ResponseEntity<?> submitEnquiry(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            User user = getAuthenticatedUser(request);

            OilEnquiry enquiry = new OilEnquiry();
            if (user != null) enquiry.setUserId(user.getId());
            enquiry.setName((String) body.get("name"));
            enquiry.setCompany((String) body.get("company"));
            enquiry.setPhone((String) body.get("phone"));
            enquiry.setEmail((String) body.get("email"));
            enquiry.setCity((String) body.get("city"));
            enquiry.setState((String) body.get("state"));
            enquiry.setOrderType((String) body.get("orderType"));
            enquiry.setMessage((String) body.get("message"));

            OilEnquiry saved = enquiryRepository.save(enquiry);
            return ResponseEntity.ok(Map.of("success", true, "message", "Enquiry submitted successfully", "id", saved.getId()));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ── ADMIN: Get all orders ─────────────────────────────────────────────
    @GetMapping("/admin/orders")
    public ResponseEntity<?> getAllOrders(HttpServletRequest request) {
        User admin = getAuthenticatedUser(request);
        if (!isAdmin(admin)) return ResponseEntity.ok(Map.of("success", false, "message", "Admin access required"));
        List<OilOrder> orders = orderRepository.findAll();
        return ResponseEntity.ok(Map.of("success", true, "orders", orders));
    }

    // ── ADMIN: Update order status ────────────────────────────────────────
    @PutMapping("/admin/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id,
                                               @RequestBody Map<String, String> body,
                                               HttpServletRequest request) {
        User admin = getAuthenticatedUser(request);
        if (!isAdmin(admin)) return ResponseEntity.ok(Map.of("success", false, "message", "Admin access required"));

        OilOrder order = orderRepository.findById(id).orElse(null);
        if (order == null) return ResponseEntity.ok(Map.of("success", false, "message", "Order not found"));

        order.setStatus(body.get("status"));
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        return ResponseEntity.ok(Map.of("success", true, "message", "Order status updated"));
    }

    // ── ADMIN: Get all enquiries ──────────────────────────────────────────
    @GetMapping("/admin/enquiries")
    public ResponseEntity<?> getAllEnquiries(HttpServletRequest request) {
        User admin = getAuthenticatedUser(request);
        if (!isAdmin(admin)) return ResponseEntity.ok(Map.of("success", false, "message", "Admin access required"));
        List<OilEnquiry> enquiries = enquiryRepository.findAll();
        return ResponseEntity.ok(Map.of("success", true, "enquiries", enquiries));
    }

    // ── ADMIN: Get stats ──────────────────────────────────────────────────
    @GetMapping("/admin/stats")
    public ResponseEntity<?> getStats(HttpServletRequest request) {
        User admin = getAuthenticatedUser(request);
        if (!isAdmin(admin)) return ResponseEntity.ok(Map.of("success", false, "message", "Admin access required"));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProducts", productRepository.count());
        stats.put("totalOrders", orderRepository.count());
        stats.put("pendingOrders", orderRepository.findByStatus("PENDING").size());
        stats.put("totalEnquiries", enquiryRepository.count());
        stats.put("pendingEnquiries", enquiryRepository.findByStatus("PENDING").size());
        stats.put("success", true);

        return ResponseEntity.ok(stats);
    }
}