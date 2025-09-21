# 📖 Pocket Backend Documentation

[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green.svg)](https://spring.io/projects/spring-boot)
[![Tests](https://img.shields.io/badge/Tests-31%2F31%20Passing-brightgreen.svg)](src/test/)
[![Security](https://img.shields.io/badge/Security-Spring%20Security-red.svg)](https://spring.io/projects/spring-security)
[![Apache](https://img.shields.io/badge/Apache-Configured-orange.svg)](docs/APACHE_SETUP.md)

Welcome to the comprehensive Pocket Backend documentation! This secure and scalable backend application is built with **Spring Boot 3.5.6** and **Java 21**, providing robust REST APIs for session management, user authentication, and secure data storage with end-to-end encryption.

## 🌟 What's New in Version 5.0.0

- **✅ Spring Boot 3.5.6**: Latest stable release with enhanced performance and security
- **✅ Complete Test Suite**: 31/31 tests passing with 100% coverage of critical paths
- **✅ Apache HTTP Server**: Production-ready configuration with SSL, load balancing, and security
- **✅ Enhanced Security**: Custom authentication filter with RSA token validation
- **✅ Comprehensive Documentation**: Detailed setup guides for development and production

## 📚 Available Languages

- **🇬🇧 English**: [README.md](README.md) - Complete documentation in English
- **🇮🇹 Italiano**: [README-IT.md](README-IT.md) - Documentazione completa in italiano

---

## 🚀 Quick Navigation

### 📋 Getting Started
- [📦 Requirements](README.md#-requirements) - Development and production requirements
- [⚡ Quick Start](README.md#-quick-start) - Get running in 5 minutes
- [🐳 Docker Deployment](README.md#-docker-deployment) - Complete containerized setup
- [🌐 Apache Configuration](docs/APACHE_SETUP.md) - Production-ready reverse proxy

### 🔐 Security & Configuration
- [🛡️ Security Features](README.md#-security-configuration) - Authentication, authorization, encryption
- [⚙️ Environment Configuration](README.md#environment-configuration) - Development and production setup
- [🔑 User Management](README.md#-user-and-device-management) - CLI tools for user and device management
- [📡 API Usage](README.md#-api-usage-examples) - Authentication and endpoint examples

### 🛠️ Development & Testing
- [🧪 Test Suite](src/test/README.md) - Comprehensive testing with 31 tests
- [🔍 Monitoring](README.md#-monitoring--health-checks) - Health checks and metrics
- [🐛 Troubleshooting](README.md#-troubleshooting) - Common issues and solutions
- [📊 Performance](README.md#performance-monitoring) - Optimization and monitoring

### 🏗️ Architecture & Technical Details

#### Core Technologies
| Component | Version | Purpose |
|-----------|---------|---------|
| **Spring Boot** | 3.5.6 | Application framework |
| **Java** | 21 LTS | Runtime platform |
| **Spring Security** | Latest | Authentication & authorization |
| **MariaDB** | 10.6+ | Primary database |
| **Apache HTTP** | 2.4+ | Reverse proxy & load balancer |
| **Docker** | 24.0+ | Containerization |

#### API Endpoints Overview
| Path | Method | Authentication | Description |
|------|--------|----------------|-------------|
| `/api/v5/{uuid}/{crypt}` | GET | RSA Token | Retrieve session data |
| `/api/v5/{uuid}/{crypt}` | POST | RSA Token | Create/update session |
| `/api/v5/{uuid}/{crypt}/check` | GET | RSA Token | Health check with auth |
| `/actuator/health` | GET | None | Application health |
| `/actuator/**` | * | HTTP Basic | Admin endpoints |

#### Security Features
- **🔐 RSA + AES Encryption**: Hybrid encryption for maximum security
- **🛡️ Custom Authentication Filter**: Path-based RSA token validation
- **🌐 Dynamic CORS**: Environment-based CORS configuration
- **📝 Input Validation**: Bean Validation with custom patterns
- **🔒 Security Headers**: HSTS, CSP, X-Frame-Options, etc.
- **⚡ Session Management**: Stateless API, secure admin sessions

### 📖 Detailed Documentation

#### Development Guides
- **🇬🇧 English**:
  - [Complete Setup Guide](README.md#-quick-start)
  - [Docker Development](README.md#-docker-deployment)
  - [Security Configuration](README.md#-security-configuration)
  - [API Documentation](README.md#-api-usage-examples)
  - [Troubleshooting Guide](README.md#-troubleshooting)

- **🇮🇹 Italiano**:
  - [Guida Completa Setup](README-IT.md#-avvio-rapido)
  - [Sviluppo con Docker](README-IT.md#-distribuzione-docker)
  - [Configurazione Sicurezza](README-IT.md#-configurazione-sicurezza)
  - [Documentazione API](README-IT.md#-esempi-uso-api)
  - [Risoluzione Problemi](README-IT.md#-risoluzione-problemi)

#### Production Deployment
- **Apache HTTP Server**: [Complete Setup Guide](docs/APACHE_SETUP.md)
  - SSL/TLS configuration with Let's Encrypt
  - Load balancing with health checks
  - Security hardening and rate limiting
  - Performance optimization (compression, caching)
  - Monitoring and troubleshooting

#### Testing Documentation
- **Test Suite Overview**: [Testing Guide](src/test/README.md)
  - Unit Tests: Controller and service layer testing
  - Integration Tests: Full application stack testing
  - Security Tests: Authentication filter and validation
  - Mock Client Tests: External API communication testing

### 🔧 Quick Commands Reference

#### Development
```bash
# Clone and setup
git clone https://github.com/passy1977/pocket-backend.git
cd pocket-backend

# Quick Docker setup
./build_docker_image.sh

# Manual build and run
mvn clean install
mvn spring-boot:run
```

#### Testing
```bash
# Run all tests
mvn test

# Run specific test suite
mvn test -Dtest=SessionRestTest
mvn test -Dtest=AuthenticationFilterTest
mvn test -Dtest=PocketApiClientTest

# Generate test coverage report
mvn test jacoco:report
```

#### Docker Operations
```bash
# Start all services
docker compose up -d

# View logs
docker compose logs -f pocket-backend

# Health check
curl http://localhost:8081/actuator/health

# User management (CLI tools)
pocket-user add -e user@example.com -p password -n "User Name"
pocket-device add -e user@example.com -d "Device Name"
```

### 🆘 Support & Community

- **📋 Issues**: [GitHub Issues](https://github.com/passy1977/pocket-backend/issues)
- **💬 Discussions**: [GitHub Discussions](https://github.com/passy1977/pocket-backend/discussions)
- **📚 Wiki**: [DeepWiki Documentation](https://deepwiki.com/passy1977/pocket-lib)
- **🔧 CLI Tools**: [Pocket CLI](https://github.com/passy1977/pocket-cli)
- **📱 Client Apps**: [iOS Client](https://github.com/passy1977/pocket-ios)

### 📄 License & Legal

This project is licensed under the **GNU General Public License v3.0**. See [LICENSE](LICENSE) for details.

---

**Made with ❤️ in Italy** | **Last Updated**: September 2025 | **Version**: 5.0.0