/**
 * Migrates legacy email_templates.difficultyLevel (string)
 * to embedded { id: "<string>", name } from difficulties (id stored as string, not ObjectId).
 *
 * Matches difficulties.name (case-insensitive). Skips templates already migrated.
 *
 * Run after email-templates-difficulty-level-enum-rename.js.
 *
 * Local run (default MongoDB port, no auth) from Git Bash:
 *
 *   cd /d/AspireTech/Workspace2/ASAT-V2-BACKEND/services/phishing/core/src/main/resources/migrations
 *   mongosh "mongodb://localhost:27017/phishing" --file email-templates-difficulty-level-embedded.js
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

  function findDifficultySnapshot(legacy) {
    if (legacy == null) return null;
    const normalized = String(legacy).trim();
    if (!normalized) return null;

    let doc = db.difficulties.findOne({
      name: { $regex: "^" + escapeRegex(normalized) + "$", $options: "i" },
    });
    if (!doc) {
      doc = db.difficulties.findOne({
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
    const dl = template.difficultyLevel;

    if (dl != null && typeof dl === "object" && dl.name != null) {
      if (typeof dl.id === "string") {
        alreadyMigrated++;
        return;
      }
      if (dl.id != null) {
        db.email_templates.updateOne(
          { _id: template._id },
          { $set: { difficultyLevel: { id: toIdString(dl.id), name: dl.name } } }
        );
        updated++;
        return;
      }
      alreadyMigrated++;
      return;
    }

    if (typeof dl !== "string") {
      if (dl != null) {
        print(
          "SKIP template " +
            template._id +
            " - difficultyLevel is not a legacy string: " +
            JSON.stringify(dl)
        );
        skipped++;
      }
      return;
    }

    const snap = findDifficultySnapshot(dl);
    if (!snap) {
      print(
        'SKIP template ' +
          template._id +
          ' - no difficulties match for: "' +
          dl +
          '"'
      );
      skipped++;
      return;
    }

    db.email_templates.updateOne({ _id: template._id }, { $set: { difficultyLevel: snap } });
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
