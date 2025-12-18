package com.flashsaleshop.service;

import com.flashsaleshop.dto.AddCartRequest;
import com.flashsaleshop.dto.AuthResponse;
import com.flashsaleshop.dto.CreateProductRequest;
import com.flashsaleshop.dto.CreateSeckillRequest;
import com.flashsaleshop.dto.LoginRequest;
import com.flashsaleshop.dto.RegisterRequest;
import com.flashsaleshop.dto.SnapshotResponse;
import com.flashsaleshop.exception.BusinessException;
import com.flashsaleshop.model.CartItem;
import com.flashsaleshop.model.Order;
import com.flashsaleshop.model.OrderItem;
import com.flashsaleshop.model.Product;
import com.flashsaleshop.model.SeckillEvent;
import com.flashsaleshop.model.UserAccount;
import com.flashsaleshop.model.UserProfile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class StoreService {

    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final Map<String, SeckillEvent> seckillEvents = new ConcurrentHashMap<>();
    private final Map<Long, List<CartItem>> carts = new ConcurrentHashMap<>();
    private final Map<Long, List<Order>> orders = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, Integer>> seckillPurchases = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> eventLocks = new ConcurrentHashMap<>();
    private final Map<Long, UserAccount> users = new ConcurrentHashMap<>();
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();
    private final AtomicLong orderSeq = new AtomicLong(1000);
    private final AtomicLong userSeq = new AtomicLong(1);
    private final AtomicLong productSeq = new AtomicLong(0);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @PostConstruct
    public void initData() {
        products.put(1L, new Product(1L, "AirPods Pro 2", "低延迟音频，搭配秒杀优惠价", "3C", new BigDecimal("1299"), 30));
        products.put(2L, new Product(2L, "保温杯 · 冬日限定", "316 不锈钢，长效保温", "居家", new BigDecimal("159"), 120));
        products.put(3L, new Product(3L, "机械键盘 98 配列", "热插拔轴体，RGB 背光", "外设", new BigDecimal("499"), 60));
        products.put(4L, new Product(4L, "石墨烯暖贴", "升温快，冬日保暖", "健康", new BigDecimal("39"), 200));

        long now = Instant.now().toEpochMilli();
        addSeckillSeed(new SeckillEvent("se-1", "耳机闪购 50% OFF", 1L, new BigDecimal("899"), 1, 15, 15, 0,
                now - 10 * 60 * 1000, now + 40 * 60 * 1000));
        addSeckillSeed(new SeckillEvent("se-2", "保温杯整点秒杀", 2L, new BigDecimal("89"), 2, 80, 80, 0,
                now + 10 * 60 * 1000, now + 70 * 60 * 1000));
        addSeckillSeed(new SeckillEvent("se-3", "暖贴买二送一", 4L, new BigDecimal("19"), 3, 0, 120, 120,
                now - 30 * 60 * 1000, now - 5 * 60 * 1000));

        productSeq.set(products.keySet().stream().mapToLong(Long::longValue).max().orElse(0));

        // 默认管理员账号
        UserAccount admin = new UserAccount(userSeq.getAndIncrement(), "小明", "18800000000", "123456", "admin",
                new BigDecimal("2000"));
        users.put(admin.getId(), admin);
    }

    public AuthResponse register(RegisterRequest request) {
        if (users.values().stream().anyMatch(u -> u.getPhone().equals(request.getPhone()))) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "手机号已注册");
        }
        long id = userSeq.getAndIncrement();
        UserAccount account = new UserAccount(id, request.getName(), request.getPhone(), request.getPassword(), "user",
                new BigDecimal("0"));
        users.put(id, account);
        String token = issueToken(id);
        return new AuthResponse(token, toProfile(account));
    }

    public AuthResponse login(LoginRequest request) {
        UserAccount user = users.values().stream()
                .filter(u -> u.getPhone().equals(request.getPhone()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "手机号未注册"));
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
        return cloneProducts();
    }

    public List<SeckillEvent> listSeckillEvents() {
        return cloneEvents();
    }

    public SnapshotResponse buySeckill(String authHeader, String eventId, int qty) {
        long uid = requireUserId(authHeader);
        ReentrantLock lock = eventLocks.computeIfAbsent(eventId, key -> new ReentrantLock());
        lock.lock();
        try {
            SeckillEvent event = requireEvent(eventId);
            validateEventActive(event);
            validateSeckillLimit(uid, event, qty, reservedInCart(uid, eventId));
            if (event.getStock() < qty) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "库存不足");
            }
            event.setStock(event.getStock() - qty);
            event.setSold(Math.max(0, event.getInitialStock() - event.getStock()));
            incrementPurchase(uid, eventId, qty);

            Order order = new Order();
            order.setId(generateOrderId("SE"));
            order.setCreatedAt(nowString());
            order.setTotal(event.getSeckillPrice().multiply(BigDecimal.valueOf(qty)));
            order.setItems(List.of(new OrderItem("se-" + event.getId(), event.getProductId(),
                    productName(event.getProductId()), "seckill", event.getSeckillPrice(), qty)));
            orders.computeIfAbsent(uid, k -> Collections.synchronizedList(new ArrayList<>())).add(0, order);
            return buildSnapshot(uid, "秒杀成功，已生成订单");
        } finally {
            lock.unlock();
        }
    }

    public SnapshotResponse addToCart(String authHeader, AddCartRequest request) {
        long uid = requireUserId(authHeader);
        String type = request.getType();
        if ("seckill".equalsIgnoreCase(type)) {
            return addSeckillToCart(uid, request);
        }
        return addNormalToCart(uid, request);
    }

    public SnapshotResponse checkout(String authHeader) {
        long uid = requireUserId(authHeader);
        List<CartItem> cart = carts.getOrDefault(uid, new ArrayList<>());
        if (cart.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "购物车为空");
        }
        BigDecimal total = cart.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<OrderItem> items = cart.stream()
                .map(item -> new OrderItem(item.getKey(), item.getProductId(), item.getName(), item.getType(),
                        item.getPrice(), item.getQty()))
                .collect(Collectors.toCollection(ArrayList::new));
        Order order = new Order(generateOrderId("NO"), nowString(), total, items);
        orders.computeIfAbsent(uid, k -> Collections.synchronizedList(new ArrayList<>())).add(0, order);

        cart.stream()
                .filter(item -> "seckill".equalsIgnoreCase(item.getType()) && item.getEventId() != null)
                .forEach(item -> incrementPurchase(uid, item.getEventId(), item.getQty()));

        cart.clear();
        return buildSnapshot(uid, "订单已提交");
    }

    public SnapshotResponse createSeckill(String authHeader, CreateSeckillRequest request) {
        long uid = requireUserId(authHeader);
        Product product = requireProduct(request.getProductId());
        if (request.getSeckillPrice().compareTo(product.getPrice()) >= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "秒杀价需低于商品原价");
        }
        if (request.getStartAt() >= request.getEndAt()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "结束时间需大于开始时间");
        }

        String id = "se-" + UUID.randomUUID().toString().substring(0, 6);
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
        addSeckillSeed(event);
        return buildSnapshot(uid, "已创建新秒杀活动");
    }

    public SnapshotResponse createProduct(String authHeader, CreateProductRequest request) {
        long uid = requireUserId(authHeader);
        long id = productSeq.incrementAndGet();
        Product product = new Product(id, request.getName(), request.getDescription(), request.getTag(),
                request.getPrice(), request.getStock());
        products.put(id, product);
        // 默认新增商品可用于秒杀表单
        return buildSnapshot(uid, "已添加商品");
    }

    private SnapshotResponse addNormalToCart(long userId, AddCartRequest request) {
        if (request.getProductId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "缺少商品信息");
        }
        Product product = requireProduct(request.getProductId());
        int qty = request.getQty();
        if (product.getStock() < qty) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "库存不足");
        }
        product.setStock(product.getStock() - qty);

        List<CartItem> cart = carts.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>()));
        String key = "p-" + product.getId();
        CartItem existing = cart.stream().filter(item -> key.equals(item.getKey())).findFirst().orElse(null);
        if (existing != null) {
            existing.setQty(existing.getQty() + qty);
        } else {
            cart.add(new CartItem(key, product.getId(), product.getName(), "normal", null, product.getPrice(), qty));
        }
        return buildSnapshot(userId, "已加入购物车");
    }

    private SnapshotResponse addSeckillToCart(long userId, AddCartRequest request) {
        if (request.getEventId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "缺少秒杀活动信息");
        }
        String eventId = request.getEventId();
        ReentrantLock lock = eventLocks.computeIfAbsent(eventId, key -> new ReentrantLock());
        lock.lock();
        try {
            SeckillEvent event = requireEvent(eventId);
            validateEventActive(event);
            int qty = request.getQty();
            validateSeckillLimit(userId, event, qty, reservedInCart(userId, eventId));
            if (event.getStock() < qty) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "库存不足");
            }
            event.setStock(event.getStock() - qty);
            event.setSold(Math.max(0, event.getInitialStock() - event.getStock()));

            List<CartItem> cart = carts.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>()));
            String key = "se-" + event.getId();
            CartItem existing = cart.stream().filter(item -> key.equals(item.getKey())).findFirst().orElse(null);
            if (existing != null) {
                existing.setQty(existing.getQty() + qty);
            } else {
                cart.add(new CartItem(key, event.getProductId(), productName(event.getProductId()), "seckill",
                        event.getId(), event.getSeckillPrice(), qty));
            }
            return buildSnapshot(userId, "已加入购物车");
        } finally {
            lock.unlock();
        }
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

    private void validateSeckillLimit(long userId, SeckillEvent event, int qty, int reserved) {
        int purchased = seckillPurchases.getOrDefault(userId, Map.of()).getOrDefault(event.getId(), 0);
        int total = purchased + reserved + qty;
        if (total > event.getLimitPerUser()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "超过限购");
        }
    }

    private SeckillEvent requireEvent(String eventId) {
        SeckillEvent event = seckillEvents.get(eventId);
        if (event == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "秒杀活动不存在");
        }
        return event;
    }

    private Product requireProduct(Long productId) {
        Product product = products.get(productId);
        if (product == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        return product;
    }

    private void addSeckillSeed(SeckillEvent event) {
        seckillEvents.put(event.getId(), event);
        eventLocks.putIfAbsent(event.getId(), new ReentrantLock());
    }

    private int reservedInCart(long userId, String eventId) {
        List<CartItem> cart = carts.getOrDefault(userId, List.of());
        return cart.stream()
                .filter(item -> eventId.equals(item.getEventId()))
                .mapToInt(CartItem::getQty)
                .sum();
    }

    private void incrementPurchase(long userId, String eventId, int qty) {
        Map<String, Integer> counts = seckillPurchases.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        counts.merge(eventId, qty, Integer::sum);
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
        List<Product> productCopy = cloneProducts();
        List<SeckillEvent> seckillCopy = cloneEvents();
        List<CartItem> cartCopy = cloneCart(userId);
        List<Order> orderCopy = cloneOrders(userId);
        Map<String, Integer> purchaseCopy = new ConcurrentHashMap<>(
                seckillPurchases.getOrDefault(userId, Map.of())
        );
        seckillCopy.sort(Comparator.comparingLong(SeckillEvent::getStartAt));
        return new SnapshotResponse(toProfile(users.get(userId)), productCopy, seckillCopy, cartCopy, orderCopy, purchaseCopy, message);
    }

    private List<Product> cloneProducts() {
        return products.values().stream()
                .map(p -> new Product(p.getId(), p.getName(), p.getDescription(), p.getTag(), p.getPrice(), p.getStock()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<SeckillEvent> cloneEvents() {
        return seckillEvents.values().stream()
                .map(e -> new SeckillEvent(e.getId(), e.getTitle(), e.getProductId(), e.getSeckillPrice(),
                        e.getLimitPerUser(), e.getStock(), e.getInitialStock(), e.getSold(), e.getStartAt(), e.getEndAt()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<CartItem> cloneCart(long userId) {
        List<CartItem> cart = carts.getOrDefault(userId, List.of());
        return cart.stream()
                .map(c -> new CartItem(c.getKey(), c.getProductId(), c.getName(), c.getType(), c.getEventId(),
                        c.getPrice(), c.getQty()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Order> cloneOrders(long userId) {
        List<Order> list = orders.getOrDefault(userId, List.of());
        return list.stream().map(o -> {
            List<OrderItem> items = o.getItems().stream()
                    .map(i -> new OrderItem(i.getKey(), i.getProductId(), i.getName(), i.getType(), i.getPrice(), i.getQty()))
                    .collect(Collectors.toCollection(ArrayList::new));
            return new Order(o.getId(), o.getCreatedAt(), o.getTotal(), items);
        }).collect(Collectors.toCollection(ArrayList::new));
    }

    private String nowString() {
        return LocalDateTime.now(ZoneId.systemDefault()).format(formatter);
    }

    private String generateOrderId(String prefix) {
        return prefix + orderSeq.incrementAndGet();
    }

    private String productName(Long productId) {
        Product product = products.get(productId);
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
