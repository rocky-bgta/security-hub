# JWT + Dynamic Current Context Demo V4

This project demonstrates the difference between application-wide JWT authentication and room-specific Current Context authorization.

## Technology
- Java 21, Spring Boot 3, Maven
- Local MongoDB: `mongodb://localhost:27017/context_demo`
- Existing Redis: `localhost:6379`

## Security model
- **ROOM-A:** JWT + active ROOM-A context required.
- **ROOM-B:** JWT only; no Current Context required.
- **ROOM-C:** JWT + active ROOM-C context required.
- A context belongs to one user and one room. A ROOM-A context cannot authorize ROOM-C.
- Context revocation affects that context only; the JWT can remain valid.
- JWT logout stores the JWT `jti` in Redis until expiry and revokes contexts created by that JWT.

## Dynamic context API
`POST /api/contexts` with JWT and body:
```json
{ "roomId": "ROOM-C" }
```
Response contains MongoDB `contextId`, `roomId`, and `active`. The same API creates contexts for ROOM-A, ROOM-C, and future context-protected rooms.

## Revoke context
`POST /api/contexts/revoke`
```json
{ "contextId": "<context-id>" }
```

## Room APIs
- `POST /api/rooms/a/enter` body `{ "contextId": "..." }`
- `GET /api/rooms/b/enter` JWT only
- `POST /api/rooms/c/enter` body `{ "contextId": "..." }`

Invalid/missing/wrong-room/revoked context returns a clear 403 response, for example:
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have an active Current Context for ROOM-C.",
  "timestamp": "..."
}
```
Invalid, expired, or revoked JWT returns 401 with a clear message.

## Run
1. Ensure MongoDB is running on `localhost:27017`.
2. Ensure Redis is running on `localhost:6379`.
3. Run `mvn spring-boot:run`.
4. Import `postman/JWT-Current-Context-Demo-V4.postman_collection.json`.
5. Execute requests in numeric order.

## Key Postman proof
1. Login and get JWT.
2. Create ROOM-A context and access ROOM-A.
3. Access ROOM-B with JWT only.
4. Create a separate ROOM-C context and access ROOM-C.
5. Try ROOM-A context on ROOM-C -> **403**.
6. Revoke ROOM-C context -> ROOM-C **403**, while ROOM-A and ROOM-B still **200**.
7. Revoke ROOM-A context -> ROOM-A **403**.
8. Create a fresh ROOM-A context, then logout/revoke JWT.
9. With the same revoked JWT, ROOM-A and ROOM-B both return **401**.

## Adding another context-protected room
For ROOM-D, reuse `CurrentContextService.createContext(...)` and `CurrentContextValidator.isValidContext(...)`; pass `ROOM-D`. No ROOM-D-specific context service or repository method is needed.
