package com.flashsaleshop.service;

import com.flashsaleshop.dto.AddCartRequest;
import com.flashsaleshop.dto.AuthResponse;
import com.flashsaleshop.dto.CreateProductRequest;
import com.flashsaleshop.dto.CreateSeckillRequest;
import com.flashsaleshop.dto.LoginRequest;
import com.flashsaleshop.dto.RegisterRequest;
import com.flashsaleshop.dto.SnapshotResponse;
import com.flashsaleshop.exception.BusinessException;
import com.flashsaleshop.mapper.CartMapper;
import com.flashsaleshop.mapper.OrderItemMapper;
import com.flashsaleshop.mapper.OrderMapper;
import com.flashsaleshop.mapper.ProductMapper;
import com.flashsaleshop.mapper.SeckillEventMapper;
import com.flashsaleshop.mapper.UserMapper;
import com.flashsaleshop.model.CartItem;
import com.flashsaleshop.model.Order;
import com.flashsaleshop.model.OrderItem;
import com.flashsaleshop.model.Product;
import com.flashsaleshop.model.SeckillEvent;
import com.flashsaleshop.model.UserAccount;
import com.flashsaleshop.model.UserProfile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StoreService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final SeckillEventMapper seckillEventMapper;
    private final CartMapper cartMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public StoreService(UserMapper userMapper,
                        ProductMapper productMapper,
                        SeckillEventMapper seckillEventMapper,
                        CartMapper cartMapper,
                        OrderMapper orderMapper,
                        OrderItemMapper orderItemMapper) {
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.seckillEventMapper = seckillEventMapper;
        this.cartMapper = cartMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userMapper.findByPhone(request.getPhone()) != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "手机号已注册");
        }
        UserAccount account = new UserAccount(null, request.getName(), request.getPhone(), request.getPassword(), "user",
                new BigDecimal("0"));
        userMapper.insert(account);
        String token = issueToken(account.getId());
        return new AuthResponse(token, toProfile(account));
    }

    public AuthResponse login(LoginRequest request) {
        UserAccount user = userMapper.findByPhone(request.getPhone());
        if (user == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "手机号未注册");
        }
        if (!user.getPassword().equals(request.getPassword())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "手机号或密码错误");
        }
        String token = issueToken(user.getId());
        return new AuthResponse(token, toProfile(user));
    }

    public SnapshotResponse snapshot(String authHeader) {
        long uid = requireUserId(authHeader);
        return buildSnapshot(uid, "ok");
    }

    public List<Product> listProducts() {
        return productMapper.findAll();
    }

    public List<SeckillEvent> listSeckillEvents() {
        return seckillEventMapper.findAll();
    }

    @Transactional
    public SnapshotResponse buySeckill(String authHeader, String eventId, int qty) {
        long uid = requireUserId(authHeader);
        SeckillEvent event = seckillEventMapper.findByIdForUpdate(eventId);
        if (event == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "秒杀活动不存在");
        }
        validateEventActive(event);
        int reserved = cartMapper.sumSeckillQty(uid, eventId);
        int purchased = orderItemMapper.sumPurchasedByUserEvent(uid, eventId);
        if (purchased + reserved + qty > event.getLimitPerUser()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "超过限购");
        }
        int updated = seckillEventMapper.decrementStock(eventId, qty);
        if (updated == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "库存不足");
        }

        Order order = new Order();
        order.setId(generateOrderId("SE"));
        order.setCreatedAt(nowString());
        order.setTotal(event.getSeckillPrice().multiply(BigDecimal.valueOf(qty)));
        order.setItems(List.of(new OrderItem("se-" + event.getId(), event.getProductId(), event.getId(),
                productName(event.getProductId()), "seckill", event.getSeckillPrice(), qty)));
        orderMapper.insert(order, uid);
        orderItemMapper.insert(order.getId(), order.getItems().get(0));
        return buildSnapshot(uid, "秒杀成功，已生成订单");
    }

    @Transactional
    public SnapshotResponse addToCart(String authHeader, AddCartRequest request) {
        long uid = requireUserId(authHeader);
        String type = request.getType();
        if ("seckill".equalsIgnoreCase(type)) {
            return addSeckillToCart(uid, request);
        }
        return addNormalToCart(uid, request);
    }

    @Transactional
    public SnapshotResponse checkout(String authHeader) {
        long uid = requireUserId(authHeader);
        List<CartItem> cart = cartMapper.findByUserId(uid);
        if (cart.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "购物车为空");
        }
        BigDecimal total = cart.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<OrderItem> items = cart.stream()
                .map(item -> new OrderItem(item.getKey(), item.getProductId(), item.getEventId(), item.getName(),
                        item.getType(), item.getPrice(), item.getQty()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        Order order = new Order(generateOrderId("NO"), nowString(), total, items);
        orderMapper.insert(order, uid);
        for (OrderItem item : items) {
            orderItemMapper.insert(order.getId(), item);
        }
        cartMapper.clearByUser(uid);
        return buildSnapshot(uid, "订单已提交");
    }

    @Transactional
    public SnapshotResponse createSeckill(String authHeader, CreateSeckillRequest request) {
        long uid = requireUserId(authHeader);
        Product product = productMapper.findById(request.getProductId());
        if (product == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        if (request.getSeckillPrice().compareTo(product.getPrice()) >= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "秒杀价需低于商品原价");
        }
        if (request.getStartAt() >= request.getEndAt()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "结束时间需大于开始时间");
        }

        String id = "se-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        SeckillEvent event = new SeckillEvent(
                id,
                request.getTitle(),
                request.getProductId(),
                request.getSeckillPrice(),
                request.getLimitPerUser(),
                request.getStock(),
                request.getStock(),
                0,
                request.getStartAt(),
                request.getEndAt()
        );
        seckillEventMapper.insert(event);
        return buildSnapshot(uid, "已创建新秒杀活动");
    }

    @Transactional
    public SnapshotResponse createProduct(String authHeader, CreateProductRequest request) {
        long uid = requireUserId(authHeader);
        Product product = new Product(null, request.getName(), request.getDescription(), request.getTag(),
                request.getPrice(), request.getStock());
        productMapper.insert(product);
        return buildSnapshot(uid, "已添加商品");
    }

    private SnapshotResponse addNormalToCart(long userId, AddCartRequest request) {
        if (request.getProductId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "缺少商品信息");
        }
        Product product = productMapper.findById(request.getProductId());
        if (product == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        int qty = request.getQty();
        int updated = productMapper.decrementStock(product.getId(), qty);
        if (updated == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "库存不足");
        }

        String key = "p-" + product.getId();
        CartItem existing = cartMapper.findByUserAndKey(userId, key);
        if (existing != null) {
            cartMapper.incrementQty(userId, key, qty);
        } else {
            CartItem item = new CartItem(key, product.getId(), product.getName(), "normal", null, product.getPrice(), qty);
            cartMapper.insert(userId, item);
        }
        return buildSnapshot(userId, "已加入购物车");
    }

    private SnapshotResponse addSeckillToCart(long userId, AddCartRequest request) {
        if (request.getEventId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "缺少秒杀活动信息");
        }
        String eventId = request.getEventId();
        SeckillEvent event = seckillEventMapper.findByIdForUpdate(eventId);
        if (event == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "秒杀活动不存在");
        }
        validateEventActive(event);
        int qty = request.getQty();
        int reserved = cartMapper.sumSeckillQty(userId, eventId);
        int purchased = orderItemMapper.sumPurchasedByUserEvent(userId, eventId);
        if (purchased + reserved + qty > event.getLimitPerUser()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "超过限购");
        }
        int updated = seckillEventMapper.decrementStock(eventId, qty);
        if (updated == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "库存不足");
        }

        String key = "se-" + event.getId();
        CartItem existing = cartMapper.findByUserAndKey(userId, key);
        if (existing != null) {
            cartMapper.incrementQty(userId, key, qty);
        } else {
            CartItem item = new CartItem(key, event.getProductId(), productName(event.getProductId()), "seckill",
                    event.getId(), event.getSeckillPrice(), qty);
            cartMapper.insert(userId, item);
        }
        return buildSnapshot(userId, "已加入购物车");
    }

    private void validateEventActive(SeckillEvent event) {
        long now = Instant.now().toEpochMilli();
        if (now < event.getStartAt()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "活动未开始");
        }
        if (now > event.getEndAt()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "活动已结束");
        }
        if (event.getStock() <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "库存不足");
        }
    }

    private long requireUserId(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        String token = authHeader.replace("Bearer", "").trim();
        Long uid = tokens.get(token);
        if (uid == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "登录已失效，请重新登录");
        }
        return uid;
    }

    private SnapshotResponse buildSnapshot(long userId, String message) {
        UserAccount user = userMapper.findById(userId);
        List<Product> products = productMapper.findAll();
        List<SeckillEvent> seckills = seckillEventMapper.findAll();
        List<CartItem> cart = cartMapper.findByUserId(userId);
        List<Order> orders = orderMapper.findByUserId(userId);
        for (Order order : orders) {
            order.setItems(orderItemMapper.findByOrderId(order.getId()));
        }
        Map<String, Integer> purchases = new HashMap<>();
        for (SeckillEvent event : seckills) {
            int purchased = orderItemMapper.sumPurchasedByUserEvent(userId, event.getId());
            if (purchased > 0) {
                purchases.put(event.getId(), purchased);
            }
        }
        return new SnapshotResponse(toProfile(user), products, seckills, cart, orders, purchases, message);
    }

    private String nowString() {
        return LocalDateTime.now(ZoneId.systemDefault()).format(formatter);
    }

    private String generateOrderId(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private String productName(Long productId) {
        Product product = productMapper.findById(productId);
        return product != null ? product.getName() : "";
    }

    private String issueToken(Long userId) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, userId);
        return token;
    }

    private UserProfile toProfile(UserAccount account) {
        if (account == null) {
            return null;
        }
        return new UserProfile(account.getId(), account.getName(), account.getRole(), account.getBalance(), account.getPhone());
    }
}
