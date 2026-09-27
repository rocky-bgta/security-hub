**Wednesday, May 6, 2026**

# DB Script

## MongoDB - Add/normalize assignedFor in sub_packages collection

```javascript
use("cms");

// 1) Initialize missing assignedFor field to null for existing records
db.getCollection("sub_packages").updateMany(
  {
    $or: [{ assignedFor: { $exists: false } }]
  },
  {
    $set: { assignedFor: null }
  }
);

// 2) Normalize old enum values to the updated enum values
db.getCollection("sub_packages").updateMany(
  { assignedFor: "PHISHING" },
  { $set: { assignedFor: "SIMULATED_PHISHING" } }
);

db.getCollection("sub_packages").updateMany(
  { assignedFor: "PHISHING_AND_TRAINING" },
  { $set: { assignedFor: "PHISHING_WITH_TRAINING" } }
);

db.getCollection("sub_packages").updateMany(
  { assignedFor: "PHISHING_AND_DEFAULT_TRAINING" },
  { $set: { assignedFor: "PHISHING_TRAINING_FOR_ALL" } }
);
```


**Sunday, May 10, 2026**

## MongoDB - Backfill `assignedFor` in campaigns collection

```javascript
use("Phishing");

// 1) For campaigns with PHISHING_WITH_TRAINING, set assignedFor accordingly
db.getCollection("campaigns").updateMany(
  {
    campaignType: "PHISHING_WITH_TRAINING",
    $or: [
      { assignedFor: { $exists: false } },
      { assignedFor: null }
    ]
  },
  {
    $set: {
      assignedFor: "PHISHING_WITH_TRAINING"
    }
  }
);

// 2) For SIMULATED_PHISHING (or missing campaignType), set a safe default
db.getCollection("campaigns").updateMany(
  {
    $or: [
      { campaignType: "SIMULATED_PHISHING" },
      { campaignType: { $exists: false } },
      { campaignType: null }
    ],
    $or: [
      { assignedFor: { $exists: false } },
      { assignedFor: null }
    ]
  },
  {
    $set: {
      assignedFor: "SIMULATED_PHISHING"
    }
  }
);
```