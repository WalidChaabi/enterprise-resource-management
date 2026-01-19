# Testing Documentation

## Overview

This directory contains all testing-related documentation for the ERM Backend project.

## Documentation Files

### Quick Start
- **[TESTING_GUIDE.md](TESTING_GUIDE.md)** - Quick reference for running and writing tests

### Detailed Guides
- **[TEST_COVERAGE.md](TEST_COVERAGE.md)** - Comprehensive guide to test coverage, metrics, and best practices
- **[TEST_IMPLEMENTATION_SUMMARY.md](TEST_IMPLEMENTATION_SUMMARY.md)** - Summary of implemented tests and coverage

## Quick Commands

```bash
# Run all tests
mvn test

# Run with coverage report
mvn clean test jacoco:report

# Run specific test
mvn test -Dtest=AuthServiceTest
```

## Test Structure

```
src/test/java/com/company/erm/
├── controller/          # Integration tests
│   ├── AuthControllerIntegrationTest.java
│   └── EmployeeControllerIntegrationTest.java
├── service/             # Unit tests
│   ├── AuthServiceTest.java
│   └── EmployeeServiceTest.java
└── util/                # Test utilities
    └── TestDataFactory.java
```

## Current Coverage

- **Estimated:** 25-30% (critical paths)
- **Target:** 30% minimum, 50%+ long-term
- **Focus:** Authentication, Employee Management, Security

## Next Steps

1. Continue adding tests for remaining services
2. Increase coverage to 40%+
3. Add domain service tests
4. Add security integration tests
