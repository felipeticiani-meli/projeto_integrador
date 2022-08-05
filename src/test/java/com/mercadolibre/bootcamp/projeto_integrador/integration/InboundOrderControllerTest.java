package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import com.mercadolibre.bootcamp.projeto_integrador.model.Warehouse;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IManagerRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IProductRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.ISectionRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IWarehouseRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class InboundOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ISectionRepository sectionRepository;

    @Autowired
    private IWarehouseRepository warehouseRepository;

    @Autowired
    private IManagerRepository managerRepository;

    @Autowired
    private IProductRepository productRepository;

    @BeforeEach
    void cleanAllRepositories() {
        context.getBeansOfType(JpaRepository.class).values().forEach(CrudRepository::deleteAll);
    }

//    {
//        "sectionCode": 43,
//        "batchStock": [
//              {
//                  "productId": 71,
//                  "currentTemperature": 6.63,
//                  "minimumTemperature": 76.63,
//                  "initialQuantity": 19,
//                  "manufacturingDate": "2030-10-14",
//                  "manufacturingTime": "2018-11-20 09:34:28",
//                  "dueDate": "2017-10-13",
//                  "productPrice": 64.03
//              }
//        ]
//    }

    @Test
    void createInboundOrder_returnsOk_whenIsGivenAValidInput() throws Exception {
        var warehouse = new Warehouse();
        warehouse.setLocation("Santo Ângelo");

        var manager = new Manager();
        manager.setName("Pedro");
        manager.setEmail("Pedro@example.com");
        manager.setUsername("pedro");

        var section = new Section();
        section.setWarehouse(warehouse);
        section.setManager(manager);
        section.setMaxBatches(10);
        section.setCategory(Section.Category.FRESH);
        section.setCurrentBatches(0);

        var product = new Product();
        product.setCategory("Fruta");
        product.setProductName("Maçã");
        product.setBrand("Natureza");

        warehouseRepository.save(warehouse);
        sectionRepository.save(section);
        managerRepository.save(manager);
        productRepository.save(product);

        var batchRequest = new BatchRequestDto();
        batchRequest.setProductId(product.getProductId());
        batchRequest.setProductPrice(new BigDecimal("100.99"));
        batchRequest.setCurrentTemperature(10.0f);
        batchRequest.set


//        sectionRepository.save();

//        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
//                .content(asJsonString()))
    }

    static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }
}
