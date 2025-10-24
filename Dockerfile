# Multi-stage Docker build for Pocket Backend with Spring Security
# Stage 1: Build the application
FROM maven:3-amazoncorretto-21-debian-trixie AS build

USER root

# Install system dependencies
RUN DEBIAN_FRONTEND=noninteractive apt update && apt-get upgrade -y
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y \
    libssl-dev curl git build-essential manpages-dev autoconf \
    automake cmake git libtool pkg-config vim \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Setup working directory and user
WORKDIR /var/www
RUN useradd -m pocket
RUN chown pocket:pocket /var/www
USER pocket

# Copy application files
RUN mkdir /var/www/scripts
COPY --chown=pocket commons ./commons
COPY --chown=pocket:pocket ./src src
COPY --chown=pocket pom.xml ./
COPY --chown=pocket ./scripts/pocket5-config.yaml /var/www/scripts/pocket5-config.yaml

# Install commons dependencies
RUN mvn install:install-file \
    -Dfile=commons/commons-base-6.0.0.jar \
    -DgroupId=it.salsi \
    -DartifactId=commons-base \
    -Dversion=6.0.0 \
    -Dpackaging=jar \
    -DgeneratePom=true

RUN mvn install:install-file \
    -Dfile=commons/commons-utils-6.0.0.jar \
    -DgroupId=it.salsi \
    -DartifactId=commons-utils \
    -Dversion=6.0.0 \
    -Dpackaging=jar \
    -DgeneratePom=true

# Build application
RUN mvn package -DskipTests

# Build CLI tools
WORKDIR /home/pocket
RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y
ENV PATH="/home/pocket/.cargo/bin:$PATH"
RUN git clone https://github.com/passy1977/pocket-cli.git
WORKDIR /home/pocket/pocket-cli
RUN cargo build --release
RUN rustup self uninstall -y
RUN cp /home/pocket/pocket-cli/target/release/pocket-device /var/www/pocket-device
RUN cp /home/pocket/pocket-cli/target/release/pocket-user /var/www/pocket-user
RUN rm -fr /home/pocket/pocket-cli

# Stage 2: Runtime image
FROM debian:trixie

# Install runtime dependencies
RUN DEBIAN_FRONTEND=noninteractive apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    bash \
    default-jdk \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Create user and setup directories
RUN useradd -m -s /bin/bash pocket
RUN mkdir -p /var/www/scripts /var/log/pocket
RUN chown -R pocket:pocket /var/www /var/log/pocket

# Copy application files from build stage
COPY --from=build --chown=pocket:pocket /var/www/target/pocket-*.jar /var/www/pocket.jar
COPY --from=build --chown=pocket:pocket /var/www/scripts /var/www/scripts
COPY --from=build --chown=pocket:pocket /var/www/pocket-device /var/www/pocket-device
COPY --from=build --chown=pocket:pocket /var/www/pocket-user /var/www/pocket-user

# Make CLI tools executable
RUN chmod +x /var/www/pocket-device /var/www/pocket-user

# Switch to non-root user
USER pocket

# Set working directory
WORKDIR /var/www

# Expose application port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Set JVM options for production
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -server"

# Default environment variables (should be overridden in production)
ENV SPRING_PROFILES_ACTIVE=docker
ENV LOG_LEVEL=INFO

# Start application
CMD java $JAVA_OPTS \
    -Dspring.config.location=classpath:application.yaml,/var/www/scripts/pocket5-config.yaml \
    -Dlogging.file.name=/var/log/pocket/application.log \
    -jar /var/www/pocket.jar



