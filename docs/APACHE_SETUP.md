# 🌐 Apache HTTP Server Configuration for Pocket Backend

[![Apache](https://img.shields.io/badge/Apache-2.4+-orange.svg)](https://httpd.apache.org/)
[![SSL](https://img.shields.io/badge/SSL-TLS%201.2%2B-green.svg)](#ssl-configuration)
[![Security](https://img.shields.io/badge/Security-mod__security-red.svg)](#security-configuration)
[![Load Balancing](https://img.shields.io/badge/Load%20Balancing-Configured-blue.svg)](#load-balancing)

Production-ready Apache HTTP Server configuration for **Pocket Backend** with **Spring Boot 3.5.6**. This setup provides SSL termination, load balancing, security hardening, and performance optimization for enterprise deployment.

## 🎯 Features Overview

### 🔒 Security Features
- **SSL/TLS Termination** with modern cipher suites (TLS 1.2+)
- **mod_security** Web Application Firewall with OWASP rules
- **Security Headers** (HSTS, CSP, X-Frame-Options, etc.)
- **Rate Limiting** and DDoS protection
- **IP-based access control** for admin endpoints

### ⚡ Performance Features
- **Load Balancing** with health checks and failover
- **Compression** (gzip/deflate) for responses
- **Caching** headers for static content
- **Keep-alive** connections and connection pooling
- **Request optimization** and resource management

### 🔧 Operational Features
- **Health Monitoring** with automatic backend detection
- **Graceful Failover** for high availability
- **Session Affinity** for stateful applications
- **Detailed Logging** for monitoring and troubleshooting
- **Management Interface** for load balancer status

## 📋 Prerequisites

### System Requirements
- **Apache HTTP Server**: 2.4.10+ (2.4.50+ recommended)
- **Operating System**: Ubuntu 20.04+, CentOS 8+, or RHEL 8+
- **Memory**: Minimum 1GB RAM for Apache (2GB+ recommended)
- **CPU**: 2+ cores recommended for production

### SSL Requirements
- **SSL Certificates**: Let's Encrypt (recommended) or commercial certificates
- **TLS Support**: TLS 1.2+ (TLS 1.3 recommended)
- **Certificate Management**: Automated renewal with certbot

### Backend Requirements
- **Pocket Backend**: Running instance(s) with Spring Boot 3.5.6
- **Health Endpoint**: `/actuator/health` accessible from Apache server
- **Network**: Apache server can reach backend on configured ports

### Skills & Knowledge
- Basic Apache configuration and virtual hosts
- SSL certificate installation and management
- Network configuration and firewall rules
- Log analysis and troubleshooting

## 🔧 Apache Modules Installation

### Ubuntu/Debian (Recommended)
```bash
# Update package list
sudo apt update

# Install Apache if not already installed
sudo apt install apache2

# Enable required modules
sudo a2enmod ssl
sudo a2enmod rewrite
sudo a2enmod proxy
sudo a2enmod proxy_http
sudo a2enmod proxy_balancer
sudo a2enmod lbmethod_byrequests
sudo a2enmod headers
sudo a2enmod security2        # Optional: Web Application Firewall
sudo a2enmod deflate         # Compression
sudo a2enmod expires         # Cache control
sudo a2enmod status          # Server status
sudo a2enmod info            # Server info

# Restart Apache to load modules
sudo systemctl restart apache2
```

### CentOS/RHEL
```bash
# Install Apache
sudo yum install httpd mod_ssl

# Most modules are compiled in, verify:
httpd -M | grep -E "(ssl|rewrite|proxy|headers|deflate)"

# Enable and start Apache
sudo systemctl enable httpd
sudo systemctl start httpd
```

### Module Verification
```bash
# Test Apache configuration
sudo apache2ctl configtest

# Check loaded modules
apache2ctl -M | grep -E "(ssl|proxy|headers|deflate|security2)"

# Verify specific modules for Pocket Backend
apache2ctl -M | grep -E "(proxy_http|proxy_balancer|lbmethod_byrequests)"
```

## 📁 Configuration Files

### Main Configuration (`apache/httpd.conf`)
- SSL/TLS configuration with modern security
- Load balancing and health checks
- Security headers and mod_security rules
- Performance optimization settings
- Logging configuration

### Virtual Hosts (`apache/vhosts.conf`)
- Development virtual host (HTTP)
- Production virtual host (HTTPS)
- HTTP to HTTPS redirect
- CORS configuration

## 🚀 Installation Steps

### 1. Install Apache HTTP Server

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install apache2 apache2-utils

# Install mod_security (optional but recommended)
sudo apt install libapache2-mod-security2
```

#### CentOS/RHEL 8+
```bash
sudo dnf install httpd httpd-tools mod_ssl

# Enable and start Apache
sudo systemctl enable httpd
sudo systemctl start httpd
```

#### macOS (Homebrew)
```bash
brew install httpd
brew services start httpd
```

### 2. Copy Configuration Files
```bash
# Create configuration directory
sudo mkdir -p /etc/apache2/sites-available

# Copy configuration files
sudo cp apache/httpd.conf /etc/apache2/conf-available/pocket-backend.conf
sudo cp apache/vhosts.conf /etc/apache2/sites-available/pocket-backend.conf

# Enable configuration
sudo a2enconf pocket-backend
sudo a2ensite pocket-backend
```

### 3. SSL Certificate Setup

#### Option A: Let's Encrypt (Recommended)
```bash
# Install Certbot
sudo apt install certbot python3-certbot-apache  # Ubuntu/Debian
sudo dnf install certbot python3-certbot-apache  # CentOS/RHEL

# Obtain certificate
sudo certbot --apache -d api.yourdomain.com -d pocket-api.yourdomain.com

# Auto-renewal
sudo crontab -e
# Add: 0 12 * * * /usr/bin/certbot renew --quiet
```

#### Option B: Custom Certificates
```bash
# Create SSL directory
sudo mkdir -p /etc/ssl/private /etc/ssl/certs

# Copy your certificates
sudo cp your-certificate.crt /etc/ssl/certs/api.yourdomain.com.crt
sudo cp your-private-key.key /etc/ssl/private/api.yourdomain.com.key
sudo cp your-ca-chain.crt /etc/ssl/certs/api.yourdomain.com-chain.crt

# Set secure permissions
sudo chmod 600 /etc/ssl/private/api.yourdomain.com.key
sudo chmod 644 /etc/ssl/certs/api.yourdomain.com.*
```

#### Option C: Self-Signed (Development Only)
```bash
# Generate self-signed certificate
sudo openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
    -keyout /etc/ssl/private/api.yourdomain.com.key \
    -out /etc/ssl/certs/api.yourdomain.com.crt \
    -subj "/C=IT/ST=State/L=City/O=Organization/CN=api.yourdomain.com"
```

### 4. Configure Backend Settings

Edit `apache/vhosts.conf` to match your environment:

#### Single Backend (Development)
```apache
# Simple proxy to single backend
ProxyPass /api/ http://127.0.0.1:8081/api/
ProxyPassReverse /api/ http://127.0.0.1:8081/api/
```

#### Load Balanced Backends (Production)
```apache
<Proxy balancer://pocket-backend>
    # Primary backend
    BalancerMember http://127.0.0.1:8081 route=backend1 status=+H
    
    # Secondary backend (uncomment for HA)
    # BalancerMember http://127.0.0.1:8082 route=backend2
    
    # External backend (for distributed setup)
    # BalancerMember http://backend.internal:8081 route=backend3
    
    # Load balancing method
    ProxySet lbmethod byrequests
    
    # Health check configuration
    ProxySet hcmethod GET
    ProxySet hcuri /actuator/health
    ProxySet retry 3
</Proxy>

# API routes with load balancing
ProxyPass /api/ balancer://pocket-backend/api/
ProxyPassReverse /api/ balancer://pocket-backend/api/
```

### 5. Domain and CORS Configuration

Update the configuration for your domain:

```apache
# Change these values in vhosts.conf
ServerName api.yourdomain.com
ServerAlias pocket-api.yourdomain.com

# Update CORS origins
SetEnvIf Origin "^https?://(www\.)?yourdomain\.com$" AccessControlAllowOrigin=$0
SetEnvIf Origin "^https?://app\.yourdomain\.com$" AccessControlAllowOrigin=$0
SetEnvIf Origin "^https?://admin\.yourdomain\.com$" AccessControlAllowOrigin=$0
```

### 6. Start and Enable Apache
```bash
# Test configuration
sudo apache2ctl configtest

# Start Apache
sudo systemctl start apache2
sudo systemctl enable apache2

# Check status
sudo systemctl status apache2
```

## 🔧 Configuration Customization

### Performance Tuning

#### Worker Configuration
```apache
# Add to httpd.conf for high-traffic sites
<IfModule mpm_prefork_module>
    StartServers             8
    MinSpareServers          5
    MaxSpareServers         20
    ServerLimit            256
    MaxRequestWorkers      256
    MaxConnectionsPerChild   0
</IfModule>

<IfModule mpm_worker_module>
    StartServers             3
    MinSpareThreads         25
    MaxSpareThreads         75
    ThreadLimit             64
    ThreadsPerChild         25
    MaxRequestWorkers      400
    MaxConnectionsPerChild   0
</IfModule>
```

#### Cache Configuration
```apache
# Enable caching for static content
<IfModule mod_expires.c>
    ExpiresActive On
    ExpiresByType text/css "access plus 1 month"
    ExpiresByType application/javascript "access plus 1 month"
    ExpiresByType image/png "access plus 1 year"
    ExpiresByType image/jpg "access plus 1 year"
    ExpiresByType image/jpeg "access plus 1 year"
    ExpiresByType image/gif "access plus 1 year"
    ExpiresByType image/ico "access plus 1 year"
    ExpiresByType image/icon "access plus 1 year"
    ExpiresByType text/plain "access plus 1 month"
    ExpiresByType application/pdf "access plus 1 month"
</IfModule>
```

### Security Enhancements

#### Rate Limiting (mod_security)
```apache
# Add to .htaccess or virtual host
<IfModule mod_security2.c>
    SecRuleEngine On
    
    # Rate limit API endpoints
    SecRule REQUEST_URI "@beginsWith /api/" \
        "id:1001,\
         phase:1,\
         pass,\
         setvar:ip.api_requests=+1,\
         setvar:ip.api_requests_window=%{TIME_EPOCH},\
         expirevar:ip.api_requests=60"
    
    SecRule IP:API_REQUESTS "@gt 100" \
        "id:1002,\
         phase:1,\
         deny,\
         status:429,\
         msg:'Rate limit exceeded for API endpoints'"
</IfModule>
```

#### Additional Security Headers
```apache
# Enhanced security headers
Header always set Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"
Header always set Permissions-Policy "geolocation=(), microphone=(), camera=()"
Header always set Cross-Origin-Embedder-Policy "require-corp"
Header always set Cross-Origin-Opener-Policy "same-origin"
Header always set Cross-Origin-Resource-Policy "cross-origin"
```

### Monitoring Configuration

#### Extended Logging
```apache
# Custom log format for API monitoring
LogFormat "%h %l %u %t \"%r\" %>s %O \"%{Referer}i\" \"%{User-Agent}i\" %D" api_combined

# Separate log files for different purposes
CustomLog logs/api_access.log api_combined env=api_request
CustomLog logs/admin_access.log combined env=admin_request
ErrorLog logs/pocket_error.log

# Log request processing time
LogFormat "%h %l %u %t \"%r\" %>s %b %D" timing
CustomLog logs/timing.log timing
```

#### Status Module Configuration
```apache
# Enable status module for monitoring
<IfModule mod_status.c>
    <Location "/server-status">
        SetHandler server-status
        Require ip 127.0.0.1
        Require ip 10.0.0.0/8
        Require ip 192.168.0.0/16
    </Location>
    
    <Location "/server-info">
        SetHandler server-info
        Require ip 127.0.0.1
        Require ip 10.0.0.0/8
        Require ip 192.168.0.0/16
    </Location>
</IfModule>
```

## 📊 Monitoring and Maintenance

### Health Checks

#### Apache Status
```bash
# Check Apache status
sudo systemctl status apache2

# Test configuration
sudo apache2ctl configtest

# Check error logs
sudo tail -f /var/log/apache2/error.log
```

#### SSL Certificate Status
```bash
# Check certificate expiration
sudo openssl x509 -in /etc/ssl/certs/api.yourdomain.com.crt -noout -dates

# Test SSL configuration
openssl s_client -connect api.yourdomain.com:443 -servername api.yourdomain.com

# SSL Labs test (online)
# https://www.ssllabs.com/ssltest/analyze.html?d=api.yourdomain.com
```

#### Backend Health
```bash
# Test backend connectivity
curl -f http://127.0.0.1:8081/actuator/health

# Check proxy status
curl -H "Host: api.yourdomain.com" http://localhost/api/v5/health

# Load balancer status
curl https://api.yourdomain.com/balancer-manager
```

### Log Analysis

#### Access Log Analysis
```bash
# Most frequent IP addresses
sudo awk '{print $1}' /var/log/apache2/access.log | sort | uniq -c | sort -nr | head -10

# Most requested endpoints
sudo awk '{print $7}' /var/log/apache2/access.log | sort | uniq -c | sort -nr | head -10

# Response code distribution
sudo awk '{print $9}' /var/log/apache2/access.log | sort | uniq -c | sort -nr

# Average response time
sudo awk '{sum+=$NF; count++} END {print "Average response time:", sum/count "ms"}' /var/log/apache2/timing.log
```

#### Error Log Analysis
```bash
# Recent errors
sudo tail -50 /var/log/apache2/error.log

# Error frequency
sudo grep -E "\[error\]|\[warn\]" /var/log/apache2/error.log | \
    awk '{print $4}' | sort | uniq -c | sort -nr

# SSL errors
sudo grep -i ssl /var/log/apache2/error.log
```

### Performance Monitoring

#### Real-time Monitoring
```bash
# Monitor active connections
watch -n 1 'netstat -an | grep :443 | wc -l'

# Monitor Apache processes
watch -n 2 'ps aux | grep apache2 | grep -v grep'

# Monitor server status
curl -s http://localhost/server-status?auto
```

#### Performance Metrics
```bash
# Requests per second
sudo tail -f /var/log/apache2/access.log | pv -l -r > /dev/null

# Response time distribution
sudo awk '{print $NF}' /var/log/apache2/timing.log | sort -n | \
    awk '{
        count[NR] = $1;
    }
    END {
        if (NR%2) {
            median = count[(NR+1)/2];
        } else {
            median = (count[(NR/2)] + count[(NR/2)+1]) / 2.0;
        }
        print "Median response time: " median "ms";
        print "95th percentile: " count[int(NR*0.95)] "ms";
    }'
```

## 🚨 Troubleshooting

### Common Issues

#### SSL Certificate Problems
```bash
# Certificate not trusted
sudo update-ca-certificates

# Certificate chain issues
openssl verify -CApath /etc/ssl/certs /etc/ssl/certs/api.yourdomain.com.crt

# Certificate permissions
sudo chmod 644 /etc/ssl/certs/api.yourdomain.com.crt
sudo chmod 600 /etc/ssl/private/api.yourdomain.com.key
```

#### Proxy Connection Issues
```bash
# Backend not reachable
telnet 127.0.0.1 8081

# DNS resolution issues
nslookup backend.internal

# Check proxy configuration
apache2ctl -S | grep -i proxy
```

#### Performance Issues
```bash
# Too many connections
ulimit -n 65536  # Increase file descriptor limit

# Memory issues
free -h
sudo systemctl status apache2

# Check Apache limits
apache2ctl -V | grep -i server_limit
```

### Recovery Procedures

#### Service Recovery
```bash
# Graceful restart
sudo systemctl reload apache2

# Full restart
sudo systemctl restart apache2

# Emergency stop and start
sudo systemctl stop apache2
sudo systemctl start apache2
```

#### Configuration Recovery
```bash
# Backup current configuration
sudo cp /etc/apache2/sites-available/pocket-backend.conf \
    /etc/apache2/sites-available/pocket-backend.conf.backup

# Restore from backup
sudo cp /etc/apache2/sites-available/pocket-backend.conf.backup \
    /etc/apache2/sites-available/pocket-backend.conf

# Test before applying
sudo apache2ctl configtest
```

#### Certificate Renewal
```bash
# Manual Let's Encrypt renewal
sudo certbot renew --dry-run
sudo certbot renew

# Check renewal status
sudo certbot certificates

# Reload Apache after renewal
sudo systemctl reload apache2
```

## 🔐 Security Best Practices

### Configuration Security
1. **Hide Apache Version**: `ServerTokens Prod` in apache2.conf
2. **Disable Server Signature**: `ServerSignature Off`
3. **Restrict Access**: Use `Require` directives for sensitive locations
4. **File Permissions**: Ensure proper ownership and permissions
5. **Regular Updates**: Keep Apache and modules updated

### Monitoring Security
1. **Log Analysis**: Regular review of access and error logs
2. **Intrusion Detection**: Monitor for suspicious patterns
3. **Rate Limiting**: Implement rate limiting for API endpoints
4. **Fail2ban**: Configure automatic IP blocking for repeated failures

### Network Security
1. **Firewall Rules**: Restrict access to necessary ports only
2. **VPN Access**: Use VPN for administrative access
3. **Network Segmentation**: Isolate backend services
4. **DDoS Protection**: Implement DDoS mitigation strategies

## 📚 Additional Resources

- [Apache HTTP Server Documentation](https://httpd.apache.org/docs/)
- [mod_ssl Documentation](https://httpd.apache.org/docs/2.4/mod/mod_ssl.html)
- [mod_proxy Documentation](https://httpd.apache.org/docs/2.4/mod/mod_proxy.html)
- [Let's Encrypt Documentation](https://letsencrypt.org/docs/)
- [Mozilla SSL Configuration Generator](https://ssl-config.mozilla.org/)

---

**Security Note**: Always test configuration changes in a development environment before applying to production. Keep backups of working configurations and maintain an incident response plan.