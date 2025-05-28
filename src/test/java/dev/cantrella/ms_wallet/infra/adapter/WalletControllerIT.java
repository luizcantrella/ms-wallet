package dev.cantrella.ms_wallet.infra.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import dev.cantrella.ms_wallet.domain.TransactionType;
import dev.cantrella.ms_wallet.infra.adapter.in.web.DepositOrWithdrawRequest;
import dev.cantrella.ms_wallet.infra.adapter.in.web.TransferRequest;
import dev.cantrella.ms_wallet.infra.adapter.out.persistence.entity.TransactionMongoEntity;
import dev.cantrella.ms_wallet.infra.adapter.out.persistence.entity.WalletEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional
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

    @Autowired
    private EntityManager entityManager;

    String bobWalletId;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection("transactions");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setSecurityContext(String email, String name) {
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(
                        Jwt.withTokenValue(name)
                                .header("alg", "RS256")
                                .claim("email", email)
                                .build()
                )
        );
    }

    private String createClientWallet(String email, String name) throws Exception {
        setSecurityContext(email, name);

        String walletId = mockMvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(walletId).get("id").asText();
    }

    @Test
    void shouldCreateWalletSuccessfully() throws Exception {
        createClientWallet("bob@mail.com", "bob");
    }

    @Test
    void shouldDepositSuccessfully() throws Exception {
        createClientWallet("bob@mail.com", "bob");
        DepositOrWithdrawRequest depositOrWithdrawRequest = new DepositOrWithdrawRequest(new BigDecimal("100.00"));
        mockMvc.perform(post("/wallets/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositOrWithdrawRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    void shouldWithdrawSuccessfully() throws Exception {
        createClientWallet("bob@mail.com", "bob");
        DepositOrWithdrawRequest depositOrWithdrawRequest = new DepositOrWithdrawRequest(new BigDecimal("200.00"));
        mockMvc.perform(post("/wallets/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositOrWithdrawRequest)))
                .andExpect(status().isOk());

        DepositOrWithdrawRequest withdrawRequest = new DepositOrWithdrawRequest(new BigDecimal("50.00"));
        mockMvc.perform(post("/wallets/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(50.00));
    }

    @Test
    void shouldConsultBalanceSuccessfully() throws Exception {
        createClientWallet("bob@mail.com", "bob");
        mockMvc.perform(get("/wallets/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").exists());
    }

    @Test
    void shouldConsultBalanceSuccessfullyAfterADeposit() throws Exception {
        createClientWallet("bob@mail.com", "bob");
        DepositOrWithdrawRequest depositOrWithdrawRequest = new DepositOrWithdrawRequest(new BigDecimal("200.00"));
        mockMvc.perform(post("/wallets/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositOrWithdrawRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/wallets/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").exists())
                .andExpect(jsonPath("$.balance").value(200.00));
    }

    @Test
    void shouldTransferSuccessfully() throws Exception {

        WalletEntity wallet = WalletEntity.builder()
                .id(UUID.randomUUID())
                .userId("john@mail.com")
                .balance(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.merge(wallet);
        String bobWalletId = createClientWallet("bob@mail.com", "bob");
        DepositOrWithdrawRequest depositOrWithdrawRequest = new DepositOrWithdrawRequest(new BigDecimal("200.00"));
        mockMvc.perform(post("/wallets/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositOrWithdrawRequest)))
                .andExpect(status().isOk());

        TransferRequest transferRequest = new TransferRequest(bobWalletId, wallet.getId().toString(), new BigDecimal("100.00"));
        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.00));
        mockMvc.perform(get("/wallets/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").exists())
                .andExpect(jsonPath("$.balance").value(100.00));

    }

    @Test
    void shouldGetHistoryWithSuccess() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        var before = LocalDateTime.now().format(formatter);
        Thread.sleep(1000);
        String bobWalletId = createClientWallet("bob@mail.com", "bob");
        mongoTemplate.save(
                TransactionMongoEntity.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .transactionId(UUID.randomUUID().toString())
                .sourceWalletId(bobWalletId)
                .destinationWalletId(null)
                .type(TransactionType.DEPOSIT.toString())
                .amount(new BigDecimal("200.00"))
                .timestamp(LocalDateTime.now().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() * 1000)
                .build());


        var firstTime = LocalDateTime.now().format(formatter);
        Thread.sleep(1000);
        mongoTemplate.save(
                TransactionMongoEntity.builder()
                        .id(UUID.randomUUID().toString().replace("-", ""))
                        .transactionId(UUID.randomUUID().toString())
                        .sourceWalletId(bobWalletId)
                        .destinationWalletId(null)
                        .type(TransactionType.DEPOSIT.toString())
                        .amount(new BigDecimal("200.00"))
                        .timestamp(LocalDateTime.now().atZone(ZoneOffset.UTC).toInstant().getEpochSecond() * 1000)
                        .build());
        var secondTime = LocalDateTime.now().format(formatter);
        mockMvc.perform(get("/wallets/balance" )
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("at", before))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0.00));
        mockMvc.perform(get("/wallets/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("at", secondTime))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(400.00));
        mockMvc.perform(get("/wallets/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("at", firstTime))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(200.00));
    }
}
