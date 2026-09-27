/**
 * Migrates legacy landing_pages.category (string) and difficultyLevel (enum string)
 * to embedded LandingPageCategoryDto / DifficultyDto subdocuments.
 *
 * Run: mongosh "<connection-string>" --file landing-pages-embedded-catalogs.js
 */
(function () {
  function escapeRegex(s) {
    return String(s).replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  }

  function findCategorySnapshot(legacy) {
    if (legacy == null) return null;
    const normalized = String(legacy).trim();
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
      id: doc._id,
      name: doc.name,
      description: doc.description,
      displayOrder: doc.displayOrder != null ? doc.displayOrder : 0,
      isDefault: doc.isDefault != null ? doc.isDefault : false,
      isActive: doc.isActive != null ? doc.isActive : true,
      createdAt: doc.createdAt,
      updatedAt: doc.updatedAt,
    };
  }

  function findDifficultySnapshot(legacy) {
    if (legacy == null) return null;
    const normalized = String(legacy).trim();
    const doc = db.difficulties.findOne({
      name: { $regex: "^" + escapeRegex(normalized) + "$", $options: "i" },
    });
    if (!doc) return null;
    return {
      id: doc._id,
      name: doc.name,
      description: doc.description,
      displayOrder: doc.displayOrder != null ? doc.displayOrder : 0,
      isDefault: doc.isDefault != null ? doc.isDefault : false,
      isActive: doc.isActive != null ? doc.isActive : true,
      createdAt: doc.createdAt,
      updatedAt: doc.updatedAt,
    };
  }

  let updated = 0;
  let skipped = 0;

  db.landing_pages.find({}).forEach((page) => {
    const hasLegacyCategory = typeof page.category === "string";
    const hasLegacyDifficulty =
      typeof page.difficultyLevel === "string" ||
      (page.difficultyLevel != null &&
        typeof page.difficultyLevel === "object" &&
        !page.difficultyLevel.id);

    if (!hasLegacyCategory && !hasLegacyDifficulty) {
      return;
    }

    const setFields = {};
    const unsetFields = {};

    if (hasLegacyCategory) {
      const snap = findCategorySnapshot(page.category);
      if (snap) {
        setFields.category = snap;
      }
    }

    if (typeof page.difficultyLevel === "string") {
      const snap = findDifficultySnapshot(page.difficultyLevel);
      if (snap) {
        setFields.difficultyLevel = snap;
      }
    }

    if (Object.keys(setFields).length === 0 && Object.keys(unsetFields).length === 0) {
      print("SKIP page " + page._id);
      skipped++;
      return;
    }

    const update = {};
    if (Object.keys(setFields).length > 0) update.$set = setFields;
    if (Object.keys(unsetFields).length > 0) update.$unset = unsetFields;

    db.landing_pages.updateOne({ _id: page._id }, update);
    updated++;
  });

  print("Migration complete. updated=" + updated + " skipped=" + skipped);
})();
