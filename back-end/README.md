# Introduction 
    ASAT version 2

# Getting Started
TODO: Guide users through getting your code up and running on their own system. In this section you can talk about:
1.	Installation process
2.	Software dependencies
3.	Latest releases
4.	API references

# Git Structure 
    Monorepo
    /
    packages/
        asat-api/
            all api service
        asat-auth/
            authentication service
    lib/
        Shared libraries and utilities.
    scripts/
        Build and deployment scripts.

# Build and Test
TODO: Describe and show how to build your code and run the tests. 

# Contribute
TODO: Explain how other users and developers can contribute to make your code better. 

If you want to learn more about creating good readme files then refer the following [guidelines](https://docs.microsoft.com/en-us/azure/devops/repos/git/create-a-readme?view=azure-devops). You can also seek inspiration from the below readme files:
- [ASP.NET Core](https://github.com/aspnet/Home)
- [Visual Studio Code](https://github.com/Microsoft/vscode)
- [Chakra Core](https://github.com/Microsoft/ChakraCore)




# ✅ Notification Service Email Template

### ✅ Welcome Email Template request
```json
curl -X 'POST' \
'http://localhost:5656/xyz/api/v1/send' \
-H 'accept: application/json' \
-H 'Content-Type: application/json' \
-d '{
"to": "Shubhajit.Nandi@aspiretss.com",
"subject": "Welcome to AspireLMS Your learning journey starts now.",
"templateId": "welcome-email",
"templateModel": {
"userName": "Shubhajit",
"password":"test",
"signupDate": "23 July 2025",
"lmsName": "AspireLMS",
"logoUrl": "test",
"accountLink": "http://example.com/login",
"helpCenterLink": "test"
},
"attachments": [
{
"bucketName": "asatv2-media-bucket",
"objectKey": "invoice1.pdf"
}
]
}'

```


### ✅ Payment invoice Email Template request
```json
{
  "to": "customer@example.com",
  "subject": "Your Invoice [INV-2025-07-29-001] from SecureCyber Academy",
  "templateId": "payment-invoice",
  "templateModel": {
    "company": {
      "name": "SecureCyber Academy"
    },
    "customer": {
      "name": "Jordan Professional Services"
    },
    "invoice": {
      "id": "INV-2025-07-29-001",
      "date": "July 29, 2025",
      "paymentLink": "https://your-lms.com/pay/INV-2025-07-29-001",
      "lineItems": [
        {
          "description": "Certified Ethical Hacker (CEH) - Annual Subscription",
          "total": "$1,200.00"
        },
        {
          "description": "Advanced Penetration Testing Labs - Seat License",
          "total": "$750.00"
        }
      ],
      "totalDue": "$2,106.00"
    }
  },
  "attachments": []
}
```

### ✅ SubPackage creation  request
```json
{
  "name": "HrGroup",
  "description": "test",
  "productId": "b365fbd8-80d6-4239-b261-a6146d2a9402",
  "packageId": "be76fce2-d5f2-41b9-bb67-1de31fba3dd3",
  "clientId": "be76fce2-d5f2-41b9-bb67-1de31fba3d123",
  "topicId": [
    "39b68e6c-18b9-4830-8d6c-243197c314a0", "50e2d839-bdbe-42ff-9f81-df4b6864d0c5"
  ],
  "createdBy": "Shubhajit",
  "status": "ACTIVE"
}
```

