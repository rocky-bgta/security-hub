package com.aspire.asat.auth.integration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test that verifies documents can be written to and read from the
 * {@code auth_sessions} collection — the same collection Auth writes and Gateway reads
 * on the Redis-fallback path.
 *
 * <p>This test uses the native MongoDB driver directly (no Spring context) to keep
 * dependencies minimal and confirm the document schema is correct.
 *
 * <p>Requires Docker. The test is automatically skipped in environments without it.
 */
@Testcontainers(disabledWithoutDocker = true)
class AuthSessionFallbackIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    private MongoClient mongoClient;
    private MongoCollection<Document> sessionsCollection;
    private MongoCollection<Document> tempTokensCollection;

    @BeforeEach
    void setUp() {
        mongoClient = MongoClients.create(mongoDBContainer.getReplicaSetUrl());
        MongoDatabase db = mongoClient.getDatabase("registration");
        sessionsCollection = db.getCollection("auth_sessions");
        tempTokensCollection = db.getCollection("auth_temp_tokens");
    }

    @AfterEach
    void tearDown() {
        sessionsCollection.drop();
        tempTokensCollection.drop();
        mongoClient.close();
    }

    // -------------------------------------------------------------------------
    // auth_sessions
    // -------------------------------------------------------------------------

    @Test
    void saveAndFind_AuthSession_DocumentRoundTrip() {
        Document session = buildSessionDocument("token-1", "user-1");

        sessionsCollection.insertOne(session);

        Document found = sessionsCollection.find(new Document("_id", "token-1")).first();
        assertNotNull(found);
        assertEquals("user-1", found.getString("userId"));
        assertEquals("checksum-abc", found.getString("accessTokenChecksum"));
    }

    @Test
    void findByUserId_ReturnsOnlyMatchingDocuments() {
        sessionsCollection.insertOne(buildSessionDocument("token-A", "user-X"));
        sessionsCollection.insertOne(buildSessionDocument("token-B", "user-X"));
        sessionsCollection.insertOne(buildSessionDocument("token-C", "user-Y"));

        long count = sessionsCollection.countDocuments(new Document("userId", "user-X"));

        assertEquals(2, count);
    }

    @Test
    void deleteByUserId_RemovesOnlyMatchingDocuments() {
        sessionsCollection.insertOne(buildSessionDocument("token-A", "user-X"));
        sessionsCollection.insertOne(buildSessionDocument("token-B", "user-X"));
        sessionsCollection.insertOne(buildSessionDocument("token-C", "user-Y"));

        sessionsCollection.deleteMany(new Document("userId", "user-X"));

        assertEquals(0, sessionsCollection.countDocuments(new Document("userId", "user-X")));
        assertEquals(1, sessionsCollection.countDocuments(new Document("userId", "user-Y")));
    }

    @Test
    void deleteById_RemovesOnlySpecifiedDocument() {
        sessionsCollection.insertOne(buildSessionDocument("token-A", "user-X"));
        sessionsCollection.insertOne(buildSessionDocument("token-B", "user-X"));

        sessionsCollection.deleteOne(new Document("_id", "token-A"));

        assertNull(sessionsCollection.find(new Document("_id", "token-A")).first());
        assertNotNull(sessionsCollection.find(new Document("_id", "token-B")).first());
    }

    @Test
    void authSession_ContainsPermissions_ForGatewayFallbackRead() {
        Document session = buildSessionDocument("token-1", "user-1");
        session.append("permissions", List.of("USR:VIEW", "USR:EDIT"));

        sessionsCollection.insertOne(session);

        Document found = sessionsCollection.find(new Document("_id", "token-1")).first();
        assertNotNull(found);
        List<?> perms = found.getList("permissions", String.class);
        assertNotNull(perms);
        assertTrue(perms.contains("USR:VIEW"));
        assertTrue(perms.contains("USR:EDIT"));
    }

    // -------------------------------------------------------------------------
    // auth_temp_tokens
    // -------------------------------------------------------------------------

    @Test
    void saveAndFind_AuthTempToken_DocumentRoundTrip() {
        Document tempToken = new Document("_id", "temp.jwt.string")
                .append("userId", "user-abc")
                .append("expiresAt", new Date(System.currentTimeMillis() + 300_000));

        tempTokensCollection.insertOne(tempToken);

        Document found = tempTokensCollection.find(new Document("_id", "temp.jwt.string")).first();
        assertNotNull(found);
        assertEquals("user-abc", found.getString("userId"));
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private Document buildSessionDocument(String tokenId, String userId) {
        return new Document("_id", tokenId)
                .append("userId", userId)
                .append("accessTokenChecksum", "checksum-abc")
                .append("username", "tester")
                .append("userType", "USER")
                .append("expiresAt", new Date(System.currentTimeMillis() + 1_200_000));
    }
}
