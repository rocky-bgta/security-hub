**Sunday, May 3, 2026**

# DB Script

## MongoDB - Add fields to EmailActivity collection

```javascript
use("Phishing");

db.getCollection("EmailActivity").updateMany(
  {},
  {
    $set: {
      recipientName: "",
      recipientEmail: "",
      campaignName: ""
    }
  }
);
```

**Sunday, May 5, 2026**

## MongoDB - Add AI context fields to email_templates collection

```javascript
use("Phishing");

db.getCollection("email_templates").updateMany(
  {},
  {
    $set: {
      department: null,
      expectedUserAction: null,
      triggerEvent: null,
      socialEngineeringStrategy: null,
      attackTechnique: null
    }
  }
);
```
```javascript

// Select DB first if needed:
// use("your_database_name");

const results = {};

// 1) email_templates
results.email_templates_easy = db.email_templates.updateMany(
  { difficultyLevel: "EASY" },
  { $set: { difficultyLevel: "BEGINNER" } }
);
results.email_templates_medium = db.email_templates.updateMany(
  { difficultyLevel: "MEDIUM" },
  { $set: { difficultyLevel: "INTERMEDIATE" } }
);
results.email_templates_hard = db.email_templates.updateMany(
  { difficultyLevel: "HARD" },
  { $set: { difficultyLevel: "ADVANCED" } }
);

// 2) landing_pages
results.landing_pages_easy = db.landing_pages.updateMany(
  { difficultyLevel: "EASY" },
  { $set: { difficultyLevel: "BEGINNER" } }
);
results.landing_pages_medium = db.landing_pages.updateMany(
  { difficultyLevel: "MEDIUM" },
  { $set: { difficultyLevel: "INTERMEDIATE" } }
);
results.landing_pages_hard = db.landing_pages.updateMany(
  { difficultyLevel: "HARD" },
  { $set: { difficultyLevel: "ADVANCED" } }
);

// 3) ai_content_generation_jobs
results.jobs_email_easy = db.ai_content_generation_jobs.updateMany(
  { "emailTemplateRequest.difficultyLevel": "EASY" },
  { $set: { "emailTemplateRequest.difficultyLevel": "BEGINNER" } }
);
results.jobs_email_medium = db.ai_content_generation_jobs.updateMany(
  { "emailTemplateRequest.difficultyLevel": "MEDIUM" },
  { $set: { "emailTemplateRequest.difficultyLevel": "INTERMEDIATE" } }
);
results.jobs_email_hard = db.ai_content_generation_jobs.updateMany(
  { "emailTemplateRequest.difficultyLevel": "HARD" },
  { $set: { "emailTemplateRequest.difficultyLevel": "ADVANCED" } }
);

results.jobs_landing_easy = db.ai_content_generation_jobs.updateMany(
  { "landingPageRequest.difficultyLevel": "EASY" },
  { $set: { "landingPageRequest.difficultyLevel": "BEGINNER" } }
);
results.jobs_landing_medium = db.ai_content_generation_jobs.updateMany(
  { "landingPageRequest.difficultyLevel": "MEDIUM" },
  { $set: { "landingPageRequest.difficultyLevel": "INTERMEDIATE" } }
);
results.jobs_landing_hard = db.ai_content_generation_jobs.updateMany(
  { "landingPageRequest.difficultyLevel": "HARD" },
  { $set: { "landingPageRequest.difficultyLevel": "ADVANCED" } }
);

results;
```

**Wednesday, May 6, 2026**

## MongoDB - Backfill new fields in campaign_recipients collection

```javascript
use("Phishing");

// 1) Initialize missing fields to null for all existing recipients
db.getCollection("campaign_recipients").updateMany(
  {
    $or: [
      { campaignName: { $exists: false } },
      { department: { $exists: false } },
      { organizationName: { $exists: false } },
      { organizationDomain: { $exists: false } }
    ]
  },
  {
    $set: {
      campaignName: null,
      department: null,
      organizationName: null,
      organizationDomain: null
    }
  }
);

// 2) Backfill campaignName from campaigns collection using campaignId
const recipients = db.getCollection("campaign_recipients")
  .find(
    {
      campaignId: { $exists: true, $ne: null, $ne: "" },
      $or: [{ campaignName: null }, { campaignName: "" }]
    },
    { _id: 1, campaignId: 1 }
  )
  .toArray();

const campaignIds = [...new Set(recipients.map(r => r.campaignId))];

const campaigns = db.getCollection("campaigns")
  .find(
    { _id: { $in: campaignIds } },
    { _id: 1, name: 1, campaignName: 1 }
  )
  .toArray();

const campaignNameMap = {};
campaigns.forEach(c => {
  campaignNameMap[c._id] = c.campaignName || c.name || null;
});

const bulkOps = recipients
  .map(r => {
    const name = campaignNameMap[r.campaignId];
    if (!name) return null;
    return {
      updateOne: {
        filter: { _id: r._id },
        update: { $set: { campaignName: name } }
      }
    };
  })
  .filter(op => op !== null);

if (bulkOps.length > 0) {
  db.getCollection("campaign_recipients").bulkWrite(bulkOps);
}
```