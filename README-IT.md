# 🔐 Pocket Backend

[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green.svg)](https://spring.io/projects/spring-boot)
[![Tests](https://img.shields.io/badge/Tests-31%2F31%20Passing-brightgreen.svg)](src/test/)
[![Apache](https://img.shields.io/badge/Apache-Configured-orange.svg)](docs/APACHE_SETUP.md)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![Security](https://img.shields.io/badge/Security-Spring%20Security-red.svg)](https://spring.io/projects/spring-security)
[![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Backend sicuro e scalabile per l'applicazione Pocket, costruito con **Spring Boot 3.5.6** e **Java 21**. Fornisce API REST robuste per la gestione di sessioni, autenticazione utenti e archiviazione sicura dei dati con crittografia end-to-end.

[🇬🇧 English](README.md) | 🇮🇹 Italiano

---

## ✨ Caratteristiche Principali

- 🔐 **Sicurezza Enterprise** con Spring Security e autenticazione personalizzata
- 🏗️ **Architettura Moderna** con Spring Boot 3.5.6 e Java 21
- 🔒 **Crittografia Robusta** RSA + AES-CBC per la protezione dei dati
- 🐳 **Containerizzazione** completa con Docker e Docker Compose
- 📊 **Monitoraggio Integrato** con Spring Boot Actuator
- 🌐 **CORS Dinamico** configurabile per ambienti multipli
- ✅ **Validazione Completa** dei dati con Bean Validation
- 🔄 **Health Checks** automatici e recovery
- 🧪 **Suite di Test Completa** con 31/31 test che passano (unit, integration, security)
- 🌐 **Configurazione Apache HTTP** pronta per produzione con SSL e load balancing
- 📈 **Client Mock API** per testing e integrazione

## 📋 Requisiti

### Ambiente di Sviluppo
- **Java**: 21+ (LTS raccomandato)
- **Maven**: 3.8+ per gestione dipendenze e build
- **Database**: MySQL 8.0+ o MariaDB 10.6+
- **IDE**: IntelliJ IDEA, Eclipse, o VS Code con estensioni Java

### Ambiente di Produzione
- **Docker**: 24.0+ con Docker Compose v2
- **Memoria**: Minimo 2GB RAM (4GB raccomandati)
- **Storage**: 10GB+ per applicazione e database
- **Rete**: Porte 8081 (API), 3306 (DB), 80/443 (HTTP/HTTPS)

### Strumenti Opzionali
- **Apache HTTP Server**: Per reverse proxy e terminazione SSL in produzione
- **Monitoraggio**: Prometheus + Grafana per metriche
- **Backup**: Soluzioni di backup automatizzate del database

## 🧪 Test e Qualità

### Suite di Test Completa

Il progetto include una suite di test completa che copre tutti gli aspetti dell'applicazione:

#### Unit Test (`src/test/java/`)
- **SessionRestTest.java**: Test unitari per il controller REST con mock Mockito
- **AuthenticationFilterTest.java**: Test del filtro di sicurezza e autenticazione
- **PocketApiClientTest.java**: Test del client API mock

#### Integration Test
- **SessionRestIntegrationTest.java**: Test di integrazione con contesto Spring completo
- **Database H2**: Test con database in-memory per isolamento
- **MockMvc**: Test HTTP completi con validazione Bean Validation

#### Mock Client API
- **PocketApiClient.java**: Client HTTP completo per test e integrazione
- **Supporto Async/Sync**: Chiamate sincrone e asincrone
- **Load Testing**: Capacità di test di carico e stress
- **PocketApiClientExample.java**: Esempi di utilizzo del client

### Esecuzione Test

```bash
# Esegui tutti i test
mvn test

# Esegui test specifici
mvn test -Dtest=SessionRestTest
mvn test -Dtest=SessionRestIntegrationTest

# Test con coverage
mvn test jacoco:report

# Test con profilo specifico
mvn test -Dspring.profiles.active=test
```

### Configurazione Test

La configurazione di test è separata in `src/test/resources/application-test.yaml`:
- Database H2 in-memory
- Logging debug per troubleshooting
- Configurazioni di sicurezza per test
- Mock services e stub per dipendenze esterne

## 🚀 Avvio Rapido

### 🐳 Deployment con Docker (Raccomandato)

Il modo più semplice per avviare Pocket Backend è utilizzare Docker Compose:

```bash
# 1. Clona il repository
git clone https://github.com/passy1977/pocket-backend.git
cd pocket-backend

# 2. Configura l'ambiente (modifica le password!)
cp .env.example .env
nano .env

# 3. Avvia tutto con un comando
./build_docker_image.sh

# 4. Verifica che tutto funzioni
curl http://localhost:8081/actuator/health
```

**Questo è tutto!** 🎉 La tua applicazione Pocket Backend è ora in esecuzione su `http://localhost:8081`

### ⚙️ Setup Manuale

Se preferisci un setup manuale senza Docker:

#### 1. Clona e Costruisci
```bash
git clone https://github.com/passy1977/pocket-backend.git
cd pocket-backend
mvn clean package
```

#### 2. Configura le Variabili d'Ambiente

#### Ambiente di Sviluppo
```bash
# Configurazione Database
export DB_USERNAME="pocket_user"
export DB_PASSWORD="tua_password_database_sicura"

# Configurazione Sicurezza
export AES_CBC_IV="tuo_iv_16_caratteri!"  # Deve essere esattamente 16 caratteri
export ADMIN_USER="admin"
export ADMIN_PASSWD="tua_password_admin_sicura"

# Configurazione CORS (opzionale)
export CORS_ADDITIONAL_ORIGINS="https://tuodominio.com,https://app.tuodominio.com"
```

#### Ambiente di Produzione
```bash
# Configurazione Database
export DB_USERNAME="pocket_prod_user"
export DB_PASSWORD="password_produzione_molto_sicura"

# Configurazione Sicurezza  
export AES_CBC_IV="prod_iv_16_caratteri!"  # Deve essere esattamente 16 caratteri
export ADMIN_USER="admin"
export ADMIN_PASSWD="password_admin_molto_sicura"

# Configurazione Server
export SERVER_URL="https://api.tuodominio.com:8081"
export CORS_ADDITIONAL_ORIGINS="https://tuodominio.com,https://app.tuodominio.com"

# Configurazione SSL (per produzione)
export SSL_KEYSTORE_PASSWORD="password_keystore"
```

### 3. Setup Database

#### Utilizzando Docker (Raccomandato per Sviluppo)
```bash
# Avvia container MariaDB
docker run --detach --name pocket-db \
  -p 3306:3306 \
  --env MARIADB_ROOT_PASSWORD=tua_password_database_sicura \
  --env MARIADB_DATABASE=pocket5 \
  --env MARIADB_USER=pocket_user \
  --env MARIADB_PASSWORD=tua_password_database_sicura \
  mariadb:latest

# Inizializza schema database
docker exec -i pocket-db mariadb -u root -ptua_password_database_sicura pocket5 < scripts/pocket5.sql
```

#### Setup Manuale Database
```sql
-- Crea database e utente
CREATE DATABASE pocket5 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'pocket_user'@'%' IDENTIFIED BY 'tua_password_database_sicura';
GRANT ALL PRIVILEGES ON pocket5.* TO 'pocket_user'@'%';
FLUSH PRIVILEGES;

-- Importa schema
mysql -u pocket_user -p pocket5 < scripts/pocket5.sql
```

### 4. Avvia l'Applicazione

```bash
# Esegui l'applicazione
java -jar target/pocket-backend-5.0.0.jar

# O con Maven
mvn spring-boot:run
```

L'applicazione sarà disponibile su `http://localhost:8081`

## 🌐 Configurazione Apache HTTP Server

Il progetto include una configurazione completa di Apache HTTP Server per deployment in produzione con reverse proxy, terminazione SSL e load balancing.

### File di Configurazione

- `apache/httpd.conf` - Configurazione principale Apache con SSL, sicurezza e load balancing
- `apache/vhosts.conf` - Configurazioni virtual host per sviluppo e produzione

### Caratteristiche

#### Sicurezza & SSL
- **TLS 1.2+** con suite di cifratura sicure
- **Terminazione SSL** per i servizi backend
- **Header di sicurezza** (HSTS, CSP, X-Frame-Options, etc.)
- **Rate limiting** e throttling delle richieste
- **Regole mod_security** per web application firewall

#### Load Balancing & Alta Disponibilità
- **Health check** per i servizi backend
- **Failover** su istanze backend secondarie
- **Session affinity** con routing basato su cookie
- **Degradazione graceful** del servizio

#### Ottimizzazione Performance
- **Compressione** (mod_deflate) per le risposte
- **Header di caching** per contenuto statico
- **Connessioni Keep-alive**
- **Connection pooling** verso i servizi backend

### Setup Rapido

#### 1. Installa Apache HTTP Server
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install apache2

# CentOS/RHEL
sudo yum install httpd

# macOS (usando Homebrew)
brew install httpd
```

#### 2. Abilita Moduli Richiesti
```bash
# Ubuntu/Debian
sudo a2enmod ssl rewrite proxy proxy_http proxy_balancer lbmethod_byrequests headers security2 deflate expires

# CentOS/RHEL
sudo systemctl enable httpd
```

#### 3. Copia File di Configurazione
```bash
# Copia configurazione principale
sudo cp apache/httpd.conf /etc/apache2/sites-available/pocket-backend.conf

# Copia virtual host
sudo cp apache/vhosts.conf /etc/apache2/sites-available/pocket-vhosts.conf

# Abilita siti (Ubuntu/Debian)
sudo a2ensite pocket-backend
sudo a2ensite pocket-vhosts
```

#### 4. Setup Certificato SSL
```bash
# Crea directory SSL
sudo mkdir -p /etc/ssl/certs /etc/ssl/private

# Per certificati Let's Encrypt
sudo certbot --apache -d api.tuodominio.com

# Per certificati personalizzati, posizionali in:
# /etc/ssl/certs/api.tuodominio.com.crt
# /etc/ssl/private/api.tuodominio.com.key
# /etc/ssl/certs/api.tuodominio.com-chain.crt
```

#### 5. Configura Servizi Backend
Aggiorna la configurazione virtual host per il tuo setup backend:

```apache
# Setup sviluppo (backend singolo)
ProxyPass /api/ http://127.0.0.1:8081/api/
ProxyPassReverse /api/ http://127.0.0.1:8081/api/

# Setup produzione (load balanced)
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

#### 6. Avvia Apache
```bash
# Testa configurazione
sudo apache2ctl configtest

# Avvia Apache
sudo systemctl start apache2
sudo systemctl enable apache2

# Ricarica configurazione (dopo modifiche)
sudo systemctl reload apache2
```

### Script di Deployment Automatico

Per semplificare l'installazione, usa lo script automatico incluso:

```bash
# Installazione completa
sudo ./apache/deploy.sh --domain api.tuodominio.com install

# Solo configurazione
sudo ./apache/deploy.sh configure

# Controllo stato
./apache/deploy.sh status
```

### Personalizzazione Configurazione

#### Configurazione Dominio
Aggiorna `apache/vhosts.conf` con il tuo dominio:
```apache
ServerName api.tuodominio.com
ServerAlias pocket-api.tuodominio.com
```

#### Configurazione CORS
Regola le impostazioni CORS per i tuoi domini frontend:
```apache
SetEnvIf Origin "^https?://(www\.)?tuodominio\.com$" AccessControlAllowOrigin=$0
Header always set Access-Control-Allow-Origin %{AccessControlAllowOrigin}e env=AccessControlAllowOrigin
```

#### Configurazione Backend
Aggiorna endpoint backend e health check:
```apache
# Cluster backend
<Proxy balancer://pocket-backend>
    BalancerMember http://backend1.internal:8081 route=backend1
    BalancerMember http://backend2.internal:8081 route=backend2
    ProxySet hcuri /actuator/health
</Proxy>
```

### Monitoraggio e Gestione

#### Stato Load Balancer
Accedi al manager balancer su:
```
https://api.tuodominio.com/balancer-manager
```

#### File di Log
- **Access Log**: `/var/log/apache2/pocket-prod_access.log`
- **Error Log**: `/var/log/apache2/pocket-prod_error.log`
- **SSL Log**: `/var/log/apache2/ssl_access.log`

#### Monitoraggio Health
```bash
# Controlla stato servizio
systemctl status apache2

# Monitora log in tempo reale
tail -f /var/log/apache2/pocket-prod_access.log

# Testa configurazione SSL
openssl s_client -connect api.tuodominio.com:443

# Testa load balancer
curl -H "Host: api.tuodominio.com" https://localhost/api/v5/health
```

### Documentazione Completa

Per istruzioni dettagliate, troubleshooting e configurazioni avanzate, consulta:
- `docs/APACHE_SETUP.md` - Guida completa alla configurazione Apache

## 🐳 Deployment Docker

### Deployment Rapido
```bash
# Tutto in un comando
./build_docker_image.sh

# O manualmente
docker compose up -d
```

### Configurazione Apache HTTP (Produzione)

Per deployment in produzione con Apache HTTP Server come reverse proxy:

```bash
# Avvia con profilo produzione (include reverse proxy)
docker compose --profile production up -d
```

### Configurazione Ambiente

#### Sviluppo (.env)
```bash
# Database
DB_USERNAME=pocket_user
DB_PASSWORD=dev_password_123
DB_ROOT_PASSWORD=dev_root_password_123

# Sicurezza (CAMBIA QUESTI VALORI!)
AES_CBC_IV=dev_iv_16_chars!!
ADMIN_USER=admin
ADMIN_PASSWD=dev_admin_123

# Server
SERVER_URL=http://localhost:8081
CORS_ADDITIONAL_ORIGINS=http://localhost:3000,http://localhost:8080

# JVM
JVM_MAX_MEMORY=1g
JVM_MIN_MEMORY=512m
```

#### Produzione (.env.production)
```bash
# Database
DB_USERNAME=pocket_prod_user
DB_PASSWORD=prod_super_secure_password_2024!
DB_ROOT_PASSWORD=prod_root_super_secure_password_2024!

# Sicurezza (GENERA NUOVI VALORI!)
AES_CBC_IV=prod_secure_16_iv!
ADMIN_USER=admin
ADMIN_PASSWD=prod_admin_super_secure_password_2024!

# Server
SERVER_URL=https://api.tuodominio.com:8081
CORS_ADDITIONAL_ORIGINS=https://tuodominio.com,https://app.tuodominio.com

# JVM
JVM_MAX_MEMORY=2g
JVM_MIN_MEMORY=1g
```

### Gestione Container

#### Comandi Base
```bash
# Avvia servizi
docker compose up -d

# Visualizza status
docker compose ps

# Visualizza logs
docker compose logs -f

# Riavvia servizio specifico
docker compose restart pocket-backend

# Ferma tutti i servizi
docker compose down

# Ferma e rimuovi volumi (ATTENZIONE: cancella i dati!)
docker compose down -v
```

#### Monitoraggio
```bash
# Verifica health checks
docker compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"

# Utilizzo risorse
docker stats

# Logs in tempo reale
docker compose logs -f pocket-backend
docker compose logs -f pocket-db
```

### Backup e Restore

#### Backup Database
```bash
# Backup automatico (eseguito dallo script)
docker compose exec pocket-db mysqldump -u root -p pocket5 > backup_$(date +%Y%m%d_%H%M%S).sql

# Backup con compressione
docker compose exec pocket-db mysqldump -u root -p pocket5 | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz

# Backup automatizzato (crontab)
0 2 * * * cd /path/to/pocket-backend && docker compose exec -T pocket-db mysqldump -u root -p"$DB_ROOT_PASSWORD" pocket5 | gzip > backups/backup_$(date +\%Y\%m\%d_\%H\%M\%S).sql.gz
```

#### Restore Database
```bash
# Restore da backup
docker compose exec -i pocket-db mysql -u root -p pocket5 < backup.sql

# Restore da backup compresso
zcat backup.sql.gz | docker compose exec -i pocket-db mysql -u root -p pocket5

# Restore con reset completo
docker compose down -v
docker compose up -d pocket-db
sleep 30
docker compose exec -i pocket-db mysql -u root -p pocket5 < backup.sql
docker compose up -d
```

### SSL/HTTPS Setup

#### Configurazione Nginx per Produzione
```bash
# Crea directory SSL
mkdir -p nginx/ssl

# Genera certificato self-signed (per test)
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout nginx/ssl/key.pem \
  -out nginx/ssl/cert.pem

# O copia certificati Let's Encrypt
cp /etc/letsencrypt/live/tuodominio.com/fullchain.pem nginx/ssl/cert.pem
cp /etc/letsencrypt/live/tuodominio.com/privkey.pem nginx/ssl/key.pem
```

#### Configurazione Nginx (nginx/nginx.conf)
```nginx
server {
    listen 443 ssl http2;
    server_name api.tuodominio.com;
    
    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    
    location / {
        proxy_pass http://pocket-backend:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 🐛 Risoluzione Problemi

### Problemi Comuni

#### 🔐 Errori di Autenticazione
```bash
# Controlla logs di autenticazione
docker compose logs pocket-backend | grep -i auth

# Verifica variabili d'ambiente
docker compose exec pocket-backend env | grep -E "(AES_CBC_IV|ADMIN_USER|DB_)"

# Testa autenticazione admin
curl -u admin:tua_password_admin http://localhost:8081/actuator/health

# Controlla logs del filtro di sicurezza
docker compose logs pocket-backend | grep "AuthenticationFilter"
```

#### 🗄️ Problemi di Connessione Database
```bash
# Testa connettività database dal container app
docker compose exec pocket-backend nc -zv pocket-db 3306

# Controlla logs database
docker compose logs pocket-db

# Testa connessione diretta al database
docker compose exec pocket-db mysql -u root -p -e "SHOW DATABASES;"

# Verifica inizializzazione database
docker compose exec pocket-db mysql -u root -p pocket5 -e "SHOW TABLES;"
```

#### 🌐 Problemi CORS
```bash
# Testa richiesta CORS preflight
curl -H "Origin: https://tuodominio.com" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: X-Requested-With" \
     -X OPTIONS \
     http://localhost:8081/api/v5/test

# Controlla logs configurazione CORS
docker compose logs pocket-backend | grep -i cors

# Verifica configurazione URL server
docker compose exec pocket-backend env | grep SERVER_URL
```

#### 🔧 Problemi di Configurazione
```bash
# Valida configurazione Spring
docker compose exec pocket-backend java -jar /var/www/pocket.jar --spring.config.location=classpath:application.yaml --debug

# Controlla proprietà applicazione
docker compose exec pocket-backend cat /var/www/scripts/pocket5-config.yaml

# Verifica mount dei volumi
docker compose config
```

#### 🚫 Problemi SSL/TLS (Produzione)
```bash
# Testa certificato SSL
openssl x509 -in nginx/ssl/cert.pem -text -noout

# Verifica configurazione SSL
docker compose exec nginx nginx -t

# Controlla logs SSL
docker compose logs nginx | grep -i ssl
```

### Problemi di Performance

#### 📊 Memoria e CPU
```bash
# Controlla utilizzo risorse container
docker stats

# Regola impostazioni memoria JVM nel .env
JVM_MAX_MEMORY=1g
JVM_MIN_MEMORY=512m

# Controlla utilizzo heap Java
docker compose exec pocket-backend jcmd 1 VM.flags
```

#### 🔍 Performance Database
```bash
# Controlla connessioni database
docker compose exec pocket-db mysql -u root -p -e "SHOW PROCESSLIST;"

# Monitora query database
docker compose logs pocket-db --tail=100

# Controlla dimensione database
docker compose exec pocket-db mysql -u root -p -e "SELECT table_schema AS 'Database', ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'DB Size in MB' FROM information_schema.tables GROUP BY table_schema;"
```

### Validazione Configurazione

#### 🔐 Checklist Sicurezza
```bash
# Verifica che tutte le password siano cambiate dai default
grep -r "change_me\|default\|admin123" .env

# Controlla lunghezza AES IV (deve essere esattamente 16 caratteri)
echo ${AES_CBC_IV} | wc -c  # Dovrebbe restituire 17 (16 + newline)

# Verifica validazione formato UUID
curl -X GET "http://localhost:8081/api/v5/invalid-uuid/test"  # Dovrebbe restituire 400

# Testa sicurezza endpoint admin
curl http://localhost:8081/actuator/metrics  # Dovrebbe restituire 401
```

#### 🌐 Connettività Rete
```bash
# Testa rete interna Docker
docker compose exec pocket-backend ping pocket-db
docker compose exec pocket-backend nslookup pocket-db

# Controlla accessibilità porte
telnet localhost 8081
telnet localhost 3306

# Verifica rete Docker
docker network ls
docker network inspect pocket-network
```

### Analisi Log

#### 📝 Log Applicazione
```bash
# Visualizza log strutturati
docker compose logs pocket-backend --tail=100 -f

# Filtra per livello di log
docker compose logs pocket-backend | grep -E "(ERROR|WARN)"

# Log relativi alla sicurezza
docker compose logs pocket-backend | grep -E "(AUTH|SECURITY|CORS)"

# Log di performance
docker compose logs pocket-backend | grep -E "(SLOW|TIMEOUT|PERFORMANCE)"
```

#### 🔍 Log Database
```bash
# Log errori MySQL
docker compose logs pocket-db | grep -i error

# Log connessioni
docker compose logs pocket-db | grep -i connect

# Log query lente (se abilitato)
docker compose exec pocket-db mysql -u root -p -e "SET GLOBAL slow_query_log = 'ON';"
```

### Procedure di Recovery

#### 🔄 Recovery Servizi
```bash
# Riavvia servizio specifico
docker compose restart pocket-backend
docker compose restart pocket-db

# Riavvio completo sistema
docker compose down
docker compose up -d

# Reset con database pulito
docker compose down -v  # ATTENZIONE: Cancella tutti i dati
docker compose up -d
```

#### 💾 Recovery Dati
```bash
# Ripristina database da backup
docker compose exec -i pocket-db mysql -u root -p pocket5 < backup.sql

# Ripristina volume da backup
docker run --rm -v pocket_db_data:/data -v $(pwd):/backup ubuntu tar xzf /backup/db_backup.tar.gz -C /
```

## 📊 Monitoraggio & Health Check

### Health Applicazione
```bash
# Health check base
curl http://localhost:8081/actuator/health

# Health dettagliato con autenticazione
curl -u admin:tua_password_admin http://localhost:8081/actuator/health/details

# Metriche applicazione
curl -u admin:tua_password_admin http://localhost:8081/actuator/metrics

# Informazioni ambiente
curl -u admin:tua_password_admin http://localhost:8081/actuator/env
```

### Health Database
```bash
# Controlla stato database
docker compose exec pocket-db mysqladmin -u root -p status

# Connessioni database
docker compose exec pocket-db mysql -u root -p -e "SHOW STATUS LIKE 'Connections'"

# Monitoraggio dimensione database
docker compose exec pocket-db mysql -u root -p -e "
SELECT 
    table_schema AS 'Database',
    ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)'
FROM information_schema.tables 
WHERE table_schema = 'pocket5'
GROUP BY table_schema;"
```

### Health Container
```bash
# Status container e utilizzo risorse
docker compose ps
docker stats

# Controlla log container
docker compose logs --tail=50 -f

# Riavvia container non salutari
docker compose restart $(docker compose ps -q --filter "health=unhealthy")
```

### Monitoraggio Performance
```bash
# Utilizzo memoria JVM
docker compose exec pocket-backend jcmd 1 GC.run_finalization
docker compose exec pocket-backend jcmd 1 VM.memory_summary

# Tempi di risposta applicazione
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8081/api/v5/health

# Performance database
docker compose exec pocket-db mysql -u root -p -e "SHOW GLOBAL STATUS LIKE 'Slow_queries'"
```

## 🛡️ Funzionalità di Sicurezza

### Autenticazione & Autorizzazione
- **Filtro Autenticazione Personalizzato**: Autenticazione basata su token RSA
- **Controllo Accesso Basato su Ruoli**: Ruoli admin e utente con permessi diversi  
- **Gestione Sessioni**: Gestione sicura delle sessioni con timeout configurabili
- **Validazione Input**: Bean Validation completa per tutti gli endpoint API

### Protezione Dati
- **Crittografia**: Crittografia AES-CBC per dati sensibili
- **Sicurezza Password**: Hashing BCrypt con forza configurabile
- **Sicurezza Database**: Prepared statement per prevenire SQL injection
- **Protezione CORS**: Configurazione CORS dinamica basata su URL server

### Sicurezza Infrastruttura
- **Sicurezza Container**: Esecuzione container non-root, immagini base Alpine minimali
- **Sicurezza Rete**: Isolamento rete Docker, esposizione porte configurabile
- **SSL/TLS**: Supporto HTTPS con configurazione reverse proxy
- **Variabili d'Ambiente**: Configurazione sensibile esternalizzata dal codice

### Header di Sicurezza
- **X-Content-Type-Options**: nosniff
- **X-Frame-Options**: DENY
- **X-XSS-Protection**: 1; mode=block
- **Cache-Control**: no-cache, no-store, must-revalidate
- **Strict-Transport-Security**: max-age=31536000; includeSubDomains

### Audit & Logging
- **Eventi di Sicurezza**: Tentativi di autenticazione, errori di autorizzazione
- **Log Accessi**: Tutti gli accessi API con identificazione utente
- **Tracciamento Errori**: Log errori dettagliati senza esposizione dati sensibili
- **Monitoraggio Performance**: Tracciamento tempi di risposta e utilizzo risorse

## 🚀 Endpoint API

### Autenticazione
- `POST /api/v5/login` - Autenticazione utente
- `POST /api/v5/logout` - Logout utente

### Gestione Sessioni
- `GET /api/v5/session/{uuid}/{crypt}` - Ottieni dati sessione
- `POST /api/v5/session` - Crea nuova sessione
- `PUT /api/v5/session/{uuid}` - Aggiorna sessione
- `DELETE /api/v5/session/{uuid}` - Elimina sessione

### Monitoraggio Health
- `GET /actuator/health` - Status health applicazione
- `GET /actuator/info` - Informazioni applicazione
- `GET /actuator/metrics` - Metriche applicazione (richiede autenticazione)

## 👥 Gestione Utenti e Dispositivi

Per accedere al server, è necessario registrare utenti e dispositivi utilizzando gli strumenti CLI da [pocket-cli](https://github.com/passy1977/pocket-cli).

### Gestione Utenti
```bash
# Aggiungi nuovo utente
pocket-user add -e utente@esempio.com -p password_utente -n "Nome Utente"

# Modifica utente
pocket-user mod -e utente@esempio.com -p nuova_password -n "Nuovo Nome"

# Rimuovi utente
pocket-user rm -e utente@esempio.com

# Ottieni informazioni utente
pocket-user get -e utente@esempio.com
```

### Gestione Dispositivi
```bash
# Aggiungi nuovo dispositivo
pocket-device add -e utente@esempio.com -d "Nome Dispositivo"

# Elenca dispositivi utente
pocket-device list -e utente@esempio.com

# Rimuovi dispositivo
pocket-device rm -e utente@esempio.com -u uuid_dispositivo

# Ottieni QR code dispositivo per setup client
pocket-device qr -e utente@esempio.com -u uuid_dispositivo
```

## 📡 Esempi di Utilizzo API

### Autenticazione
Tutte le chiamate API richiedono token di autenticazione crittografati RSA nel percorso URL:

```
GET /api/v5/{uuid}/{token_crittografato}
POST /api/v5/{uuid}/{token_crittografato}
PUT /api/v5/{uuid}/{token_crittografato}/{parametri_aggiuntivi}
DELETE /api/v5/{uuid}/{token_crittografato}
```

### Esempi di Chiamate API

```bash
# Ottieni dati (sostituisci con UUID e token crittografato reali)
curl -X GET "https://api.tuodominio.com:8081/api/v5/12345678-1234-1234-1234-123456789abc/token_auth_crittografato"

# Invia dati
curl -X POST "https://api.tuodominio.com:8081/api/v5/12345678-1234-1234-1234-123456789abc/token_auth_crittografato" \
  -H "Content-Type: application/json" \
  -d '{"groups":[],"groupFields":[],"fields":[]}'

# Controlla sessione
curl -X GET "https://api.tuodominio.com:8081/api/v5/12345678-1234-1234-1234-123456789abc/token_auth_crittografato/check"
```

## 🔍 Monitoraggio e Health Check

### Endpoint Health
```bash
# Health applicazione
curl http://localhost:8081/actuator/health

# Informazioni applicazione
curl http://localhost:8081/actuator/info

# Metriche (richiede autenticazione admin)
curl -u admin:tua_password_admin http://localhost:8081/actuator/metrics
```

### Logging
I log dell'applicazione includono:
- Tentativi di autenticazione e fallimenti
- Errori di validazione input
- Eventi di sicurezza
- Operazioni database
- Richieste CORS

## 🚨 Best Practice di Sicurezza

### Prima del Deployment in Produzione

1. **Cambia Tutte le Password Default**
   ```bash
   # Non usare mai valori default in produzione
   export AES_CBC_IV="tuo_iv_unico_16_char"
   export ADMIN_PASSWD="password_admin_molto_sicura"
   export DB_PASSWORD="password_database_molto_sicura"
   ```

2. **Abilita HTTPS**
   - Configura certificati SSL
   - Imposta `server.ssl.enabled=true`
   - Usa impostazioni cookie sicure

3. **Sicurezza Database**
   - Usa utente database dedicato con privilegi minimi
   - Abilita SSL per connessioni database
   - Rotazione password regolare

4. **Sicurezza Rete**
   - Usa firewall per limitare accesso
   - VPN per accesso admin
   - Aggiornamenti di sicurezza regolari

5. **Monitoraggio**
   - Configura aggregazione log
   - Monitora tentativi di autenticazione falliti
   - Alert su eventi di sicurezza

## 📚 Risorse Aggiuntive

- [Documentazione Sicurezza](SECURITY.md) - Implementazione sicurezza dettagliata
- [Riferimento API](https://deepwiki.com/passy1977/pocket-lib) - Documentazione API completa
- [Applicazioni Client](https://github.com/passy1977/pocket-ios) - Implementazione client iOS
- [Strumenti CLI](https://github.com/passy1977/pocket-cli) - Strumenti gestione utenti e dispositivi
- [Configurazione Apache HTTP](docs/APACHE_SETUP.md) - Guida completa setup Apache

## 📄 Licenza

Questo programma è software libero: puoi ridistribuirlo e/o modificarlo sotto i termini della GNU General Public License come pubblicata dalla Free Software Foundation, versione 3 della Licenza.

---

**Fatto con ❤️ in Italia** | [🇬🇧 English](README.md) | 🇮🇹 **Italiano**

**⚠️ Importante**: Cambia sempre le password e i valori di configurazione default prima del deployment in produzione!