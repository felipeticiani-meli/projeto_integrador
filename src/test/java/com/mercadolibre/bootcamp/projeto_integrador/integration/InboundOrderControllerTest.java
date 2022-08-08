package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.*;
import com.mercadolibre.bootcamp.projeto_integrador.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class InboundOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

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

        BatchRequestDto batchRequest = getBatchRequest(product);

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

    @Test
    public void putUpdateInboundOrder_returnCreated_whenBatchExists() throws Exception {
        Warehouse warehouse = getWarehouse();
        Manager manager = getManager();
        Section section = getSection(warehouse, manager);
        Product product = getProduct();

        warehouseRepository.save(warehouse);
        managerRepository.save(manager);
        sectionRepository.save(section);
        productRepository.save(product);

        // Generate BatchRequestDto object
        BatchRequestDto batchRequest = getBatchRequest(product);

        // From BatchRequestDto map Batch to save in DB
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(BatchRequestDto.class, Batch.class).addMappings(mapper -> {
            mapper.map(BatchRequestDto::getProductId, Batch::setProduct);
        });
        Batch batch = modelMapper.map(batchRequest, Batch.class);

        // Save an InboundOrder in DB
        InboundOrder ib = new InboundOrder();
        ib.setOrderDate(LocalDate.now());
        ib.setSection(sectionRepository.findById(1L).get());
        inboundOrderRepository.save(ib);

        // Save Batch in DB
        batch.setInboundOrder(ib);
        batchRepository.save(batch);

        // Get the current temperature from batch to make a PUT to update it (specify BatchNumber).
        float newTemperature = batchRequest.getCurrentTemperature()+1;
        batchRequest.setBatchNumber(1L);
        batchRequest.setCurrentTemperature(newTemperature);

        // Create InboundOrderRequestDto to send in PUT body
        InboundOrderRequestDto requestDto = new InboundOrderRequestDto();
        requestDto.setSectionCode(section.getSectionCode());
        requestDto.setBatchStock(List.of(batchRequest));

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                .param("orderNumber", "1")
                .content(asJsonString(requestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        batch = batchRepository.findById(1L).get();
        assertThat(batch.getCurrentTemperature()).isEqualTo(newTemperature);
    }

    @Test
    public void putUpdateInboundOrder_returnCreated_whenBatchNotExists() throws Exception {
        Warehouse warehouse = getWarehouse();
        Manager manager = getManager();
        Section section = getSection(warehouse, manager);
        Product product = getProduct();

        warehouseRepository.save(warehouse);
        managerRepository.save(manager);
        sectionRepository.save(section);
        productRepository.save(product);

        // Generate BatchRequestDto object
        BatchRequestDto batchRequest = getBatchRequest(product);

        // Save an InboundOrder in DB
        InboundOrder ib = new InboundOrder();
        ib.setOrderDate(LocalDate.now());
        ib.setSection(sectionRepository.findById(1L).get());
        inboundOrderRepository.save(ib);

        // Create InboundOrderRequestDto to send in PUT body
        InboundOrderRequestDto requestDto = new InboundOrderRequestDto();
        requestDto.setSectionCode(section.getSectionCode());
        requestDto.setBatchStock(List.of(batchRequest));

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                .param("orderNumber", "1")
                .content(asJsonString(requestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        Batch batch = batchRepository.findById(1L).get();
        assertThat(batch).isNotNull();
        assertThat(batch.getInboundOrder().getOrderNumber()).isEqualTo(1L);
    }

    @Test
    public void putUpdateInboundOrder_returnCreated_whenInboundOrderIdNotExists() throws Exception {
        Warehouse warehouse = getWarehouse();
        Manager manager = getManager();
        Section section = getSection(warehouse, manager);
        Product product = getProduct();

        warehouseRepository.save(warehouse);
        managerRepository.save(manager);
        sectionRepository.save(section);
        productRepository.save(product);

        // Generate BatchRequestDto object
        BatchRequestDto batchRequest = getBatchRequest(product);

        // Save an InboundOrder in DB
        InboundOrder ib = new InboundOrder();
        ib.setOrderDate(LocalDate.now());
        ib.setSection(sectionRepository.findById(1L).get());
        inboundOrderRepository.save(ib);

        // Create InboundOrderRequestDto to send in PUT body
        InboundOrderRequestDto requestDto = new InboundOrderRequestDto();
        requestDto.setSectionCode(section.getSectionCode());
        requestDto.setBatchStock(List.of(batchRequest));

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                .param("orderNumber", "2")
                .content(asJsonString(requestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.name").value("Inbound not found."));
    }

    @Test
    public void putUpdateInboundOrder_returnCreated_whenSectionDoesNotHaveEnoughSpace() throws Exception {
        Warehouse warehouse = getWarehouse();
        Manager manager = getManager();
        Section section = getSection(warehouse, manager);
        section.setMaxBatches(1);
        Product product = getProduct();

        warehouseRepository.save(warehouse);
        managerRepository.save(manager);
        sectionRepository.save(section);
        productRepository.save(product);

        // Generate BatchRequestDto object
        BatchRequestDto batchRequest = getBatchRequest(product);

        // From BatchRequestDto map Batch to save in DB
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(BatchRequestDto.class, Batch.class).addMappings(mapper -> {
            mapper.map(BatchRequestDto::getProductId, Batch::setProduct);
        });
        Batch batch = modelMapper.map(batchRequest, Batch.class);

        // Save an InboundOrder in DB
        InboundOrder ib = new InboundOrder();
        ib.setOrderDate(LocalDate.now());
        ib.setSection(sectionRepository.findById(1L).get());
        inboundOrderRepository.save(ib);

        // Save Batch in DB
        batch.setInboundOrder(ib);
        batchRepository.save(batch);

        // Create InboundOrderRequestDto to send in PUT body
        InboundOrderRequestDto requestDto = new InboundOrderRequestDto();
        requestDto.setSectionCode(section.getSectionCode());
        requestDto.setBatchStock(List.of(batchRequest));

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                .param("orderNumber", "1")
                .content(asJsonString(requestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Section"))
                .andExpect(jsonPath("$.message", containsString("enough space")));
    }

    @Test
    public void putUpdateInboundOrder_returnCreated_whenInvalidInitialQuantity() throws Exception {
        Warehouse warehouse = getWarehouse();
        Manager manager = getManager();
        Section section = getSection(warehouse, manager);
        Product product = getProduct();

        warehouseRepository.save(warehouse);
        managerRepository.save(manager);
        sectionRepository.save(section);
        productRepository.save(product);

        // Generate BatchRequestDto object
        BatchRequestDto batchRequest = getBatchRequest(product);

        // From BatchRequestDto map Batch to save in DB
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(BatchRequestDto.class, Batch.class).addMappings(mapper -> {
            mapper.map(BatchRequestDto::getProductId, Batch::setProduct);
        });
        Batch batch = modelMapper.map(batchRequest, Batch.class);
        batch.setCurrentQuantity(batch.getInitialQuantity()-5);

        // Save an InboundOrder in DB
        InboundOrder ib = new InboundOrder();
        ib.setOrderDate(LocalDate.now());
        ib.setSection(sectionRepository.findById(1L).get());
        inboundOrderRepository.save(ib);

        // Save Batch in DB
        batch.setInboundOrder(ib);
        batchRepository.save(batch);

        // Change initial quantity to less than what have been sold
        batchRequest.setBatchNumber(1L);
        batchRequest.setInitialQuantity(batch.getInitialQuantity() - (batch.getCurrentQuantity()+1));

        // Create InboundOrderRequestDto to send in PUT body
        InboundOrderRequestDto requestDto = new InboundOrderRequestDto();
        requestDto.setSectionCode(section.getSectionCode());
        requestDto.setBatchStock(List.of(batchRequest));

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                .param("orderNumber", "1")
                .content(asJsonString(requestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Invalid batch quantity"))
                .andExpect(jsonPath("$.message", containsString("update batch initial quantity")));
    }

    private BatchRequestDto getBatchRequest(Product product){
        BatchRequestDto batchRequest = new BatchRequestDto();
        batchRequest.setCurrentTemperature(-1);
        batchRequest.setMinimumTemperature(-1);
        batchRequest.setManufacturingTime(LocalDateTime.now());
        batchRequest.setManufacturingDate(LocalDate.now());
        batchRequest.setInitialQuantity(10);
        batchRequest.setProductPrice(new BigDecimal(10));
        batchRequest.setDueDate(LocalDate.of(2022, 9,01));
        batchRequest.setProductId(product.getProductId());
        return batchRequest;
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

    private String asJsonString(final Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
