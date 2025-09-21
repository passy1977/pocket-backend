# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [5.0.0] - 2024/12/xx
### Added
- **Complete Test Suite**: Unit tests, integration tests, and security tests
  - `SessionRestTest.java` - Unit tests for REST controller with Mockito
  - `SessionRestIntegrationTest.java` - Full Spring context integration tests
  - `AuthenticationFilterTest.java` - Security filter testing
  - Test configuration with H2 in-memory database
- **Mock API Client**: Full-featured HTTP client for testing and integration
  - `PocketApiClient.java` - Async/sync API client with load testing support
  - `PocketApiClientTest.java` - Comprehensive client tests
  - `PocketApiClientExample.java` - Usage examples and documentation
- **Apache HTTP Server Configuration**: Production-ready reverse proxy setup
  - `apache/httpd.conf` - Main Apache configuration with SSL and load balancing
  - `apache/vhosts.conf` - Virtual host configurations for dev/prod environments
  - SSL termination, security headers, mod_security rules
  - Health checks, failover, and session affinity
- **Enhanced Documentation**:
  - `docs/APACHE_SETUP.md` - Comprehensive Apache setup guide
  - Updated README.md with Apache configuration instructions
  - Testing documentation and troubleshooting guides

### Fixed
- Fixed Maven dependency issue with spring-boot-admin version
- Corrected test dependencies and configuration
- Fixed Bean Validation test method signatures

### Changed
- Updated Spring Boot Admin to version 3.4.2 (compatible with Spring Boot 3.4.4)
- Enhanced security configuration documentation
- Improved Docker deployment instructions
- Added comprehensive monitoring and health check procedures

### Security
- Added comprehensive security testing framework
- Enhanced authentication filter validation
- Implemented production-ready Apache security configuration
- Added rate limiting and DDoS protection guidelines

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

