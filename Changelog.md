# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [5.0.0] - 2025/09/21
### Added
- **Spring Boot 3.5.6**: Upgraded from 3.4.4 for enhanced performance and security
- **Complete Test Suite (31/31 passing)**: Comprehensive testing framework
  - `SessionRestTest.java` - 8 unit tests for REST controller with Mockito
  - `AuthenticationFilterTest.java` - 8 security filter tests with RSA validation
  - `PocketApiClientTest.java` - 15 HTTP client tests with async communication
  - Test configuration with H2 in-memory database and proper isolation
- **Mock API Client**: Full-featured HTTP client for testing and integration
  - `PocketApiClient.java` - Async/sync API client with CompletableFuture support
  - Health check requests with connection failure handling
  - JSON serialization and deserialization for API communication
- **Socket Management Service**: Built-in TCP socket for user/device administration
  - `IpcSocketManagerImpl.java` - Real-time user and device management
  - TCP socket on port 8300 with password authentication
  - Commands: ADD_USER, MOD_USER, RM_USER, GET_USER, ADD_DEVICE, RM_DEVICE, GET_DEVICE
  - JSON responses with structured error codes and RSA key generation
- **Apache HTTP Server Configuration**: Production-ready reverse proxy setup
  - Complete SSL/TLS configuration with Let's Encrypt support
  - Load balancing with health checks and failover
  - Security headers (HSTS, CSP, X-Frame-Options, etc.)
  - Compression (mod_deflate) and performance optimization
  - Rate limiting and DDoS protection
- **Enhanced Documentation**:
  - `docs/APACHE_SETUP.md` - Comprehensive Apache setup guide
  - Updated README.md with Spring Boot 3.5.6, Socket Service, and Apache configuration
  - Complete troubleshooting and monitoring guides
  - Testing documentation with coverage reports
  - Socket management documentation in English and Italian

### Fixed
- **Spring Boot Compatibility**: Fixed spring-boot-admin version to 3.5.0 for Spring Boot 3.5.6
- **Test Infrastructure**: Resolved authentication filter interference with Bean Validation
- **Port Conflicts**: Changed PocketApiClientTest from port 8081 to 9999
- **Test Isolation**: Proper mocking strategies for Spring Security filter chain
- **JAR Packaging**: Successful Maven build with 76MB executable JAR

### Changed
- **Framework Upgrade**: Spring Boot 3.4.4 → 3.5.6 with full compatibility verification
- **Test Coverage**: Enhanced from basic to comprehensive 31-test suite
- **Security Implementation**: Custom authentication filter with proper test isolation
- **Documentation**: Complete rewrite with current architecture and deployment guides
- **Build Process**: Improved Maven configuration with dependency management

### Security
- **Authentication Filter Testing**: Complete validation of RSA token processing
- **Input Validation**: Bean Validation with custom UUID and crypt patterns
- **Security Headers**: Production-ready security configuration
- **Apache Security**: mod_security rules and rate limiting implementation
- **SSL/TLS**: Modern cipher suites and certificate management

## [4.1.0] - 2023/xx/xx
### Fixed
- Fix many CVE
### Added
### Changed
- Removed Vaadin gui
- Removed Spring security
- Switch from spring boot 2.7 to 3.3

## [4.0.12] - 2023/xx/xx
### Fixed
- Fix common base in pom
- Fix UTC date time
- Fix Import optimisation 
### Added
### Changed

## [4.0.11] - 2023/05/27
### Fixed
- Memory optimization
- Fix ResponseEntityUtils
- Fix safety issue on UserRest
### Added
 - Ad debug db H2
### Changed
- Update to Vaadin 14.10.0
- Update to Spring Boot 2.7.12
- From time Rome to UTC

## [4.0.10] - 2022/04/02
### Fixed
### Added
### Changed
- Update to Vaadin 14.8.6
- Update to Spring Boot 2.6.6

## [4.0.9] - 2021/12/14
### Fixed
### Added
### Changed
- Update to java 17
- Update to Vaadin 14.8.0
- Update to Spring Boot 2.4.5

## [4.0.8] - 2021/09/09
### Fixed
- Bump vaadin.version from 14.4.6 to 1.4.8 dependencies
- Logo on admin area
### Added
- kubernetes yaml
### Changed

## [4.0.5] - 2021/01/17
### Fixed
### Added
- Add ResponseEntityUtils to help rest return response 
### Changed
- Update vaadin to 14.4.6

## [4.0.4] - 2021/01/10
### Fixed
 - Fix rest return Set to List for big import data
 - Fix added ResponseEntity to all responses
### Added
 - Add title to reserved area
 - Add Dockerfile
### Changed
 - Update vaadin to 14.4.5
 - Update spring boot to 2.3.2 
 - Update commons to 4.0.7
 - Update banner.txt

## [4.0.3] - 2020/08/06
### Fixed
 - Fix remove password override to user
### Added
### Changed
 - Update vaadin to 14.3.9
 - Update spring boot to 2.3.2 

## [4.0.2] - 2020/08/05
### Fixed
 - Fix wrong remote address on login
### Added
### Changed

## [4.0.1] - 2020/05/11
### Fixed
 - Fix wrong remote address on login
### Added
### Changed

## [4.0.0] - 2020/05/07
### Fixed
### Added
### Changed
 - First released

