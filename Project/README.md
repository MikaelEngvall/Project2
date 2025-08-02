# DFRM - Duggals Fastigheter Management System

## 📋 Projektöversikt

**DFRM** (Duggals Fastigheter Management) är ett robust fastighetshanteringssystem byggt med moderna teknologier och säkerhetsförst-arkitektur. Systemet hanterar hyresgästadministration, felanmälningar, intresseanmälningar och uppgiftshantering för Duggals Fastigheter.

## 🏗️ Teknisk Stack

### Frontend
- **Framework**: Next.js 14 med App Router
- **Språk**: TypeScript 5.0+
- **Styling**: Tailwind CSS + shadcn/ui
- **State Management**: React Query (@tanstack/react-query)
- **Autentisering**: OAuth2 Authorization Code Flow med PKCE
- **Internationell**: Custom i18n system (sv, en, bg, pl, sq, uk)

### Backend
- **Framework**: Spring Boot 3
- **Språk**: Java 17
- **Databas**: PostgreSQL 14+
- **ORM**: Spring Data JPA med Hibernate
- **Migration**: Flyway
- **Autentisering**: Spring Security OAuth2
- **E-post**: JavaMail API med IMAP
- **Cache**: Redis

### Deployment
- **Frontend**: Vercel
- **Backend**: Render.com
- **Databas**: Render PostgreSQL

## 🚀 Snabbstart

### Förutsättningar
- Node.js 18+
- Java 17+
- PostgreSQL 14+
- Redis (valfritt för caching)

### Installation

1. **Klona projektet**
```bash
git clone <repository-url>
cd dfrm
```

2. **Konfigurera miljövariabler**
```bash
# Kopiera .env.example till .env och fyll i dina värden
cp .env.example .env
```

3. **Starta backend**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

4. **Starta frontend**
```bash
cd frontend
npm install
npm run dev
```

5. **Öppna applikationen**
```
http://localhost:3000
```

## 📁 Projektstruktur

```
dfrm/
├── frontend/                 # Next.js frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── dashboard/   # Dashboard layout
│   │   │   ├── pages/       # Alla sidor
│   │   │   └── login/
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── utils/
│   │   ├── types/
│   │   ├── contexts/
│   │   ├── locales/
│   │   └── lib/
├── backend/                  # Spring Boot backend
│   ├── src/main/java/se/duggals/dfrm/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── dto/
│   │   ├── config/
│   │   ├── security/
│   │   └── util/
│   └── src/main/resources/
└── docs/                     # Dokumentation
```

## 🔧 Huvudfunktioner

### 🏠 Lägenhetshantering
- CRUD-operationer för lägenheter
- Nyckelhantering
- Statusspårning (ledig/upptagen)

### 👥 Hyresgästhantering
- Hyresgästregistrering
- Uppsägning med 3-månaders uppsägningstid
- Utflyttningshantering
- Arkivering i former_tenants

### 📧 Intresseanmälningar
- Automatisk e-postintegration
- Adressparsning från e-post
- Visningsbokning med kalender
- Smart filtrering

### 🛠️ Felanmälningar
- E-postbaserad felanmälan
- Godkännande/avvisning
- Automatisk konvertering till uppgifter
- Statusspårning

### ✅ Uppgiftshantering
- Uppgiftskreation från felanmälningar
- Kommentarsystem
- Tilldelning och statusspårning
- E-postnotifieringar

## 🔒 Säkerhet

### Autentisering
- OAuth2 Authorization Code Flow med PKCE
- Rotatable refresh tokens
- HttpOnly cookies (ej localStorage)
- Session timeout

### Autorisering
- Rollbaserad åtkomstkontroll (USER, ADMIN, SUPERADMIN)
- Granular behörighetskontroll
- API-säkerhet med CORS

### Dataskydd
- Input validation
- SQL injection protection
- XSS protection
- CSRF protection
- Rate limiting

## 📊 API Endpoints

### Autentisering
- `POST /api/auth/login` - OAuth2 login
- `POST /api/auth/refresh` - Token rotation
- `POST /api/auth/logout` - Logout

### Lägenheter
- `GET /api/apartments` - Lista lägenheter
- `POST /api/apartments` - Skapa lägenhet
- `PUT /api/apartments/{id}` - Uppdatera lägenhet
- `DELETE /api/apartments/{id}` - Ta bort lägenhet

### Hyresgäster
- `GET /api/tenants` - Lista hyresgäster
- `POST /api/tenants` - Skapa hyresgäst
- `PUT /api/tenants/{id}` - Uppdatera hyresgäst
- `POST /api/tenants/{id}/terminate` - Säg upp hyresgäst
- `POST /api/tenants/{id}/move-out` - Flytta ut hyresgäst

### Intresseanmälningar
- `GET /api/interests` - Lista intresseanmälningar
- `POST /api/interests` - Skapa intresseanmälan
- `POST /api/interests/{id}/book-viewing` - Boka visning
- `POST /api/interests/check-emails` - Kontrollera e-post

### Felanmälningar
- `GET /api/issues` - Lista felanmälningar
- `POST /api/issues` - Skapa felanmälan
- `POST /api/issues/{id}/approve` - Godkänn felanmälan
- `POST /api/issues/{id}/reject` - Avvisa felanmälan

### Uppgifter
- `GET /api/tasks` - Lista uppgifter
- `POST /api/tasks` - Skapa uppgift
- `PUT /api/tasks/{id}` - Uppdatera uppgift
- `GET /api/tasks/status/{status}` - Uppgifter per status

## 🧪 Testing

### Frontend
```bash
cd frontend
npm run test          # Unit-tester
npm run test:e2e      # E2E-tester
npm run test:coverage # Coverage-rapport
```

### Backend
```bash
cd backend
mvn test              # Unit-tester
mvn test -Dtest=IntegrationTest  # Integration-tester
mvn jacoco:report     # Coverage-rapport
```

## 📈 Performance

### Mål
- Frontend: < 2s första laddning
- Backend: < 300ms API-svar
- Databas: < 100ms queries
- 99.9% uptime

### Optimeringar
- React Query caching
- Redis caching för statiska data
- Database connection pooling
- Lazy loading och code splitting

## 🚀 Deployment

### Miljöer
- **Development**: Lokal utveckling
- **Staging**: Render.com (testmiljö)
- **Production**: Render.com + Vercel

### CI/CD
- GitHub Actions för automation
- Automated testing
- Automated deployment
- Database migrations

## 📚 Dokumentation

- [API-dokumentation](docs/api.md)
- [Arkitekturdiagram](docs/architecture.md)
- [Deployment-guide](docs/deployment.md)
- [Användarmanual](docs/user-manual.md)
- [Utvecklardokumentation](docs/developer.md)

## 🔧 Utveckling

### Kodstandard
- **TypeScript**: Strict mode, explicit typing
- **Java**: Manuella getters/setters (ingen Lombok)
- **Database**: snake_case för tabeller och kolumner
- **Git**: Conventional commits

### Förbjudna patterns
- ALDRIG använda axios direkt - endast apiClient
- ALDRIG använda Lombok annotations
- ALDRIG exponera känslig data i logs
- ALDRIG använda localStorage för känslig data

## 🤝 Bidrag

1. Forka projektet
2. Skapa en feature branch (`git checkout -b feature/amazing-feature`)
3. Committa dina ändringar (`git commit -m 'Add amazing feature'`)
4. Pusha till branchen (`git push origin feature/amazing-feature`)
5. Öppna en Pull Request

## 📄 Licens

Detta projekt är proprietärt för Duggals Fastigheter.

## 📞 Support

För support eller frågor, kontakta:
- **Teknisk support**: mikael.engvall.me@gmail.com
- **Projektägare**: Duggals Fastigheter

## 📋 Senaste uppdateringar

### ✅ Slutförda komponenter
- **Backend-struktur**: Alla modeller, repositories, services och controllers implementerade
- **API-endpoints**: Komplett REST API för alla entiteter (6 controllers, 6 services, 6 repositories)
- **Databas-schema**: PostgreSQL-migrationer med indexes och constraints
- **Kompilering**: Alla 25 Java-filer kompilerar utan fel
- **Server-start**: Backend-servern startar framgångsrikt på port 8080 ✅
- **Databasmigrationer**: Flyway-migrationer fungerar korrekt (V1, V2, V3)
- **Schema-synkronisering**: JPA-konfiguration uppdaterad för automatisk schema-synkronisering
- **Säkerhetskonfiguration**: Spring Security med JWT-autentisering implementerad ✅
- **API-testning**: Alla endpoints fungerar korrekt med säkerhetskonfiguration ✅

### 🔄 Pågående utveckling
- **Frontend-implementation**: Next.js 14 med TypeScript
- **Autentiseringsflöde**: OAuth2 med PKCE för frontend
- **UI/UX**: Moderna, responsiva komponenter med Tailwind CSS
- **Integrationstestning**: Frontend-backend integration

### 📊 Teknisk status
- **Backend**: 100% komplett (server startar och fungerar)
- **API**: 100% komplett (alla endpoints implementerade och testade)
- **Databas**: 100% komplett (migrationer och schema)
- **Frontend**: 0% komplett (nästa steg)
- **Autentisering**: 90% komplett (backend klar, frontend återstår)

## 🗂️ Implementerade komponenter

### Backend-struktur (100% komplett)
- ✅ **Modeller** (6 st): User, Apartment, Tenant, Interest, Issue, Task
- ✅ **Repositories** (6 st): UserRepository, ApartmentRepository, TenantRepository, InterestRepository, IssueRepository, TaskRepository
- ✅ **Services** (6 st): UserService, ApartmentService, TenantService, InterestService, IssueService, TaskService
- ✅ **Controllers** (6 st): UserController, ApartmentController, TenantController, InterestController, IssueController, TaskController

### Datatyper och variabler

#### User-modell
```java
UUID id                    // Primärnyckel
String firstName           // Förnamn
String lastName            // Efternamn
String email               // E-postadress
UserRole role              // USER, ADMIN, SUPERADMIN
String preferredLanguage   // sv, en, bg, pl, sq, uk
Boolean isActive           // Aktiveringsstatus
String permissions         // JSON-array av behörigheter
String phone               // Telefonnummer
LocalDateTime createdAt    // Skapandedatum
LocalDateTime updatedAt    // Uppdateringsdatum
```

#### Apartment-modell
```java
UUID id                    // Primärnyckel
String street              // Gatuadress
String number              // Husnummer
String apartmentNumber     // Lägenhetsnummer
Integer size               // Storlek i m²
Integer floor              // Våning
String area                // Område
Integer rooms              // Antal rum
BigDecimal monthlyRent     // Månadshyra
String postalCode          // Postnummer
Boolean occupied           // Upptagen/ledig
LocalDateTime createdAt    // Skapandedatum
LocalDateTime updatedAt    // Uppdateringsdatum
```

#### Tenant-modell
```java
UUID id                    // Primärnyckel
String firstName           // Förnamn
String lastName            // Efternamn
String email               // E-postadress
String phone               // Telefonnummer
String personalNumber      // Personnummer
Apartment apartment        // Koppling till lägenhet
LocalDate moveInDate       // Inflyttningsdatum
LocalDate moveOutDate      // Utflyttningsdatum
BigDecimal monthlyRent     // Månadshyra
TenantStatus status        // ACTIVE, TERMINATED, TERMINATED_NOT_MOVED_OUT
String terminationReason   // Uppsägningsorsak
LocalDate terminationDate  // Uppsägningsdatum
LocalDateTime createdAt    // Skapandedatum
LocalDateTime updatedAt    // Uppdateringsdatum
```

#### Interest-modell
```java
UUID id                    // Primärnyckel
String firstName           // Förnamn
String lastName            // Efternamn
String email               // E-postadress
String phone               // Telefonnummer
Apartment apartment        // Koppling till lägenhet
InterestStatus status      // PENDING, CONFIRMED, REJECTED
LocalDate viewingDate      // Visningsdatum
String viewingTime         // Visningstid
Boolean viewingConfirmed   // Visning bekräftad
Boolean viewingEmailSent   // E-post skickad
LocalDateTime viewingEmailSentDate // E-post skickad datum
String notes               // Anteckningar
LocalDateTime createdAt    // Skapandedatum
LocalDateTime updatedAt    // Uppdateringsdatum
```

#### Issue-modell
```java
UUID id                    // Primärnyckel
String firstName           // Förnamn
String lastName            // Efternamn
String email               // E-postadress
String phone               // Telefonnummer
Apartment apartment        // Koppling till lägenhet
String subject             // Ämne
String description         // Beskrivning
IssuePriority priority     // LOW, MEDIUM, HIGH, URGENT
IssueStatus status         // NEW, APPROVED, REJECTED
LocalDate approvedDate     // Godkännandedatum
LocalDate rejectedDate     // Avvisningsdatum
String rejectionReason     // Avvisningsorsak
Boolean emailSent          // E-post skickad
LocalDateTime emailSentDate // E-post skickad datum
LocalDateTime createdAt    // Skapandedatum
LocalDateTime updatedAt    // Uppdateringsdatum
```

#### Task-modell
```java
UUID id                    // Primärnyckel
String title               // Titel
String description         // Beskrivning
Apartment apartment        // Koppling till lägenhet
User assignedUser          // Tilldelad användare
TaskPriority priority      // LOW, MEDIUM, HIGH, URGENT
TaskStatus status          // PENDING, IN_PROGRESS, COMPLETED, CANCELLED, ON_HOLD
LocalDateTime dueDate      // Förfallodatum
LocalDateTime completedDate // Slutförd datum
Double estimatedHours      // Beräknade timmar
Double actualHours         // Faktiska timmar
Double cost                // Kostnad
Boolean emailSent          // E-post skickad
LocalDateTime emailSentDate // E-post skickad datum
LocalDateTime createdAt    // Skapandedatum
LocalDateTime updatedAt    // Uppdateringsdatum
LocalDateTime deletedAt    // Borttagningsdatum (soft delete)
```

### API-endpoints (100% komplett)

#### User API (`/api/users`)
- `GET /api/users` - Lista alla användare
- `POST /api/users` - Skapa användare
- `GET /api/users/{id}` - Hämta användare
- `PUT /api/users/{id}` - Uppdatera användare
- `DELETE /api/users/{id}` - Ta bort användare
- `GET /api/users/active` - Aktiva användare
- `GET /api/users/role/{role}` - Användare per roll
- `GET /api/users/search` - Sök användare

#### Apartment API (`/api/apartments`)
- `GET /api/apartments` - Lista alla lägenheter
- `POST /api/apartments` - Skapa lägenhet
- `GET /api/apartments/{id}` - Hämta lägenhet
- `PUT /api/apartments/{id}` - Uppdatera lägenhet
- `DELETE /api/apartments/{id}` - Ta bort lägenhet
- `GET /api/apartments/available` - Lediga lägenheter
- `GET /api/apartments/occupied` - Upptagna lägenheter
- `GET /api/apartments/search` - Sök lägenheter

#### Tenant API (`/api/tenants`)
- `GET /api/tenants` - Lista alla hyresgäster
- `POST /api/tenants` - Skapa hyresgäst
- `GET /api/tenants/{id}` - Hämta hyresgäst
- `PUT /api/tenants/{id}` - Uppdatera hyresgäst
- `DELETE /api/tenants/{id}` - Ta bort hyresgäst
- `POST /api/tenants/{id}/move-in` - Registrera inflyttning
- `POST /api/tenants/{id}/move-out` - Registrera utflyttning
- `POST /api/tenants/{id}/terminate` - Avsluta kontrakt

#### Interest API (`/api/interests`)
- `GET /api/interests` - Lista alla intresseanmälningar
- `POST /api/interests` - Skapa intresseanmälan
- `GET /api/interests/{id}` - Hämta intresseanmälan
- `PUT /api/interests/{id}` - Uppdatera intresseanmälan
- `DELETE /api/interests/{id}` - Ta bort intresseanmälan
- `POST /api/interests/{id}/schedule-viewing` - Boka visning
- `POST /api/interests/{id}/confirm-viewing` - Bekräfta visning
- `POST /api/interests/{id}/cancel-viewing` - Avboka visning

#### Issue API (`/api/issues`)
- `GET /api/issues` - Lista alla felanmälningar
- `POST /api/issues` - Skapa felanmälan
- `GET /api/issues/{id}` - Hämta felanmälan
- `PUT /api/issues/{id}` - Uppdatera felanmälan
- `DELETE /api/issues/{id}` - Ta bort felanmälan
- `POST /api/issues/{id}/approve` - Godkänn felanmälan
- `POST /api/issues/{id}/reject` - Avvisa felanmälan
- `GET /api/issues/new` - Nya felanmälningar
- `GET /api/issues/high-priority` - Högprioriterade felanmälningar

#### Task API (`/api/tasks`)
- `GET /api/tasks` - Lista alla uppgifter
- `POST /api/tasks` - Skapa uppgift
- `GET /api/tasks/{id}` - Hämta uppgift
- `PUT /api/tasks/{id}` - Uppdatera uppgift
- `DELETE /api/tasks/{id}` - Ta bort uppgift
- `POST /api/tasks/{id}/complete` - Slutför uppgift
- `POST /api/tasks/{id}/pause` - Pausa uppgift
- `POST /api/tasks/{id}/resume` - Återuppta uppgift
- `POST /api/tasks/{id}/cancel` - Avbryt uppgift
- `GET /api/tasks/overdue` - Försenade uppgifter
- `GET /api/tasks/high-priority` - Högprioriterade uppgifter

---

**Version**: 4.1.0  
**Senaste uppdatering**: 2024-12-19  
**Status**: Under utveckling 