# Token Bucket Rate Limiter with Redis

A distributed rate limiter implementation using the Token Bucket algorithm and Redis.

## Overview

This project implements a Token Bucket Rate Limiter using Redis as a backend store. The rate limiter can be used to control the rate of actions (e.g., API requests) performed by users or services in a distributed system.

### What is a Token Bucket Rate Limiter?

The Token Bucket algorithm is a widely used rate limiting algorithm that works as follows:

1. Each client has a bucket that holds tokens
2. Tokens are added to the bucket at a constant rate (the refill rate)
3. The bucket has a maximum capacity
4. When a client makes a request, a token is removed from the bucket
5. If the bucket is empty, the request is denied
6. This allows for bursts of traffic up to the bucket capacity, while maintaining a long-term average rate

### Why Redis?

Redis is used to store the token bucket state, making this rate limiter suitable for distributed environments where multiple application instances need to enforce the same rate limits.

## Implementation Details

The implementation consists of the following components:

### TokenBucketRateLimiter

The main class that implements the rate limiting logic:

- Uses Redis transactions to ensure atomicity
- Dynamically calculates token refills based on elapsed time
- Supports multiple clients with separate rate limits
- Thread-safe for concurrent access

### Key Features

- **Distributed**: Works across multiple application instances
- **Accurate**: Properly handles time-based token refills
- **Efficient**: Uses Redis for fast, in-memory operations
- **Flexible**: Configurable bucket capacity and refill rate
- **Thread-safe**: Handles concurrent requests correctly

## Project Structure

```
.
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   │       └── org/
│   │   │           └── example/
│   │   │               └── TokenBucketRateLimiter.java
│   │   └── test/
│   │       └── java/
│   │           └── org/
│   │               └── example/
│   │                   ├── TokenBucketRateLimiterIntegrationTest.java
│   └── build.gradle.kts
├── gradle/
├── .gitignore
├── .gitattributes
├── gradle.properties
├── gradlew
├── gradlew.bat
├── run-tests.sh
├── settings.gradle.kts
├── LICENSE
└── README.md
```

## Test Suite

The project includes:

1. **Integration Tests**: Test with a real Redis instance using TestContainers

### Test Scenarios

- Basic rate limiting functionality
- Token refill behavior
- Multiple client handling

## Prerequisites

- Java 21 or higher
- Gradle (or use the included Gradle wrapper)
- Docker (for running integration tests with TestContainers)

## Getting Started

### Building the Project

```bash
# Clone the repository
git clone https://github.com/yourusername/token-bucket-rate-limiter.git
cd token-bucket-rate-limiter

# Build the project
./gradlew build
```

### Running the Tests

#### Using Helper Scripts

**Unix/Linux/macOS:**

```bash
# Make the script executable
chmod +x run-tests.sh

# Run all tests
./run-tests.sh all

# Run integration tests
./run-tests.sh integration

# Build without running tests
./run-tests.sh build

# Show help
./run-tests.sh help
```


#### Using Gradle Directly

```bash
# Run all tests
./gradlew test

# Run integration tests
./gradlew test --tests "org.example.TokenBucketRateLimiterIntegrationTest"
```

### Using the Rate Limiter in Your Project

```java
import org.example.TokenBucketRateLimiter;
import redis.clients.jedis.Jedis;

// Create a Jedis client
Jedis jedis = new Jedis("localhost", 6379);

// Create a rate limiter with:
// - bucket capacity of 10 tokens
// - refill rate of 2 tokens per second
TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(jedis, 10, 2);

// Check if a client is allowed to perform an action
String clientId = "user-123";
if (rateLimiter.isAllowed(clientId)) {
    // Perform the action
    System.out.println("Request allowed");
} else {
    // Deny the action
    System.out.println("Rate limit exceeded");
}

// Don't forget to close the Jedis client when done
jedis.close();
```

## Configuration Options

The `TokenBucketRateLimiter` constructor accepts the following parameters:

- `jedis`: A Redis client instance
- `bucketCapacity`: The maximum number of tokens a bucket can hold
- `refillRate`: The number of tokens added to the bucket per second

## Performance Considerations

- Redis operations are performed using transactions to ensure atomicity
- The rate limiter calculates token refills based on elapsed time, minimizing Redis operations
- For high-throughput applications, consider using a Redis connection pool

## Troubleshooting

### Common Issues

1. **Java Not Found**: Ensure Java 21 or higher is installed and JAVA_HOME is set correctly.

   ```bash
   java -version
   ```

2. **Docker Not Running**: For integration tests, Docker must be running.

   ```bash
   docker info
   ```

3. **Redis Connection Issues**: If using an external Redis instance, ensure it's running and accessible.

   ```bash
   redis-cli ping
   ```

4. **Test Failures**: If tests fail, check Docker is running and has enough resources allocated.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request
