package com.Shubham.carDealership.controller;

import com.Shubham.carDealership.config.JwtUtil;
import com.Shubham.carDealership.dto.UserDto;
import com.Shubham.carDealership.model.Message;
import com.Shubham.carDealership.model.OilEnquiry;
import com.Shubham.carDealership.model.OilOrder;
import com.Shubham.carDealership.model.OilProduct;
import com.Shubham.carDealership.model.User;
import com.Shubham.carDealership.repository.MessageRepository;
import com.Shubham.carDealership.repository.OilEnquiryRepository;
import com.Shubham.carDealership.repository.OilOrderRepository;
import com.Shubham.carDealership.repository.OilProductRepository;
import com.Shubham.carDealership.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "https://bhavishya-frontend.onrender.com"})
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private OilProductRepository productRepository;

    @Autowired
    private OilOrderRepository orderRepository;

    @Autowired
    private OilEnquiryRepository enquiryRepository;

    @Autowired
    private JwtUtil jwtUtil;

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

    private boolean isSuperAdmin(User user) {
        return user != null && "SUPER_ADMIN".equals(user.getRole());
    }

    // ── Dashboard Stats ────────────────────────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(HttpServletRequest request) {
        User admin = getAuthenticatedUser(request);
        if (!isAdmin(admin)) return ResponseEntity.ok(Map.of("success", false, "message", "Admin access required"));

        List<User> allUsers = userRepository.findAll();
        Map<String, Object> stats = new HashMap<>();
        stats.put("success", true);
        stats.put("totalUsers", allUsers.size());
        stats.put("totalProducts", productRepository.count());
        stats.put("totalOrders", orderRepository.count());
        stats.put("pendingOrders", orderRepository.findByStatus("PENDING").size());
        stats.put("totalEnquiries", enquiryRepository.count());
        stats.put("pendingEnquiries", enquiryRepository.findByStatus("PENDING").size());
        stats.put("totalMessages", messageRepository.count());
        stats.put("recentUsers", allUsers.stream().limit(5).map(this::mapToUserDto).collect(Collectors.toList()));
        stats.put("recentOrders", orderRepository.findAll().stream().limit(5).collect(Collectors.toList()));
        stats.put("recentEnquiries", enquiryRepository.findAll().stream().limit(5).collect(Collectors.toList()));
        return ResponseEntity.ok(stats);
    }

    // ── Users ──────────────────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        User admin = getAuthenticatedUser(request);
        if (!isAdmin(admin)) return ResponseEntity.ok(Map.of("success", false, "message", "Admin access required"));
        List<UserDto> users = userRepository.findAll().stream().map(this::mapToUserDto).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "users", users));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long userId,
                                            @RequestBody Map<String, String> payload,
                                            HttpServletRequest request) {
        User currentAdmin = getAuthenticatedUser(request);
        if (!isSuperAdmin(currentAdmin)) return ResponseEntity.ok(Map.of("success", false, "message", "Super Admin access required"));

        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) return ResponseEntity.ok(Map.of("success", false, "message", "User not found"));

        String newRole = payload.get("role");
        if (!List.of("USER", "ADMIN", "SUPER_ADMIN").contains(newRole))
            return ResponseEntity.ok(Map.of("success", false, "message", "Invalid role"));

        targetUser.setRole(newRole);
        userRepository.save(targetUser);
        return ResponseEntity.ok(Map.of("success", true, "message", "Role updated to " + newRole));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        User admin = getAuthenticatedUser(request);
        if (!isAdmin(admin)) return ResponseEntity.ok(Map.of("success", false, "message", "Admin access required"));
        if (admin.getId().equals(userId)) return ResponseEntity.ok(Map.of("success", false, "message", "Cannot delete yourself"));

        User target = userRepository.findById(userId).orElse(null);
        if (target == null) return ResponseEntity.ok(Map.of("success", false, "message", "User not found"));

        userRepository.delete(target);
        return ResponseEntity.ok(Map.of("success", true, "message", "User deleted"));
    }

    // ── Messages ───────────────────────────────────────────────────────────
    @GetMapping("/messages")
    public ResponseEntity<?> getAllMessages(HttpServletRequest request) {
        User admin = getAuthenticatedUser(request);
        if (!isAdmin(admin)) return ResponseEntity.ok(Map.of("success", false, "message", "Admin access required"));

        List<Message> messages = messageRepository.findAll();
        List<Map<String, Object>> enriched = messages.stream().map(msg -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", msg.getId());
            m.put("content", msg.getContent());
            m.put("senderId", msg.getSenderId());
            m.put("receiverId", msg.getReceiverId());
            m.put("isRead", msg.getIsRead());
            m.put("createdAt", msg.getCreatedAt());
            User sender = userRepository.findById(msg.getSenderId()).orElse(null);
            User receiver = userRepository.findById(msg.getReceiverId()).orElse(null);
            m.put("senderName", sender != null ? sender.getUsername() : "Unknown");
            m.put("receiverName", receiver != null ? receiver.getUsername() : "Unknown");
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("success", true, "messages", enriched));
    }

    private UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }
}