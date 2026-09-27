/**
 * Migrates legacy landing_pages.category (string)
 * to embedded { id: "<string>", name } from landing_page_categories.
 *
 * Matches landing_page_categories.name (case-insensitive). Skips pages already migrated.
 *
 * Run after landing-pages-category-enum-rename.js.
 *
 * Local run (default MongoDB port, no auth) from Git Bash:
 *
 *   cd /d/AspireTech/Workspace2/ASAT-V2-BACKEND/services/phishing/core/src/main/resources/migrations
 *   mongosh "mongodb://localhost:27017/phishing" --file landing-pages-category-embedded.js
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

  function findCategorySnapshot(legacy) {
    if (legacy == null) return null;
    const normalized = String(legacy).trim();
    if (!normalized) return null;

    let doc = db.landing_page_categories.findOne({
      name: { $regex: "^" + escapeRegex(normalized) + "$", $options: "i" },
    });
    if (!doc) {
      doc = db.landing_page_categories.findOne({
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
    const cat = page.category;

    if (cat != null && typeof cat === "object" && cat.name != null) {
      if (typeof cat.id === "string") {
        alreadyMigrated++;
        return;
      }
      if (cat.id != null) {
        db.landing_pages.updateOne(
          { _id: page._id },
          { $set: { category: { id: toIdString(cat.id), name: cat.name } } }
        );
        updated++;
        return;
      }
      alreadyMigrated++;
      return;
    }

    if (typeof cat !== "string") {
      if (cat != null) {
        print(
          "SKIP page " +
            page._id +
            " - category is not a legacy string: " +
            JSON.stringify(cat)
        );
        skipped++;
      }
      return;
    }

    const snap = findCategorySnapshot(cat);
    if (!snap) {
      print(
        'SKIP page ' +
          page._id +
          ' - no landing_page_categories match for: "' +
          cat +
          '"'
      );
      skipped++;
      return;
    }

    db.landing_pages.updateOne({ _id: page._id }, { $set: { category: snap } });
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
