# Numbered ER Diagrams

These diagrams split the project into readable bounded contexts. They are not intended to be one giant physical schema diagram; the project has many Mongo collections, and one page would be too dense to use.

1. [Auth ERD](01-auth-erd.mmd)
2. [Registration Admin/Client ERD](02-registration-admin-client-erd.mmd)
3. [Billing ERD](03-billing-erd.mmd)
4. [CMS Learning ERD](04-cms-learning-erd.mmd)
5. [Phishing ERD](05-phishing-erd.mmd)
6. [Notification ERD](06-notification-erd.mmd)
7. [Universal ERD](07-universal-erd.mmd)
8. [Breach Detection ERD](08-breach-detection-erd.mmd)
9. [Reference Data/Menu ERD](09-reference-data-erd.mmd)

## Main Client Data Key

Across services, the most important client-scoping field is:

```text
clientAdminId
```

Admin/client screens commonly use `clientAdminId` to load:

```text
client_admins
aspire_user
client_products
user_licence
end_user_packages
departments
invoices/payments
campaigns
notification settings/history
```
