/**
 * Renames legacy enum socialEngineeringStrategy strings on email_templates to catalog display names.
 * Only updates documents where socialEngineeringStrategy is still a plain string (not an embedded object).
 *
 * Run before email-templates-social-engineering-strategy-embedded.js.
 *
 * Local run (default MongoDB port, no auth) from Git Bash:
 *
 *   cd /d/AspireTech/Workspace2/ASAT-V2-BACKEND/services/phishing/core/src/main/resources/migrations
 *   mongosh "mongodb://localhost:27017/phishing" --file email-templates-social-engineering-strategy-enum-rename.js
 */
(function () {
  const LEGACY_TO_DISPLAY = {
    "Trust / Relationship": "Trust Exploitation",
    "Urgency / Fear": "Urgency & Fear",
    "Compliance / Policy": "Compliance Enforcement",
  };

  let totalModified = 0;

  Object.entries(LEGACY_TO_DISPLAY).forEach(([legacy, displayName]) => {
    const result = db.email_templates.updateMany(
      { socialEngineeringStrategy: legacy },
      { $set: { socialEngineeringStrategy: displayName } }
    );

    if (result.modifiedCount > 0) {
      print(legacy + " -> " + displayName + ": modified=" + result.modifiedCount);
    }
    totalModified += result.modifiedCount;
  });

  print("Migration complete. totalModified=" + totalModified);
})();
