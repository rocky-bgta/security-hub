/**
 * Renames legacy enum category strings on landing_pages to catalog display names.
 * Only updates documents where category is still a plain string (not an embedded object).
 *
 * Run before landing-pages-category-embedded.js.
 *
 * Local run (default MongoDB port, no auth) from Git Bash:
 *
 *   cd /d/AspireTech/Workspace2/ASAT-V2-BACKEND/services/phishing/core/src/main/resources/migrations
 *   mongosh "mongodb://localhost:27017/phishing" --file landing-pages-category-enum-rename.js
 */
(function () {
  const LEGACY_TO_DISPLAY = {
    BUSINESS: "Business",
    SOCIAL_MEDIA: "Social Media",
    EMAIL_PROVIDER: "Email Provider",
    CLOUD_APP: "Cloud App",
    FINANCIAL: "Financial",
    GOVERNMENT: "Government",
    HEALTHCARE: "Healthcare",
  };

  let totalModified = 0;

  Object.entries(LEGACY_TO_DISPLAY).forEach(([legacy, displayName]) => {
    const result = db.landing_pages.updateMany(
      { category: legacy },
      { $set: { category: displayName } }
    );

    if (result.modifiedCount > 0) {
      print(legacy + " -> " + displayName + ": modified=" + result.modifiedCount);
    }
    totalModified += result.modifiedCount;
  });

  print("Migration complete. totalModified=" + totalModified);
})();
