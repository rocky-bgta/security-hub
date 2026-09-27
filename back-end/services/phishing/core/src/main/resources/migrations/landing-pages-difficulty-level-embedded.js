/**
 * Migrates legacy landing_pages.difficultyLevel (string)
 * to embedded { id: "<string>", name } from difficulties (id stored as string, not ObjectId).
 *
 * Matches difficulties.name (case-insensitive). Skips pages already migrated.
 *
 * Run after landing-pages-difficulty-level-enum-rename.js.
 *
 * Local run (default MongoDB port, no auth) from Git Bash:
 *
 *   cd /d/AspireTech/Workspace2/ASAT-V2-BACKEND/services/phishing/core/src/main/resources/migrations
 *   mongosh "mongodb://localhost:27017/phishing" --file landing-pages-difficulty-level-embedded.js
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

  db.landing_pages.find({}).forEach((page) => {
    const dl = page.difficultyLevel;

    if (dl != null && typeof dl === "object" && dl.name != null) {
      if (typeof dl.id === "string") {
        alreadyMigrated++;
        return;
      }
      if (dl.id != null) {
        db.landing_pages.updateOne(
          { _id: page._id },
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
          "SKIP page " +
            page._id +
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
        'SKIP page ' +
          page._id +
          ' - no difficulties match for: "' +
          dl +
          '"'
      );
      skipped++;
      return;
    }

    db.landing_pages.updateOne({ _id: page._id }, { $set: { difficultyLevel: snap } });
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
