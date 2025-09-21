#!/bin/bash

# Apache HTTP Server Deployment Script for Pocket Backend
# This script helps automate the Apache configuration deployment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APACHE_DIR="$SCRIPT_DIR"
DOMAIN="${DOMAIN:-api.yourdomain.com}"
BACKEND_HOST="${BACKEND_HOST:-127.0.0.1}"
BACKEND_PORT="${BACKEND_PORT:-8081}"

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_root() {
    if [[ $EUID -ne 0 ]]; then
        log_error "This script must be run as root (use sudo)"
        exit 1
    fi
}

detect_os() {
    if [[ -f /etc/debian_version ]]; then
        OS="debian"
        APACHE_SERVICE="apache2"
        APACHE_CONFIG_DIR="/etc/apache2"
        SITES_AVAILABLE="$APACHE_CONFIG_DIR/sites-available"
        SITES_ENABLED="$APACHE_CONFIG_DIR/sites-enabled"
        CONF_AVAILABLE="$APACHE_CONFIG_DIR/conf-available"
        CONF_ENABLED="$APACHE_CONFIG_DIR/conf-enabled"
    elif [[ -f /etc/redhat-release ]]; then
        OS="redhat"
        APACHE_SERVICE="httpd"
        APACHE_CONFIG_DIR="/etc/httpd"
        SITES_AVAILABLE="$APACHE_CONFIG_DIR/conf.d"
        SITES_ENABLED="$APACHE_CONFIG_DIR/conf.d"
        CONF_AVAILABLE="$APACHE_CONFIG_DIR/conf.d"
        CONF_ENABLED="$APACHE_CONFIG_DIR/conf.d"
    else
        log_error "Unsupported operating system"
        exit 1
    fi
    
    log_info "Detected OS: $OS"
}

install_apache() {
    log_info "Installing Apache HTTP Server..."
    
    if [[ "$OS" == "debian" ]]; then
        apt update
        apt install -y apache2 apache2-utils libapache2-mod-security2
        
        # Enable required modules
        a2enmod ssl rewrite proxy proxy_http proxy_balancer lbmethod_byrequests headers security2 deflate expires status
        
    elif [[ "$OS" == "redhat" ]]; then
        if command -v dnf &> /dev/null; then
            dnf install -y httpd httpd-tools mod_ssl mod_security
        else
            yum install -y httpd httpd-tools mod_ssl mod_security
        fi
        
        systemctl enable $APACHE_SERVICE
    fi
    
    log_success "Apache installed successfully"
}

configure_apache() {
    log_info "Configuring Apache for Pocket Backend..."
    
    # Create backup of original configuration
    if [[ -f "$APACHE_CONFIG_DIR/apache2.conf" ]] && [[ ! -f "$APACHE_CONFIG_DIR/apache2.conf.backup" ]]; then
        cp "$APACHE_CONFIG_DIR/apache2.conf" "$APACHE_CONFIG_DIR/apache2.conf.backup"
        log_info "Created backup of original Apache configuration"
    fi
    
    # Copy main configuration
    if [[ -f "$APACHE_DIR/httpd.conf" ]]; then
        cp "$APACHE_DIR/httpd.conf" "$CONF_AVAILABLE/pocket-backend.conf"
        
        if [[ "$OS" == "debian" ]]; then
            a2enconf pocket-backend
        fi
        
        log_success "Copied main configuration"
    else
        log_error "Main configuration file not found: $APACHE_DIR/httpd.conf"
        exit 1
    fi
    
    # Copy virtual hosts configuration
    if [[ -f "$APACHE_DIR/vhosts.conf" ]]; then
        # Replace placeholders with actual values
        sed -e "s/api\.yourdomain\.com/$DOMAIN/g" \
            -e "s/pocket-api\.yourdomain\.com/pocket-$DOMAIN/g" \
            -e "s/127\.0\.0\.1:8081/$BACKEND_HOST:$BACKEND_PORT/g" \
            "$APACHE_DIR/vhosts.conf" > "$SITES_AVAILABLE/pocket-backend.conf"
        
        if [[ "$OS" == "debian" ]]; then
            a2ensite pocket-backend
        fi
        
        log_success "Copied virtual hosts configuration"
    else
        log_error "Virtual hosts configuration file not found: $APACHE_DIR/vhosts.conf"
        exit 1
    fi
}

setup_ssl() {
    log_info "Setting up SSL configuration..."
    
    # Create SSL directories
    mkdir -p /etc/ssl/private /etc/ssl/certs
    
    if [[ ! -f "/etc/ssl/certs/$DOMAIN.crt" ]]; then
        log_warning "SSL certificate not found for $DOMAIN"
        log_info "Options:"
        log_info "1. Use Let's Encrypt: sudo certbot --apache -d $DOMAIN"
        log_info "2. Copy existing certificates to:"
        log_info "   - /etc/ssl/certs/$DOMAIN.crt"
        log_info "   - /etc/ssl/private/$DOMAIN.key"
        log_info "   - /etc/ssl/certs/$DOMAIN-chain.crt (optional)"
        log_info "3. Generate self-signed certificate (development only)"
        
        read -p "Generate self-signed certificate for development? [y/N]: " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
                -keyout "/etc/ssl/private/$DOMAIN.key" \
                -out "/etc/ssl/certs/$DOMAIN.crt" \
                -subj "/C=IT/ST=State/L=City/O=Organization/CN=$DOMAIN"
            
            chmod 600 "/etc/ssl/private/$DOMAIN.key"
            chmod 644 "/etc/ssl/certs/$DOMAIN.crt"
            
            log_success "Generated self-signed certificate"
        fi
    else
        log_success "SSL certificate found for $DOMAIN"
    fi
}

test_configuration() {
    log_info "Testing Apache configuration..."
    
    if [[ "$OS" == "debian" ]]; then
        apache2ctl configtest
    else
        httpd -t
    fi
    
    if [[ $? -eq 0 ]]; then
        log_success "Apache configuration is valid"
    else
        log_error "Apache configuration test failed"
        exit 1
    fi
}

start_apache() {
    log_info "Starting Apache HTTP Server..."
    
    systemctl start $APACHE_SERVICE
    systemctl enable $APACHE_SERVICE
    
    if systemctl is-active --quiet $APACHE_SERVICE; then
        log_success "Apache is running"
    else
        log_error "Failed to start Apache"
        systemctl status $APACHE_SERVICE
        exit 1
    fi
}

test_backend_connectivity() {
    log_info "Testing backend connectivity..."
    
    if curl -f -s "http://$BACKEND_HOST:$BACKEND_PORT/actuator/health" > /dev/null; then
        log_success "Backend is reachable at $BACKEND_HOST:$BACKEND_PORT"
    else
        log_warning "Backend is not reachable at $BACKEND_HOST:$BACKEND_PORT"
        log_info "Make sure Pocket Backend is running before testing the proxy"
    fi
}

show_status() {
    log_info "Apache Configuration Summary:"
    echo "  - OS: $OS"
    echo "  - Apache Service: $APACHE_SERVICE"
    echo "  - Domain: $DOMAIN"
    echo "  - Backend: $BACKEND_HOST:$BACKEND_PORT"
    echo "  - Configuration Dir: $APACHE_CONFIG_DIR"
    echo ""
    
    log_info "Service Status:"
    systemctl status $APACHE_SERVICE --no-pager -l
    echo ""
    
    log_info "Listening Ports:"
    ss -tlnp | grep -E ':(80|443)\s'
    echo ""
    
    if [[ -f "/etc/ssl/certs/$DOMAIN.crt" ]]; then
        log_info "SSL Certificate Info:"
        openssl x509 -in "/etc/ssl/certs/$DOMAIN.crt" -noout -dates -subject
        echo ""
    fi
    
    log_info "Test URLs:"
    echo "  - HTTP:  http://$DOMAIN/"
    echo "  - HTTPS: https://$DOMAIN/"
    echo "  - Health: http://$DOMAIN/health"
    echo "  - API:   http://$DOMAIN/api/v5/"
    echo "  - Status: http://$DOMAIN/server-status (if enabled)"
}

usage() {
    echo "Usage: $0 [OPTIONS] COMMAND"
    echo ""
    echo "Commands:"
    echo "  install     Install and configure Apache"
    echo "  configure   Configure Apache only (requires Apache to be installed)"
    echo "  ssl         Setup SSL certificates"
    echo "  test        Test configuration"
    echo "  status      Show status and configuration summary"
    echo "  start       Start Apache service"
    echo "  restart     Restart Apache service"
    echo "  stop        Stop Apache service"
    echo ""
    echo "Options:"
    echo "  --domain DOMAIN       Set domain name (default: api.yourdomain.com)"
    echo "  --backend-host HOST   Set backend host (default: 127.0.0.1)"
    echo "  --backend-port PORT   Set backend port (default: 8081)"
    echo "  --help               Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --domain api.example.com install"
    echo "  $0 --backend-host 192.168.1.100 configure"
    echo "  $0 status"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --domain)
            DOMAIN="$2"
            shift 2
            ;;
        --backend-host)
            BACKEND_HOST="$2"
            shift 2
            ;;
        --backend-port)
            BACKEND_PORT="$2"
            shift 2
            ;;
        --help)
            usage
            exit 0
            ;;
        install|configure|ssl|test|status|start|restart|stop)
            COMMAND="$1"
            shift
            ;;
        *)
            log_error "Unknown option: $1"
            usage
            exit 1
            ;;
    esac
done

# Check if command is provided
if [[ -z "$COMMAND" ]]; then
    log_error "No command specified"
    usage
    exit 1
fi

# Main execution
log_info "Pocket Backend Apache Deployment Script"
log_info "Domain: $DOMAIN"
log_info "Backend: $BACKEND_HOST:$BACKEND_PORT"
echo ""

detect_os

case "$COMMAND" in
    install)
        check_root
        install_apache
        configure_apache
        setup_ssl
        test_configuration
        start_apache
        test_backend_connectivity
        show_status
        ;;
    configure)
        check_root
        configure_apache
        test_configuration
        systemctl reload $APACHE_SERVICE
        test_backend_connectivity
        ;;
    ssl)
        check_root
        setup_ssl
        ;;
    test)
        test_configuration
        test_backend_connectivity
        ;;
    start)
        check_root
        start_apache
        ;;
    restart)
        check_root
        systemctl restart $APACHE_SERVICE
        log_success "Apache restarted"
        ;;
    stop)
        check_root
        systemctl stop $APACHE_SERVICE
        log_success "Apache stopped"
        ;;
    status)
        show_status
        ;;
    *)
        log_error "Unknown command: $COMMAND"
        usage
        exit 1
        ;;
esac

log_success "Operation completed successfully!"