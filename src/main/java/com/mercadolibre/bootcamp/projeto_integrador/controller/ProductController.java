package com.mercadolibre.bootcamp.projeto_integrador.controller;

import com.mercadolibre.bootcamp.projeto_integrador.dto.ProductDetailsResponseDto;
import com.mercadolibre.bootcamp.projeto_integrador.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ProductController {

    @Autowired
    private IProductService productService;

    @GetMapping("/fresh-products/list")
    public ResponseEntity<ProductDetailsResponseDto> getProductDetails(@RequestParam long productId,
                                                                       @RequestParam(required = false) String orderBy,
                                                                       @RequestHeader("Manager-Id") long managerId) {
        return ResponseEntity.ok(productService.getProductDetails(productId, managerId, orderBy));
    }
}
