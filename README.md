# ChatBot — U6W1D3

Chat bot full-stack: **React + Vite** (frontend) → **Spring Boot** (backend) → **OpenRouter** (modello).

Il backend fa da proxy verso OpenRouter: è l'unico che conosce la API key.
Il browser non la vede mai.

```
React :5173  ──POST /api/chats/{id}/messages──►  Spring Boot :8080  ──Bearer key──►  OpenRouter
                                                        │
                                                        ▼
                                                 PostgreSQL "ChatBot"
```

---

## 1. Prerequisiti

- PostgreSQL in ascolto su `localhost:5432`
- Database **`ChatBot`** (già creato, owner `postgres`)
- Java 25, Node 20+

Le credenziali del DB stanno in `BE/src/main/resources/application.properties`,
nelle due variabili in cima:

```properties
db.username=postgres
db.password=1234
```

Sono gli unici due valori da toccare se cambi utente o password in pgAdmin.

Le tabelle `chats` e `messages` le crea Hibernate da solo al primo avvio
(`spring.jpa.hibernate.ddl-auto=update`).

## 2. La API key di OpenRouter

**Non va scritta in `application.properties`**: si legge da variabile d'ambiente,
così non finisce mai in un commit.

Da terminale, prima di avviare il backend:

```bash
export OPENROUTER_API_KEY='sk-or-v1-...'
cd BE && ./mvnw spring-boot:run
```

Da IntelliJ: *Run → Edit Configurations → Environment variables* →
`OPENROUTER_API_KEY=sk-or-v1-...`

Se la chiave manca, il backend parte lo stesso ma al primo messaggio risponde
`502` con un messaggio esplicito.

## 3. Avvio

```bash
# terminale 1 — backend su :8080
export OPENROUTER_API_KEY='sk-or-v1-...'
cd BE && ./mvnw spring-boot:run

# terminale 2 — frontend su :5173
cd FEJSX && npm install && npm run dev
```

---

## 4. Dov'è finito il comando cURL

Il cURL copiato da OpenRouter non si incolla da nessuna parte: è la descrizione
della richiesta HTTP, e ogni suo pezzo è finito in un punto diverso del codice.

| Pezzo del cURL | Dove sta ora |
|---|---|
| `https://openrouter.ai/api/v1` | `openrouter.base-url` → `RestClientConfig` |
| `/chat/completions` | `OpenRouterService.complete()` |
| `-H "Authorization: Bearer ..."` | `OpenRouterService`, dalla env var |
| `-H "Content-Type: application/json"` | `RestClientConfig` (header di default) |
| `-d '{"model":..., "messages":[...]}'` | record `ChatCompletionRequest` |

L'API è **senza memoria**: a ogni richiesta il backend rispedisce tutta la
conversazione salvata a DB, altrimenti il modello non ricorderebbe nulla.

---

## 5. API

| Metodo | Endpoint | Cosa fa |
|---|---|---|
| `GET` | `/api/chats` | Elenco per la sidebar |
| `GET` | `/api/chats/{id}` | Una chat con tutta la cronologia |
| `POST` | `/api/chats` | Crea una conversazione vuota |
| `POST` | `/api/chats/{id}/messages` | Manda il prompt, salva domanda e risposta |
| `PUT` | `/api/chats/{id}` | Sostituisce `title` + `model` + `favorite` insieme |
| `PATCH` | `/api/chats/{id}` | Cambia **un campo solo** |
| `DELETE` | `/api/chats/{id}` | Elimina chat e messaggi (cascade) |
| `GET` | `/api/models` | Modelli della tendina (pubblico) |

### I modelli della tendina

L'elenco **non è scritto a mano**: `ModelCatalogService` lo scarica da
`https://openrouter.ai/api/v1/models` all'avvio e poi ogni 6 ore.

**Solo modelli gratuiti.** Un modello entra nell'elenco solo se costano zero
sia il prompt sia la risposta *e* l'id finisce per `:free`. Sono gli stessi
filtri della pagina di OpenRouter (`max_output_price=0`, `variant=free`,
`output_modalities=text`). Il filtro è nel codice: un modello a pagamento non
può comparire nemmeno se OpenRouter ne aggiunge di nuovi.

**Ordinamento: dal più veloce al più lento, per approssimazione.** La pagina di
OpenRouter ordina per latenza, ma quel dato non è recuperabile: il campo
`latency_last_30m` dell'API è vuoto su tutte le varianti `:free` (verificato
anche sulle corrispondenti a pagamento). Usiamo allora la dimensione ricavata
dal nome del modello — `2.6b`, `26b`, `30b`, `120b`, `550b` — perché i modelli
piccoli rispondono quasi sempre prima. Chi non dichiara la dimensione nel nome
finisce in fondo, in ordine alfabetico.

**Se OpenRouter non risponde** l'app non si pianta: tiene l'ultimo elenco
scaricato, e se non ne ha mai avuto uno usa la lista di riserva in
`application.properties` (`openrouter.models[...]`).

### Autenticazione

| Metodo | Endpoint | Cosa fa |
|---|---|---|
| `POST` | `/api/auth/register` | Crea l'account, restituisce subito il token |
| `POST` | `/api/auth/login` | Verifica le credenziali, restituisce il token |
| `GET` | `/api/auth/me` | Chi sono, in base al token |

Tutti gli endpoint `/api/chats/**` richiedono l'header
`Authorization: Bearer <token>`. Senza, rispondono `401`.

### Serve davvero il PATCH? Sì.

Questa app ha due operazioni che toccano **un solo campo**:

- la **stella** dei preferiti (`favorite`)
- la **rinomina** di una chat (`title`)

Con il solo `PUT` il frontend sarebbe costretto a rimandare *tutti* i campi solo
per girare un booleano. Due problemi concreti:

1. **Dati sovrascritti per sbaglio.** Se nel frattempo il titolo è cambiato (la
   prima domanda lo genera in automatico), il `PUT` lo riscriverebbe con il
   valore vecchio che il client aveva in memoria.
2. **Il client deve conoscere lo stato completo.** Per mettere una stella
   dovrebbe prima fare una `GET` per recuperare gli altri campi: due chiamate
   invece di una.

Con `PATCH {"favorite": true}` si manda solo ciò che cambia. È esattamente la
differenza semantica tra i due verbi: `PUT` **sostituisce** la risorsa, `PATCH`
la **modifica parzialmente**.

Entrambi sono implementati e i due comportamenti sono distinti davvero:
`PUT` con un campo mancante restituisce `400` e ti dice di usare `PATCH`.

---

## 6. Struttura

```
BE/src/main/java/com/example/demo/
├── config/       CorsConfig, RestClientConfig, OpenRouterProperties
├── security/     SecurityConfig, JwtService, JwtAuthenticationFilter,
│                 AppUserDetailsService
├── controller/   ChatController, ModelController, AuthController
├── dto/          record di richiesta/risposta (+ dto/openrouter per il "filo")
├── entity/       Chat, Message, MessageRole, User
├── exception/    GlobalExceptionHandler e le eccezioni custom
├── repository/   ChatRepository, MessageRepository, UserRepository
└── service/      ChatService (logica), OpenRouterService (il cURL tradotto),
                 AuthService, ModelCatalogService (elenco modelli gratuiti)

FEJSX/src/
├── api/client.js       tutte le fetch verso :8080
├── components/         TopBar, Sidebar, Composer, MessageList, Mascot,
│                       Modal, AuthModal, Icons
├── hooks/useTheme.js   tema chiaro/scuro + localStorage
├── hooks/AuthProvider.jsx  sessione, token, login/logout
├── hooks/authContext.js    il context e l'hook useAuth
├── App.jsx             stato dell'applicazione
├── App.css             layout e animazioni
└── index.css           token dei due temi
```

## 7. CORS

`CorsConfig` apre `/api/**` a `http://localhost:5173` per
`GET, POST, PUT, PATCH, DELETE, OPTIONS`.
Senza, il browser bloccherebbe ogni chiamata: Vite e Spring Boot sono su due
porte diverse, quindi due origini diverse.

## 8. Come funziona l'autenticazione

Login e registrazione sono reali: Spring Security + JWT.

**Le password non vengono mai salvate.** Al registrarsi, BCrypt trasforma la
password in un hash non reversibile con un "sale" casuale. Al login si
riapplica lo stesso calcolo e si confrontano gli hash: nel database la password
in chiaro non esiste in nessun momento.

**Il token** è un JWT firmato con `security.jwt.secret` (env var `JWT_SECRET`),
valido 120 minuti. Il frontend lo salva in `localStorage` e lo rimanda in ogni
richiesta nell'header `Authorization: Bearer <token>`.

Attenzione a un equivoco comune: il contenuto di un JWT **non è cifrato**, solo
codificato in base64, quindi chiunque può leggerlo. Quello che nessuno può fare
senza il segreto è *falsificare la firma*. Per questo dentro non si mettono mai
dati sensibili: c'è solo l'email e la scadenza.

**Le chat sono private.** `Chat` ha un `user_id` obbligatorio e ogni query
filtra per proprietario. Chiedere la chat di un altro utente restituisce
`404 non trovata`, non `403 vietato`: così non si scopre nemmeno se quell'id
esiste.

### Il giro completo di una richiesta protetta

1. `JwtAuthenticationFilter` legge l'header `Authorization`
2. `JwtService` verifica la firma e la scadenza, e ne estrae l'email
3. `AppUserDetailsService` carica l'utente dal database
4. L'utente finisce nel `SecurityContext`
5. Nel controller, `@AuthenticationPrincipal User user` lo riceve già pronto

Se il token manca o non è valido il filtro non blocca nulla: lascia passare
senza autenticare, e sarà `SecurityConfig` a rispondere `401` sugli endpoint
che richiedevano il login.

### Due dettagli di configurazione

- **Niente CSRF e niente sessioni**: il token viaggia in un header che il JS
  deve aggiungere apposta, quindi l'attacco che il CSRF previene qui non
  si applica. Ogni richiesta porta la propria identità: il server non ricorda
  nulla fra una e l'altra.
- **Il CORS è un `CorsConfigurationSource`, non un `WebMvcConfigurer`**: ora le
  richieste passano prima dai filtri di Security, e il preflight `OPTIONS`
  verrebbe respinto con `401` prima ancora di arrivare al controller.

## 9. Cosa manca ancora

- Nessun refresh token: scaduti i 120 minuti si rifà il login.
- Nessun recupero password.
- Il ruolo utente esiste (`ROLE_USER`) ma non è ancora usato per distinguere
  permessi diversi.
