/**
 * Migrates legacy email_templates.socialEngineeringStrategy (string)
 * to embedded { id: "<string>", name } from social_engineering_strategies.
 *
 * Run after email-templates-social-engineering-strategy-enum-rename.js.
 *
 * Matches social_engineering_strategies.name (case-insensitive). Skips templates already migrated.
 *
 * Local run (default MongoDB port, no auth) from Git Bash:
 *
 *   cd /d/AspireTech/Workspace2/ASAT-V2-BACKEND/services/phishing/core/src/main/resources/migrations
 *   mongosh "mongodb://localhost:27017/phishing" --file email-templates-social-engineering-strategy-embedded.js
 */
(function () {
  function escapeRegex(s) {
    return String(s).replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  }

  function toIdString(value) {
    if (value == null) return null;
    if (typeof value === "string") return value;
    if (typeof value.toString === "function") return value.toString();
    return String(value);
  }

  function findSnapshot(legacy) {
    if (legacy == null) return null;
    const normalized = String(legacy).trim();
    if (!normalized) return null;

    let doc = db.social_engineering_strategies.findOne({
      name: { $regex: "^" + escapeRegex(normalized) + "$", $options: "i" },
    });
    if (!doc) {
      doc = db.social_engineering_strategies.findOne({
        name: { $regex: normalized.replace(/_/g, " "), $options: "i" },
      });
    }
    if (!doc) return null;

    return {
      id: toIdString(doc._id),
      name: doc.name,
    };
  }

  let updated = 0;
  let skipped = 0;
  let alreadyMigrated = 0;

  db.email_templates.find({}).forEach((template) => {
    const field = template.socialEngineeringStrategy;

    if (field != null && typeof field === "object" && field.name != null) {
      if (typeof field.id === "string") {
        alreadyMigrated++;
        return;
      }
      if (field.id != null) {
        db.email_templates.updateOne(
          { _id: template._id },
          {
            $set: {
              socialEngineeringStrategy: { id: toIdString(field.id), name: field.name },
            },
          }
        );
        updated++;
        return;
      }
      alreadyMigrated++;
      return;
    }

    if (typeof field !== "string") {
      if (field != null) {
        print(
          "SKIP template " +
            template._id +
            " - socialEngineeringStrategy is not a legacy string: " +
            JSON.stringify(field)
        );
        skipped++;
      }
      return;
    }

    const snap = findSnapshot(field);
    if (!snap) {
      print(
        'SKIP template ' +
          template._id +
          ' - no social_engineering_strategies match for: "' +
          field +
          '"'
      );
      skipped++;
      return;
    }

    db.email_templates.updateOne(
      { _id: template._id },
      { $set: { socialEngineeringStrategy: snap } }
    );
    updated++;
  });

  print(
    "Migration complete. updated=" +
      updated +
      " skipped=" +
      skipped +
      " alreadyMigrated=" +
      alreadyMigrated
  );
})();
