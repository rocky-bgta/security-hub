/**
 * Sets channel=EMAIL on campaigns missing the field (backward compatibility).
 *
 *   mongosh "mongodb://localhost:27017/phishing" --file campaigns-channel-default-email.js
 */
(function () {
  const result = db.campaigns.updateMany(
    { channel: { $exists: false } },
    { $set: { channel: "EMAIL" } }
  );
  print("campaigns-channel-default-email: matched=" + result.matchedCount + " modified=" + result.modifiedCount);
})();
