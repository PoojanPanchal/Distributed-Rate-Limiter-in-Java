#!/bin/bash

# Script to run different types of tests for the Token Bucket Rate Limiter

# Function to display help
show_help() {
    echo "Usage: ./run-tests.sh [OPTION]"
    echo "Run tests for the Token Bucket Rate Limiter project."
    echo ""
    echo "Options:"
    echo "  all         Run all tests"
    echo "  unit        Run only unit tests"
    echo "  integration Run only integration tests"
    echo "  performance Run only performance tests (requires removing @Disabled annotation)"
    echo "  build       Build the project without running tests"
    echo "  help        Display this help and exit"
    echo ""
    echo "Example: ./run-tests.sh unit"
}

# Check if Docker is running (needed for integration tests)
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo "Error: Docker is not running or not installed."
        echo "Docker is required for integration tests using TestContainers."
        echo "Please start Docker and try again."
        exit 1
    fi
}

# Main script logic
case "$1" in
    all)
        check_docker
        echo "Running all tests..."
        ./gradlew clean test
        ;;
    unit)
        echo "Running unit tests..."
        ./gradlew clean test --tests "org.example.TokenBucketRateLimiterTest"
        ;;
    integration)
        check_docker
        echo "Running integration tests..."
        ./gradlew clean test --tests "org.example.TokenBucketRateLimiterIntegrationTest"
        ;;
    performance)
        check_docker
        echo "Running performance tests..."
        echo "Note: You may need to remove the @Disabled annotation in TokenBucketRateLimiterPerformanceTest.java"
        ./gradlew clean test --tests "org.example.TokenBucketRateLimiterPerformanceTest"
        ;;
    build)
        echo "Building the project..."
        ./gradlew clean build -x test
        ;;
    help|*)
        show_help
        ;;
esac

exit 0 