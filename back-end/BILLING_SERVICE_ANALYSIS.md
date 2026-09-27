# Billing Service - Comprehensive API and Flow Analysis

## Overview
The Billing Service is a Spring Boot microservice that handles all billing-related operations including invoice management, payment processing (manual and online), coupon management, credit system, refunds, VAT configuration, and analytics.

## Architecture

### Technology Stack
- **Framework**: Spring Boot
- **Database**: MongoDB (Spring Data MongoDB)
- **Payment Gateways**: Stripe, PayPal
- **Storage**: Azure Blob Storage, AWS S3
- **Communication**: REST APIs, Webhooks
- **Documentation**: Swagger/OpenAPI

### Service Structure
```
services/billing/
├── api/          # DTOs and request/response models
├── core/         # Business logic, services, controllers, models
└── service/      # Application entry point and configuration
```

---

## Core Models

### 1. Invoice Model
**Collection**: `invoices`

**Key Fields**:
- `id`: Unique invoice ID (format: `INV-{timestamp}`)
- `clientAdminId`, `mspAdminId`: Client and MSP identifiers
- `clientName`, `mspName`: Organization names
- `roleType`: CLIENT or MSP
- `subtotal`, `discountAmount`, `discountPercentage`, `vatAmount`, `totalAmount`
- `discountType`: FLAT or PERCENTAGE
- `status`: PENDING, PAID, FAILED, CANCELLED, ON_PROGRESS
- `productSelections`: List of selected products/packages
- `paymentMethod`: BANK_TRANSFER, CHECK_PAYMENT
- `paymentDetails`: Embedded payment information
- `countryName`, `countryCode`, `countryId`, `stateName`, `stateCode`
- `createdAt`, `paidAt`

### 2. Payment Model
**Collection**: `payments`

**Key Fields**:
- `id`: UUID payment ID
- `invoiceId`: Linked invoice
- `clientId`, `mspAdminId`, `countryId`, `roleType`: For filtering
- `amount`: Grand total paid
- `actualAmount`: Original amount before discount
- `status`: PENDING, SUCCESS, FAILED, CANCELLED
- `online`: Boolean flag for online vs manual payment
- `isActive`: Payment verification status
- `paymentSources`: Multi-method payment support (CREDIT, STRIPE, PAYPAL, BANK_TRANSFER, CHECK_PAYMENT)
- `couponId`, `couponCode`, `discountAmount`
- `subtotal`, `vatAmount`
- `transactionId`: Gateway transaction ID
- `breakdown`: Discounted item details
- `paymentType`: PRODUCT_PAYMENT, CREDIT_PAYMENT

### 3. Coupon Model
**Collection**: `coupons`

**Key Fields**:
- `code`: Unique coupon code
- `type`: PERCENTAGE or FIXED
- `value`: Discount value
- `currency`: For FIXED type
- `validFrom`, `validUntil`: Validity period
- `usageLimit`, `totalUsed`: Usage tracking
- `minPurchaseAmount`: Minimum purchase requirement
- `active`: Active status
- `stackable`: Can be combined with other coupons
- `geographicRestrictions`: Country/region restrictions
- `productRestrictions`: Product/package restrictions
- `qrCodeUrl`: QR code image URL

### 4. Credit Model
**Collection**: `credits`

**Key Fields**:
- `clientId`: Client identifier
- `availableAmount`: Current credit balance
- `expirationDate`: Credit expiry
- `active`: Active status
- `remarks`: Reason/notes

### 5. CreditTransaction Model
**Collection**: `credit_transactions`

**Key Fields**:
- `id`: Transaction ID
- `clientId`: Client identifier
- `type`: USAGE, DEPOSIT, WITHDRAWAL, TRANSFER
- `status`: UNPAID, PAID, PENDING
- `amount`: Transaction amount
- `referenceType`: PAYMENT, INVOICE, etc.
- `referenceId`: Related entity ID
- `reversed`: Reversal flag

---

## API Endpoints

### 1. Invoice Management APIs
**Base Path**: `/billing/api/v1/invoice`

#### 1.1 Create Invoice
- **Endpoint**: `POST /create`
- **Description**: Creates a new invoice with PENDING status
- **Flow**:
  1. Generate invoice ID (`INV-{timestamp}`)
  2. Set status to PENDING
  3. Store payment details if provided (BANK_TRANSFER or CHECK_PAYMENT)
  4. Save invoice to MongoDB
  5. Create CommentLog if present in completedPayment
  6. Return invoice response

#### 1.2 Update Invoice
- **Endpoint**: `PUT /update/{id}`
- **Description**: Updates existing invoice details
- **Flow**:
  1. Fetch invoice by ID
  2. Update all fields from request
  3. Handle payment status updates
  4. Save updated invoice

#### 1.3 Get Invoice by ID
- **Endpoint**: `GET /id/{id}`
- **Description**: Retrieves invoice details by ID

#### 1.4 List Invoices
- **Endpoint**: `GET /list`
- **Query Parameters**:
  - `clientId`, `mspId`, `countryId`: Filter by identifiers
  - `status`: List of InvoiceStatus (supports multiple)
  - `roleType`: CLIENT or MSP
  - `startDate`, `endDate`: Date range filter
  - `search`: Text search
  - `offset`, `limit`: Pagination
- **Description**: Returns paginated list with dynamic filtering

#### 1.5 Delete Invoice
- **Endpoint**: `DELETE /delete/{id}`
- **Description**: Deletes invoice by ID

#### 1.6 Download Invoice PDF
- **Endpoint**: `GET /download/{id}`
- **Description**: Generates and downloads invoice PDF

#### 1.7 Download Invoice CSV
- **Endpoint**: `GET /download-csv/{id}`
- **Description**: Generates and downloads invoice as CSV

#### 1.8 Send Invoice Email
- **Endpoint**: `POST /send-email/{id}`
- **Description**: Sends invoice PDF as email attachment

---

### 2. Payment Management APIs
**Base Path**: `/billing/api/v1/payment`

#### 2.1 Log Manual Payment
- **Endpoint**: `POST /manual-entry`
- **Description**: Logs offline payment (bank transfer, check, etc.)
- **Flow**:
  1. Validate invoice exists
  2. Validate payment details match invoice (for BANK_TRANSFER/CHECK_PAYMENT)
  3. Validate payment sources total matches amount
  4. Process coupon if provided:
     - Validate coupon
     - Calculate discount
     - Update coupon usage
  5. Calculate normal bill if no coupon
  6. Create Payment record with status PENDING
  7. If BANK_TRANSFER or CHECK_PAYMENT:
     - Mark payment as SUCCESS immediately
     - Update invoice to PAID
     - Activate client admin
  8. Return payment response

#### 2.2 Log Online Payment
- **Endpoint**: `POST /online-entry`
- **Description**: Initiates online payment via Stripe/PayPal
- **Flow**:
  1. Validate payment sources total matches amount
  2. Fetch invoice and validate status
  3. Process coupon if provided
  4. Calculate bill (with/without coupon)
  5. Create Payment record with status PENDING
  6. Update invoice status to ON_PROGRESS
  7. Process payment sources:
     - **CREDIT**: Deduct from credit account, create credit transaction
     - **STRIPE/PAYPAL**: Create gateway session
  8. If gateway payment:
     - Create Stripe Checkout Session or PayPal Order
     - Store transaction ID
     - Return checkout URL
  9. If fully paid via credits:
     - Mark payment as SUCCESS
     - Update invoice to PAID
  10. Apply commission if eligible
  11. Update coupon usage
  12. Return payment response with checkout URL

#### 2.3 Stripe Webhook
- **Endpoint**: `POST /stripe/webhook/payment`
- **Description**: Handles Stripe webhook events
- **Supported Events**:
  - `payment_intent.created`: Links payment intent to payment record
  - `payment_intent.succeeded`: Updates payment to SUCCESS, marks invoice as PAID, activates client
  - `payment_intent.payment_failed`: Handles payment failure
  - `checkout.session.completed`: Alternative success handler
  - `invoice.paid`: Legacy invoice payment handler
- **Flow**:
  1. Verify Stripe signature
  2. Parse event payload
  3. Extract paymentId from metadata
  4. Update payment status based on event type
  5. Update invoice status if payment succeeded
  6. Activate client admin if payment succeeded

#### 2.4 PayPal Webhook
- **Endpoint**: `POST /paypal/webhook/payment`
- **Description**: Handles PayPal webhook events
- **Supported Events**:
  - `PAYMENT.CAPTURE.COMPLETED`: Payment completed
  - `PAYMENT.CAPTURE.DENIED`: Payment denied
- **Flow**: Similar to Stripe webhook

#### 2.5 Get Payment History
- **Endpoint**: `GET /history`
- **Query Parameters**:
  - `status`, `method`, `startDate`, `endDate`
  - `clientId`, `mspId`, `countryId`, `roleType`
  - `offset`, `limit`: Pagination
- **Description**: Returns paginated payment history with filters

#### 2.6 Download Payment History Excel
- **Endpoint**: `GET /history/download`
- **Description**: Generates Excel file and returns download link

#### 2.7 Get Payment History Summary
- **Endpoint**: `GET /history/summary`
- **Description**: Returns summary statistics (totals, counts, month-over-month changes)

#### 2.8 Export Payment History CSV
- **Endpoint**: `GET /history/export/csv`
- **Description**: Returns CSV file download

#### 2.9 Get Payment Details
- **Endpoint**: `GET /details/{paymentId}`
- **Description**: Returns detailed payment information including invoice data

#### 2.10 Pay for Used Credit
- **Endpoint**: `POST /credit/pay`
- **Description**: Allows paying an existing credit transaction
- **Flow**:
  1. Fetch credit transaction
  2. Validate transaction status
  3. Process payment (manual or online)
  4. Update transaction status to PENDING
  5. Store payment reference

---

### 3. Coupon Management APIs
**Base Path**: `/billing/api/v1/coupon`

#### 3.1 Create Coupon
- **Endpoint**: `POST /create`
- **Description**: Creates a new discount coupon
- **Validations**:
  - Coupon code must be unique
  - Date range validation (validFrom <= validUntil)
  - Currency required for FIXED type
  - Type must be PERCENTAGE or FIXED
- **Flow**:
  1. Check coupon code uniqueness
  2. Validate coupon type and fields
  3. Create Coupon entity
  4. Save to database
  5. Return coupon response

#### 3.2 Generate QR Code
- **Endpoint**: `POST /{code}/qr`
- **Description**: Generates QR code for coupon and uploads to Azure
- **Flow**:
  1. Fetch coupon by code
  2. Validate coupon is active and not expired
  3. Generate QR code image
  4. Upload to Azure Blob Storage
  5. Update coupon with QR code URL
  6. Return QR code response

#### 3.3 Get Coupon by ID
- **Endpoint**: `GET /id/{id}`
- **Description**: Retrieves coupon by ID

#### 3.4 List Coupons
- **Endpoint**: `GET /list`
- **Query Parameters**:
  - `offset`, `limit`: Pagination
  - `searchParam`: Search by coupon code
  - `isActive`: Filter by active status
  - `isExpired`: Filter by expiration
- **Description**: Returns paginated list with filters

#### 3.5 Update Coupon
- **Endpoint**: `PUT /update/{id}`
- **Description**: Updates coupon details
- **Validations**: Same as create

#### 3.6 Delete Coupon
- **Endpoint**: `DELETE /delete/{id}`
- **Description**: Deletes coupon by ID

#### 3.7 Get Coupon by Code
- **Endpoint**: `GET /code/{code}`
- **Description**: Retrieves coupon by code (used for validation)

---

### 4. Credit Management APIs
**Base Path**: `/billing/api/v1/credit`

#### 4.1 Create Credit
- **Endpoint**: `POST /create`
- **Description**: Creates credit account for client
- **Validations**: One credit account per client
- **Flow**:
  1. Check if credit account exists for client
  2. Create Credit entity
  3. Set availableAmount
  4. Save to database

#### 4.2 Update Credit
- **Endpoint**: `PUT /update/{creditId}`
- **Description**: Updates credit account details

#### 4.3 Get Credit by ID
- **Endpoint**: `GET /id/{creditId}`
- **Description**: Retrieves credit account by ID

#### 4.4 Get Credits by Client ID
- **Endpoint**: `GET /client/{clientId}`
- **Description**: Returns credit accounts for client

#### 4.5 Deactivate/Activate Credit
- **Endpoint**: `PUT /switch-mode/{creditId}`
- **Description**: Toggles credit account active status

#### 4.6 Log Credit Transaction
- **Endpoint**: `POST /transaction/log`
- **Description**: Logs credit transaction (USAGE, DEPOSIT, WITHDRAWAL, TRANSFER)
- **Flow**:
  1. Validate amount > 0
  2. Create CreditTransaction
  3. Set status (default: UNPAID)
  4. Save transaction

#### 4.7 Get Credit Transaction by ID
- **Endpoint**: `GET /transaction/id/{transactionId}`
- **Description**: Retrieves transaction by ID

#### 4.8 Get Transactions by Credit ID
- **Endpoint**: `GET /transaction/credit/{creditId}`
- **Description**: Returns all transactions for a credit account

#### 4.9 Get Transactions by Client ID
- **Endpoint**: `GET /transaction/client/{clientId}`
- **Description**: Returns all credit transactions for a client

#### 4.10 Transfer Credit
- **Endpoint**: `POST /transfer`
- **Description**: Transfers credit between clients
- **Flow**:
  1. Validate source and destination clients
  2. Validate sufficient balance
  3. Deduct from source
  4. Add to destination
  5. Create transfer transactions

#### 4.11 Deposit Credit
- **Endpoint**: `POST /deposit`
- **Description**: Adds credit to account

#### 4.12 Withdraw Credit
- **Endpoint**: `POST /withdraw`
- **Description**: Withdraws credit from account

#### 4.13 Get Credit Usage Summary
- **Endpoint**: `GET /transaction/usage-list/{clientId}`
- **Description**: Returns credit usage list with filters
- **Query Parameters**: `invoiceId`, `status`, `type`, `offset`, `limit`

#### 4.14 Get Credit Usage Totals
- **Endpoint**: `GET /transaction/usage-summary/{clientId}`
- **Description**: Returns total used, paid, and due credits

---

### 5. Refund Management APIs
**Base Path**: `/billing/api/v1/refund`

#### 5.1 Create Refund
- **Endpoint**: `POST /create`
- **Description**: Creates refund for a payment
- **Flow**:
  1. Validate payment exists
  2. Validate refund amount <= payment amount
  3. Create Refund entity with status PENDING
  4. Save refund

#### 5.2 Get Refund by ID
- **Endpoint**: `GET /{id}`
- **Description**: Retrieves refund details

#### 5.3 List Refunds
- **Endpoint**: `GET /list`
- **Query Parameters**: `clientId`, `mspId`, `countryId`, `status`, `startDate`, `endDate`, `offset`, `limit`
- **Description**: Returns paginated refund list

#### 5.4 Get Refunds by Payment ID
- **Endpoint**: `GET /by-payment/{paymentId}`
- **Description**: Returns all refunds for a payment

#### 5.5 Get Refunds by Invoice ID
- **Endpoint**: `GET /by-invoice/{invoiceId}`
- **Description**: Returns all refunds for an invoice

#### 5.6 Process Refund
- **Endpoint**: `PUT /{id}/process`
- **Description**: Updates refund status to PROCESSED or FAILED
- **Query Parameters**: `status`, `transactionId`

---

### 6. VAT Configuration APIs
**Base Path**: `/billing/api/v1/vat`

#### 6.1 Create VAT Config
- **Endpoint**: `POST /create`
- **Description**: Creates VAT configuration for country/state

#### 6.2 Update VAT Config
- **Endpoint**: `PUT /update/{countryCode}`
- **Description**: Updates VAT configuration

#### 6.3 Get VAT by Country
- **Endpoint**: `GET /country/{countryCode}`
- **Description**: Retrieves VAT configuration for country

#### 6.4 Get All VAT Configs
- **Endpoint**: `GET /all`
- **Description**: Lists all VAT configurations with pagination

#### 6.5 Delete VAT Config
- **Endpoint**: `DELETE /delete/{countryCode}`
- **Description**: Deletes VAT configuration

---

### 7. Comment Log APIs
**Base Path**: `/billing/api/v1/comment-log`

#### 7.1 Create Comment Log
- **Endpoint**: `POST /create`
- **Description**: Creates comment log for invoice
- **Flow**:
  1. Validate invoiceId exists
  2. Create CommentLog with approved status (default: false)
  3. Link to invoice
  4. Save comment log

#### 7.2 Update Comment Log
- **Endpoint**: `PUT /update/{id}`
- **Description**: Updates comment log details

#### 7.3 Get Comment Log by ID
- **Endpoint**: `GET /id/{id}`
- **Description**: Retrieves comment log

#### 7.4 Get Comment Logs by Invoice ID
- **Endpoint**: `GET /invoice/{invoiceId}`
- **Description**: Returns all comment logs for an invoice

---

## Key Business Flows

### Flow 1: Invoice Creation with Completed Payment
1. Registration service calls `/billing/api/v1/invoice/create`
2. Invoice created with PENDING status
3. If `completedPayment` is provided:
   - Payment method stored (BANK_TRANSFER or CHECK_PAYMENT)
   - Payment details embedded in invoice
   - CommentLog created if present
4. Invoice PDF generated and uploaded to Azure/S3
5. Invoice email sent to client

### Flow 2: Manual Payment Processing
1. Admin logs payment via `/billing/api/v1/payment/manual-entry`
2. Payment validated against invoice
3. Coupon processed if provided
4. Payment record created with PENDING status
5. If BANK_TRANSFER or CHECK_PAYMENT:
   - Payment marked as SUCCESS immediately
   - Invoice updated to PAID
   - Client admin activated
6. Coupon usage incremented

### Flow 3: Online Payment Processing (Stripe)
1. Client initiates payment via `/billing/api/v1/payment/online-entry`
2. Payment record created with PENDING status
3. Invoice status updated to ON_PROGRESS
4. Stripe Checkout Session created:
   - Metadata includes: paymentId, invoiceId, clientId, paymentType
   - Success URL: `{frontEndUrl}/payment-success?amount={amount}&Transactionid={paymentId}&orderNumber={invoiceId}`
   - Cancel URL: `{frontEndUrl}/payment-failed`
5. Checkout URL returned to client
6. Client completes payment on Stripe
7. Stripe sends webhook events:
   - `payment_intent.created`: Links payment intent ID
   - `payment_intent.succeeded`: Updates payment to SUCCESS, invoice to PAID, activates client
8. Client redirected to success URL

### Flow 4: Online Payment Processing (PayPal)
1. Similar to Stripe flow
2. PayPal Order created instead of Checkout Session
3. Webhook events: `PAYMENT.CAPTURE.COMPLETED`, `PAYMENT.CAPTURE.DENIED`

### Flow 5: Credit Payment Processing
1. Payment source includes CREDIT method
2. Credit account validated:
   - Account exists and is active
   - Sufficient balance available
3. Credit deducted from account
4. CreditTransaction created with type USAGE, status UNPAID
5. If fully paid via credits:
   - Payment marked as SUCCESS
   - Invoice updated to PAID
   - Client activated

### Flow 6: Coupon Validation and Application
1. Coupon code/ID provided in payment request
2. Coupon validated:
   - Exists and is active
   - Not expired (validUntil check)
   - Usage limit not exceeded
   - Minimum purchase amount met
   - Geographic restrictions satisfied
   - Product restrictions satisfied
3. Discount calculated:
   - PERCENTAGE: `discountAmount = (subtotal * value) / 100`
   - FIXED: `discountAmount = min(value, subtotal)`
4. VAT recalculated on discounted subtotal
5. Grand total calculated
6. Coupon usage incremented after successful payment

### Flow 7: Multi-Method Payment
1. Payment sources can include multiple methods:
   - CREDIT: Deducted immediately
   - STRIPE/PAYPAL: Gateway session created
   - BANK_TRANSFER/CHECK_PAYMENT: Verified manually
2. Total of all sources must equal payment amount
3. Each source processed according to its method
4. Payment status updated based on all sources

### Flow 8: Invoice Status Updates
- **PENDING**: Initial state when invoice created
- **ON_PROGRESS**: When online payment initiated
- **PAID**: When payment completed successfully
- **FAILED**: When payment fails
- **CANCELLED**: When invoice is cancelled

### Flow 9: Client Activation
- Triggered when invoice is marked as PAID
- Calls registration service to activate client admin
- Endpoint: `PUT {registrationUrl}/client-admin/{clientId}/activate`

### Flow 10: Commission Application
- Applied after successful payment
- Calculated based on commission rate configuration
- Linked to invoice and payment

---

## Integration Points

### 1. Registration Service
- **Activate Client**: `PUT /client-admin/{clientId}/activate`
- **Get Client Info**: For invoice creation
- **Create Invoice**: Called during onboarding

### 2. Notification Service
- **Send Invoice Email**: With PDF attachment
- **Payment Notifications**: Success/failure notifications

### 3. External Services
- **Stripe API**: Payment processing, webhook verification
- **PayPal API**: Payment processing, webhook verification
- **Azure Blob Storage**: Invoice PDF, QR code storage
- **AWS S3**: Alternative storage for invoices

---

## Error Handling

### Common Exceptions
1. **ResourceNotFoundException**: Entity not found
2. **BillingServiceException**: General business logic errors
3. **CouponNotFoundException**: Coupon not found
4. **CouponValidationException**: Coupon validation failed
5. **CouponAlreadyExistsException**: Duplicate coupon code
6. **InsufficientCreditBalanceException**: Not enough credit
7. **CreditAccountInactiveException**: Credit account inactive
8. **UnsupportedCouponTypeException**: Invalid coupon type

### Error Response Format
```json
{
  "message": "Error message",
  "statusCode": 400,
  "data": null
}
```

---

## Security Considerations

1. **JWT Authentication**: All endpoints require JWT token (except webhooks)
2. **Webhook Signature Verification**: Stripe and PayPal webhooks verified
3. **Public URLs**: Webhook endpoints added to `public-urls.json` in gateway
4. **SSL/TLS**: HTTPS required for all external communications

---

## Configuration

### Key Properties
- `stripe.secret-key`: Stripe API secret key
- `stripe.webhook-secret`: Stripe webhook signing secret
- `paypal.client-id`: PayPal client ID
- `paypal.secret`: PayPal secret
- `paypal.mode`: sandbox or live
- `service.registration.url`: Registration service URL
- `service.frontEnd.url`: Frontend URL for redirects
- `aws.s3.bucket-name`: S3 bucket for invoice storage

---

## Testing Considerations

1. **Stripe Test Mode**: Use test API keys and test cards
2. **PayPal Sandbox**: Use sandbox credentials
3. **Webhook Testing**: Use Stripe CLI or PayPal webhook simulator
4. **Mock Services**: Mock external services for unit tests

---

## Performance Optimizations

1. **Pagination**: All list endpoints support pagination
2. **Indexing**: MongoDB indexes on frequently queried fields
3. **Caching**: Consider caching VAT configurations and coupon lookups
4. **Async Processing**: Webhook processing can be async

---

## Future Enhancements

1. **Payment Retry Logic**: Automatic retry for failed payments
2. **Refund Automation**: Automatic refund processing
3. **Multi-Currency Support**: Enhanced currency handling
4. **Payment Plans**: Installment payment support
5. **Advanced Analytics**: More detailed reporting and analytics

