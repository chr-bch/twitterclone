# Twitterclone

Vereinfachter Twitter-Klon: Beiträge mit bis zu 280 Zeichen, mit Autor und Zeitstempel, neueste zuerst.

**Stack:** Spring Boot 4 (Java 25), PostgreSQL, Flyway, Keycloak (OAuth2/OIDC), Vue 3 mit TypeScript und Tailwind, Docker Compose

## Schnellstart

Voraussetzung ist nur Docker.

```bash
docker compose up --build
```

| Was | URL | Zugang |
|---|---|---|
| Frontend | http://localhost:5173 | `clone` / `clone123` |
| Swagger UI | http://localhost:8081/swagger-ui.html | „Authorize“ → Login als `clone` |
| Keycloak-Admin | http://localhost:8180 | `admin` / `admin` |

Beim ersten Start werden Demo-Nutzer und -Beiträge angelegt, damit der Feed nicht leer ist.
Zurücksetzen: `docker compose down -v`.

## Entwicklung

- **Backend:** `TwittercloneApplication` in der IDE starten (Java 25). Spring Boots Docker-Compose-Support startet
  Postgres und Keycloak aus `compose.infra.yaml` automatisch mit.
- **Frontend:** `cd frontend && npm install && npm run dev` (Node 24). Vite leitet `/api` an `localhost:8081` weiter.
- **Tests:** `./mvnw test`. Die Integrationstests laufen mit Testcontainers gegen ein echtes Postgres.

## Architektur

```
Browser ──► web (nginx: Vue-Build, /api → app) ──► app (Spring Boot) ──► postgres
   │                                                    │
   └──── Login (PKCE) ──► keycloak ◄── Signaturschlüssel ┘
```

| Methode | Pfad | Auth |
|---|---|---|
| `GET` | `/api/posts?cursor=&limit=` | – |
| `GET` | `/api/posts/{id}` | – |
| `POST` | `/api/posts` | Bearer-Token |
| `GET` | `/api/users/{username}` | – |
| `GET` | `/api/users/{username}/posts?cursor=&limit=` | – |
| `GET` | `/api/users/me` | Bearer-Token |

Das Backend ist nach Fachlichkeit paketiert (`post`, `user`) statt nach technischen Schichten.

