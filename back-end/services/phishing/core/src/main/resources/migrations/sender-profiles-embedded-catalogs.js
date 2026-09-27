/**
 * Migrates legacy sender_profiles.deceptionLevel and personalizationLevel (enum strings)
 * to embedded DeceptionLevelDto / PersonalizationLevelDto subdocuments.
 *
 * Run: mongosh "<connection-string>" --file sender-profiles-embedded-catalogs.js
 */
(function () {
  function escapeRegex(s) {
    return String(s).replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
  }

  function findDeceptionSnapshot(legacy) {
    if (legacy == null) return null;
    const normalized = String(legacy).trim();
    let doc = db.deception_levels.findOne({
      name: { $regex: "^" + escapeRegex(normalized) + "$", $options: "i" },
    });
    if (!doc) {
      doc = db.deception_levels.findOne({
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

  function findPersonalizationSnapshot(legacy) {
    if (legacy == null) return null;
    const normalized = String(legacy).trim();
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

  db.sender_profiles.find({}).forEach((profile) => {
    const hasLegacyDeception = typeof profile.deceptionLevel === "string";
    const hasLegacyPersonalization = typeof profile.personalizationLevel === "string";

    if (!hasLegacyDeception && !hasLegacyPersonalization) {
      return;
    }

    const setFields = {};

    if (hasLegacyDeception) {
      const snap = findDeceptionSnapshot(profile.deceptionLevel);
      if (snap) {
        setFields.deceptionLevel = snap;
      }
    }

    if (hasLegacyPersonalization) {
      const snap = findPersonalizationSnapshot(profile.personalizationLevel);
      if (snap) {
        setFields.personalizationLevel = snap;
      }
    }

    if (Object.keys(setFields).length === 0) {
      print("SKIP profile " + profile._id);
      skipped++;
      return;
    }

    db.sender_profiles.updateOne({ _id: profile._id }, { $set: setFields });
    updated++;
  });

  print("Migration complete. updated=" + updated + " skipped=" + skipped);
})();
