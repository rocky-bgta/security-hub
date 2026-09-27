/**
 * Backfills payload_types.channel to EMAIL for legacy documents.
 *
 *   mongosh "mongodb://localhost:27017/phishing" --file payload-types-channel-default-email.js
 */
(function () {
  const result = db.payload_types.updateMany(
    { $or: [{ channel: { $exists: false } }, { channel: null }] },
    { $set: { channel: "EMAIL" } }
  );

  print("payload_types channel backfill: matched=" + result.matchedCount + " modified=" + result.modifiedCount);

  try {
    db.payload_types.createIndex(
      { name: 1, channel: 1 },
      { name: "payload_types_name_channel_idx", unique: false }
    );
    print("Created index payload_types_name_channel_idx");
  } catch (e) {
    print("Index payload_types_name_channel_idx: " + e.message);
  }
})();
