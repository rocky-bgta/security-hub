/**
 * Migrates legacy sender_profiles.personalizationLevel (string)
 * to embedded { id: "<string>", name } from personalization_levels.
 *
 * Matches personalization_levels.name (case-insensitive). Skips profiles already migrated.
 *
 * Run after sender-profiles-personalization-level-enum-rename.js.
 *
 * Local run (default MongoDB port, no auth) from Git Bash:
 *
 *   cd /d/AspireTech/Workspace2/ASAT-V2-BACKEND/services/phishing/core/src/main/resources/migrations
 *   mongosh "mongodb://localhost:27017/phishing" --file sender-profiles-personalization-level-embedded.js
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

  function findPersonalizationSnapshot(legacy) {
    if (legacy == null) return null;
    const normalized = String(legacy).trim();
    if (!normalized) return null;

    let doc = db.personalization_levels.findOne({
      name: { $regex: "^" + escapeRegex(normalized) + "$", $options: "i" },
    });
    if (!doc) {
      doc = db.personalization_levels.findOne({
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

  db.sender_profiles.find({}).forEach((profile) => {
    const field = profile.personalizationLevel;

    if (field != null && typeof field === "object" && field.name != null) {
      if (typeof field.id === "string") {
        alreadyMigrated++;
        return;
      }
      if (field.id != null) {
        db.sender_profiles.updateOne(
          { _id: profile._id },
          {
            $set: {
              personalizationLevel: { id: toIdString(field.id), name: field.name },
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
          "SKIP profile " +
            profile._id +
            " - personalizationLevel is not a legacy string: " +
            JSON.stringify(field)
        );
        skipped++;
      }
      return;
    }

    const snap = findPersonalizationSnapshot(field);
    if (!snap) {
      print(
        'SKIP profile ' +
          profile._id +
          ' - no personalization_levels match for: "' +
          field +
          '"'
      );
      skipped++;
      return;
    }

    db.sender_profiles.updateOne(
      { _id: profile._id },
      { $set: { personalizationLevel: snap } }
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
