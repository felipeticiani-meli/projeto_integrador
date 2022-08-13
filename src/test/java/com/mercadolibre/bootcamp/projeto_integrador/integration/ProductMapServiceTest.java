package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.integration.listeners.ResetDatabase;
import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import com.mercadolibre.bootcamp.projeto_integrador.model.Warehouse;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IProductRepository;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ResetDatabase
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductMapServiceTest extends BaseControllerTest {
    private Manager manager;
    private Product product;
    private InboundOrderRequestDto validInboundOrderRequest;

    private IProductRepository productRepositoryMock;
    private DefaultSingletonBeanRegistry beans;

    @Autowired
    private ApplicationContext context;

    @BeforeAll
    void beforeAll() {
        productRepositoryMock = Mockito.mock(IProductRepository.class);
        beans = (DefaultSingletonBeanRegistry) context.getAutowireCapableBeanFactory();
    }

    @BeforeEach
    void setup() {
        Warehouse warehouse = getSavedWarehouse();
        manager = getSavedManager();
        Section section = getSavedFreshSection(warehouse, manager);
        product = getSavedFreshProduct();
        validInboundOrderRequest = getValidInboundOrderRequestDto(section, getValidBatchRequest(product));

        beans.destroySingleton("IProductRepository");
        beans.registerSingleton("IProductRepository", productRepositoryMock);
    }

    @AfterEach
    void tearDown() {
        beans.destroySingleton("IProductRepository");
        beans.registerSingleton("IProductRepository", productRepository);
        Mockito.reset(productRepositoryMock);
    }

    @Test
    void it_isCreatedOnlyOncePerRequest() throws Exception {
        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                        .content(asJsonString(validInboundOrderRequest))
                        .header("Manager-Id", manager.getManagerId())
                        .contentType(MediaType.APPLICATION_JSON));

        verify(productRepositoryMock, times(1)).findAllById(List.of(product.getProductId()));
    }

    @Test
    void it_isCreatedOnEachRequest() throws Exception {
        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                .content(asJsonString(validInboundOrderRequest))
                .header("Manager-Id", manager.getManagerId())
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                .content(asJsonString(validInboundOrderRequest))
                .header("Manager-Id", manager.getManagerId())
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                .content(asJsonString(validInboundOrderRequest))
                .header("Manager-Id", manager.getManagerId())
                .contentType(MediaType.APPLICATION_JSON));

        verify(productRepositoryMock, times(3)).findAllById(List.of(product.getProductId()));
    }
}
