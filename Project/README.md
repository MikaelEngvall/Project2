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

---

**Version**: 4.1.0  
**Senaste uppdatering**: 2024-12-19  
**Status**: Under utveckling 