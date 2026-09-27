# Product Reassignment API - Test Cases

**Endpoint**: `POST /api/v1/client-admin/product/reassign`

**Base URL**: `http://localhost:8080/registration` (adjust based on your environment)

---

## Important Flow Details

### Reassignment Process
1. **Product Addition**: Reassigned products are **added** to the existing product list. Original products are NOT removed.
2. **License Status**: All reassigned products start with `licenseStatus = "PENDING"`, regardless of payment status.
3. **License Activation**: Licenses must be activated separately via `PUT /api/v1/client-admin/activate-license/{clientId}` endpoint.
4. **Invoice Creation**: A new invoice is created for the reassigned products with status `PENDING`.
5. **Payment Status**: 
   - `PENDING`: Payment not completed, products remain PENDING until payment and activation
   - `COMPLETED`: Payment details stored, but products still need activation
6. **CMS Update**: Dashboard is automatically updated after successful reassignment.
7. **Rollback**: If CMS update fails, all changes are automatically rolled back.

### Key Points
- ✅ Products are **added** to existing list (not replaced)
- ✅ All products start as **PENDING** status
- ✅ **Separate activation** required via activate-license endpoint
- ✅ Invoice PDF is generated and emailed automatically
- ✅ **MSP ID is optional** - if not provided, uses client admin's existing MSP ID
- ✅ If MSP ID is provided, it must match client's existing MSP ID

---

## Test Case 1: Basic Reassignment - Single Product (Pending Payment) - Without MSP ID

**Description**: Reassign a single product to an existing client with pending payment status. MSP ID is optional and will be automatically retrieved from the client admin record.

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 10,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    }
  ],
  "invoice": {
    "subtotal": 999.90,
    "discountType": null,
    "discountPercentage": null,
    "discountAmount": null,
    "couponCode": null,
    "vatRate": 5.0,
    "vatAmount": 49.995,
    "totalAmount": 1049.895,
    "paymentStatus": "PENDING",
    "completedPayment": null
  }
}
```

---

## Test Case 2: Multiple Products Reassignment (Pending Payment) - With MSP ID

**Description**: Reassign multiple products to an existing client with pending payment status. MSP ID is provided for validation.

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 10,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    },
    {
      "productId": "product-xyz-002",
      "packageId": "package-def-456",
      "licenseCount": 5,
      "pricePerLicense": 149.99,
      "validityPeriod": 6,
      "validityUnit": "MONTH",
      "productName": "Data Privacy Compliance",
      "packageName": "Privacy Pro Package"
    }
  ],
  "invoice": {
    "subtotal": 1749.85,
    "discountType": null,
    "discountPercentage": null,
    "discountAmount": null,
    "couponCode": null,
    "vatRate": 5.0,
    "vatAmount": 87.4925,
    "totalAmount": 1837.3425,
    "paymentStatus": "PENDING",
    "completedPayment": null
  }
}
```

---

## Test Case 3: Single Product with Percentage Discount (Pending Payment)

**Description**: Reassign a product with percentage-based discount applied.

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 20,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    }
  ],
  "invoice": {
    "subtotal": 1799.82,
    "discountType": "PERCENTAGE",
    "discountPercentage": 10.0,
    "discountAmount": 179.982,
    "couponCode": null,
    "vatRate": 5.0,
    "vatAmount": 80.9919,
    "totalAmount": 1700.8299,
    "paymentStatus": "PENDING",
    "completedPayment": null
  }
}
```

---

## Test Case 4: Single Product with Flat Discount (Pending Payment)

**Description**: Reassign a product with flat amount discount applied.

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 15,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    }
  ],
  "invoice": {
    "subtotal": 1399.85,
    "discountType": "FLAT",
    "discountPercentage": null,
    "discountAmount": 100.0,
    "couponCode": null,
    "vatRate": 5.0,
    "vatAmount": 64.9925,
    "totalAmount": 1364.8425,
    "paymentStatus": "PENDING",
    "completedPayment": null
  }
}
```

---

## Test Case 5: Single Product with Coupon Code (Pending Payment)

**Description**: Reassign a product with coupon code applied.

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 25,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    }
  ],
  "invoice": {
    "subtotal": 2249.75,
    "discountType": "PERCENTAGE",
    "discountPercentage": 15.0,
    "discountAmount": 337.4625,
    "couponCode": "SUMMER2024",
    "vatRate": 5.0,
    "vatAmount": 95.614375,
    "totalAmount": 2007.901875,
    "paymentStatus": "PENDING",
    "completedPayment": null
  }
}
```

---

## Test Case 6: Single Product with Completed Bank Transfer Payment

**Description**: Reassign a product with completed bank transfer payment.

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 10,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    }
  ],
  "invoice": {
    "subtotal": 999.90,
    "discountType": null,
    "discountPercentage": null,
    "discountAmount": null,
    "couponCode": null,
    "vatRate": 5.0,
    "vatAmount": 49.995,
    "totalAmount": 1049.895,
    "paymentStatus": "COMPLETED",
    "completedPayment": {
      "paymentMethod": "BANK_TRANSFER",
      "bankTransferDetails": {
        "bankName": "Chase Bank",
        "accountNumber": "123456789",
        "bankBranchName": "Main Branch",
        "transactionNumber": "TXN-123456",
        "paymentDate": "2025-12-07",
        "paymentAmount": 1049.895,
        "transactionReceiptUrl": "https://s3.amazonaws.com/receipts/txn-123456.pdf"
      },
      "checkPaymentDetails": null
    }
  }
}
```

---

## Test Case 7: Single Product with Completed Check Payment

**Description**: Reassign a product with completed check payment.

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 10,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    }
  ],
  "invoice": {
    "subtotal": 999.90,
    "discountType": null,
    "discountPercentage": null,
    "discountAmount": null,
    "couponCode": null,
    "vatRate": 5.0,
    "vatAmount": 49.995,
    "totalAmount": 1049.895,
    "paymentStatus": "COMPLETED",
    "completedPayment": {
      "paymentMethod": "CHECK_PAYMENT",
      "bankTransferDetails": null,
      "checkPaymentDetails": {
        "checkNumber": "CHK-789012",
        "bankName": "Bank of America",
        "branchName": "Downtown Branch",
        "paymentDate": "2025-12-07",
        "checkImageUrl": "https://s3.amazonaws.com/checks/chk-789012.jpg"
      }
    }
  }
}
```

---

## Test Case 8: Multiple Products with Completed Payment and Discount

**Description**: Reassign multiple products with completed payment and discount applied.

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 10,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    },
    {
      "productId": "product-xyz-002",
      "packageId": "package-def-456",
      "licenseCount": 5,
      "pricePerLicense": 149.99,
      "validityPeriod": 6,
      "validityUnit": "MONTH",
      "productName": "Data Privacy Compliance",
      "packageName": "Privacy Pro Package"
    }
  ],
  "invoice": {
    "subtotal": 1749.85,
    "discountType": "PERCENTAGE",
    "discountPercentage": 10.0,
    "discountAmount": 174.985,
    "couponCode": "BULK2024",
    "vatRate": 5.0,
    "vatAmount": 78.74325,
    "totalAmount": 1653.60825,
    "paymentStatus": "COMPLETED",
    "completedPayment": {
      "paymentMethod": "BANK_TRANSFER",
      "bankTransferDetails": {
        "bankName": "Wells Fargo",
        "accountNumber": "987654321",
        "bankBranchName": "Corporate Branch",
        "transactionNumber": "TXN-987654",
        "paymentDate": "2025-12-07",
        "paymentAmount": 1653.60825,
        "transactionReceiptUrl": "https://s3.amazonaws.com/receipts/txn-987654.pdf"
      },
      "checkPaymentDetails": null
    }
  }
}
```

---

## Test Case 9: Yearly Validity Period

**Description**: Reassign a product with yearly validity period.

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 10,
      "pricePerLicense": 99.99,
      "validityPeriod": 1,
      "validityUnit": "YEAR",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Annual Package"
    }
  ],
  "invoice": {
    "subtotal": 999.90,
    "discountType": null,
    "discountPercentage": null,
    "discountAmount": null,
    "couponCode": null,
    "vatRate": 5.0,
    "vatAmount": 49.995,
    "totalAmount": 1049.895,
    "paymentStatus": "PENDING",
    "completedPayment": null
  }
}
```

---

## Test Case 10: Minimum Values (Edge Case)

**Description**: Test with minimum valid values (1 license, minimum price).

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 1,
      "pricePerLicense": 0.01,
      "validityPeriod": 1,
      "validityUnit": "MONTH",
      "productName": "Test Product",
      "packageName": "Test Package"
    }
  ],
  "invoice": {
    "subtotal": 0.01,
    "discountType": null,
    "discountPercentage": null,
    "discountAmount": null,
    "couponCode": null,
    "vatRate": 0.0,
    "vatAmount": 0.0,
    "totalAmount": 0.01,
    "paymentStatus": "PENDING",
    "completedPayment": null
  }
}
```

---

## Test Case 11: Large Quantity (Edge Case)

**Description**: Test with large number of licenses.

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 1000,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    }
  ],
  "invoice": {
    "subtotal": 99990.0,
    "discountType": "PERCENTAGE",
    "discountPercentage": 20.0,
    "discountAmount": 19998.0,
    "couponCode": "ENTERPRISE2024",
    "vatRate": 5.0,
    "vatAmount": 3999.6,
    "totalAmount": 83991.6,
    "paymentStatus": "PENDING",
    "completedPayment": null
  }
}
```

---

## Test Case 12: Zero VAT Rate

**Description**: Test with zero VAT rate (tax-exempt scenario).

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 10,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    }
  ],
  "invoice": {
    "subtotal": 999.90,
    "discountType": null,
    "discountPercentage": null,
    "discountAmount": null,
    "couponCode": null,
    "vatRate": 0.0,
    "vatAmount": 0.0,
    "totalAmount": 999.90,
    "paymentStatus": "PENDING",
    "completedPayment": null
  }
}
```

---

## Test Case 13: Multiple Products with Different Validity Units

**Description**: Reassign multiple products with different validity periods (monthly and yearly).

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 10,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    },
    {
      "productId": "product-xyz-003",
      "packageId": "package-ghi-789",
      "licenseCount": 5,
      "pricePerLicense": 999.99,
      "validityPeriod": 2,
      "validityUnit": "YEAR",
      "productName": "Enterprise Security Suite",
      "packageName": "Enterprise Annual Package"
    }
  ],
  "invoice": {
    "subtotal": 5999.85,
    "discountType": "PERCENTAGE",
    "discountPercentage": 15.0,
    "discountAmount": 899.9775,
    "couponCode": "ENTERPRISE2024",
    "vatRate": 5.0,
    "vatAmount": 254.993625,
    "totalAmount": 5354.866125,
    "paymentStatus": "PENDING",
    "completedPayment": null
  }
}
```

---

## Test Case 14: Complete Flow - Reassignment + License Activation

**Description**: This demonstrates the complete workflow:
1. Reassign products with COMPLETED payment
2. Activate licenses separately

**Step 1: Reassign Products (with COMPLETED payment)**

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 10,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    }
  ],
  "invoice": {
    "subtotal": 999.90,
    "discountType": null,
    "discountPercentage": null,
    "discountAmount": null,
    "couponCode": null,
    "vatRate": 5.0,
    "vatAmount": 49.995,
    "totalAmount": 1049.895,
    "paymentStatus": "COMPLETED",
    "completedPayment": {
      "paymentMethod": "BANK_TRANSFER",
      "bankTransferDetails": {
        "bankName": "Chase Bank",
        "accountNumber": "123456789",
        "bankBranchName": "Main Branch",
        "transactionNumber": "TXN-123456",
        "paymentDate": "2025-12-07",
        "paymentAmount": 1049.895,
        "transactionReceiptUrl": "https://s3.amazonaws.com/receipts/txn-123456.pdf"
      },
      "checkPaymentDetails": null
    }
  }
}
```

**Expected Response**:
```json
{
  "message": "Products reassigned successfully",
  "status": 200,
  "data": {
    "clientAdminId": "client-admin-uuid-123",
    "clientAdminEmail": "admin@example.com",
    "organizationName": "Example Organization",
    "assignedProductIds": ["new-product-uuid-1"],
    "totalProductsReassigned": 1,
    "totalAmount": 999.90,
    "invoiceId": "invoice-uuid-789",
    "portalLink": "https://portal.aspireelearning.com/auth/login"
  }
}
```

**Step 2: Activate Licenses** (Separate API Call)

**Endpoint**: `PUT /api/v1/client-admin/activate-license/{clientId}`

**Request**: No body required, just the clientId in path

**Expected Response**:
```json
{
  "message": "Client license activated successfully",
  "status": 200,
  "data": null
}
```

**Note**: After activation, the reassigned products will have `licenseStatus = "ACTIVE"` and will be visible to the client.

---

## Error Test Cases

### Error Case 1: Missing Required Fields

**Description**: Test with missing clientAdminId (should return 400 Bad Request).

```json
{
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 10,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    }
  ],
  "invoice": {
    "subtotal": 999.90,
    "vatRate": 5.0,
    "vatAmount": 49.995,
    "totalAmount": 1049.895,
    "paymentStatus": "PENDING"
  }
}
```

### Error Case 2: Invalid MSP ID Mismatch

**Description**: Test with MSP ID that doesn't match the client's MSP (should return error). Note: If you omit `mspId`, this error won't occur as the system uses the client's existing MSP ID.

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "wrong-msp-uuid",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 10,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    }
  ],
  "invoice": {
    "subtotal": 999.90,
    "vatRate": 5.0,
    "vatAmount": 49.995,
    "totalAmount": 1049.895,
    "paymentStatus": "PENDING"
  }
}
```

### Error Case 3: Invalid License Count

**Description**: Test with zero or negative license count (should return 400 Bad Request).

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 0,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    }
  ],
  "invoice": {
    "subtotal": 0.0,
    "vatRate": 5.0,
    "vatAmount": 0.0,
    "totalAmount": 0.0,
    "paymentStatus": "PENDING"
  }
}
```

### Error Case 4: Invalid Price Per License

**Description**: Test with zero or negative price (should return 400 Bad Request).

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 10,
      "pricePerLicense": 0.0,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    }
  ],
  "invoice": {
    "subtotal": 0.0,
    "vatRate": 5.0,
    "vatAmount": 0.0,
    "totalAmount": 0.0,
    "paymentStatus": "PENDING"
  }
}
```

### Error Case 5: Invalid Validity Unit

**Description**: Test with invalid validity unit (should return 400 Bad Request).

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 10,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "WEEK",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    }
  ],
  "invoice": {
    "subtotal": 999.90,
    "vatRate": 5.0,
    "vatAmount": 49.995,
    "totalAmount": 1049.895,
    "paymentStatus": "PENDING"
  }
}
```

### Error Case 6: Missing Payment Details for Completed Payment

**Description**: Test with COMPLETED status but missing completedPayment (should return 400 Bad Request).

```json
{
  "clientAdminId": "client-admin-uuid-123",
  "mspId": "msp-uuid-456",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 10,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    }
  ],
  "invoice": {
    "subtotal": 999.90,
    "vatRate": 5.0,
    "vatAmount": 49.995,
    "totalAmount": 1049.895,
    "paymentStatus": "COMPLETED",
    "completedPayment": null
  }
}
```

### Error Case 7: Invalid Client Admin ID

**Description**: Test with non-existent clientAdminId (should return 404 Not Found). Note: `mspId` is optional and can be omitted.

```json
{
  "clientAdminId": "non-existent-client-id",
  "productSelections": [
    {
      "productId": "product-xyz-001",
      "packageId": "package-abc-123",
      "licenseCount": 10,
      "pricePerLicense": 99.99,
      "validityPeriod": 12,
      "validityUnit": "MONTH",
      "productName": "Cybersecurity Essentials",
      "packageName": "Cyber Pro Monthly Package"
    }
  ],
  "invoice": {
    "subtotal": 999.90,
    "vatRate": 5.0,
    "vatAmount": 49.995,
    "totalAmount": 1049.895,
    "paymentStatus": "PENDING"
  }
}
```

---

## Postman Collection Setup

### Environment Variables

Create a Postman environment with these variables:

```
base_url: http://localhost:8080/registration
client_admin_id: client-admin-uuid-123
msp_id: msp-uuid-456
product_id_1: product-xyz-001
package_id_1: package-abc-123
product_id_2: product-xyz-002
package_id_2: package-def-456
```

### Headers

```
Content-Type: application/json
Accept: application/json
```

### Expected Response (Success)

```json
{
  "message": "Products reassigned successfully",
  "status": 200,
  "data": {
    "clientAdminId": "client-admin-uuid-123",
    "clientAdminEmail": "admin@example.com",
    "organizationName": "Example Organization",
    "assignedProductIds": ["product-uuid-1", "product-uuid-2"],
    "totalProductsReassigned": 2,
    "totalAmount": 1837.3425,
    "invoiceId": "invoice-uuid-789",
    "portalLink": "https://portal.aspireelearning.com/auth/login"
  }
}
```

### Expected Response (Error)

```json
{
  "message": "Client admin not found with ID: non-existent-client-id",
  "status": 404,
  "data": null
}
```

---

## Notes

1. **Replace UUIDs**: Replace all placeholder UUIDs (`client-admin-uuid-123`, `msp-uuid-456`, etc.) with actual IDs from your database.

2. **Payment Status**: 
   - `PENDING`: Payment is not yet completed. Products are created with `licenseStatus = "PENDING"`.
   - `COMPLETED`: Payment is completed (requires `completedPayment` object). Payment details are stored, but **products are still created with `licenseStatus = "PENDING"`** and must be activated separately via `/activate-license/{clientId}` endpoint.

3. **Important - License Activation**: 
   - **All reassigned products start with `licenseStatus = "PENDING"`**, regardless of payment status.
   - Even if `paymentStatus = "COMPLETED"`, licenses are NOT automatically activated.
   - You must call `PUT /api/v1/client-admin/activate-license/{clientId}` separately to activate licenses.
   - Only `ACTIVE` products are visible to clients in their dashboard.

4. **Discount Types**:
   - `FLAT`: Fixed amount discount
   - `PERCENTAGE`: Percentage-based discount

5. **Validity Units**:
   - `MONTH`: Monthly validity period
   - `YEAR`: Yearly validity period

6. **Payment Methods** (for COMPLETED status):
   - `BANK_TRANSFER`: Requires `bankTransferDetails` object with all required fields
   - `CHECK_PAYMENT`: Requires `checkPaymentDetails` object with all required fields

7. **VAT Calculation**: Ensure `vatAmount` = `subtotal` × `vatRate` / 100

8. **Total Amount**: Ensure `totalAmount` = `subtotal` - `discountAmount` + `vatAmount`

9. **Invoice Status**: 
   - Invoice is created with `status = "PENDING"` initially, even if `paymentStatus = "COMPLETED"`.
   - Payment details are stored for reference.
   - Invoice status can be updated separately if needed.

10. **CMS Integration**: The API automatically updates the CMS dashboard after successful reassignment. If this fails, automatic rollback occurs.

11. **Rollback**: If CMS update fails, the system performs automatic rollback of:
    - Newly created ClientProduct records
    - Updated ClientAdmin product IDs list
    - Any other related entities

12. **Invoice PDF**: A PDF invoice is generated and sent via email notification after successful reassignment (non-blocking operation).

13. **MSP ID**: 
    - **Optional field** - If not provided, the system automatically uses the client admin's existing MSP ID
    - If provided, it must match the client admin's existing MSP ID, otherwise the request will fail with an error
    - Recommended: Omit `mspId` to let the system use the client's existing MSP ID automatically

14. **Product IDs**: The reassigned products are **added** to the existing product list. Original products are not removed.

