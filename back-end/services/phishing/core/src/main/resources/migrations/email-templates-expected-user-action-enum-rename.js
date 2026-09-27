/**
 * Renames legacy enum expectedUserAction strings on email_templates to catalog display names.
 * Only updates documents where expectedUserAction is still a plain string (not an embedded object).
 *
 * Run before email-templates-expected-user-action-embedded.js.
 *
 * Local run (default MongoDB port, no auth) from Git Bash:
 *
 *   cd /d/AspireTech/Workspace2/ASAT-V2-BACKEND/services/phishing/core/src/main/resources/migrations
 *   mongosh "mongodb://localhost:27017/phishing" --file email-templates-expected-user-action-enum-rename.js
 */
(function () {
  const LEGACY_TO_DISPLAY = {
    "Click + submit": "Form Submitted",
    "Click Link": "Link Clicked",
    "Submit Credentials": "Credentials Submitted",
    "Download attachment": "Attachment Downloaded",
  };

  let totalModified = 0;

  Object.entries(LEGACY_TO_DISPLAY).forEach(([legacy, displayName]) => {
    const result = db.email_templates.updateMany(
      { expectedUserAction: legacy },
      { $set: { expectedUserAction: displayName } }
    );

    if (result.modifiedCount > 0) {
      print(legacy + " -> " + displayName + ": modified=" + result.modifiedCount);
    }
    totalModified += result.modifiedCount;
  });

  print("Migration complete. totalModified=" + totalModified);
})();
