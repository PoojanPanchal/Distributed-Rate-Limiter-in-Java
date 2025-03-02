package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class TokenBucketRateLimiterIntegrationTest {

    private static final int REDIS_PORT = 6379;
    private static final String CLIENT_ID = "test-client";
    private static final int BUCKET_CAPACITY = 5;
    private static final int REFILL_RATE = 2; // 2 tokens per second

    @Container
    public GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(REDIS_PORT);

    private Jedis jedis;
    private TokenBucketRateLimiter rateLimiter;

    @BeforeEach
    public void setUp() {
        String redisHost = redis.getHost();
        Integer redisPort = redis.getMappedPort(REDIS_PORT);
        
        jedis = new Jedis(redisHost, redisPort);
        rateLimiter = new TokenBucketRateLimiter(jedis, BUCKET_CAPACITY, REFILL_RATE);
        
        // Clear Redis before each test
        jedis.flushAll();
    }

    @AfterEach
    public void tearDown() {
        if (jedis != null) {
            jedis.close();
        }
    }

    /**
     * Test scenario: Basic rate limiting functionality
     * Expected: Allow requests up to the bucket capacity, then deny
     */
    @Test
    public void testBasicRateLimiting() {
        // First BUCKET_CAPACITY requests should be allowed
        for (int i = 0; i < BUCKET_CAPACITY; i++) {
            assertTrue(rateLimiter.isAllowed(CLIENT_ID), "Request " + (i + 1) + " should be allowed");
        }
        
        // Next request should be denied
        assertFalse(rateLimiter.isAllowed(CLIENT_ID), "Request exceeding capacity should be denied");
    }

    /**
     * Test scenario: Token refill after waiting
     * Expected: After waiting, tokens should be refilled
     */
    @Test
    public void testTokenRefill() throws InterruptedException {
        // Use all tokens
        for (int i = 0; i < BUCKET_CAPACITY; i++) {
            assertTrue(rateLimiter.isAllowed(CLIENT_ID), "Initial request " + (i + 1) + " should be allowed");
        }
        
        // Next request should be denied
        assertFalse(rateLimiter.isAllowed(CLIENT_ID), "Request after using all tokens should be denied");
        
        // Wait for tokens to refill (2 seconds should give us 4 tokens)
        Thread.sleep(2000);
        
        // Next 4 requests should be allowed
        for (int i = 0; i < 4; i++) {
            assertTrue(rateLimiter.isAllowed(CLIENT_ID), "Request " + (i + 1) + " after refill should be allowed");
        }
        
        // Next request should be denied
        assertFalse(rateLimiter.isAllowed(CLIENT_ID), "Request exceeding refilled capacity should be denied");
    }

    /**
     * Test scenario: Multiple clients
     * Expected: Each client should have its own rate limit
     */
    @Test
    public void testMultipleClients() {
        String clientId1 = "client1";
        String clientId2 = "client2";
        
        // Use all tokens for client1
        for (int i = 0; i < BUCKET_CAPACITY; i++) {
            assertTrue(rateLimiter.isAllowed(clientId1), "Request " + (i + 1) + " for client1 should be allowed");
        }
        
        // Client1 should be denied
        assertFalse(rateLimiter.isAllowed(clientId1), "Request exceeding capacity for client1 should be denied");
        
        // Client2 should still be allowed
        for (int i = 0; i < BUCKET_CAPACITY; i++) {
            assertTrue(rateLimiter.isAllowed(clientId2), "Request " + (i + 1) + " for client2 should be allowed");
        }
        
        // Client2 should now be denied
        assertFalse(rateLimiter.isAllowed(clientId2), "Request exceeding capacity for client2 should be denied");
    }

    /**
     * Test scenario: Partial refill
     * Expected: Tokens should be partially refilled based on elapsed time
     */
    @Test
    public void testPartialRefill() throws InterruptedException {
        // Use all tokens
        for (int i = 0; i < BUCKET_CAPACITY; i++) {
            assertTrue(rateLimiter.isAllowed(CLIENT_ID), "Initial request " + (i + 1) + " should be allowed");
        }
        
        // Next request should be denied
        assertFalse(rateLimiter.isAllowed(CLIENT_ID), "Request after using all tokens should be denied");
        
        // Wait for partial refill (500ms should give us 1 token with refill rate of 2/sec)
        Thread.sleep(500);
        
        // One request should be allowed
        assertTrue(rateLimiter.isAllowed(CLIENT_ID), "One request should be allowed after partial refill");
        
        // Next request should be denied
        assertFalse(rateLimiter.isAllowed(CLIENT_ID), "Request exceeding refilled capacity should be denied");
    }
} 