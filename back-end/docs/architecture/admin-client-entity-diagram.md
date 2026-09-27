# Admin and Client Entity Diagram

## Entity Relationship

```mermaid
erDiagram
    ASPIRE_USER {
        UUID id PK
        UUID userId
        string email
        string username
        string userType
        string status
        string clientAdminId FK
        string mspId FK
        string[] roles FK
        string department
        string companyName
    }

    ROLE {
        string id PK
        string roleName
        int accessLevel
        string status
        boolean systemRole
    }

    ROLE_PERMISSION {
        string id PK
        string roleId FK
        string roleName
        object[] menuPermissions
    }

    CLIENT_ADMIN {
        string id PK
        string clientAdminId
        string organizationName
        string email
        string mspId FK
        string status
        string[] roleIds FK
        string[] clientProductIds FK
        string country
        string domain
    }

    MSP_USER {
        string id PK
        string mspId
        string organizationName
        string contactEmail
        string status
        string[] clientProductIds FK
        string[] roleIds FK
    }

    CLIENT_PRODUCT {
        string id PK
        string clientAdminId FK
        string productId
        string packageId
        int licenseCount
        int usedLicenseCount
        string licenseStatus
        string mspId FK
    }

    END_USER_PACKAGE {
        string id PK
        string userId FK
        string clientAdminId FK
        string productId
        string subPackageId
        double progress
        string status
        boolean active
    }

    USER_LICENCE {
        string id PK
        string userId FK
        string clientAdminId FK
        string productId
        string packageId
        string licenceStatus
    }

    DEPARTMENT {
        string id PK
        string clientAdminId FK
        string name
        boolean active
        boolean isSystemDefined
    }

    CLIENT_ADMIN ||--o{ ASPIRE_USER : "owns users by clientAdminId"
    CLIENT_ADMIN ||--o{ CLIENT_PRODUCT : "has products/licenses"
    CLIENT_ADMIN ||--o{ END_USER_PACKAGE : "has user package progress"
    CLIENT_ADMIN ||--o{ USER_LICENCE : "has assigned licences"
    CLIENT_ADMIN ||--o{ DEPARTMENT : "has departments"

    ASPIRE_USER }o--o{ ROLE : "roles[]"
    ROLE ||--o| ROLE_PERMISSION : "permissions"

    MSP_USER ||--o{ CLIENT_ADMIN : "manages clients by mspId"
    MSP_USER ||--o{ CLIENT_PRODUCT : "assigned through mspId"

    ASPIRE_USER ||--o{ END_USER_PACKAGE : "userId"
    ASPIRE_USER ||--o{ USER_LICENCE : "userId"
```

## How Admin Sees Client Data

```mermaid
flowchart TD
    LOGIN[Admin logs in] --> TOKEN[JWT / CurrentUserContext]
    TOKEN --> TYPE{userType}

    TYPE -->|SUPER_ADMIN / ASPIRE_ADMIN| ALL[Can query client list broadly]
    TYPE -->|MSP| MSP[Can query clients scoped by mspId or clientAdminIds]
    TYPE -->|CLIENT_ADMIN| CLIENT[Scoped to own clientAdminId]

    ALL --> PICK[Select clientAdminId]
    MSP --> PICK
    CLIENT --> PICK

    PICK --> QUERY[Repository/service queries]
    QUERY --> CA[client_admins by id]
    QUERY --> USERS[aspire_user where clientAdminId = selected client]
    QUERY --> PRODUCTS[client_products where clientAdminId = selected client]
    QUERY --> LICENCES[user_licences where clientAdminId = selected client]
    QUERY --> PACKAGES[end_user_packages where clientAdminId = selected client]
    QUERY --> DEPTS[departments where clientAdminId = selected client]

    CA --> VIEW[Admin sees client profile + related data]
    USERS --> VIEW
    PRODUCTS --> VIEW
    LICENCES --> VIEW
    PACKAGES --> VIEW
    DEPTS --> VIEW
```

## Main Join Key

Most client-owned data is grouped by:

```text
clientAdminId
```

For example:

```text
aspire_user.clientAdminId
client_products.clientAdminId
end_user_packages.clientAdminId
user_licences.clientAdminId
departments.clientAdminId
```

That is why admin screens can load a selected client and then fetch all related data using the same `clientAdminId`.
