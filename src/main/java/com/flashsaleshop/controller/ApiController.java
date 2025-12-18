package com.flashsaleshop.controller;

import com.flashsaleshop.dto.AddCartRequest;
import com.flashsaleshop.dto.BuyRequest;
import com.flashsaleshop.dto.CreateProductRequest;
import com.flashsaleshop.dto.CreateSeckillRequest;
import com.flashsaleshop.dto.SnapshotResponse;
import com.flashsaleshop.model.Product;
import com.flashsaleshop.model.SeckillEvent;
import com.flashsaleshop.service.StoreService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class ApiController {

    private final StoreService storeService;

    public ApiController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping("/bootstrap")
    public SnapshotResponse bootstrap(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return storeService.snapshot(authHeader);
    }

    @GetMapping("/products")
    public List<Product> products() {
        return storeService.listProducts();
    }

    @GetMapping("/seckills")
    public List<SeckillEvent> seckills() {
        return storeService.listSeckillEvents();
    }

    @PostMapping("/seckills/{eventId}/buy")
    public SnapshotResponse buy(@PathVariable String eventId,
                                @Valid @RequestBody(required = false) BuyRequest request,
                                @RequestHeader(value = "Authorization", required = false) String authHeader) {
        int qty = request != null ? request.getQty() : 1;
        return storeService.buySeckill(authHeader, eventId, qty);
    }

    @PostMapping("/cart")
    public SnapshotResponse addCart(@Valid @RequestBody AddCartRequest request,
                                    @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return storeService.addToCart(authHeader, request);
    }

    @PostMapping("/orders/checkout")
    public SnapshotResponse checkout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return storeService.checkout(authHeader);
    }

    @PostMapping("/admin/seckills")
    public SnapshotResponse createSeckill(@Valid @RequestBody CreateSeckillRequest request,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return storeService.createSeckill(authHeader, request);
    }

    @PostMapping("/admin/products")
    public SnapshotResponse createProduct(@Valid @RequestBody CreateProductRequest request,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return storeService.createProduct(authHeader, request);
    }
}
