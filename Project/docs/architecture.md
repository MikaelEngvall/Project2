# DFRM - Systemarkitektur

## 🏗️ Övergripande Arkitektur

```mermaid
graph TB
    subgraph "Frontend (Next.js 14)"
        A[React App] --> B[API Client]
        B --> C[Auth Context]
        B --> D[React Query]
        D --> E[Cache Layer]
    end
    
    subgraph "Backend (Spring Boot 3)"
        F[Controllers] --> G[Services]
        G --> H[Repositories]
        H --> I[Database]
        G --> J[Email Service]
        G --> K[Audit Service]
    end
    
    subgraph "External Services"
        L[PostgreSQL]
        M[Redis Cache]
        N[Email Servers]
        O[Google Translate API]
    end
    
    A --> F
    I --> L
    E --> M
    J --> N
    G --> O
```

## 🔐 Säkerhetsarkitektur

```mermaid
graph LR
    subgraph "Client Layer"
        A[Browser] --> B[OAuth2 PKCE]
    end
    
    subgraph "API Gateway"
        C[Rate Limiting] --> D[CORS]
        D --> E[Input Validation]
    end
    
    subgraph "Authentication"
        F[JWT Tokens] --> G[Role-based Access]
        G --> H[Permission Matrix]
    end
    
    subgraph "Data Layer"
        I[SQL Injection Protection] --> J[Encrypted Storage]
        J --> K[Audit Logging]
    end
    
    B --> C
    E --> F
    H --> I
```

## 📊 Databasarkitektur

```mermaid
erDiagram
    USER {
        uuid id PK
        string firstName
        string lastName
        string email
        enum role
        string preferredLanguage
        boolean isActive
        json permissions
        string phone
        timestamp createdAt
        timestamp updatedAt
    }
    
    APARTMENT {
        uuid id PK
        string street
        string number
        string apartmentNumber
        integer size
        integer floor
        string area
        integer rooms
        decimal monthlyRent
        string postalCode
        boolean occupied
        timestamp createdAt
        timestamp updatedAt
    }
    
    TENANT {
        uuid id PK
        string firstName
        string lastName
        string email
        string phone
        uuid apartmentId FK
        date moveInDate
        date moveOutDate
        enum status
        timestamp createdAt
        timestamp updatedAt
        timestamp deletedAt
    }
    
    FORMER_TENANT {
        uuid id PK
        string name
        string email
        string phone
        uuid apartmentId FK
        date moveInDate
        date moveOutDate
        timestamp createdAt
        timestamp updatedAt
    }
    
    INTEREST {
        uuid id PK
        string name
        string email
        string phone
        uuid apartmentId FK
        text message
        enum status
        date applicationDate
        date viewingDate
        time viewingTime
        string viewingHost
        text notes
        timestamp createdAt
        timestamp updatedAt
        timestamp deletedAt
    }
    
    ISSUE {
        uuid id PK
        string title
        text description
        enum status
        enum priority
        date dueDate
        integer estimatedHours
        uuid apartmentId FK
        uuid assigneeId FK
        string reporterName
        string reporterEmail
        string reporterPhone
        timestamp createdAt
        timestamp updatedAt
        timestamp deletedAt
    }
    
    TASK {
        uuid id PK
        string title
        text description
        enum status
        enum priority
        date dueDate
        integer estimatedHours
        uuid apartmentId FK
        uuid assignedToId FK
        uuid createdById FK
        timestamp createdAt
        timestamp updatedAt
    }
    
    TASK_COMMENT {
        uuid id PK
        uuid taskId FK
        text content
        uuid createdById FK
        timestamp createdAt
        timestamp updatedAt
    }
    
    KEY {
        uuid id PK
        uuid apartmentId FK
        enum keyType
        string location
        enum status
        timestamp createdAt
        timestamp updatedAt
    }
    
    AUDIT_LOG {
        uuid id PK
        uuid userId FK
        string action
        string entity
        uuid entityId
        string ipAddress
        timestamp timestamp
    }
    
    NOTIFICATION_LOG {
        uuid id PK
        uuid userId FK
        string event
        string channel
        enum status
        timestamp timestamp
    }
    
    APARTMENT ||--o{ TENANT : "has"
    APARTMENT ||--o{ FORMER_TENANT : "had"
    APARTMENT ||--o{ INTEREST : "receives"
    APARTMENT ||--o{ ISSUE : "has"
    APARTMENT ||--o{ TASK : "has"
    APARTMENT ||--o{ KEY : "has"
    USER ||--o{ TENANT : "assigns"
    USER ||--o{ ISSUE : "assigns"
    USER ||--o{ TASK : "assigns"
    USER ||--o{ TASK_COMMENT : "creates"
    USER ||--o{ AUDIT_LOG : "logs"
    USER ||--o{ NOTIFICATION_LOG : "receives"
    TASK ||--o{ TASK_COMMENT : "has"
    ISSUE ||--|| TASK : "converts_to"
```

## 🔄 Dataflöde

### Hyresgästhantering
```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant B as Backend
    participant D as Database
    participant E as Email Service
    
    U->>F: Skapa hyresgäst
    F->>B: POST /api/tenants
    B->>D: Spara hyresgäst
    B->>D: Uppdatera lägenhet (occupied=true)
    B->>E: Skicka välkomstmail
    B->>F: Returnera hyresgästdata
    F->>U: Visa bekräftelse
    
    Note over U,E: Uppsägning
    U->>F: Säg upp hyresgäst
    F->>B: POST /api/tenants/{id}/terminate
    B->>D: Uppdatera status till TERMINATED_NOT_MOVED_OUT
    B->>D: Sätt moveOutDate (3 månader framåt)
    B->>E: Skicka uppsägningsmail
    B->>F: Returnera uppdaterad data
    F->>U: Visa bekräftelse
```

### Felanmälningsflöde
```mermaid
sequenceDiagram
    participant E as Email
    participant B as Backend
    participant D as Database
    participant U as User
    participant F as Frontend
    
    E->>B: Ny e-post till felanmalan@
    B->>B: Parsa adress från e-post
    B->>D: Skapa felanmälan (status=NEW)
    B->>D: Koppla till lägenhet
    B->>F: Real-time update
    
    U->>F: Godkänn felanmälan
    F->>B: POST /api/issues/{id}/approve
    B->>D: Uppdatera status till APPROVED
    B->>D: Skapa task från felanmälan
    B->>B: Skicka bekräftelsemail
    B->>F: Returnera uppdaterad data
    F->>U: Visa bekräftelse
```

### Intresseanmälningsflöde
```mermaid
sequenceDiagram
    participant E as Email
    participant B as Backend
    participant D as Database
    participant U as User
    participant F as Frontend
    
    E->>B: Ny e-post till intresse@
    B->>B: Parsa adress från e-post
    B->>D: Skapa intresseanmälan
    B->>D: Koppla till lägenhet
    B->>F: Real-time update
    
    U->>F: Boka visning
    F->>B: POST /api/interests/{id}/book-viewing
    B->>D: Uppdatera viewingDate och viewingTime
    B->>B: Skicka bekräftelsemail
    B->>F: Returnera uppdaterad data
    F->>U: Visa bekräftelse
```

## 🎯 Komponentarkitektur

### Frontend-komponenter
```mermaid
graph TD
    subgraph "Layout Components"
        A[DashboardLayout] --> B[HeaderBar]
        A --> C[Sidebar]
        A --> D[Footer]
    end
    
    subgraph "Shared Components"
        E[StandardModal] --> F[Table]
        E --> G[Form]
        E --> H[Toast]
    end
    
    subgraph "Feature Components"
        I[ApartmentList] --> J[ApartmentCard]
        I --> K[ApartmentModal]
        L[TenantList] --> M[TenantCard]
        L --> N[TerminationModal]
        O[InterestList] --> P[InterestCard]
        O --> Q[ViewingModal]
        R[IssueList] --> S[IssueCard]
        R --> T[ApproveModal]
        U[TaskList] --> V[TaskCard]
        U --> W[CommentModal]
    end
    
    subgraph "Context Providers"
        X[AuthContext] --> Y[ThemeContext]
        Y --> Z[LanguageContext]
    end
```

### Backend-lager
```mermaid
graph TD
    subgraph "Controller Layer"
        A[REST Controllers] --> B[Request Validation]
        A --> C[Response Mapping]
    end
    
    subgraph "Service Layer"
        D[Business Services] --> E[Email Service]
        D --> F[Audit Service]
        D --> G[Validation Service]
    end
    
    subgraph "Repository Layer"
        H[Data Repositories] --> I[Custom Queries]
        H --> J[Transaction Management]
    end
    
    subgraph "Model Layer"
        K[Domain Models] --> L[JPA Entities]
        K --> M[DTOs]
    end
    
    A --> D
    D --> H
    H --> K
```

## 🔧 Teknisk Stack Detaljer

### Frontend Stack
- **Next.js 14**: App Router, Server Components
- **TypeScript 5.0+**: Strict mode, explicit typing
- **Tailwind CSS**: Utility-first styling
- **shadcn/ui**: Komponentbibliotek
- **React Query**: Server state management
- **React Hook Form**: Formulärhantering
- **Zod**: Schema validation

### Backend Stack
- **Spring Boot 3**: Framework
- **Java 17**: LTS version
- **Spring Security**: Autentisering och auktorisering
- **Spring Data JPA**: Databasåtkomst
- **Hibernate**: ORM
- **Flyway**: Databasmigrationer
- **JavaMail**: E-posthantering

### Databas och Cache
- **PostgreSQL 14+**: Primärdatabas
- **Redis**: Caching och session storage
- **UUID**: Primärnycklar
- **Soft Delete**: deleted_at för känsliga entiteter

### Deployment och DevOps
- **Vercel**: Frontend hosting
- **Render.com**: Backend hosting
- **GitHub Actions**: CI/CD
- **Docker**: Containerisering (valfritt)

## 📊 Performance-arkitektur

### Caching-strategi
```mermaid
graph LR
    A[Client Request] --> B{Redis Cache?}
    B -->|Hit| C[Return Cached Data]
    B -->|Miss| D[Database Query]
    D --> E[Cache Result]
    E --> F[Return Data]
    C --> G[Client Response]
    F --> G
```

### Database-optimering
- **Indexes**: Strategiska indexes för sökningar
- **Connection Pooling**: HikariCP
- **Query Optimization**: N+1 problem prevention
- **Partitioning**: För stora tabeller (framtida)

### Frontend-optimering
- **Code Splitting**: Lazy loading av komponenter
- **Bundle Optimization**: Tree shaking
- **Image Optimization**: Next.js Image component
- **CDN**: Vercel Edge Network

## 🔒 Säkerhetsarkitektur

### Autentisering
- **OAuth2 PKCE**: Authorization Code Flow
- **JWT Tokens**: Rotatable refresh tokens
- **HttpOnly Cookies**: Säker token storage
- **Session Management**: Timeout och re-auth

### Autorisering
- **Role-based Access Control**: USER, ADMIN, SUPERADMIN
- **Permission Matrix**: Granular behörigheter
- **API Security**: CORS, Rate limiting
- **Input Validation**: Strikt validering

### Dataskydd
- **Encryption**: At rest och in transit
- **Audit Logging**: Alla känsliga operationer
- **Data Sanitization**: Input och output encoding
- **Vulnerability Scanning**: Automatisk säkerhetsskanning

## 📈 Monitoring och Logging

### Logging-strategi
- **Structured Logging**: JSON-format
- **Log Levels**: ERROR, WARN, INFO, DEBUG
- **Centralized Logging**: Samlad logghantering
- **Performance Monitoring**: Response times

### Health Checks
- **Database Connectivity**: Databasanslutning
- **External Services**: E-post, API-nycklar
- **Memory Usage**: JVM heap monitoring
- **Response Times**: API performance

## 🚀 Deployment-arkitektur

### Miljöer
```mermaid
graph LR
    subgraph "Development"
        A[Local Development] --> B[Local Database]
        A --> C[Local Redis]
    end
    
    subgraph "Staging"
        D[Render Backend] --> E[Render PostgreSQL]
        F[Vercel Frontend] --> D
    end
    
    subgraph "Production"
        G[Render Backend] --> H[Render PostgreSQL]
        I[Vercel Frontend] --> G
        J[CDN] --> I
    end
```

### CI/CD Pipeline
```mermaid
graph LR
    A[Code Push] --> B[GitHub Actions]
    B --> C[Run Tests]
    C --> D[Build Application]
    D --> E[Security Scan]
    E --> F[Deploy to Staging]
    F --> G[Integration Tests]
    G --> H[Deploy to Production]
```

---

**Senaste uppdatering**: 2024-12-19  
**Version**: 4.1.0  
**Status**: Under utveckling 