package dev.cantrella.ms_wallet.infra.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import dev.cantrella.ms_wallet.domain.TransactionType;
import dev.cantrella.ms_wallet.infra.adapter.in.web.CreateWalletRequest;
import dev.cantrella.ms_wallet.infra.adapter.in.web.DepositRequest;
import dev.cantrella.ms_wallet.infra.adapter.in.web.TransferRequest;
import dev.cantrella.ms_wallet.infra.adapter.out.persistence.entity.TransactionMongoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Testcontainers
class WalletControllerIT {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");

    @Container
    private static final RedisContainer REDIS_CONTAINER =
            new RedisContainer(DockerImageName.parse("redis:5.0.3-alpine")).withExposedPorts(6379);

    @DynamicPropertySource
    static void setMongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection("transactions");
    }

    @Test
    void shouldCreateWalletSuccessfully() throws Exception {
        CreateWalletRequest request = new CreateWalletRequest(UUID.randomUUID().toString());

        mockMvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldDepositSuccessfully() throws Exception {
        // Primeiro cria a wallet
        String userId = UUID.randomUUID().toString();
        CreateWalletRequest createRequest = new CreateWalletRequest(userId);

        String walletId = mockMvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String createdWalletId = objectMapper.readTree(walletId).get("id").asText();

        DepositRequest depositRequest = new DepositRequest(new BigDecimal("100.00"));

        mockMvc.perform(post("/wallets/{walletId}/deposit", createdWalletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    void shouldWithdrawSuccessfully() throws Exception {
        // Criar a wallet
        String userId = UUID.randomUUID().toString();
        CreateWalletRequest createRequest = new CreateWalletRequest(userId);

        String walletId = mockMvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String createdWalletId = objectMapper.readTree(walletId).get("id").asText();

        // Deposita um valor para poder sacar
        DepositRequest depositRequest = new DepositRequest(new BigDecimal("200.00"));

        mockMvc.perform(post("/wallets/{walletId}/deposit", createdWalletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk());

        // Agora realiza o saque
        DepositRequest withdrawRequest = new DepositRequest(new BigDecimal("50.00"));

        mockMvc.perform(post("/wallets/{walletId}/withdraw", createdWalletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(50.00));
    }

    @Test
    void shouldConsultBalanceSuccessfully() throws Exception {
        String userId = UUID.randomUUID().toString();
        CreateWalletRequest createRequest = new CreateWalletRequest(userId);

        String walletId = mockMvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String createdWalletId = objectMapper.readTree(walletId).get("id").asText();

        // Consulta o saldo
        mockMvc.perform(get("/wallets/{walletId}/balance", createdWalletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").exists());
    }

    @Test
    void shouldConsultBalanceSuccessfullyAfterADeposit() throws Exception {
        String userId = UUID.randomUUID().toString();
        CreateWalletRequest createRequest = new CreateWalletRequest(userId);

        String walletId = mockMvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String createdWalletId = objectMapper.readTree(walletId).get("id").asText();

        // Deposita um valor para poder sacar
        DepositRequest depositRequest = new DepositRequest(new BigDecimal("200.00"));

        mockMvc.perform(post("/wallets/{walletId}/deposit", createdWalletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk());

        // Consulta o saldo
        mockMvc.perform(get("/wallets/{walletId}/balance", createdWalletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").exists())
                .andExpect(jsonPath("$.balance").value(200.00));
    }

    @Test
    void shouldTransferSuccessfully() throws Exception {
        // Cria carteira de origem
        String userId1 = UUID.randomUUID().toString();
        String userId2 = UUID.randomUUID().toString();

        String walletId1 = objectMapper.readTree(mockMvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateWalletRequest(userId1))))
                .andReturn().getResponse().getContentAsString()).get("id").asText();

        String walletId2 = objectMapper.readTree(mockMvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateWalletRequest(userId2))))
                .andReturn().getResponse().getContentAsString()).get("id").asText();

        // Deposita na carteira de origem
        mockMvc.perform(post("/wallets/{walletId}/deposit", walletId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DepositRequest(new BigDecimal("500.00")))))
                .andExpect(status().isOk());

        // Realiza a transferência
        TransferRequest transferRequest = new TransferRequest(walletId1, walletId2, new BigDecimal("100.00"));

        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.00));

        mockMvc.perform(get("/wallets/{walletId}/balance", walletId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").exists())
                .andExpect(jsonPath("$.balance").value(400.00));

        mockMvc.perform(get("/wallets/{walletId}/balance", walletId2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").exists())
                .andExpect(jsonPath("$.balance").value(100.00));
    }

    @Test
    void shouldGetHistoryWithSuccess() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String userId = UUID.randomUUID().toString();
        CreateWalletRequest createRequest = new CreateWalletRequest(userId);

        String walletId = mockMvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String createdWalletId = objectMapper.readTree(walletId).get("id").asText();

        var mongoId = UUID.randomUUID().toString().replace("-", "");

        mongoTemplate.save(
                TransactionMongoEntity.builder()
                .id(mongoId)
                .transactionId(UUID.randomUUID().toString())
                .sourceWalletId(createdWalletId)
                .destinationWalletId(null)
                .type(TransactionType.DEPOSIT.toString())
                .amount(new BigDecimal("200.00"))
                .currency("BRL")
                .timestamp(LocalDateTime.now().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() * 1000)
                .build());

        mockMvc.perform(get("/wallets/{walletId}/balance/history", createdWalletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("at", "2025-05-26T20:47:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0.00));

        mockMvc.perform(get("/wallets/{walletId}/balance/history", createdWalletId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("at", "2025-05-27T20:47:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(200.00));
    }
}
