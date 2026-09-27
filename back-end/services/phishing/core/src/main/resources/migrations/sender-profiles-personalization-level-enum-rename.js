/**
 * Renames legacy enum personalizationLevel strings on sender_profiles to catalog display names.
 * Only updates documents where personalizationLevel is still a plain string (not an embedded object).
 *
 * Run before sender-profiles-personalization-level-embedded.js.
 *
 * Local run (default MongoDB port, no auth) from Git Bash:
 *
 *   cd /d/AspireTech/Workspace2/ASAT-V2-BACKEND/services/phishing/core/src/main/resources/migrations
 *   mongosh "mongodb://localhost:27017/phishing" --file sender-profiles-personalization-level-enum-rename.js
 */
(function () {
  const LEGACY_TO_DISPLAY = {
    GENERIC: "Generic",
    HIGHLY_TARGETED: "Highly Targeted",
  };

  let totalModified = 0;

  Object.entries(LEGACY_TO_DISPLAY).forEach(([legacy, displayName]) => {
    const result = db.sender_profiles.updateMany(
      { personalizationLevel: legacy },
      { $set: { personalizationLevel: displayName } }
    );

    if (result.modifiedCount > 0) {
      print(legacy + " -> " + displayName + ": modified=" + result.modifiedCount);
    }
    totalModified += result.modifiedCount;
  });

  print("Migration complete. totalModified=" + totalModified);
})();
