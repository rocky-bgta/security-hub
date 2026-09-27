# Policy Management API Documentation

## Overview
Complete CRUD API for Policy and Policy Type management with limit-offset based pagination.

## Response Format
All list-based APIs return the following format:
```json
{
  "message": "Success message",
  "statusCode": 200,
  "data": {
    "offset": 0,
    "pageSize": 10,
    "total": 100,
    "items": []
  }
}
```

---

## Policy Type APIs

### 1. Get All Policy Types (Paginated)
**Endpoint:** `GET /api/policy-types?limit=10&offset=0`

**Response:**
```json
{
  "message": "Policy types retrieved successfully",
  "statusCode": 200,
  "data": {
    "items": [
      {
        "id": "string",
        "name": "string",
        "code": "IT|SECURITY|COMPLIANCE|PRIVACY|HUMAN_RESOURCE",
        "status": "ACTIVE|INACTIVE|DRAFT",
        "createdBy": "string",
        "updatedBy": "string",
        "createdAt": "2025-11-01T10:00:00",
        "updatedAt": "2025-11-01T10:00:00"
      }
    ],
    "total": 100,
    "pageSize": 10,
    "offset": 0
  }
}
```

### 2. Get Active Policy Types Only
**Endpoint:** `GET /api/policy-types/active`

**Response:**
```json
{
  "message": "Active policy types retrieved successfully",
  "statusCode": 200,
  "data": [
    {
      "id": "string",
      "name": "string",
      "code": "IT",
      "status": "ACTIVE",
      "createdBy": "string",
      "updatedBy": "string",
      "createdAt": "2025-11-01T10:00:00",
      "updatedAt": "2025-11-01T10:00:00"
    }
  ]
}
```

### 3. Get Policy Type by ID
**Endpoint:** `GET /api/policy-types/{id}`

**Response:**
```json
{
  "message": "Policy type retrieved successfully",
  "statusCode": 200,
  "data": {
    "id": "string",
    "name": "string",
    "code": "IT",
    "status": "ACTIVE",
    "createdBy": "string",
    "updatedBy": "string",
    "createdAt": "2025-11-01T10:00:00",
    "updatedAt": "2025-11-01T10:00:00"
  }
}
```

### 4. Create Policy Type
**Endpoint:** `POST /api/policy-types`

**Request Body:**
```json
{
  "name": "Information Technology",
  "code": "IT",
  "status": "ACTIVE"
}
```

**Response:**
```json
{
  "message": "Policy type created successfully",
  "statusCode": 201,
  "data": {
    "id": "generated-id",
    "name": "Information Technology",
    "code": "IT",
    "status": "ACTIVE",
    "createdBy": "user-id",
    "updatedBy": "user-id",
    "createdAt": "2025-11-01T10:00:00",
    "updatedAt": "2025-11-01T10:00:00"
  }
}
```

### 5. Update Policy Type
**Endpoint:** `PUT /api/policy-types/{id}`

**Request Body:**
```json
{
  "name": "Updated IT Policy",
  "code": "IT",
  "status": "INACTIVE"
}
```

### 6. Delete Policy Type
**Endpoint:** `DELETE /api/policy-types/{id}`

**Response:**
```json
{
  "message": "Policy type deleted successfully",
  "statusCode": 204,
  "data": null
}
```

---

## Policy APIs

### 1. Get All Policies (Paginated)
**Endpoint:** `GET /api/policies?limit=10&offset=0`

**Response:**
```json
{
  "message": "Policies retrieved successfully",
  "statusCode": 200,
  "data": {
    "items": [
      {
        "id": "string",
        "policyName": "string",
        "policyTypeId": "string",
        "policyTypeName": "Information Technology",
        "effectiveDate": "2025-01-01",
        "status": "ACTIVE|INACTIVE|EXPIRED",
        "policyEndDate": "2026-01-01",
        "description": "string",
        "files": [
          {
            "fileUrl": "https://example.com/file.pdf",
            "fileType": "PDF|WORD"
          }
        ],
        "industryId": "string",
        "companyName": "string (optional)",
        "createdBy": "string",
        "updatedBy": "string",
        "createdAt": "2025-11-01T10:00:00",
        "updatedAt": "2025-11-01T10:00:00"
      }
    ],
    "total": 100,
    "pageSize": 10,
    "offset": 0
  }
}
```

### 2. Get Active Policies Only (Paginated)
**Endpoint:** `GET /api/policies/active?limit=10&offset=0`

**Response:** Same structure as Get All Policies, but filtered to ACTIVE status only.

### 3. Get Policy by ID
**Endpoint:** `GET /api/policies/{id}`

**Response:**
```json
{
  "message": "Policy retrieved successfully",
  "statusCode": 200,
  "data": {
    "id": "string",
    "policyName": "Data Protection Policy",
    "policyTypeId": "string",
    "policyTypeName": "Privacy",
    "effectiveDate": "2025-01-01",
    "status": "ACTIVE",
    "policyEndDate": "2026-01-01",
    "description": "Comprehensive data protection policy",
    "files": [
      {
        "fileUrl": "https://example.com/policy.pdf",
        "fileType": "PDF"
      }
    ],
    "industryId": "industry-123",
    "companyName": "Aspire Technologies",
    "createdBy": "user-id",
    "updatedBy": "user-id",
    "createdAt": "2025-11-01T10:00:00",
    "updatedAt": "2025-11-01T10:00:00"
  }
}
```

### 4. Create Policy
**Endpoint:** `POST /api/policies`

**Request Body:**
```json
{
  "policyName": "Data Protection Policy",
  "policyTypeId": "policy-type-id",
  "effectiveDate": "2025-01-01",
  "status": "ACTIVE",
  "policyEndDate": "2026-01-01",
  "description": "Comprehensive data protection policy",
  "files": [
    {
      "fileUrl": "https://example.com/policy.pdf",
      "fileType": "PDF"
    },
    {
      "fileUrl": "https://example.com/policy.docx",
      "fileType": "WORD"
    }
  ],
  "industryId": "industry-123",
  "companyName": "Aspire Technologies"
}
```

**Response:**
```json
{
  "message": "Policy created successfully",
  "statusCode": 201,
  "data": {
    "id": "generated-id",
    "policyName": "Data Protection Policy",
    "policyTypeId": "policy-type-id",
    "policyTypeName": "Privacy",
    "effectiveDate": "2025-01-01",
    "status": "ACTIVE",
    "policyEndDate": "2026-01-01",
    "description": "Comprehensive data protection policy",
    "files": [
      {
        "fileUrl": "https://example.com/policy.pdf",
        "fileType": "PDF"
      }
    ],
    "industryId": "industry-123",
    "companyName": "Aspire Technologies",
    "createdBy": "user-id",
    "updatedBy": "user-id",
    "createdAt": "2025-11-01T10:00:00",
    "updatedAt": "2025-11-01T10:00:00"
  }
}
```

### 5. Update Policy
**Endpoint:** `PUT /api/policies/{id}`

**Request Body:** Same as Create Policy

### 6. Delete Policy
**Endpoint:** `DELETE /api/policies/{id}`

**Response:**
```json
{
  "message": "Policy deleted successfully",
  "statusCode": 204,
  "data": null
}
```

---

## Field Details

### Policy Type Fields
- **name**: Policy type name
- **code**: Enum values: IT, SECURITY, COMPLIANCE, PRIVACY, HUMAN_RESOURCE
- **status**: Enum values: ACTIVE, INACTIVE, DRAFT
- **createdBy**: Auto-populated from user context
- **updatedBy**: Auto-populated from user context
- **createdAt**: Auto-populated timestamp
- **updatedAt**: Auto-populated timestamp

### Policy Fields
- **policyName**: Name of the policy
- **policyTypeId**: Reference to Policy Type entity
- **policyTypeName**: Auto-populated from Policy Type (read-only)
- **effectiveDate**: Date when policy becomes effective
- **status**: Enum values: ACTIVE, INACTIVE, EXPIRED
- **policyEndDate**: Date when policy expires
- **description**: Detailed description
- **files**: Array of file attachments (PDF/WORD links)
- **industryId**: Reference to Industry entity (stored as ID)
- **companyName**: Optional company name
- **createdBy**: Auto-populated from user context
- **updatedBy**: Auto-populated from user context
- **createdAt**: Auto-populated timestamp
- **updatedAt**: Auto-populated timestamp

---

## Implementation Details

✅ **Lombok** used for all classes (@RequiredArgsConstructor, @Data, etc.)
✅ **Service Layer** contains all business logic
✅ **Limit-Offset Pagination** for all list APIs
✅ **Active-only endpoints** available for both Policy and Policy Type
✅ **Response format** matches specification: `{ message, statusCode, data: { items, total, pageSize, offset } }`
✅ **Audit fields** auto-populated (createdBy, updatedBy, timestamps)
✅ **Policy Type name** automatically fetched and included in Policy responses
✅ **File attachments** support with FileType enum (PDF/WORD)

