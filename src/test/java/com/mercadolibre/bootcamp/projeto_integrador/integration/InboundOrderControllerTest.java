package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import com.mercadolibre.bootcamp.projeto_integrador.model.Warehouse;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IManagerRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IProductRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.ISectionRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IWarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class InboundOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

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

    @Autowired
    private IBatchRepository batchRepository;

    @Autowired
    private IInboundOrderRepository inboundOrderRepository;

    private final ObjectMapper objectMapper;

    public InboundOrderControllerTest() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()
        ) {
            statement.execute("SET REFERENTIAL_INTEGRITY FALSE");
            ResultSet records = statement.executeQuery("SHOW TABLES");

            while (records.next()) {
                Statement truncateStatement = connection.createStatement();
                truncateStatement.executeUpdate("TRUNCATE TABLE " + records.getString(1));
                truncateStatement.close();
            }

            statement.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }

        /*EasyRandomParameters parameters = new EasyRandomParameters();
        parameters.excludeField(FieldPredicates.named("^.+(Code|Id|Number)$"));

        generator = new EasyRandom(parameters);
        var warehouse = generator.nextObject(Warehouse.class);
        var manager = generator.nextObject(Manager.class);
        var section = generator.nextObject(Section.class);
        section.setWarehouse(warehouse);
        section.setManager(manager);
        var product = generator.nextObject(Product.class);*/
    }

    @Test
    void createInboundOrder_returnsOk_whenIsGivenAValidInput() throws Exception {
        Warehouse warehouse = getWarehouse();
        Manager manager = getManager();
        Section section = getSection(warehouse, manager);
        Product product = getProduct();

        warehouseRepository.save(warehouse);
        managerRepository.save(manager);
        sectionRepository.save(section);
        productRepository.save(product);
    }

        BatchRequestDto batchRequest = new BatchRequestDto();
        batchRequest.setProductId(product.getProductId());
        batchRequest.setProductPrice(new BigDecimal("100.99"));
        batchRequest.setCurrentTemperature(10.0f);
        batchRequest.setMinimumTemperature(10.0f);
        batchRequest.setDueDate(LocalDate.now().plusWeeks(1));
        batchRequest.setManufacturingTime(LocalDateTime.now());
        batchRequest.setManufacturingDate(LocalDate.now());
        batchRequest.setInitialQuantity(10);

        InboundOrderRequestDto requestDto = new InboundOrderRequestDto();
        requestDto.setBatchStock(List.of(batchRequest));
        requestDto.setSectionCode(1L);

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                .content(asJsonString(requestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void createInboundOrder_returnsError_whenIsGivenAnInvalidInput() throws Exception {
        Warehouse warehouse = getWarehouse();
        Manager manager = getManager();
        Section section = getSection(warehouse, manager);
        Product product = getProduct();

        warehouseRepository.save(warehouse);
        managerRepository.save(manager);
        sectionRepository.save(section);
        productRepository.save(product);

        System.out.println(manager.getManagerId());

        BatchRequestDto batchRequest = new BatchRequestDto();
        batchRequest.setProductId(product.getProductId());

        // Valores inválidos.
        batchRequest.setProductPrice(new BigDecimal("-100.99"));
        batchRequest.setCurrentTemperature(-1);
        batchRequest.setMinimumTemperature(-1);
        batchRequest.setDueDate(LocalDate.now().minusWeeks(1));
        batchRequest.setManufacturingTime(LocalDateTime.now().plusDays(1));
        batchRequest.setManufacturingDate(LocalDate.now().plusDays(1));
        batchRequest.setInitialQuantity(-1);

        InboundOrderRequestDto requestDto = new InboundOrderRequestDto();
        requestDto.setBatchStock(List.of(batchRequest));
        requestDto.setSectionCode(section.getSectionCode());

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                        .content(asJsonString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    private Warehouse getWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.setLocation("New York");
        return warehouse;
    }

    private Product getProduct() {
        Product product = new Product();
        product.setProductName("Apple");
        product.setBrand("Nature");
        product.setCategory("Fruit");
        return product;
    }

    private Section getSection(Warehouse warehouse, Manager manager) {
        Section section = new Section();
        section.setCurrentBatches(1);
        section.setCategory(Section.Category.FRESH);
        section.setWarehouse(warehouse);
        section.setManager(manager);
        section.setMaxBatches(10);
        return section;
    }

    private Manager getManager() {
        Manager manager = new Manager();
        manager.setName("John Doe");
        manager.setUsername("john");
        manager.setEmail("john@example.com");
        return manager;
    }

    public void putUpdateInboundOrder_returnCreated_whenBatchExists() throws Exception {
        var batchRequest = generator.nextObject(BatchRequestDto.class);
        batchRequest.setManufacturingTime(LocalDateTime.now());
        batchRequest.setManufacturingDate(LocalDate.now());
        batchRequest.setDueDate(LocalDate.of(2022, 9,01));
        batchRequest.setProductId(1L);

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(BatchRequestDto.class, Batch.class).addMappings(mapper -> {
            mapper.map(BatchRequestDto::getProductId, Batch::setProduct);
        });
        Batch batch = modelMapper.map(batchRequest, Batch.class);

        batchRepository.save(batch);

        InboundOrderRequestDto inboundOrderRequestDto = new InboundOrderRequestDto();
        inboundOrderRequestDto.setSectionCode(1L);
        inboundOrderRequestDto.setBatchStock(Arrays.asList(batchRequest));

        InboundOrder ib = new InboundOrder();
        ib.setOrderDate(LocalDate.now());
        ib.setSection(sectionRepository.findById(1L).get());

        inboundOrderRepository.save(ib);

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                .param("orderNumber", "1")
                .content(asJsonString(inboundOrderRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

    private String asJsonString(final Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
