# ASAT Backend Architecture Diagrams

## Service Communication

```mermaid
flowchart TD
    FE[Frontend / Client] --> GW[API Gateway]

    GW --> AUTH[Auth Service]
    GW --> REG[Registration Service]
    GW --> BILL[Billing Service]
    GW --> CMS[CMS Service]
    GW --> NOTIF[Notification Service]
    GW --> PHISH[Phishing Service]

    AUTH --> REG
    AUTH --> BILL
    AUTH --> CMS
    AUTH --> NOTIF

    REG --> AUTH
    REG --> BILL
    REG --> CMS
    REG --> NOTIF
    REG --> PHISH

    BILL --> REG
    BILL --> CMS
    BILL --> NOTIF

    AUTH -.uses.-> COMMON[Common Module]
    REG -.uses.-> COMMON
    BILL -.uses.-> COMMON
    CMS -.uses.-> COMMON
    NOTIF -.uses.-> COMMON
    PHISH -.uses.-> COMMON
```

## Module Structure

```mermaid
flowchart TD
    APP[Spring Boot Application<br/>service module] --> API[api module]
    APP --> CORE[core module]
    APP --> COMMON[common:core]

    API --> DTO[DTOs / Requests / Responses / Enums]
    CORE --> CONTROLLER[Controllers]
    CORE --> SERVICE[Business Services]
    CORE --> REPO[Repositories]
    CORE --> CLIENT[Other Service Clients]
    CORE --> ENTITY[Entities / Models]

    CONTROLLER --> SERVICE
    SERVICE --> REPO
    SERVICE --> CLIENT
    CLIENT --> OTHER[Other Microservice HTTP Endpoint]
```

## Auth Login Flow

```mermaid
flowchart LR
    FE[Frontend] --> GW[Gateway<br/>/gateway/auth/**]
    GW --> LC[Auth LoginController<br/>core/controller]
    LC --> LCI[LoginControllerImpl]
    LCI --> TS[AccessTokenService]
    TS --> RC[Registration Client / WebClient]
    RC --> REGAPI[Registration Service Endpoint]
```

## Module Responsibility

```text
service module
  -> starts the Spring Boot app
  -> contains application.yml files
  -> depends on api + core + common

api module
  -> DTOs
  -> request/response classes
  -> enums/contracts

core module
  -> controllers
  -> controller implementations
  -> services
  -> repositories
  -> clients for calling other services
  -> business logic

common module
  -> shared DTOs
  -> shared exceptions
  -> utilities
  -> file/storage logic
  -> shared enums
  -> shared clients/config
```
