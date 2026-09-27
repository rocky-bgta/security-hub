/**
 * Ensures unique indexes on short_urls for smishing per-recipient short links.
 *
 *   mongosh "mongodb://localhost:27017/phishing" --file short-urls-indexes.js
 */
(function () {
  db.short_urls.createIndex(
    { shortCode: 1 },
    { name: "short_code_idx", unique: true }
  );
  db.short_urls.createIndex(
    { trackingId: 1 },
    { name: "tracking_id_idx", unique: true }
  );
  print("short-urls-indexes: indexes ensured");
})();
