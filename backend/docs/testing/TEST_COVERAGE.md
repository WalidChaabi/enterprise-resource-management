# Test Coverage Guide

## Overview

This project uses **JUnit 5** and **Mockito** for testing, with **JaCoCo** for code coverage reporting.

## Test Structure

```
src/test/java/com/company/erm/
├── controller/          # Integration tests for REST controllers
├── service/             # Unit tests for service layer
└── util/               # Test utilities and helpers
```

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Tests with Coverage Report

```bash
mvn clean test jacoco:report
```

The coverage report will be generated at: `target/site/jacoco/index.html`

### Run Specific Test Class

```bash
mvn test -Dtest=AuthServiceTest
```

### Run Tests in IDE

- **IntelliJ IDEA:** Right-click on test class → Run
- **Eclipse:** Right-click on test class → Run As → JUnit Test
- **VS Code:** Use Java Test Runner extension

## Test Coverage Goals

### Current Coverage

- **Target:** 30% minimum (critical paths)
- **Focus Areas:**
  - Authentication & Authorization
  - Employee Management
  - Security Enforcement
  - Business Logic

### Coverage by Layer

| Layer | Target | Status |
|-------|--------|--------|
| Services | 50%+ | In Progress |
| Controllers | 40%+ | In Progress |
| Security | 60%+ | In Progress |
| Domain Services | 40%+ | Pending |

## Test Types

### 1. Unit Tests

**Location:** `src/test/java/com/company/erm/service/`

**Purpose:** Test individual service methods in isolation

**Example:**
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @InjectMocks private AuthService authService;
    
    @Test
    void login_shouldReturnToken_whenCredentialsValid() {
        // Test implementation
    }
}
```

**Coverage:**
- ✅ AuthService - Login flow, error handling
- ✅ EmployeeService - CRUD operations, security

### 2. Integration Tests

**Location:** `src/test/java/com/company/erm/controller/`

**Purpose:** Test full HTTP request/response cycle

**Example:**
```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    
    @Test
    void login_shouldReturn200_whenCredentialsValid() {
        // Test implementation
    }
}
```

**Coverage:**
- ✅ AuthController - Authentication endpoints
- ✅ EmployeeController - Employee management with security

### 3. Security Tests

**Purpose:** Test RBAC enforcement and multi-tenant isolation

**Coverage:**
- ✅ Permission checks in EmployeeService
- ✅ Role-based access in controllers
- ⏳ Multi-tenant isolation (pending)

## Test Utilities

### TestDataFactory

**Location:** `src/test/java/com/company/erm/util/TestDataFactory.java`

**Purpose:** Create test entities with minimal boilerplate

**Usage:**
```java
Organization org = TestDataFactory.createOrganization();
User user = TestDataFactory.createUser(org, dept, "email@example.com", "password", passwordEncoder);
```

## Test Configuration

### Test Database

Tests use **H2 in-memory database** for fast execution:

```properties
spring.datasource.url=jdbc:h2:mem:ermdb
spring.flyway.enabled=false
```

### Test Profile

Tests run with `@ActiveProfiles("test")` to use test-specific configuration.

## Coverage Reports

### Generate Report

```bash
mvn clean test jacoco:report
```

### View Report

Open `target/site/jacoco/index.html` in a browser.

### Coverage Metrics

JaCoCo tracks:
- **Line Coverage:** Percentage of lines executed
- **Branch Coverage:** Percentage of branches executed
- **Method Coverage:** Percentage of methods executed
- **Class Coverage:** Percentage of classes executed

## Best Practices

### 1. Test Naming

Use descriptive test names:
```java
@Test
@DisplayName("Should return token when credentials are valid")
void login_shouldReturnToken_whenCredentialsValid() {
    // Test implementation
}
```

### 2. Arrange-Act-Assert Pattern

```java
@Test
void testExample() {
    // Arrange - Set up test data
    User user = TestDataFactory.createUser(...);
    
    // Act - Execute the method
    AuthResponse response = authService.login(request);
    
    // Assert - Verify results
    assertThat(response).isNotNull();
    assertThat(response.accessToken()).isNotEmpty();
}
```

### 3. Mock External Dependencies

```java
@Mock
private UserRepository userRepository;

@Mock
private PermissionChecker permissionChecker;
```

### 4. Test Critical Paths First

Priority order:
1. Authentication & Authorization
2. Security enforcement
3. Business logic
4. Data validation
5. Error handling

## Current Test Coverage

### ✅ Completed

- **AuthService:** Login flow, error cases, inactive user
- **EmployeeService:** Create, update, get, list, deactivate
- **AuthController:** Full authentication flow
- **EmployeeController:** CRUD with security

### ⏳ Pending

- **DepartmentService:** CRUD operations
- **ProjectService:** Project management
- **RoleService:** Role management
- **Security:** Multi-tenant isolation tests
- **Domain Services:** Business rule validation

## CI/CD Integration

### Maven Command

```bash
mvn clean test jacoco:report
```

### Coverage Threshold

JaCoCo is configured to fail the build if coverage drops below 30%:
- Package level: 30% line coverage
- Class level: 30% line coverage

### GitHub Actions Example

```yaml
- name: Run tests with coverage
  run: mvn clean test jacoco:report

- name: Upload coverage report
  uses: codecov/codecov-action@v3
  with:
    file: target/site/jacoco/jacoco.xml
```

## Troubleshooting

### Tests Fail with Database Errors

**Solution:** Ensure H2 is in classpath and Flyway is disabled in test profile.

### Mockito Mocks Not Working

**Solution:** Ensure `@ExtendWith(MockitoExtension.class)` is present.

### Integration Tests Fail

**Solution:** Check that `@SpringBootTest` and `@AutoConfigureMockMvc` are configured correctly.

## Next Steps

1. ✅ Add more service layer tests
2. ⏳ Add domain service tests
3. ⏳ Add security integration tests
4. ⏳ Increase coverage to 50%+
5. ⏳ Add performance tests for critical paths

## References

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
