# 🔐 Pocket Backend

[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-green.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![Security](https://img.shields.io/badge/Security-Spring%20Security-red.svg)](https://spring.io/projects/spring-security)
[![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/passy1977/pocket-lib)

Secure and scalable backend for the Pocket application, built with Spring Boot 3.4.4 and Java 21. Provides robust REST APIs for session management, user authentication, and secure data storage with end-to-end encryption.

🇬🇧 **English** | [🇮🇹 Italiano](README-IT.md)

---

## ✨ Key Features

- 🔐 **Enterprise Security** with Spring Security and custom authentication
- 🏗️ **Modern Architecture** using Spring Boot 3.4.4 and Java 21
- � **Robust Encryption** RSA + AES-CBC for data protection
- � **Full Containerization** with Docker and Docker Compose
- 📊 **Integrated Monitoring** with Spring Boot Actuator
- 🌐 **Dynamic CORS** configurable for multiple environments
- ✅ **Complete Validation** with Bean Validation
- � **Automatic Health Checks** and recovery

## 📋 Requirements

### Development Environment
- **Java**: 21+ (LTS recommended)
- **Maven**: 3.8+ for dependency management and build
- **Database**: MySQL 8.0+ or MariaDB 10.6+
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code with Java extensions

### Production Environment
- **Docker**: 24.0+ with Docker Compose v2
- **Memory**: Minimum 2GB RAM (4GB recommended)
- **Storage**: 10GB+ for application and database
- **Network**: Ports 8081 (API), 3306 (DB), 80/443 (HTTP/HTTPS)

### Optional Tools
- **Nginx**: For reverse proxy and SSL termination
- **Monitoring**: Prometheus + Grafana for metrics
- **Backup**: Automated database backup solutions

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/passy1977/pocket-backend.git
cd pocket-backend
```

### 2. Environment Configuration

#### Development Environment
Create a `.env` file or export environment variables:
```bash
# Database Configuration
export DB_USERNAME="pocket_user"
export DB_PASSWORD="your_secure_database_password"

# Security Configuration
export AES_CBC_IV="your_16_char_iv_!"  # Must be exactly 16 characters
export ADMIN_USER="admin"
export ADMIN_PASSWD="your_secure_admin_password"

# CORS Configuration (optional)
export CORS_ADDITIONAL_ORIGINS="https://yourdomain.com,https://app.yourdomain.com"
```

#### Production Environment
```bash
# Database Configuration
export DB_USERNAME="pocket_prod_user"
export DB_PASSWORD="very_secure_production_password"

# Security Configuration  
export AES_CBC_IV="prod_16_char_iv_!"  # Must be exactly 16 characters
export ADMIN_USER="admin"
export ADMIN_PASSWD="very_secure_admin_password"

# Server Configuration
export SERVER_URL="https://api.yourdomain.com:8081"
export CORS_ADDITIONAL_ORIGINS="https://yourdomain.com,https://app.yourdomain.com"

# SSL Configuration (for production)
export SSL_KEYSTORE_PASSWORD="your_keystore_password"
```

### 3. Database Setup

#### Using Docker (Recommended for Development)
```bash
# Start MariaDB container
docker run --detach --name pocket-db \
  -p 3306:3306 \
  --env MARIADB_ROOT_PASSWORD=your_secure_database_password \
  --env MARIADB_DATABASE=pocket5 \
  --env MARIADB_USER=pocket_user \
  --env MARIADB_PASSWORD=your_secure_database_password \
  mariadb:latest

# Initialize database schema
docker exec -i pocket-db mariadb -u root -pyour_secure_database_password pocket5 < scripts/pocket5.sql
```

#### Manual Database Setup
```bash
# Connect to MariaDB
mysql -u root -p

# Create database and user
CREATE DATABASE pocket5;
CREATE USER 'pocket_user'@'%' IDENTIFIED BY 'your_secure_database_password';
GRANT ALL PRIVILEGES ON pocket5.* TO 'pocket_user'@'%';
FLUSH PRIVILEGES;

# Import schema
mysql -u pocket_user -p pocket5 < scripts/pocket5.sql
```

### 4. Application Configuration

Update `src/main/resources/application.yaml` for your environment:

#### Development Configuration
```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/pocket5?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&autoReconnect=true&sslMode=DISABLED
    username: ${DB_USERNAME:pocket_user}
    password: ${DB_PASSWORD}

server:
  port: 8081
  url: http://localhost:8081
  aes.cbc.iv: ${AES_CBC_IV}
  auth:
    user: ${ADMIN_USER:admin}
    passwd: ${ADMIN_PASSWD}

logging:
  level:
    it.salsi.pocket: DEBUG
    org.springframework.security: DEBUG

security:
  cors:
    additional-origins: ${CORS_ADDITIONAL_ORIGINS:}
```

#### Production Configuration
```yaml
spring:
  datasource:
    url: jdbc:mariadb://your-db-host:3306/pocket5?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&autoReconnect=true&sslMode=REQUIRED
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

server:
  port: 8081
  url: ${SERVER_URL:https://api.yourdomain.com:8081}
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
  aes.cbc.iv: ${AES_CBC_IV}
  auth:
    user: ${ADMIN_USER}
    passwd: ${ADMIN_PASSWD}
  servlet:
    session:
      cookie:
        secure: true
        http-only: true
        same-site: strict

logging:
  level:
    it.salsi.pocket: INFO
    org.springframework.security: WARN

security:
  cors:
    additional-origins: ${CORS_ADDITIONAL_ORIGINS}
```

### 5. Build and Run

```bash
# Build the application
mvn clean install

# Run in development mode
mvn spring-boot:run

# Or run the JAR file
java -jar target/pocket-backend-5.0.0.jar
```

## 🐳 Docker Deployment

### Quick Docker Setup (Recommended)

The easiest way to deploy Pocket Backend is using the automated Docker setup script:

```bash
# Clone and setup
git clone https://github.com/passy1977/pocket-backend.git
cd pocket-backend

# Run automated setup (handles everything)
./build_docker_image.sh
```

The script will:
- ✅ Generate secure passwords automatically
- ✅ Configure all security settings
- ✅ Build Docker images
- ✅ Start all services
- ✅ Install CLI management tools
- ✅ Perform health checks

### Manual Docker Setup

#### 1. Environment Configuration
```bash
# Copy environment template
cp .env.example .env

# Edit .env with your secure values
nano .env
```

#### 2. Required Environment Variables
```bash
# Database Configuration
DB_ROOT_PASSWORD=your_secure_root_password
DB_USERNAME=pocket_user
DB_PASSWORD=your_secure_user_password

# Security Configuration (CRITICAL - Change in production)
AES_CBC_IV=your_16_char_iv_!  # Exactly 16 characters
ADMIN_USER=admin
ADMIN_PASSWD=your_secure_admin_password

# Server Configuration
SERVER_URL=http://localhost:8081
SERVER_PORT=8081
CORS_ADDITIONAL_ORIGINS=https://yourdomain.com
```

#### 3. Build and Deploy
```bash
# Build the application
docker build -t pocket-backend:5.0.0 .

# Start all services
docker compose up -d

# Check service health
docker compose ps
docker compose logs -f
```

### Production Docker Setup

For production deployment with SSL and reverse proxy:

```bash
# Start with production profile (includes reverse proxy)
docker compose --profile production up -d
```

## 🌐 Apache HTTP Server Configuration

The project includes comprehensive Apache HTTP server configuration for production deployment with reverse proxy, SSL termination, and load balancing.

### Configuration Files

- `apache/httpd.conf` - Main Apache configuration with SSL, security, and load balancing
- `apache/vhosts.conf` - Virtual host configurations for development and production

### Features

#### Security & SSL
- **TLS 1.2+** with secure cipher suites
- **SSL termination** for backend services
- **Security headers** (HSTS, CSP, X-Frame-Options, etc.)
- **Rate limiting** and request throttling
- **mod_security** rules for web application firewall

#### Load Balancing & High Availability
- **Health checks** for backend services
- **Failover** to secondary backend instances
- **Session affinity** with cookie-based routing
- **Graceful service degradation**

#### Performance Optimization
- **Compression** (mod_deflate) for responses
- **Caching** headers for static content
- **Keep-alive** connections
- **Connection pooling** to backend services

### Quick Setup

#### 1. Install Apache HTTP Server
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install apache2

# CentOS/RHEL
sudo yum install httpd

# macOS (using Homebrew)
brew install httpd
```

#### 2. Enable Required Modules
```bash
# Ubuntu/Debian
sudo a2enmod ssl rewrite proxy proxy_http proxy_balancer lbmethod_byrequests headers security2 deflate expires

# CentOS/RHEL
sudo systemctl enable httpd
```

#### 3. Copy Configuration Files
```bash
# Copy main configuration
sudo cp apache/httpd.conf /etc/apache2/sites-available/pocket-backend.conf

# Copy virtual hosts
sudo cp apache/vhosts.conf /etc/apache2/sites-available/pocket-vhosts.conf

# Enable sites (Ubuntu/Debian)
sudo a2ensite pocket-backend
sudo a2ensite pocket-vhosts
```

#### 4. SSL Certificate Setup
```bash
# Create SSL directory
sudo mkdir -p /etc/ssl/certs /etc/ssl/private

# For Let's Encrypt certificates
sudo certbot --apache -d api.yourdomain.com

# For custom certificates, place them in:
# /etc/ssl/certs/api.yourdomain.com.crt
# /etc/ssl/private/api.yourdomain.com.key
# /etc/ssl/certs/api.yourdomain.com-chain.crt
```

#### 5. Configure Backend Services
Update virtual host configuration to match your backend setup:

```apache
# Development setup (single backend)
ProxyPass /api/ http://127.0.0.1:8081/api/
ProxyPassReverse /api/ http://127.0.0.1:8081/api/

# Production setup (load balanced)
<Proxy balancer://pocket-backend>
    BalancerMember http://127.0.0.1:8081 route=backend1
    BalancerMember http://127.0.0.1:8082 route=backend2
    ProxySet lbmethod byrequests
    ProxySet hcmethod GET
    ProxySet hcuri /actuator/health
</Proxy>

ProxyPass /api/ balancer://pocket-backend/api/
ProxyPassReverse /api/ balancer://pocket-backend/api/
```

#### 6. Start Apache
```bash
# Test configuration
sudo apache2ctl configtest

# Start Apache
sudo systemctl start apache2
sudo systemctl enable apache2

# Reload configuration (after changes)
sudo systemctl reload apache2
```

### Configuration Customization

#### Domain Configuration
Update `apache/vhosts.conf` with your domain:
```apache
ServerName api.yourdomain.com
ServerAlias pocket-api.yourdomain.com
```

#### CORS Configuration
Adjust CORS settings for your frontend domains:
```apache
SetEnvIf Origin "^https?://(www\.)?yourdomain\.com$" AccessControlAllowOrigin=$0
Header always set Access-Control-Allow-Origin %{AccessControlAllowOrigin}e env=AccessControlAllowOrigin
```

#### Backend Configuration
Update backend endpoints and health checks:
```apache
# Backend cluster
<Proxy balancer://pocket-backend>
    BalancerMember http://backend1.internal:8081 route=backend1
    BalancerMember http://backend2.internal:8081 route=backend2
    ProxySet hcuri /actuator/health
</Proxy>
```

### Monitoring and Management

#### Load Balancer Status
Access the balancer manager at:
```
https://api.yourdomain.com/balancer-manager
```

#### Log Files
- **Access Log**: `/var/log/apache2/pocket-prod_access.log`
- **Error Log**: `/var/log/apache2/pocket-prod_error.log`
- **SSL Log**: `/var/log/apache2/ssl_access.log`

#### Health Monitoring
```bash
# Check service status
systemctl status apache2

# Monitor logs in real-time
tail -f /var/log/apache2/pocket-prod_access.log

# Test SSL configuration
openssl s_client -connect api.yourdomain.com:443

# Test load balancer
curl -H "Host: api.yourdomain.com" https://localhost/api/v5/health
```

### Security Best Practices

1. **Regular Updates**: Keep Apache and modules updated
2. **Log Monitoring**: Monitor access and error logs for suspicious activity
3. **Rate Limiting**: Adjust rate limits based on your traffic patterns
4. **SSL Configuration**: Use modern TLS versions and secure cipher suites
5. **Firewall Rules**: Restrict access to management endpoints

### Troubleshooting

#### SSL Issues
```bash
# Test SSL certificate
openssl x509 -in /etc/ssl/certs/api.yourdomain.com.crt -text -noout

# Check certificate chain
openssl verify -CAfile /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/api.yourdomain.com.crt
```

#### Proxy Issues
```bash
# Test backend connectivity
curl -f http://127.0.0.1:8081/actuator/health

# Check proxy configuration
apache2ctl -S

# Enable detailed proxy logging
LogLevel proxy:debug
```

#### Performance Issues
```bash
# Monitor connections
netstat -an | grep :80 | wc -l
netstat -an | grep :443 | wc -l

# Check Apache processes
ps aux | grep apache2

# Monitor resource usage
top -p $(pgrep apache2 | tr '\n' ',' | sed 's/,$//')
```

### Docker Services

| Service | Port | Description | Health Check |
|---------|------|-------------|--------------|
| **pocket-backend** | 8081 | Main application | `/actuator/health` |
| **pocket-db** | 3306 | MariaDB database | `mysqladmin ping` |
| **nginx** | 80/443 | Reverse proxy | `/health` |

### Docker Volumes

| Volume | Purpose | Backup Required |
|--------|---------|-----------------|
| `pocket_db_data` | Database storage | ✅ Yes |
| `pocket_logs` | Application logs | No |
| `nginx_logs` | Nginx logs | No |

### Health Monitoring

```bash
# Check all services
docker compose ps

# View logs
docker compose logs -f pocket-backend
docker compose logs -f pocket-db

# Check application health
curl http://localhost:8081/actuator/health

# Check database health
docker compose exec pocket-db mysqladmin ping -h localhost
```

### 🔧 Container Management Commands

```bash
# User Management (automatically installed)
pocket-user add -e user@example.com -p user_password -n "User Name"
pocket-user mod -e user@example.com -p new_password -n "New Name"
pocket-user rm -e user@example.com
pocket-user get -e user@example.com

# Device Management (automatically installed)
pocket-device add -e user@example.com -d "Device Name"
pocket-device list -e user@example.com
pocket-device rm -e user@example.com -u device_uuid
pocket-device qr -e user@example.com -u device_uuid

# Docker Operations
docker compose logs -f              # View all logs
docker compose logs -f pocket-backend # View app logs only
docker compose restart             # Restart all services
docker compose restart pocket-backend # Restart app only
docker compose down                # Stop all services
docker compose up -d               # Start all services
docker compose ps                  # Check service status
```

### 🔄 Backup and Maintenance

```bash
# Database Backup
docker compose exec pocket-db mysqldump -u root -p pocket5 > backup-$(date +%Y%m%d).sql

# Volume Backup
docker run --rm \
  -v pocket_db_data:/data \
  -v $(pwd):/backup \
  ubuntu tar czf /backup/db_backup-$(date +%Y%m%d).tar.gz /data

# Application Update
git pull
docker compose build pocket-backend
docker compose up -d pocket-backend

# Resource Monitoring
docker compose top                  # Process list
docker stats                       # Resource usage
docker system df                   # Disk usage
```

### 🚨 Troubleshooting Docker

```bash
# Check container logs
docker compose logs pocket-backend --tail=100

# Enter container for debugging
docker compose exec pocket-backend /bin/bash

# Reset and rebuild
docker compose down -v             # Remove volumes
docker compose build --no-cache    # Rebuild images
docker compose up -d               # Start fresh

# Check network connectivity
docker compose exec pocket-backend curl -f http://pocket-db:3306
docker compose exec pocket-backend ping pocket-db
```

## 🔐 Security Configuration

### 🛡️ Implemented Security Features

**Spring Security Integration**:
- ✅ **Custom Authentication Filter**: RSA-based authentication for API endpoints
- ✅ **HTTP Basic Authentication**: For admin/actuator endpoints
- ✅ **Input Validation**: Regex patterns for UUID and crypt parameters
- ✅ **CORS Configuration**: Dynamic based on server configuration
- ✅ **Security Headers**: HSTS, XSS Protection, Frame Options, CSP
- ✅ **Session Management**: Stateless for API, secure cookies for admin
- ✅ **Error Handling**: Secure error responses without information disclosure

### Authentication Endpoints

| Endpoint Type | Path | Authentication | Authorization | Description |
|---------------|------|----------------|---------------|-------------|
| **Public** | `/actuator/health` | None | Public | Health check |
| **Public** | `/actuator/info` | None | Public | Application info |
| **Admin** | `/actuator/**` | HTTP Basic | ADMIN role | Admin endpoints |
| **API** | `/api/v5/**` | Custom RSA + UUID | USER role | Application API |

### Security Headers

Automatically configured security headers:
- **HSTS**: `max-age=31536000; includeSubDomains`
- **X-Frame-Options**: `DENY`
- **X-Content-Type-Options**: `nosniff`
- **X-XSS-Protection**: `1; mode=block`
- **Referrer-Policy**: `strict-origin-when-cross-origin`
- **Content-Security-Policy**: Restrictive policy

### CORS Configuration

Dynamic CORS configuration based on:
1. **Server URL**: `server.url` property (HTTP + HTTPS variants)
2. **Development**: `localhost:*` (both HTTP and HTTPS)
3. **Additional Origins**: `security.cors.additional-origins` property
4. **Environment Variable**: `CORS_ADDITIONAL_ORIGINS`

Example configuration:
```yaml
server:
  url: https://api.yourdomain.com:8081

security:
  cors:
    additional-origins: https://app.yourdomain.com,https://admin.yourdomain.com
```

### Input Validation

**UUID Validation**:
- Pattern: `^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$`
- Ensures proper UUID v4 format

**Crypt Parameter Validation**:
- Pattern: `^[A-Za-z0-9_-]{10,2048}(=*)$`
- Length: 10-2048 characters
- Characters: Base64 URL-safe only

### Rate Limiting (with Nginx)

When using the Nginx reverse proxy:
- **API endpoints**: 10 requests/second, burst 20
- **Admin endpoints**: 5 requests/second, burst 5
- **Configurable per endpoint**

## 👥 User and Device Management

Pocket Backend provides multiple ways to manage users and devices: CLI tools, Socket API, and direct database access.

### 🔌 Socket Management Service

The backend includes a built-in socket service for real-time user and device management. This service runs on port **8300** (configurable) and provides administrative commands for system configuration.

#### Socket Connection
```bash
# Connect to management socket
telnet localhost 8300

# Or using netcat
nc localhost 8300
```

#### Authentication (REQUIRED)
**⚠️ CRITICAL**: Before executing ANY command, authentication is mandatory using `server.auth.passwd` value:

```bash
# 1. Connect to socket
nc localhost 8300

# 2. FIRST: Send authentication password (must be exactly 32 characters)
your_32_character_admin_password
# Response: 0 (OK) or 7 (WRONG_PASSWD)

# 3. ONLY AFTER successful authentication, you can execute commands
```

**Configuration Reference**:
```yaml
server:
  auth:
    passwd: ${ADMIN_PASSWD:____admin_password_change_me____}  # MUST be 32 characters
```

#### Socket Commands

**⚠️ All commands require prior authentication with `server.auth.passwd`**

**User Management Commands:**
```bash
# Add new user (AFTER authentication)
ADD_USER|user@example.com|password|User Name

# Modify existing user (AFTER authentication)
MOD_USER|user@example.com|new_password|New User Name

# Remove user (AFTER authentication)
RM_USER|user@example.com

# Get user information (AFTER authentication)
GET_USER|user@example.com
```

**Device Management Commands:**
```bash
# Add new device for user (AFTER authentication)
ADD_DEVICE|user@example.com|device_note

# Modify device note (AFTER authentication)
MOD_DEVICE|user@example.com|device_uuid|new_note

# Remove device (AFTER authentication)
RM_DEVICE|user@example.com|device_uuid

# Get device information and keys (AFTER authentication)
GET_DEVICE|user@example.com|device_uuid

# Remove device
RM_DEVICE|user@example.com|device_uuid

# Get device information and keys
GET_DEVICE|user@example.com|device_uuid
```

#### Response Codes
| Code | Status | Description |
|------|--------|-------------|
| `0` | OK | Command executed successfully |
| `1` | ERROR | Generic error occurred |
| `2` | WRONG_PARAMS | Invalid parameters provided |
| `3` | USER_ALREADY_EXIST | User already exists |
| `4` | DEVICE_ALREADY_EXIST | Device already exists |
| `5` | USER_NOT_EXIST | User not found |
| `6` | DEVICE_NOT_EXIST | Device not found |
| `7` | WRONG_PASSWD | Authentication failed |

#### Socket Configuration
```yaml
server:
  socket-port: 8300                    # Socket service port
  auth:
    passwd: ${ADMIN_PASSWD:your_32_char_password}  # Authentication password (EXACTLY 32 chars)
```

**⚠️ Security Requirements**:
- Password MUST be exactly 32 characters long
- Authentication is required for EVERY socket connection
- Failed authentication results in connection termination
- No commands are executed without valid authentication

#### Example Socket Session
```bash
$ nc localhost 8300
# STEP 1: Authenticate first (password must be 32 characters)
your_32_character_admin_password_here
0                                      # Success response

# STEP 2: Now you can execute commands
ADD_USER|john@example.com|securepass123|John Doe
{"id":1,"name":"John Doe","email":"john@example.com","passwd":"$2a$10$..."}
0

# STEP 3: Add device for the user
ADD_DEVICE|john@example.com|iPhone 15
{"id":"a1b2c3d4-e5f6-7890-abcd-ef1234567890",...,"hostPublicKey":"-----BEGIN PUBLIC KEY-----\n..."}
0

# Authentication failure example:
wrong_password
7                                      # WRONG_PASSWD response - connection may be terminated
```

### 🛠️ CLI Tools Management

For easier management, use the CLI tools from [pocket-cli](https://github.com/passy1977/pocket-cli):

#### User Management
```bash
# Add new user
pocket-user add -e user@example.com -p user_password -n "User Name"

# Modify user
pocket-user mod -e user@example.com -p new_password -n "New Name"

# Remove user
pocket-user rm -e user@example.com

# Get user information
pocket-user get -e user@example.com
```

#### Device Management
```bash
# Add new device
pocket-device add -e user@example.com -d "Device Name"

# List user devices
pocket-device list -e user@example.com

# Remove device
pocket-device rm -e user@example.com -u device_uuid

# Get device QR code for client setup
pocket-device qr -e user@example.com -u device_uuid
```

## 📡 API Usage Examples

### Authentication
All API calls require RSA-encrypted authentication tokens in the URL path:

```
GET /api/v5/{uuid}/{encrypted_token}
POST /api/v5/{uuid}/{encrypted_token}
PUT /api/v5/{uuid}/{encrypted_token}/{additional_params}
DELETE /api/v5/{uuid}/{encrypted_token}
```

### Example API Calls

```bash
# Get data (replace with actual UUID and encrypted token)
curl -X GET "https://api.yourdomain.com:8081/api/v5/12345678-1234-1234-1234-123456789abc/encrypted_auth_token"

# Post data
curl -X POST "https://api.yourdomain.com:8081/api/v5/12345678-1234-1234-1234-123456789abc/encrypted_auth_token" \
  -H "Content-Type: application/json" \
  -d '{"groups":[],"groupFields":[],"fields":[]}'

# Check session
curl -X GET "https://api.yourdomain.com:8081/api/v5/12345678-1234-1234-1234-123456789abc/encrypted_auth_token/check"
```

## 🔍 Monitoring and Health Checks

### Health Endpoints
```bash
# Application health
curl http://localhost:8081/actuator/health

# Application info
curl http://localhost:8081/actuator/info

# Metrics (requires admin authentication)
curl -u admin:your_admin_password http://localhost:8081/actuator/metrics
```

### Logging
Application logs include:
- Authentication attempts and failures
- Input validation errors
- Security events
- Database operations
- CORS requests

## 🚨 Security Best Practices

### Before Production Deployment

1. **Change All Default Passwords**
   ```bash
   # Never use default values in production
   export AES_CBC_IV="your_unique_16_char_iv"
   export ADMIN_PASSWD="very_secure_admin_password"
   export DB_PASSWORD="very_secure_database_password"
   ```

2. **Enable HTTPS**
   - Configure SSL certificates
   - Set `server.ssl.enabled=true`
   - Use secure cookie settings

3. **Database Security**
   - Use dedicated database user with minimal privileges
   - Enable SSL for database connections
   - Regular password rotation

4. **Network Security**
   - Use firewalls to restrict access
   - VPN for admin access
   - Regular security updates

5. **Monitoring**
   - Set up log aggregation
   - Monitor failed authentication attempts
   - Alert on security events

## 🐛 Troubleshooting

## 🐛 Troubleshooting

### Common Issues

#### 🔐 Authentication Failures
```bash
# Check authentication logs
docker compose logs pocket-backend | grep -i auth

# Verify environment variables
docker compose exec pocket-backend env | grep -E "(AES_CBC_IV|ADMIN_USER|DB_)"

# Test admin authentication
curl -u admin:your_admin_password http://localhost:8081/actuator/health

# Check security filter logs
docker compose logs pocket-backend | grep "AuthenticationFilter"
```

#### 🗄️ Database Connection Issues
```bash
# Test database connectivity from app container
docker compose exec pocket-backend nc -zv pocket-db 3306

# Check database logs
docker compose logs pocket-db

# Test direct database connection
docker compose exec pocket-db mysql -u root -p -e "SHOW DATABASES;"

# Verify database initialization
docker compose exec pocket-db mysql -u root -p pocket5 -e "SHOW TABLES;"
```

#### 🌐 CORS Issues
```bash
# Test CORS preflight request
curl -H "Origin: https://yourdomain.com" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: X-Requested-With" \
     -X OPTIONS \
     http://localhost:8081/api/v5/test

# Check CORS configuration logs
docker compose logs pocket-backend | grep -i cors

# Verify server URL configuration
docker compose exec pocket-backend env | grep SERVER_URL
```

#### 🔧 Configuration Issues
```bash
# Validate Spring configuration
docker compose exec pocket-backend java -jar /var/www/pocket.jar --spring.config.location=classpath:application.yaml --debug

# Check application properties
docker compose exec pocket-backend cat /var/www/scripts/pocket5-config.yaml

# Verify volume mounts
docker compose config
```

#### 🚫 SSL/TLS Issues (Production)
```bash
# Test SSL certificate
openssl x509 -in nginx/ssl/cert.pem -text -noout

# Verify SSL configuration
docker compose exec nginx nginx -t

# Check SSL logs
docker compose logs nginx | grep -i ssl
```

### Performance Issues

#### 📊 Memory and CPU
```bash
# Check container resource usage
docker stats

# Adjust JVM memory settings in .env
JVM_MAX_MEMORY=1g
JVM_MIN_MEMORY=512m

# Check Java heap usage
docker compose exec pocket-backend jcmd 1 VM.flags
```

#### 🔍 Database Performance
```bash
# Check database connections
docker compose exec pocket-db mysql -u root -p -e "SHOW PROCESSLIST;"

# Monitor database queries
docker compose logs pocket-db --tail=100

# Check database size
docker compose exec pocket-db mysql -u root -p -e "SELECT table_schema AS 'Database', ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'DB Size in MB' FROM information_schema.tables GROUP BY table_schema;"
```

### Configuration Validation

#### 🔐 Security Checklist
```bash
# Verify all passwords are changed from defaults
grep -r "change_me\|default\|admin123" .env

# Check AES IV length (must be exactly 16 characters)
echo ${AES_CBC_IV} | wc -c  # Should output 17 (16 + newline)

# Verify UUID format validation
curl -X GET "http://localhost:8081/api/v5/invalid-uuid/test"  # Should return 400

# Test admin endpoint security
curl http://localhost:8081/actuator/metrics  # Should return 401
```

#### 🌐 Network Connectivity
```bash
# Test internal Docker network
docker compose exec pocket-backend ping pocket-db
docker compose exec pocket-backend nslookup pocket-db

# Check port accessibility
telnet localhost 8081
telnet localhost 3306

# Verify Docker network
docker network ls
docker network inspect pocket-network
```

### Log Analysis

#### 📝 Application Logs
```bash
# View structured logs
docker compose logs pocket-backend --tail=100 -f

# Filter by log level
docker compose logs pocket-backend | grep -E "(ERROR|WARN)"

# Security-related logs
docker compose logs pocket-backend | grep -E "(AUTH|SECURITY|CORS)"

# Performance logs
docker compose logs pocket-backend | grep -E "(SLOW|TIMEOUT|PERFORMANCE)"
```

#### 🔍 Database Logs
```bash
# MySQL error logs
docker compose logs pocket-db | grep -i error

# Connection logs
docker compose logs pocket-db | grep -i connect

# Slow query logs (if enabled)
docker compose exec pocket-db mysql -u root -p -e "SET GLOBAL slow_query_log = 'ON';"
```

### Recovery Procedures

#### 🔄 Service Recovery
```bash
# Restart specific service
docker compose restart pocket-backend
docker compose restart pocket-db

# Full system restart
docker compose down
docker compose up -d

# Reset with fresh database
docker compose down -v  # WARNING: Deletes all data
docker compose up -d
```

#### 💾 Data Recovery
```bash
# Restore database from backup
docker compose exec -i pocket-db mysql -u root -p pocket5 < backup.sql

# Restore volume from backup
docker run --rm -v pocket_db_data:/data -v $(pwd):/backup ubuntu tar xzf /backup/db_backup.tar.gz -C /
```

## 📊 Monitoring & Health Checks

### Application Health
```bash
# Basic health check
curl http://localhost:8081/actuator/health

# Detailed health with authentication
curl -u admin:your_admin_password http://localhost:8081/actuator/health/details

# Application metrics
curl -u admin:your_admin_password http://localhost:8081/actuator/metrics

# Environment information
curl -u admin:your_admin_password http://localhost:8081/actuator/env
```

### Database Health
```bash
# Check database status
docker compose exec pocket-db mysqladmin -u root -p status

# Database connections
docker compose exec pocket-db mysql -u root -p -e "SHOW STATUS LIKE 'Connections'"

# Database size monitoring
docker compose exec pocket-db mysql -u root -p -e "
SELECT 
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables 
WHERE table_schema = 'pocket5'
GROUP BY table_schema;"
```

### Container Health
```bash
# Container status and resource usage
docker compose ps
docker stats

# Check container logs
docker compose logs --tail=50 -f

# Restart unhealthy containers
docker compose restart $(docker compose ps -q --filter "health=unhealthy")
```

### Performance Monitoring
```bash
# JVM memory usage
docker compose exec pocket-backend jcmd 1 GC.run_finalization
docker compose exec pocket-backend jcmd 1 VM.memory_summary

# Application response times
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8081/api/v5/health

# Database performance
docker compose exec pocket-db mysql -u root -p -e "SHOW GLOBAL STATUS LIKE 'Slow_queries'"
```

## 🛡️ Security Features

### Authentication & Authorization
- **Custom Authentication Filter**: RSA-based token authentication
- **Role-Based Access Control**: Admin and user roles with different permissions  
- **Session Management**: Secure session handling with configurable timeouts
- **Input Validation**: Comprehensive Bean Validation for all API endpoints

### Data Protection
- **Encryption**: AES-CBC encryption for sensitive data
- **Password Security**: BCrypt hashing with configurable strength
- **Database Security**: Prepared statements to prevent SQL injection
- **CORS Protection**: Dynamic CORS configuration based on server URL

### Infrastructure Security
- **Container Security**: Non-root container execution, minimal Alpine base images
- **Network Security**: Docker network isolation, configurable port exposure
- **SSL/TLS**: HTTPS support with reverse proxy configuration
- **Environment Variables**: Sensitive configuration externalized from code

### Security Headers
- **X-Content-Type-Options**: nosniff
- **X-Frame-Options**: DENY
- **X-XSS-Protection**: 1; mode=block
- **Cache-Control**: no-cache, no-store, must-revalidate
- **Strict-Transport-Security**: max-age=31536000; includeSubDomains

### Audit & Logging
- **Security Events**: Authentication attempts, authorization failures
- **Access Logging**: All API access with user identification
- **Error Tracking**: Detailed error logs without sensitive data exposure
- **Performance Monitoring**: Response time and resource usage tracking

## 🚀 API Endpoints

### Authentication
- `POST /api/v5/login` - User authentication
- `POST /api/v5/logout` - User logout

### Session Management
- `GET /api/v5/session/{uuid}/{crypt}` - Get session data
- `POST /api/v5/session` - Create new session
- `PUT /api/v5/session/{uuid}` - Update session
- `DELETE /api/v5/session/{uuid}` - Delete session

### Health Monitoring
- `GET /actuator/health` - Application health status
- `GET /actuator/info` - Application information
- `GET /actuator/metrics` - Application metrics (requires authentication)

## 📚 Additional Resources

- [Security Documentation](SECURITY.md) - Detailed security implementation
- [API Reference](https://deepwiki.com/passy1977/pocket-lib) - Complete API documentation
- [Client Applications](https://github.com/passy1977/pocket-ios) - iOS client implementation
- [CLI Tools](https://github.com/passy1977/pocket-cli) - User and device management tools

## 📄 License

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License.

---

**Made with ❤️ in Italy** | 🇬🇧 **English** | [🇮🇹 Italiano](README-IT.md)

**⚠️ Important**: Always change default passwords and configuration values before deploying to production!
