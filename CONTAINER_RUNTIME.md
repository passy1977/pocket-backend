# Container Runtime Support

Pocket Backend supports both **Docker** and **Podman** as container runtimes. The build script automatically detects which one is available on your system and uses it accordingly.

## Supported Runtimes

### Docker (Recommended for most users)
- **Version**: 24.0+
- **Compose**: Docker Compose v2
- **Installation**: https://docs.docker.com/get-docker/

### Podman (Recommended for rootless containers)
- **Version**: 4.0+
- **Compose**: podman-compose (install: `pip install podman-compose`)
- **Installation**: https://podman.io/getting-started/installation

## Automatic Detection

The `build_docker_image.sh` script automatically detects the available container runtime:

```bash
./build_docker_image.sh
```

The script will:
1. Check if `podman` is available
2. If not, check if `docker` is available
3. Use the detected runtime for all operations
4. Exit with an error if neither is found

## Manual Container Runtime Selection

If you want to force a specific runtime, you can modify the script or use the appropriate commands directly:

### Using Docker
```bash
# Build image
docker build -t pocket-backend:5.0.0 .

# Start services
docker compose up -d

# View logs
docker compose logs -f

# Stop services
docker compose down
```

### Using Podman
```bash
# Build image
podman build -t pocket-backend:5.0.0 .

# Start services
podman-compose up -d

# View logs
podman-compose logs -f

# Stop services
podman-compose down
```

## Differences Between Docker and Podman

### Docker
- ✅ More mature ecosystem
- ✅ Better GUI tools available
- ✅ More documentation and community support
- ❌ Requires daemon running as root
- ❌ Larger resource footprint

### Podman
- ✅ Daemonless (no background service)
- ✅ Rootless containers (better security)
- ✅ Drop-in replacement for Docker CLI
- ✅ Smaller resource footprint
- ❌ Some compose features may differ
- ❌ Less GUI tooling available

## Compatibility

Both Docker and Podman use the same:
- Dockerfile format
- Compose file format (docker-compose.yaml / compose.yaml)
- Container images (OCI standard)
- Registry format (Docker Hub, etc.)

This means you can build with one and run with the other!

## Troubleshooting

### Podman: compose command not found
```bash
pip install podman-compose
```

### Docker: compose v2 not available
```bash
# Install Docker Compose plugin
sudo apt-get install docker-compose-plugin
# or
sudo yum install docker-compose-plugin
```

### Permission denied (Docker)
```bash
# Add your user to docker group
sudo usermod -aG docker $USER
newgrp docker
```

### Permission denied (Podman)
Podman should work without sudo by default. If not:
```bash
# Enable user namespaces
sudo sysctl -w kernel.unprivileged_userns_clone=1
```

## Environment Variables

The script uses the same environment variables regardless of runtime:
- `DB_ROOT_PASSWORD`: Database root password
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password
- `AES_CBC_IV`: AES encryption IV
- `ADMIN_USER`: Admin username
- `ADMIN_PASSWD`: Admin password
- `SERVER_URL`: Server URL
- `SERVER_PORT`: Server port

All are stored in `.env` file.
