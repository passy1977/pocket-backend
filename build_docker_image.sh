#!/bin/bash

# Pocket Backend Docker Build and Setup Script with Spring Security
# This script builds and configures the Pocket Backend with secure defaults

set -e  # Exit on any error

# Configuration
NETWORK=pocket-network
ENV_FILE=".env"
DOCKER_DATA_DIR="docker_data"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Function to generate random secure passwords
generate_password() {
    local length=${1:-32}
    openssl rand -base64 32 | tr -d "=+/" | cut -c1-$length
}

# Function to generate AES IV (16 characters)
generate_aes_iv() {
    openssl rand -base64 12 | tr -d "=+/" | cut -c1-16
}

# Function to validate AES IV
validate_aes_iv() {
    local iv="$1"
    if [[ ${#iv} -ne 16 ]]; then
        log_error "AES CBC IV must be exactly 16 characters long"
        return 1
    fi
    # Check if contains only valid characters
    if [[ ! "$iv" =~ ^[A-Za-z0-9_-]+$ ]]; then
        log_error "AES CBC IV contains invalid characters. Use only A-Z, a-z, 0-9, _, -"
        return 1
    fi
    return 0
}

# Function to setup Docker network
setup_network() {
    log_info "Setting up Docker network '$NETWORK'..."
    
    if docker network inspect $NETWORK &> /dev/null; then
        log_success "Network '$NETWORK' already exists"
    else
        log_info "Creating network '$NETWORK'..."
        docker network create $NETWORK
        log_success "Network '$NETWORK' created"
    fi
}

# Function to setup directories
setup_directories() {
    log_info "Setting up directories..."
    
    if [ ! -d "$DOCKER_DATA_DIR" ]; then
        mkdir -p "$DOCKER_DATA_DIR"
        log_success "Created $DOCKER_DATA_DIR directory"
    fi
    
    if [ ! -d "$DOCKER_DATA_DIR/logs" ]; then
        mkdir -p "$DOCKER_DATA_DIR/logs"
        log_success "Created logs directory"
    fi
}

# Function to setup environment variables
setup_environment() {
    log_info "Setting up environment variables..."
    
    if [ -f "$ENV_FILE" ]; then
        log_info "Loading existing environment from $ENV_FILE"
        source "$ENV_FILE"
    fi
    
    # Database configuration
    if [ -z "$DB_ROOT_PASSWORD" ]; then
        echo
        log_info "Database Configuration"
        echo "=================="
        
        read -p "Generate secure database root password automatically? [Y/n]: " auto_db_pass
        if [[ "$auto_db_pass" =~ ^[Nn]$ ]]; then
            read -s -p "Enter MariaDB root password: " DB_ROOT_PASSWORD
            echo
        else
            DB_ROOT_PASSWORD=$(generate_password 32)
            log_success "Generated secure database root password"
        fi
        
        read -p "Database username [pocket_user]: " DB_USERNAME
        DB_USERNAME=${DB_USERNAME:-pocket_user}
        
        read -p "Generate secure database user password automatically? [Y/n]: " auto_user_pass
        if [[ "$auto_user_pass" =~ ^[Nn]$ ]]; then
            read -s -p "Enter database user password: " DB_PASSWORD
            echo
        else
            DB_PASSWORD=$(generate_password 32)
            log_success "Generated secure database user password"
        fi
    fi
    
    # Security configuration
    if [ -z "$AES_CBC_IV" ]; then
        echo
        log_info "Security Configuration"
        echo "==================="
        
        read -p "Generate secure AES CBC IV automatically? [Y/n]: " auto_aes
        if [[ "$auto_aes" =~ ^[Nn]$ ]]; then
            while true; do
                read -p "Enter AES CBC IV (exactly 16 characters): " AES_CBC_IV
                if validate_aes_iv "$AES_CBC_IV"; then
                    break
                fi
            done
        else
            AES_CBC_IV=$(generate_aes_iv)
            log_success "Generated secure AES CBC IV"
        fi
        
        read -p "Admin username [admin]: " ADMIN_USER
        ADMIN_USER=${ADMIN_USER:-admin}
        
        read -p "Generate secure admin password automatically? [Y/n]: " auto_admin_pass
        if [[ "$auto_admin_pass" =~ ^[Nn]$ ]]; then
            read -s -p "Enter admin password: " ADMIN_PASSWD
            echo
        else
            ADMIN_PASSWD=$(generate_password 32)
            log_success "Generated secure admin password"
        fi
    fi
    
    # Server configuration
    if [ -z "$SERVER_URL" ]; then
        echo
        log_info "Server Configuration"
        echo "=================="
        
        read -p "Server URL [http://localhost:8081]: " SERVER_URL
        SERVER_URL=${SERVER_URL:-http://localhost:8081}
        
        read -p "Server port [8081]: " SERVER_PORT
        SERVER_PORT=${SERVER_PORT:-8081}
        
        read -p "Additional CORS origins (comma-separated) []: " CORS_ADDITIONAL_ORIGINS
    fi
    
    # Save environment variables
    cat > "$ENV_FILE" << EOF
# Database Configuration
DB_ROOT_PASSWORD=$DB_ROOT_PASSWORD
DB_USERNAME=$DB_USERNAME
DB_PASSWORD=$DB_PASSWORD

# Security Configuration
AES_CBC_IV=$AES_CBC_IV
ADMIN_USER=$ADMIN_USER
ADMIN_PASSWD=$ADMIN_PASSWD

# Server Configuration
SERVER_URL=$SERVER_URL
SERVER_PORT=$SERVER_PORT
CORS_ADDITIONAL_ORIGINS=$CORS_ADDITIONAL_ORIGINS

# JVM Configuration
JVM_MAX_MEMORY=512m
JVM_MIN_MEMORY=256m
LOG_LEVEL=INFO
EOF
    
    log_success "Environment configuration saved to $ENV_FILE"
    
    # Display configuration summary
    echo
    log_info "Configuration Summary"
    echo "==================="
    echo "Database User: $DB_USERNAME"
    echo "Server URL: $SERVER_URL"
    echo "Server Port: $SERVER_PORT"
    echo "Admin User: $ADMIN_USER"
    echo "AES IV Length: ${#AES_CBC_IV} characters"
    if [ -n "$CORS_ADDITIONAL_ORIGINS" ]; then
        echo "CORS Origins: $CORS_ADDITIONAL_ORIGINS"
    fi
    echo
}

# Function to build Docker images
build_images() {
    log_info "Building Docker images..."
    
    # Load environment variables
    source "$ENV_FILE"
    
    # Build the main application
    log_info "Building Pocket Backend application..."
    docker build -t pocket-backend:5.0.0 .
    
    log_success "Docker images built successfully"
}

# Function to start services
start_services() {
    log_info "Starting services..."
    
    # Start with docker-compose
    docker compose up -d
    
    log_info "Waiting for services to be ready..."
    
    # Wait for database
    log_info "Waiting for database to be ready..."
    for i in {1..30}; do
        if docker compose exec pocket-db mysqladmin ping -h localhost --silent; then
            log_success "Database is ready"
            break
        fi
        if [ $i -eq 30 ]; then
            log_error "Database failed to start within timeout"
            exit 1
        fi
        sleep 2
    done
    
    # Wait for application
    log_info "Waiting for application to be ready..."
    for i in {1..60}; do
        if curl -f http://localhost:${SERVER_PORT}/actuator/health &> /dev/null; then
            log_success "Application is ready"
            break
        fi
        if [ $i -eq 60 ]; then
            log_warning "Application may not be fully ready yet"
            break
        fi
        sleep 2
    done
}

# Function to setup CLI tools
setup_cli_tools() {
    log_info "Setting up CLI tools..."
    
    # Create pocket-user command
    sudo tee /usr/local/bin/pocket-user > /dev/null << 'EOF'
#!/bin/bash
docker compose exec pocket-backend /var/www/pocket-user "$@"
EOF
    sudo chmod +x /usr/local/bin/pocket-user
    
    # Create pocket-device command
    sudo tee /usr/local/bin/pocket-device > /dev/null << 'EOF'
#!/bin/bash
docker compose exec pocket-backend /var/www/pocket-device "$@"
EOF
    sudo chmod +x /usr/local/bin/pocket-device
    
    log_success "CLI tools installed to /usr/local/bin/"
}

# Function to display final information
display_final_info() {
    source "$ENV_FILE"
    
    echo
    log_success "🎉 Pocket Backend setup completed successfully!"
    echo
    echo "📋 Service Information:"
    echo "====================="
    echo "🌐 Application URL: $SERVER_URL"
    echo "📊 Health Check: $SERVER_URL/actuator/health"
    echo "🔧 Admin Panel: $SERVER_URL/actuator (user: $ADMIN_USER)"
    echo "🗄️  Database: localhost:3306 (user: $DB_USERNAME)"
    echo
    echo "🔧 Management Commands:"
    echo "====================="
    echo "📱 User Management: pocket-user --help"
    echo "📟 Device Management: pocket-device --help"
    echo "🐳 View Logs: docker compose logs -f"
    echo "⏹️  Stop Services: docker compose down"
    echo "🔄 Restart Services: docker compose restart"
    echo
    echo "🔐 Security Information:"
    echo "====================="
    echo "⚠️  Keep your .env file secure and never commit it to version control"
    echo "🔑 Admin credentials and database passwords are stored in .env"
    echo "🛡️  AES IV and other secrets are automatically generated"
    echo
    log_warning "Remember to backup your .env file and database volumes!"
}

# Main execution
main() {
    log_info "🚀 Starting Pocket Backend Docker setup..."
    echo
    
    # Check requirements
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    if ! command -v docker compose &> /dev/null; then
        log_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    # Execute setup steps
    setup_network
    setup_directories
    setup_environment
    build_images
    start_services
    setup_cli_tools
    display_final_info
}

# Run main function
main "$@"
