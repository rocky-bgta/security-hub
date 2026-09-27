/**
 * Migrates legacy email_templates.department (string)
 * to embedded { id: "<string>", name } from departments_copy.
 *
 * Matches departments_copy.name (case-insensitive). Skips templates already migrated.
 *
 * Local run (default MongoDB port, no auth) from Git Bash:
 *
 *   cd /d/AspireTech/Workspace2/ASAT-V2-BACKEND/services/phishing/core/src/main/resources/migrations
 *   mongosh "mongodb://localhost:27017/phishing" --file email-templates-department-embedded.js
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

  function findDepartmentSnapshot(legacy) {
    if (legacy == null) return null;
    const normalized = String(legacy).trim();
    if (!normalized) return null;

    let doc = db.departments_copy.findOne({
      name: { $regex: "^" + escapeRegex(normalized) + "$", $options: "i" },
    });
    if (!doc) {
      doc = db.departments_copy.findOne({
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
    const dept = template.department;

    if (dept != null && typeof dept === "object" && dept.name != null) {
      if (typeof dept.id === "string") {
        alreadyMigrated++;
        return;
      }
      if (dept.id != null) {
        db.email_templates.updateOne(
          { _id: template._id },
          { $set: { department: { id: toIdString(dept.id), name: dept.name } } }
        );
        updated++;
        return;
      }
      alreadyMigrated++;
      return;
    }

    if (typeof dept !== "string") {
      if (dept != null) {
        print(
          "SKIP template " +
            template._id +
            " - department is not a legacy string: " +
            JSON.stringify(dept)
        );
        skipped++;
      }
      return;
    }

    const snap = findDepartmentSnapshot(dept);
    if (!snap) {
      print(
        'SKIP template ' +
          template._id +
          ' - no departments_copy match for: "' +
          dept +
          '"'
      );
      skipped++;
      return;
    }

    db.email_templates.updateOne({ _id: template._id }, { $set: { department: snap } });
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
