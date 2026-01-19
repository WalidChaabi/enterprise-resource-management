# Testing Guide

## Quick Start

### Run All Tests

```bash
mvn test
```

### Run Tests with Coverage

```bash
mvn clean test jacoco:report
# View report: target/site/jacoco/index.html
```

### Run Specific Test

```bash
mvn test -Dtest=AuthServiceTest
```

## Test Structure

```
src/test/java/com/company/erm/
├── controller/          # Integration tests
│   ├── AuthControllerIntegrationTest.java
│   └── EmployeeControllerIntegrationTest.java
├── service/            # Unit tests
│   ├── AuthServiceTest.java
│   └── EmployeeServiceTest.java
└── util/               # Test utilities
    └── TestDataFactory.java
```

## Writing Tests

### Unit Test Example

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    @Mock
    private MyRepository repository;
    
    @InjectMocks
    private MyService service;
    
    @Test
    void shouldDoSomething_whenCondition() {
        // Arrange
        when(repository.findById(any())).thenReturn(Optional.of(entity));
        
        // Act
        Result result = service.doSomething(id);
        
        // Assert
        assertThat(result).isNotNull();
        verify(repository).findById(id);
    }
}
```

### Integration Test Example

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MyControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldReturn200_whenRequestValid() throws Exception {
        mockMvc.perform(get("/api/endpoint")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.field").value("expected"));
    }
}
```

## Test Utilities

### TestDataFactory

Create test entities easily:

```java
Organization org = TestDataFactory.createOrganization();
User user = TestDataFactory.createUser(org, dept, "email@example.com", "password", passwordEncoder);
```

## Coverage Goals

- **Current Target:** 30% (critical paths)
- **Future Target:** 50%+ (comprehensive)

## See Also

- [TEST_COVERAGE.md](TEST_COVERAGE.md) - Detailed coverage guide
- [ARCHITECTURAL_REVIEW.md](../architecture/ARCHITECTURAL_REVIEW.md) - Testing recommendations
