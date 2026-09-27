# Leaderboard CRUD API Documentation - FINAL

## Overview
Complete implementation of Leaderboard Management API for the registration service with MongoDB and Spring Boot.

## ✅ Final Requirements

### Key Features:
1. **Status is ENUM**: `ACTIVE` or `INACTIVE` (not Boolean)
2. **Both Admin AND Client can Create/Update/Delete**:
   - **Admin**: Can create, update, delete **anyone's** leaderboards
   - **Client**: Can create, update, delete **only their own** leaderboards
3. **Panel API** (CRUD List):
   - **Admin Login**: Sees ALL leaderboards (all clients + admin's own)
   - **Client Login**: Sees their own + admin's default leaderboards
4. **Web API**: Returns up to 2 active leaderboards
   - **HIGH PRIORITY**: Client's own active leaderboards first
   - If client has < 2 active, fill remaining from admin defaults

## Authorization Matrix

| Operation | Admin | Client |
|-----------|-------|--------|
| **Create** | ✅ Can create for anyone or as default | ✅ Can create for themselves |
| **Update** | ✅ Can update anyone's leaderboard | ✅ Can only update their own |
| **Delete** | ✅ Can delete anyone's leaderboard | ✅ Can only delete their own |
| **Update Status** | ✅ Can update anyone's status | ✅ Can only update their own status |
| **View Panel** | ✅ Sees ALL (all clients + admin's) | ✅ Sees own + admin defaults |
| **View Web** | ✅ Up to 2 active | ✅ Up to 2 active (own priority) |

## API Endpoints

### Base URL
```
/api/v1/leaderboards
```

### 1. Create Leaderboard
```
POST /api/v1/leaderboards
```

**Who Can Use:**
- ✅ Admin (can create for any client or as default)
- ✅ Client (creates for themselves)

**Client Creating Their Own:**
```json
{
  "name": "Client Leader",
  "designation": "VP Engineering",
  "videoType": "UPLOAD_FILE",
  "videoUrl": "https://example.com/video.mp4",
  "thumbnailUrl": "https://example.com/thumbnail.jpg",
  "status": "ACTIVE"
}
```

**Admin Creating Default:**
```json
{
  "name": "CEO Message",
  "designation": "CEO",
  "videoType": "URL",
  "videoUrl": "https://example.com/ceo.mp4",
  "thumbnailUrl": "https://example.com/ceo.jpg",
  "status": "ACTIVE",
  "isDefault": true
}
```

### 2. Get All Leaderboards (For Admin/Client Panel)
```
GET /api/v1/leaderboards
```

**Response Behavior:**
- **Admin Login**: Returns ALL leaderboards from all clients + admin's own
- **Client Login**: Returns their own leaderboards + admin's default leaderboards

**Example Response for Client:**
```json
{
  "message": "Leaderboards fetched successfully",
  "statusCode": 200,
  "data": [
    {
      "id": "uuid1",
      "name": "My Leader 1",
      "status": "ACTIVE",
      "clientId": "my-client-id",
      "isDefault": false
    },
    {
      "id": "uuid2",
      "name": "My Leader 2",
      "status": "ACTIVE",
      "clientId": "my-client-id",
      "isDefault": false
    },
    {
      "id": "uuid3",
      "name": "Admin Default Leader",
      "status": "ACTIVE",
      "clientId": null,
      "isDefault": true
    }
  ]
}
```

**Example Response for Admin:**
```json
{
  "message": "Leaderboards fetched successfully",
  "statusCode": 200,
  "data": [
    {
      "id": "uuid1",
      "name": "Client A Leader",
      "clientId": "client-a-id",
      "isDefault": false
    },
    {
      "id": "uuid2",
      "name": "Client B Leader",
      "clientId": "client-b-id",
      "isDefault": false
    },
    {
      "id": "uuid3",
      "name": "Admin Default",
      "clientId": null,
      "isDefault": true
    }
  ]
}
```

### 3. 🆕 Get Active Leaderboards for Web (Up to 2)
```
GET /api/v1/leaderboards/active-web
```

**Purpose**: Frontend/Website display

**Priority Logic:**
1. **HIGH PRIORITY**: Get client's own ACTIVE leaderboards first (up to 2)
2. If client has < 2 active, fill remaining with admin defaults

**Scenarios:**

**Scenario A: Client has 0 active**
```json
// Returns 2 admin defaults
[
  { "name": "Admin Default 1", "isDefault": true },
  { "name": "Admin Default 2", "isDefault": true }
]
```

**Scenario B: Client has 1 active**
```json
// Returns 1 client + 1 admin default
[
  { "name": "Client Leader", "clientId": "uuid", "isDefault": false },
  { "name": "Admin Default 1", "clientId": null, "isDefault": true }
]
```

**Scenario C: Client has 2+ active**
```json
// Returns only first 2 client leaderboards
[
  { "name": "Client Leader 1", "clientId": "uuid", "isDefault": false },
  { "name": "Client Leader 2", "clientId": "uuid", "isDefault": false }
]
```

### 4. Update Leaderboard
```
PUT /api/v1/leaderboards/{id}
```

**Authorization:**
- ✅ Admin: Can update anyone's leaderboard
- ✅ Client: Can only update their own leaderboard

**Error if Client tries to update another's:**
```json
{
  "message": "You can only update your own leaderboards",
  "statusCode": 400
}
```

### 5. Delete Leaderboard
```
DELETE /api/v1/leaderboards/{id}
```

**Authorization:**
- ✅ Admin: Can delete anyone's leaderboard
- ✅ Client: Can only delete their own leaderboard

**Error if Client tries to delete another's:**
```json
{
  "message": "You can only delete your own leaderboards",
  "statusCode": 400
}
```

### 6. Update Leaderboard Status
```
PATCH /api/v1/leaderboards/status
```

**Request:**
```json
{
  "id": "leaderboard-uuid",
  "status": "INACTIVE"
}
```

**Authorization:**
- ✅ Admin: Can update anyone's status
- ✅ Client: Can only update their own status

## Use Cases

### Use Case 1: Admin Creates Defaults for All Clients
```bash
# Admin creates 2 default active leaders
POST /api/v1/leaderboards (as admin)
{
  "name": "CEO Message",
  "designation": "CEO",
  "videoType": "URL",
  "videoUrl": "https://cdn.example.com/ceo.mp4",
  "status": "ACTIVE",
  "isDefault": true
}

POST /api/v1/leaderboards (as admin)
{
  "name": "Company Vision",
  "designation": "Founder",
  "videoType": "URL",
  "videoUrl": "https://cdn.example.com/vision.mp4",
  "status": "ACTIVE",
  "isDefault": true
}
```

### Use Case 2: Client Creates Their Own Leader
```bash
# Client ABC creates their own leader
POST /api/v1/leaderboards (as Client ABC)
{
  "name": "Our Team Leader",
  "designation": "VP Engineering",
  "videoType": "UPLOAD_FILE",
  "videoUrl": "https://cdn.example.com/team.mp4",
  "status": "ACTIVE"
}
# clientId is automatically set to Client ABC's ID
```

### Use Case 3: Client Views Panel (CRUD List)
```bash
# Client ABC logs in and views panel
GET /api/v1/leaderboards (as Client ABC)

# Returns:
# - Client ABC's own leaderboards
# - Admin's default leaderboards
# Total: 3 items (1 own + 2 admin defaults)
```

### Use Case 4: Admin Views Panel (CRUD List)
```bash
# Admin logs in and views panel
GET /api/v1/leaderboards (as Admin)

# Returns:
# - ALL leaderboards from ALL clients
# - Admin's default leaderboards
# Can see Client A's, Client B's, Client C's leaders, etc.
```

### Use Case 5: Client Views Web (Has 1 Active)
```bash
# Client ABC has 1 active leader
GET /api/v1/leaderboards/active-web (as Client ABC)

# Returns (2 items):
# [
#   { "name": "Our Team Leader", "clientId": "abc-uuid" },
#   { "name": "CEO Message", "isDefault": true }
# ]
```

### Use Case 6: Client Updates Their Own Leader
```bash
# Client ABC updates their leader
PUT /api/v1/leaderboards/{own-leader-id} (as Client ABC)
{
  "name": "Updated Team Leader",
  "designation": "CTO",
  ...
}
# ✅ SUCCESS
```

### Use Case 7: Client Tries to Update Admin's Default
```bash
# Client ABC tries to update admin default
PUT /api/v1/leaderboards/{admin-default-id} (as Client ABC)
# ❌ ERROR: "You can only update your own leaderboards"
```

### Use Case 8: Admin Updates Any Leader
```bash
# Admin updates Client ABC's leader
PUT /api/v1/leaderboards/{client-abc-leader-id} (as Admin)
# ✅ SUCCESS - Admin can update anyone's
```

## Panel API vs Web API Comparison

| Aspect | Panel API (`GET /leaderboards`) | Web API (`GET /leaderboards/active-web`) |
|--------|--------------------------------|------------------------------------------|
| **Purpose** | Management/CRUD listing | Frontend display |
| **Admin Sees** | ALL leaderboards from all clients | Up to 2 active |
| **Client Sees** | Own + admin defaults | Up to 2 active (own priority) |
| **Count** | Variable (all matching) | Fixed (up to 2) |
| **Status Filter** | All statuses (ACTIVE + INACTIVE) | Only ACTIVE |
| **Use Case** | Admin panel, Client dashboard | Website hero section |

## Error Handling

### Client Authorization Errors

**Client tries to update another client's leaderboard:**
```json
{
  "message": "You can only update your own leaderboards",
  "statusCode": 400
}
```

**Client tries to delete admin default:**
```json
{
  "message": "You can only delete your own leaderboards",
  "statusCode": 400
}
```

**Client tries to update status of another's leaderboard:**
```json
{
  "message": "You can only update status of your own leaderboards",
  "statusCode": 400
}
```

### Duplicate Name Errors

**Same name within client's leaderboards:**
```json
{
  "message": "Leaderboard with name 'John Doe' already exists for this client",
  "statusCode": 409
}
```

**Same name within admin defaults:**
```json
{
  "message": "Default leaderboard with name 'CEO Message' already exists",
  "statusCode": 409
}
```

## Entity Structure

```java
public class Leaderboard {
    private UUID id;
    private String name;
    private String designation;
    private VideoType videoType;        // UPLOAD_FILE, URL
    private String videoUrl;
    private String thumbnailUrl;
    private LeaderboardStatus status;   // ACTIVE, INACTIVE
    private Instant createdDate;
    private String createdBy;
    private String updatedBy;
    private Instant updatedAt;
    private UUID clientId;              // null = admin default
    private Boolean isDefault;          // true = admin default
}
```

## MongoDB Schema

**Collection**: `leaderboards`

**Sample Documents:**

**Admin Default:**
```json
{
  "_id": "uuid-1",
  "name": "CEO Message",
  "designation": "CEO",
  "videoType": "URL",
  "videoUrl": "https://cdn.example.com/ceo.mp4",
  "thumbnailUrl": "https://cdn.example.com/ceo.jpg",
  "status": "ACTIVE",
  "createdDate": "2025-10-16T10:00:00Z",
  "createdBy": "admin",
  "updatedAt": "2025-10-16T10:00:00Z",
  "clientId": null,
  "isDefault": true
}
```

**Client's Leader:**
```json
{
  "_id": "uuid-2",
  "name": "Our Team Leader",
  "designation": "VP Engineering",
  "videoType": "UPLOAD_FILE",
  "videoUrl": "https://cdn.example.com/team.mp4",
  "thumbnailUrl": "https://cdn.example.com/team.jpg",
  "status": "ACTIVE",
  "createdDate": "2025-10-16T11:00:00Z",
  "createdBy": "client-abc-user",
  "updatedAt": "2025-10-16T11:00:00Z",
  "clientId": "client-abc-uuid",
  "isDefault": false
}
```

**Indexes:**
```javascript
db.leaderboards.createIndex({ "clientId": 1, "status": 1 });
db.leaderboards.createIndex({ "isDefault": 1, "status": 1 });
db.leaderboards.createIndex({ "status": 1 });
db.leaderboards.createIndex({ "name": 1 });
```

## Summary of Requirements

### ✅ Both Can Create
- Admin can create for anyone or as default
- Client can create for themselves

### ✅ Permission-Based Update/Delete
- Admin can modify anyone's leaderboards
- Client can only modify their own

### ✅ Panel API (CRUD List)
- Admin sees: ALL (all clients + admin's)
- Client sees: Own + admin defaults

### ✅ Web API (Display)
- Returns up to 2 active leaderboards
- HIGH PRIORITY: Client's own active first
- Fallback: Admin defaults if needed

### ✅ Status Enum
- ACTIVE
- INACTIVE

### ✅ Case-Insensitive Duplicate Check
- Within same client scope
- Within admin defaults scope

---

**Implementation Status**: ✅ **COMPLETE & CORRECTED**

All requirements have been properly implemented according to your specifications.
