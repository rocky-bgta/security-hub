/**
 * Backfill policies.isDefault, mspId, and clientAdminId using aspire_user_copy in the universal database.
 *
 * Steps:
 *  1. SYSTEM_USER  -> policies where createdBy = user.userId: isDefault = true
 *  2. MSP          -> policies where createdBy = user.userId: isDefault = false, mspId = user.userId
 *  3. CLIENT_ADMIN -> policies where createdBy = user.userId: isDefault = false, clientAdminId = user.userId
 *  4. Cleanup      -> policies that already have clientAdminId set but isDefault is not false
 *
 * Local run (Git Bash / PowerShell):
 *   cd services/universal/core/src/main/resources/migrations
 *   mongosh "mongodb://localhost:27017/universal" --file policies-backfill-ownership-from-aspire-user.js
 *
 * Cosmos / DocumentDB: pass the appropriate connection string instead of localhost.
 */
(function () {
  const UNIVERSAL_DB_NAME = "universal";
  const ASPIRE_USER_COLLECTION = "aspire_user_copy";
  const POLICIES_COLLECTION = "policies";

  const USER_TYPES = {
    SYSTEM_USER: "SYSTEM_USER",
    MSP: "MSP",
    CLIENT_ADMIN: "CLIENT_ADMIN",
  };

  const universalDb = db.getSiblingDB(UNIVERSAL_DB_NAME);
  const aspireUsers = universalDb.getCollection(ASPIRE_USER_COLLECTION);
  const policies = universalDb.getCollection(POLICIES_COLLECTION);

  print("==========================================");
  print("Migration: Backfill policy ownership fields from aspire_user_copy");
  print("Database: " + UNIVERSAL_DB_NAME);
  print("Started at: " + new Date().toISOString());
  print("==========================================\n");

  const aspireUserCount = aspireUsers.countDocuments();
  const policyCount = policies.countDocuments();

  print("Verification:");
  print("  - " + ASPIRE_USER_COLLECTION + ": " + aspireUserCount + " documents");
  print("  - " + POLICIES_COLLECTION + ": " + policyCount + " documents");
  print("");

  if (aspireUserCount === 0) {
    print("[ERROR] No documents found in " + ASPIRE_USER_COLLECTION + ". Aborting.");
    print("==========================================\n");
    return;
  }

  if (policyCount === 0) {
    print("[WARNING] No documents found in " + POLICIES_COLLECTION + ". Nothing to update.");
    print("==========================================\n");
    return;
  }

  function toIdString(value) {
    if (value === null || value === undefined) {
      return null;
    }
    if (typeof value === "string") {
      const trimmed = value.trim();
      return trimmed === "" ? null : trimmed;
    }
    if (value.toString) {
      const trimmed = value.toString().trim();
      return trimmed === "" ? null : trimmed;
    }
    return String(value);
  }

  function resolveAspireUserId(user) {
    return toIdString(user.userId);
  }

  function nowTimestamp() {
    return new Date();
  }

  function migrateForUserType(userType, buildUpdate) {
    const users = aspireUsers.find({ userType: userType }).toArray();
    const stats = {
      userType: userType,
      usersProcessed: users.length,
      usersWithPolicies: 0,
      policiesUpdated: 0,
      policiesMatched: 0,
      usersSkippedNoId: 0,
    };

    print("Processing userType=" + userType + " (" + users.length + " users)");

    users.forEach(function (user) {
      const userId = resolveAspireUserId(user);
      if (!userId) {
        stats.usersSkippedNoId++;
        print("  [SKIP] Aspire user missing userId: " + toIdString(user._id));
        return;
      }

      const matchedCount = policies.countDocuments({ createdBy: userId });
      if (matchedCount === 0) {
        return;
      }

      stats.usersWithPolicies++;
      stats.policiesMatched += matchedCount;

      const update = buildUpdate(userId);
      const result = policies.updateMany({ createdBy: userId }, update);

      stats.policiesUpdated += result.modifiedCount;

      print(
        "  userId=" + userId +
        " matched=" + matchedCount +
        " modified=" + result.modifiedCount
      );
    });

    print(
      "  Summary for " + userType +
      ": users=" + stats.usersProcessed +
      ", usersWithPolicies=" + stats.usersWithPolicies +
      ", policiesMatched=" + stats.policiesMatched +
      ", policiesUpdated=" + stats.policiesUpdated +
      ", usersSkippedNoId=" + stats.usersSkippedNoId
    );
    print("");

    return stats;
  }

  function fixPoliciesWithClientAdminIdButNotDefaultFalse() {
    const filter = {
      clientAdminId: { $exists: true, $nin: [null, ""] },
      isDefault: { $ne: false },
    };

    const matchedCount = policies.countDocuments(filter);
    print(
      "Processing policies with clientAdminId set but isDefault != false (" +
      matchedCount + " policies)"
    );

    const result = policies.updateMany(filter, {
      $set: {
        isDefault: false,
        updatedAt: nowTimestamp(),
      },
    });

    print(
      "  matched=" + matchedCount +
      " modified=" + result.modifiedCount
    );
    print("");

    return {
      userType: "CLIENT_ADMIN_ID_CLEANUP",
      usersProcessed: 0,
      policiesMatched: matchedCount,
      policiesUpdated: result.modifiedCount,
    };
  }

  try {
    const systemUserStats = migrateForUserType(USER_TYPES.SYSTEM_USER, function () {
      return {
        $set: {
          isDefault: true,
          updatedAt: nowTimestamp(),
        },
      };
    });

    const mspStats = migrateForUserType(USER_TYPES.MSP, function (userId) {
      return {
        $set: {
          isDefault: false,
          mspId: userId,
          updatedAt: nowTimestamp(),
        },
      };
    });

    const clientAdminStats = migrateForUserType(USER_TYPES.CLIENT_ADMIN, function (userId) {
      return {
        $set: {
          isDefault: false,
          clientAdminId: userId,
          updatedAt: nowTimestamp(),
        },
      };
    });

    const clientAdminIdCleanupStats = fixPoliciesWithClientAdminIdButNotDefaultFalse();

    print("==========================================");
    print("Migration Summary");
    print("Completed at: " + new Date().toISOString());
    print("==========================================");
    [systemUserStats, mspStats, clientAdminStats, clientAdminIdCleanupStats].forEach(function (stats) {
      print(
        stats.userType +
        ": users=" + (stats.usersProcessed || 0) +
        ", policiesMatched=" + (stats.policiesMatched || 0) +
        ", policiesUpdated=" + stats.policiesUpdated
      );
    });
    print("==========================================\n");
  } catch (error) {
    print("\n==========================================");
    print("FATAL ERROR during migration:");
    print(error.message || String(error));
    print("==========================================\n");
    throw error;
  }
})();
