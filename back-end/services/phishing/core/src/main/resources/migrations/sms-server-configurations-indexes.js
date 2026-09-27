/**
 * Ensures indexes on sms_server_configurations collection.
 *
 *   mongosh "mongodb://localhost:27017/phishing" --file sms-server-configurations-indexes.js
 */
(function () {
  db.sms_server_configurations.createIndex(
    { clientId: 1, isDefault: 1 },
    { name: "client_default_idx" }
  );
  db.sms_server_configurations.createIndex(
    { clientId: 1, name: 1 },
    { name: "client_name_idx", unique: true }
  );
  db.campaign_sms_deliveries.createIndex(
    { campaignId: 1, recipientId: 1 },
    { name: "campaign_recipient_idx", unique: true }
  );
  print("sms-server-configurations-indexes: indexes ensured");
})();
