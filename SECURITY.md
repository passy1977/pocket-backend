# 🔒 SICUREZZA - CONFIGURAZIONI SPRING SECURITY

## ✅ Implementazioni di Sicurezza Aggiunte

### 1. **Spring Security Configuration**
- **File**: `SecurityConfig.java`
- **Funzionalità**:
  - Autenticazione HTTP Basic per endpoints amministrativi
  - Filtro personalizzato per autenticazione API basata su UUID/crypt
  - Configurazione CORS sicura
  - Headers di sicurezza (HSTS, XSS Protection, Frame Options)
  - Gestione sessioni stateless per API REST

### 2. **Filtro di Autenticazione Personalizzato**
- **File**: `AuthenticationFilter.java`
- **Funzionalità**:
  - Validazione formato UUID e crypt
  - Decrittografia RSA sicura
  - Verifica credenziali utente
  - Protezione contro attacchi di replay
  - Logging di tentativi di accesso

### 3. **Validazione Input Robusta**
- **File**: `SessionRest.java` (aggiornato)
- **Funzionalità**:
  - Validazione regex per UUID
  - Validazione lunghezza e caratteri per parametri crypt
  - Annotazioni Bean Validation
  - Sanitizzazione input automatica

### 4. **Configurazione CORS Dinamica**
- **File**: `SecurityConfig.java` (aggiornato)
- **Funzionalità**:
  - CORS basato su `server.url` dalla configurazione
  - Supporto automatico HTTP/HTTPS
  - Origini aggiuntive configurabili tramite `security.cors.additional-origins`
  - Localhost permesso per sviluppo

### 5. **Gestione Globale delle Eccezioni**
- **File**: `GlobalExceptionHandler.java`
- **Funzionalità**:
  - Gestione errori di validazione
  - Risposte di errore standardizzate
  - Protezione contro information disclosure
  - Logging sicuro degli errori

## 🔧 Configurazioni di Sicurezza

### Headers di Sicurezza Implementati:
- **HSTS**: Strict Transport Security
- **X-Frame-Options**: DENY
- **X-Content-Type-Options**: nosniff
- **X-XSS-Protection**: Abilitata
- **Referrer-Policy**: strict-origin-when-cross-origin

### Protezioni Implementate:
- ✅ **Input Validation**: Regex patterns per UUID e crypt
- ✅ **CSRF Protection**: Disabilitato per API REST
- ✅ **CORS Configuration**: Configurazione sicura
- ✅ **Session Management**: Stateless per API
- ✅ **Error Handling**: Gestione sicura degli errori
- ✅ **Authentication**: Filtro personalizzato
- ✅ **Authorization**: Controllo accesso basato su ruoli

## ⚠️ AZIONI RICHIESTE PRIMA DEL DEPLOYMENT

### 1. **Configurare Variabili d'Ambiente**
```bash
export DB_USERNAME="your_db_user"
export DB_PASSWORD="your_secure_db_password"
export AES_CBC_IV="your_16_char_iv_key"
export ADMIN_USER="your_admin_username"
export ADMIN_PASSWD="your_secure_admin_password"
```

### 2. **Aggiornare application.yaml per Produzione**
```yaml
# Configurazione server
server:
  url: https://your-production-domain.com:8081

# Configurazione sicurezza
security:
  cors:
    additional-origins: https://your-frontend-domain.com,https://admin.yourdomain.com

# Rimuovere o cambiare valori di default
server:
  aes.cbc.iv: ${AES_CBC_IV}
  auth:
    user: ${ADMIN_USER}
    passwd: ${ADMIN_PASSWD}

spring:
  datasource:
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### 3. **Configurazioni Aggiuntive Raccomandate**

#### SSL/TLS (HTTPS)
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
```

#### Rate Limiting (Aggiungere dipendenza)
```xml
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-spring-boot-starter</artifactId>
    <version>0.10.1</version>
</dependency>
```

#### Database Security
- Usare pool di connessioni sicure
- Configurare SSL per connessioni DB
- Implementare rotation delle password

## 🔍 Controlli di Sicurezza

### Endpoint Pubblici:
- `/actuator/health`
- `/actuator/info`

### Endpoint Protetti (HTTP Basic):
- `/actuator/**` (richiede ruolo ADMIN)

### Endpoint API Protetti (Custom Auth):
- `/api/v5/**` (richiede autenticazione UUID/crypt valida)

## 📊 Monitoraggio e Logging

### Logs di Sicurezza:
- Tentativi di autenticazione falliti
- Errori di validazione input
- Accessi non autorizzati
- Eccezioni di sicurezza

### Metriche da Monitorare:
- Numero di tentativi di autenticazione falliti
- Tempo di risposta degli endpoint
- Utilizzo delle sessioni
- Errori di validazione

## 🚨 Raccomandazioni Aggiuntive

1. **Implementare Rate Limiting** per prevenire attacchi brute force
2. **Configurare WAF** (Web Application Firewall)
3. **Audit Logging** per compliance e forensics
4. **Penetration Testing** regolari
5. **Dependency Scanning** per vulnerabilità note
6. **Backup e Recovery** procedures sicure

## 📋 Checklist Pre-Produzione

- [ ] Variabili d'ambiente configurate
- [ ] Credenziali di default cambiate
- [ ] SSL/HTTPS abilitato
- [ ] Database sicuro configurato
- [ ] Logging di produzione configurato
- [ ] Monitoraggio implementato
- [ ] Backup testati
- [ ] Security testing completato

---

**⚠️ IMPORTANTE**: Non utilizzare mai le configurazioni di default in produzione. Cambiare sempre password, IV, e altri segreti prima del deployment.