/**
 * Private micro-content topics (CMS DB): backfill + indexes for Topic.clientId / Topic.isPrivate.
 *
 * New documents created via phishing MicroContentService set:
 *   clientId  = current client's admin id
 *   isPrivate = true   (derived in Topic.toTopic when clientId is present)
 *
 * Existing catalog topics must remain public (isPrivate != true / no clientId).
 *
 * Run against the cms database:
 *   mongosh "mongodb://localhost:27017/cms" --file topics-private-microcontent.js
 */
(function () {
  const topics = db.getCollection("topic");

  // Explicitly mark legacy topics as public when isPrivate is missing.
  // Privacy filter treats isPrivate != true as public; setting false keeps
  // documents self-describing. Do NOT invent a clientId for public topics.
  const backfill = topics.updateMany(
    { isPrivate: { $exists: false } },
    { $set: { isPrivate: false } }
  );

  print(
    "topics-private-microcontent backfill: matched=" +
      backfill.matchedCount +
      " modified=" +
      backfill.modifiedCount
  );

  // Indexes used by:
  //   findByIsPrivateTrueAndClientId*
  //   TopicPrivacyCriteria (isPrivate + clientId)
  //   private topics list API
  topics.createIndex({ clientId: 1 }, { name: "clientId_idx", sparse: true });
  topics.createIndex(
    { isPrivate: 1, clientId: 1 },
    { name: "isPrivate_clientId_idx" }
  );
  topics.createIndex(
    { isPrivate: 1, clientId: 1, status: 1 },
    { name: "isPrivate_clientId_status_idx" }
  );

  print("topics-private-microcontent: indexes ensured on topic");
})();
