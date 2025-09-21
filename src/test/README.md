# 🧪 Pocket Backend Testing Suite

This directory contains a comprehensive testing suite for the Pocket Backend application.

## 📁 Test Structure

```
src/test/java/it/salsi/pocket/
├── rests/                  # REST API layer tests
│   └── SessionRestTest.java
├── integration/            # Integration tests  
│   └── SessionRestIntegrationTest.java
├── security/              # Security component tests
│   └── AuthenticationFilterTest.java
├── client/                # Mock client and examples
│   ├── PocketApiClient.java
│   ├── PocketApiClientTest.java
│   └── PocketApiClientExample.java
└── resources/
    └── application-test.yaml
```

## 🎯 Test Categories

### 1. Unit Tests
- **SessionRestTest**: Tests REST controller logic in isolation
- **AuthenticationFilterTest**: Tests security filter behavior
- Mock dependencies using Mockito
- Fast execution, no external dependencies

### 2. Integration Tests  
- **SessionRestIntegrationTest**: Tests full Spring context with real HTTP requests
- Uses H2 in-memory database
- Tests API validation and error handling
- Spring Boot Test with MockMvc

### 3. Client Tests
- **PocketApiClient**: HTTP client for API testing
- **PocketApiClientTest**: Tests client functionality
- **PocketApiClientExample**: Demonstrates usage patterns

## 🚀 Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Categories
```bash
# Unit tests only
mvn test -Dtest="*Test"

# Integration tests only  
mvn test -Dtest="*IntegrationTest"

# Client tests only
mvn test -Dtest="*ClientTest"
```

### Run Tests with Coverage
```bash
mvn jacoco:prepare-agent test jacoco:report
```

### Run Tests in Specific Profile
```bash
mvn test -Dspring.profiles.active=test
```

## 📊 Test Coverage

The test suite covers:

### REST API Endpoints
- ✅ GET `/api/v5/{uuid}/{crypt}` - Session data retrieval
- ✅ POST `/api/v5/{uuid}/{crypt}` - Session data persistence  
- ✅ PUT `/api/v5/{uuid}/{crypt}/{changePasswd}` - Password change
- ✅ DELETE `/api/v5/{uuid}/{crypt}` - Cache record deletion
- ✅ GET `/api/v5/{uuid}/{crypt}/check` - Cache status check

### Validation Testing
- ✅ UUID format validation (RFC 4122)
- ✅ Crypt parameter validation (alphanumeric + underscore/dash)
- ✅ Parameter length validation (10-2048 chars)
- ✅ Request body validation (JSON structure)
- ✅ HTTP method validation

### Security Testing
- ✅ Authentication filter behavior
- ✅ Request routing and bypassing
- ✅ Error handling for invalid credentials
- ✅ Security context management

### Error Scenarios
- ✅ Invalid UUID formats
- ✅ Invalid crypt parameters
- ✅ Missing required parameters
- ✅ Malformed JSON requests
- ✅ Network connectivity issues

## 🛠️ Test Configuration

### Test Database (H2)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### Test Properties
- Random server port for parallel test execution
- Debug logging for troubleshooting
- In-memory database for isolation
- Mock authentication credentials

## 🧪 Mock Client Usage

### Basic Usage
```java
PocketApiClient client = new PocketApiClient("http://localhost:8081");

// Test connection
boolean connected = client.testConnection();

// Get session data
ApiResponse<Container> response = client.getData(uuid, crypt);

// Handle response
if (response.isSuccess()) {
    Container data = response.getData();
    // Process data
} else {
    System.err.println("Error: " + response.getErrorMessage());
}
```

### Async Operations
```java
CompletableFuture<ApiResponse<Container>> future = client.getDataAsync(uuid, crypt);
ApiResponse<Container> response = future.get(10, TimeUnit.SECONDS);
```

### Batch Testing
```java
CompletableFuture<ApiResponse<Container>>[] futures = 
    client.createBatchRequests(uuid, crypt, 10);
CompletableFuture.allOf(futures).get();
```

## 📈 Performance Testing

### Load Testing
```java
// Run the example client for load testing
java -cp target/test-classes it.salsi.pocket.client.PocketApiClientExample
```

### Concurrent Requests
- Tests 1, 5, 10, 20 concurrent requests
- Measures response times and success rates
- Validates server stability under load

## 🔧 Test Dependencies

### Maven Dependencies
```xml
<!-- Spring Boot Test Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Spring Security Test -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- H2 Database for Testing -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- Testcontainers (for advanced integration testing) -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- WireMock (for HTTP mocking) -->
<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-standalone</artifactId>
    <scope>test</scope>
</dependency>
```

## 📝 Best Practices

### Test Naming
- Use descriptive test method names
- Include expected behavior in test names
- Group related tests in nested classes

### Test Data
- Use realistic test data
- Create test data builders/factories
- Clean up test data between tests

### Assertions
- Use specific assertions
- Test both positive and negative cases
- Verify error messages and status codes

### Mocking
- Mock external dependencies
- Use @Mock and @InjectMocks annotations
- Verify interactions with mocks

## 🐛 Troubleshooting

### Common Issues

#### Test Database Connection
```bash
# Check H2 console (if enabled)
http://localhost:8080/h2-console

# JDBC URL: jdbc:h2:mem:testdb
# Username: sa
# Password: (empty)
```

#### Port Conflicts
```yaml
# Use random port in tests
server:
  port: 0
```

#### Authentication Issues
```bash
# Check test configuration
cat src/test/resources/application-test.yaml

# Verify mock authentication setup
```

### Debug Logging
```yaml
logging:
  level:
    it.salsi.pocket: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
```

## 📚 Additional Resources

- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [MockMvc Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#spring-mvc-test-framework)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

---

**Happy Testing!** 🎉