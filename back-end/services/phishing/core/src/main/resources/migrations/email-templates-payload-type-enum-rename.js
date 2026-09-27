/**
 * Renames legacy enum payloadType strings on email_templates to catalog display names.
 * Only updates documents where payloadType is still a plain string (not an embedded object).
 *
 * Run before email-templates-payload-type-embedded.js.
 *
 * Local run (default MongoDB port, no auth) from Git Bash:
 *
 *   cd /d/AspireTech/Workspace2/ASAT-V2-BACKEND/services/phishing/core/src/main/resources/migrations
 *   mongosh "mongodb://localhost:27017/phishing" --file email-templates-payload-type-enum-rename.js
 */
(function () {
  const LEGACY_TO_DISPLAY = {
    SOCIAL_MEDIA_PHISHING: "Social Media",
    PHISHING_WEBSITE: "Website",
    PHISHING_ATTACHMENT: "Attachment",
    FAKE_PAYMENT_REQUEST: "Website",
    INFORMATION_REQUEST: "Website",
    CALLBACK_REQUEST: "Website",
    FAKE_SOFTWARE_UPDATE: "Website",
    QR: "Website",
  };

  let totalModified = 0;

  Object.entries(LEGACY_TO_DISPLAY).forEach(([legacy, displayName]) => {
    const result = db.email_templates.updateMany(
      { payloadType: legacy },
      { $set: { payloadType: displayName } }
    );

    if (result.modifiedCount > 0) {
      print(legacy + " -> " + displayName + ": modified=" + result.modifiedCount);
    }
    totalModified += result.modifiedCount;
  });

  print("Migration complete. totalModified=" + totalModified);
})();
