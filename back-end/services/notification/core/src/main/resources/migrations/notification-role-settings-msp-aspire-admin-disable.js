/**
 * Disables role-based notification settings for MSP and Aspire Admin across every
 * notification type. Channel flags (email / in-app / SMS / etc.) are left unchanged
 * so re-enabling a type in the Admin Portal keeps the existing channel matrix.
 *
 * Aligns existing environments with the seeder defaults where MSP and ASPIRE_ADMIN
 * are opt-in (enabled=false). USER and CLIENT_ADMIN rows are not touched.
 *
 * Safe to re-run: rows already disabled are matched but remain enabled=false.
 *
 * Local run (default MongoDB port, no auth):
 *
 *   cd services/notification/core/src/main/resources/migrations
 *   mongosh "mongodb://localhost:27017/notification" --file notification-role-settings-msp-aspire-admin-disable.js
 */
(function () {
  const COLLECTION = "notification_role_settings";
  const ROLES = ["MSP", "ASPIRE_ADMIN"];

  const filter = {
    role: { $in: ROLES },
    enabled: { $ne: false },
  };

  const matchedBefore = db[COLLECTION].countDocuments({ role: { $in: ROLES } });
  const stillEnabled = db[COLLECTION].countDocuments(filter);

  print(
    "Disabling MSP / ASPIRE_ADMIN role settings. totalRows=" +
      matchedBefore +
      " stillEnabled=" +
      stillEnabled
  );

  const result = db[COLLECTION].updateMany(filter, {
    $set: {
      enabled: false,
      updated_at: new Date(),
    },
  });

  const stillEnabledAfter = db[COLLECTION].countDocuments(filter);

  print(
    "Migration complete. matched=" +
      result.matchedCount +
      " modified=" +
      result.modifiedCount +
      " stillEnabledAfter=" +
      stillEnabledAfter
  );
})();
