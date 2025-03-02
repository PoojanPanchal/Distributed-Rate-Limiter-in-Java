package org.example;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Token Bucket Rate Limiter
 * 
 * This class implements a token bucket rate limiter using Redis.
 * It allows clients to check if they can perform an action within a given time window.
 * 
 * The limiter uses a token bucket algorithm to limit the rate of requests.
 * 
 */
public class TokenBucketRateLimiter {
    private final Jedis jedis;
    private final int bucketCapacity;
    private final double refillRate;
    
    // Constructor
    public TokenBucketRateLimiter(Jedis jedis, int bucketCapacity, int refillRate) {
        this.jedis = jedis;
        this.bucketCapacity = bucketCapacity;
        this.refillRate = refillRate;
    }

    /**
     * Check if the client is allowed to perform an action
     * 
     * @param clientId The client ID
     * @return true if the client is allowed to perform an action, false otherwise
     */
    public boolean isAllowed(String clientId) {
        String keyCount = "rate_limit:" + clientId + ":count"; 
        String keyLastRefill = "rate_limit:" + clientId + ":last_refill";

        // Get the current count and last refill time
        Transaction transaction = jedis.multi();
        transaction.get(keyCount);
        transaction.get(keyLastRefill);
        var result = transaction.exec();


        // Calculate the current time and last refill time
        long currentTime = System.currentTimeMillis();
        long lastRefillTime = result.get(1) != null ? Long.parseLong((String) result.get(1)) : currentTime;
        int tokenCount = result.get(0) != null ? Integer.parseInt((String) result.get(0)) : bucketCapacity;

        // Calculate the elapsed time and tokens to add        
        long elapsedTimeMs = currentTime - lastRefillTime;
        double elapsedTimeSeconds = elapsedTimeMs / 1000.0;

        int tokensToAdd = (int) (elapsedTimeSeconds * refillRate);
        tokenCount = Math.min(tokenCount + tokensToAdd, bucketCapacity);

        // Check if the client is allowed to perform an action
        boolean isAllowed = tokenCount >= 1;
        if (isAllowed) {
            tokenCount--;
        }

        // Update the count and last refill time
        transaction = jedis.multi();
        transaction.set(keyCount, String.valueOf(tokenCount));
        transaction.set(keyLastRefill, String.valueOf(currentTime));
        transaction.exec();

        // Return the result
        return isAllowed;   
    }
}