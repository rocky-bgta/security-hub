/**
 * Renames legacy enum difficultyLevel strings on landing_pages to catalog display names.
 * Only updates documents where difficultyLevel is still a plain string (not an embedded object).
 *
 * Run before landing-pages-difficulty-level-embedded.js.
 *
 * Local run (default MongoDB port, no auth) from Git Bash:
 *
 *   cd /d/AspireTech/Workspace2/ASAT-V2-BACKEND/services/phishing/core/src/main/resources/migrations
 *   mongosh "mongodb://localhost:27017/phishing" --file landing-pages-difficulty-level-enum-rename.js
 */
(function () {
  const LEGACY_TO_DISPLAY = {
    BEGINNER: "Beginner",
    INTERMEDIATE: "Intermediate",
    ADVANCED: "Advanced",
    SPEAR_PHISHING: "Spear Phishing",
  };

  let totalModified = 0;

  Object.entries(LEGACY_TO_DISPLAY).forEach(([legacy, displayName]) => {
    const result = db.landing_pages.updateMany(
      { difficultyLevel: legacy },
      { $set: { difficultyLevel: displayName } }
    );

    if (result.modifiedCount > 0) {
      print(legacy + " -> " + displayName + ": modified=" + result.modifiedCount);
    }
    totalModified += result.modifiedCount;
  });

  print("Migration complete. totalModified=" + totalModified);
})();
