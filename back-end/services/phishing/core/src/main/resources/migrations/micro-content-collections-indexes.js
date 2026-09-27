/**
 * Micro-content (phishing DB): create collections + indexes used by
 * POST /api/v1/deepfake/micro-content (MicroContentJob + ClientMicroContentTopic).
 *
 * Collections are created on first insert if missing; this script ensures indexes
 * exist even when spring.data.mongodb.auto-index-creation is false.
 *
 * Run against the phishing database:
 *   mongosh "mongodb://localhost:27017/phishing" --file micro-content-collections-indexes.js
 */
(function () {
  const jobs = db.getCollection("micro_content_jobs");
  const topics = db.getCollection("client_micro_content_topics");

  // ---- micro_content_jobs ----
  // Unique job id (poll + SQS worker lookup)
  jobs.createIndex({ jobId: 1 }, { name: "jobId_unique", unique: true });
  // Client-scoped listing / ownership checks
  jobs.createIndex({ clientId: 1 }, { name: "clientId_idx" });
  // Status filter / stale-processing sweeps
  jobs.createIndex({ status: 1 }, { name: "status_idx" });
  // Duplicate deepfake-video guard:
  //   existsByClientIdAndVideosDeepfakeVideoIdAndStatusIn
  //   findByClientIdAndVideosDeepfakeVideoIdIn
  jobs.createIndex(
    { clientId: 1, "videos.deepfakeVideoId": 1, status: 1 },
    { name: "client_deepfakeVideo_status_idx" }
  );
  jobs.createIndex(
    { "videos.deepfakeVideoId": 1 },
    { name: "videos_deepfakeVideoId_idx" }
  );

  // ---- client_micro_content_topics ----
  // One CMS micro-content topic mapping per client
  topics.createIndex({ clientId: 1 }, { name: "clientId_unique", unique: true });

  print("micro-content-collections-indexes: indexes ensured on micro_content_jobs + client_micro_content_topics");
})();
