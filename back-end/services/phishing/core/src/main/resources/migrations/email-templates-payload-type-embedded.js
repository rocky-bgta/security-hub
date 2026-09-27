/**
 * Migrates legacy email_templates.payloadType (string)
 * to embedded { id: "<string>", name } (id stored as string, not ObjectId).
 *
 * Matches payload_types.name (case-insensitive). Skips templates already migrated.
 *
 * Local run (default MongoDB port, no auth) from Git Bash:
 *
 *   cd /d/AspireTech/Workspace2/ASAT-V2-BACKEND/services/phishing/core/src/main/resources/migrations
 *   mongosh "mongodb://localhost:27017/phishing" --file email-templates-payload-type-embedded.js
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

  function findPayloadTypeSnapshot(legacy) {
    if (legacy == null) return null;
    const normalized = String(legacy).trim();
    if (!normalized) return null;

    let doc = db.payload_types.findOne({
      name: { $regex: "^" + escapeRegex(normalized) + "$", $options: "i" },
    });
    if (!doc) {
      doc = db.payload_types.findOne({
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
    const pt = template.payloadType;

    if (pt != null && typeof pt === "object" && pt.name != null) {
      if (typeof pt.id === "string") {
        alreadyMigrated++;
        return;
      }
      if (pt.id != null) {
        db.email_templates.updateOne(
          { _id: template._id },
          { $set: { payloadType: { id: toIdString(pt.id), name: pt.name } } }
        );
        updated++;
        return;
      }
      alreadyMigrated++;
      return;
    }

    if (typeof pt !== "string") {
      if (pt != null) {
        print(
          "SKIP template " +
            template._id +
            " - payloadType is not a legacy string: " +
            JSON.stringify(pt)
        );
        skipped++;
      }
      return;
    }

    const snap = findPayloadTypeSnapshot(pt);
    if (!snap) {
      print(
        'SKIP template ' +
          template._id +
          ' - no payload_types match for: "' +
          pt +
          '"'
      );
      skipped++;
      return;
    }

    db.email_templates.updateOne({ _id: template._id }, { $set: { payloadType: snap } });
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
