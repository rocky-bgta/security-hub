/**
 * Seeds role-specific notification templates on the canonical notification types by
 * copying admin-addressed content from the legacy sibling types.
 *
 * Creates up to 22 role variants (CLIENT_ADMIN / MSP / ASPIRE_ADMIN) for:
 *   WELCOME_EMAIL, USER_PASSWORD_CHANGE, COURSE_COMPLETION_USER, CERTIFICATE_ISSUED,
 *   PACKAGE_ASSIGNED_USER, PACKAGE_ASSIGNED_AND_USER_CREDENTIAL, USER_SUSPENSION
 *
 * Combinations the role matrix disables are intentionally omitted
 * (WELCOME_EMAIL IN_APP MSP, COURSE_COMPLETION_USER MSP,
 * PACKAGE_ASSIGNED_AND_USER_CREDENTIAL MSP).
 *
 * Also:
 *   - clears is_default on any role-specific row (default means the base template)
 *   - creates a unique index on (notification_type, channel, recipient_role)
 *
 * Safe to re-run: existing (type, channel, role) rows are skipped.
 *
 * Local run (default MongoDB port, no auth):
 *
 *   cd services/notification/core/src/main/resources/migrations
 *   mongosh "mongodb://localhost:27017/notification" --file notification-templates-role-variants.js
 */
(function () {
  const COLLECTION = "notification_templates";

  /**
   * Declarative mapping of role variants to create.
   * Content is copied from the source (type, channel); identity fields are set from the target.
   */
  const MAPPINGS = [
    // WELCOME_EMAIL — admin-side roles reuse NEW_USER_REGISTERED wording.
    // No MSP in-app: WELCOME_EMAIL is in MSP_IN_APP_OFF_TYPES.
    {
      type: "WELCOME_EMAIL",
      channel: "EMAIL",
      role: "CLIENT_ADMIN",
      sourceType: "NEW_USER_REGISTERED",
      sourceChannel: "EMAIL",
      templateName: "Welcome Email Client Admin Email Template",
    },
    {
      type: "WELCOME_EMAIL",
      channel: "EMAIL",
      role: "MSP",
      sourceType: "NEW_USER_REGISTERED",
      sourceChannel: "EMAIL",
      templateName: "Welcome Email MSP Email Template",
    },
    {
      type: "WELCOME_EMAIL",
      channel: "EMAIL",
      role: "ASPIRE_ADMIN",
      sourceType: "NEW_USER_REGISTERED",
      sourceChannel: "EMAIL",
      templateName: "Welcome Email Aspire Admin Email Template",
    },
    {
      type: "WELCOME_EMAIL",
      channel: "IN_APP",
      role: "CLIENT_ADMIN",
      sourceType: "NEW_USER_REGISTERED",
      sourceChannel: "IN_APP",
      templateName: "Welcome Email Client Admin In-App Template",
    },
    {
      type: "WELCOME_EMAIL",
      channel: "IN_APP",
      role: "ASPIRE_ADMIN",
      sourceType: "NEW_USER_REGISTERED",
      sourceChannel: "IN_APP",
      templateName: "Welcome Email Aspire Admin In-App Template",
    },

    // USER_PASSWORD_CHANGE — CLIENT_ADMIN already exists; MSP + ASPIRE_ADMIN from *_ADMIN.
    {
      type: "USER_PASSWORD_CHANGE",
      channel: "EMAIL",
      role: "MSP",
      sourceType: "USER_PASSWORD_CHANGE_ADMIN",
      sourceChannel: "EMAIL",
      templateName: "User Password Change MSP Email Template",
    },
    {
      type: "USER_PASSWORD_CHANGE",
      channel: "EMAIL",
      role: "ASPIRE_ADMIN",
      sourceType: "USER_PASSWORD_CHANGE_ADMIN",
      sourceChannel: "EMAIL",
      templateName: "User Password Change Aspire Admin Email Template",
    },
    {
      type: "USER_PASSWORD_CHANGE",
      channel: "IN_APP",
      role: "MSP",
      sourceType: "USER_PASSWORD_CHANGE_ADMIN",
      sourceChannel: "IN_APP",
      templateName: "User Password Change MSP In-App Template",
    },
    {
      type: "USER_PASSWORD_CHANGE",
      channel: "IN_APP",
      role: "ASPIRE_ADMIN",
      sourceType: "USER_PASSWORD_CHANGE_ADMIN",
      sourceChannel: "IN_APP",
      templateName: "User Password Change Aspire Admin In-App Template",
    },

    // COURSE_COMPLETION_USER — CLIENT_ADMIN already exists; MSP disabled for this type.
    {
      type: "COURSE_COMPLETION_USER",
      channel: "EMAIL",
      role: "ASPIRE_ADMIN",
      sourceType: "COURSE_COMPLETION",
      sourceChannel: "EMAIL",
      templateName: "Course Completion Aspire Admin Email Template",
    },
    {
      type: "COURSE_COMPLETION_USER",
      channel: "IN_APP",
      role: "ASPIRE_ADMIN",
      sourceType: "COURSE_COMPLETION",
      sourceChannel: "IN_APP",
      templateName: "Course Completion Aspire Admin In-App Template",
    },

    // CERTIFICATE_ISSUED — CLIENT_ADMIN EMAIL already exists; no IN_APP variants (base is third-person).
    {
      type: "CERTIFICATE_ISSUED",
      channel: "EMAIL",
      role: "MSP",
      sourceType: "CERTIFICATE_ISSUED_ADMIN",
      sourceChannel: "EMAIL",
      templateName: "Certificate Issued MSP Email Template",
    },
    {
      type: "CERTIFICATE_ISSUED",
      channel: "EMAIL",
      role: "ASPIRE_ADMIN",
      sourceType: "CERTIFICATE_ISSUED_ADMIN",
      sourceChannel: "EMAIL",
      templateName: "Certificate Issued Aspire Admin Email Template",
    },

    // PACKAGE_ASSIGNED_USER — CLIENT_ADMIN already exists.
    {
      type: "PACKAGE_ASSIGNED_USER",
      channel: "EMAIL",
      role: "MSP",
      sourceType: "PACKAGE_ASSIGNED",
      sourceChannel: "EMAIL",
      templateName: "Package Assigned MSP Email Template",
    },
    {
      type: "PACKAGE_ASSIGNED_USER",
      channel: "EMAIL",
      role: "ASPIRE_ADMIN",
      sourceType: "PACKAGE_ASSIGNED",
      sourceChannel: "EMAIL",
      templateName: "Package Assigned Aspire Admin Email Template",
    },
    {
      type: "PACKAGE_ASSIGNED_USER",
      channel: "IN_APP",
      role: "MSP",
      sourceType: "PACKAGE_ASSIGNED",
      sourceChannel: "IN_APP",
      templateName: "Package Assigned MSP In-App Template",
    },
    {
      type: "PACKAGE_ASSIGNED_USER",
      channel: "IN_APP",
      role: "ASPIRE_ADMIN",
      sourceType: "PACKAGE_ASSIGNED",
      sourceChannel: "IN_APP",
      templateName: "Package Assigned Aspire Admin In-App Template",
    },

    // PACKAGE_ASSIGNED_AND_USER_CREDENTIAL — CLIENT_ADMIN already exists; MSP disabled for this type.
    {
      type: "PACKAGE_ASSIGNED_AND_USER_CREDENTIAL",
      channel: "EMAIL",
      role: "ASPIRE_ADMIN",
      sourceType: "PACKAGE_ASSIGNED",
      sourceChannel: "EMAIL",
      templateName: "Package Assigned Aspire Admin Email Template",
    },
    {
      type: "PACKAGE_ASSIGNED_AND_USER_CREDENTIAL",
      channel: "IN_APP",
      role: "ASPIRE_ADMIN",
      sourceType: "PACKAGE_ASSIGNED",
      sourceChannel: "IN_APP",
      templateName: "Package Assigned Aspire Admin In-App Template",
    },

    // USER_SUSPENSION — base email is user-addressed; admin wording lives in USER_SUSPENDED.
    // Email only: in-app is third-person and SMS is user-addressed in both types.
    {
      type: "USER_SUSPENSION",
      channel: "EMAIL",
      role: "CLIENT_ADMIN",
      sourceType: "USER_SUSPENDED",
      sourceChannel: "EMAIL",
      templateName: "User Suspension Client Admin Email Template",
    },
    {
      type: "USER_SUSPENSION",
      channel: "EMAIL",
      role: "MSP",
      sourceType: "USER_SUSPENDED",
      sourceChannel: "EMAIL",
      templateName: "User Suspension MSP Email Template",
    },
    {
      type: "USER_SUSPENSION",
      channel: "EMAIL",
      role: "ASPIRE_ADMIN",
      sourceType: "USER_SUSPENDED",
      sourceChannel: "EMAIL",
      templateName: "User Suspension Aspire Admin Email Template",
    },
  ];

  let inserted = 0;
  let skippedExisting = 0;
  let missingSource = 0;

  MAPPINGS.forEach((mapping) => {
    const existing = db[COLLECTION].findOne({
      notification_type: mapping.type,
      channel: mapping.channel,
      recipient_role: mapping.role,
    });
    if (existing) {
      skippedExisting++;
      return;
    }

    const source = db[COLLECTION].findOne({
      notification_type: mapping.sourceType,
      channel: mapping.sourceChannel,
      $or: [{ recipient_role: null }, { recipient_role: { $exists: false } }],
    });
    if (!source) {
      print(
        "WARN missing source for " +
          mapping.type +
          "/" +
          mapping.channel +
          "/" +
          mapping.role +
          " (expected " +
          mapping.sourceType +
          "/" +
          mapping.sourceChannel +
          " base)"
      );
      missingSource++;
      return;
    }

    const greetingKey = roleGreetingPlaceholder(mapping.role);
    const now = new Date();
    db[COLLECTION].insertOne({
      notification_type: mapping.type,
      channel: mapping.channel,
      recipient_role: mapping.role,
      template_name: mapping.templateName,
      subject_template: source.subject_template != null ? source.subject_template : null,
      html_template: remapAdminGreeting(source.html_template, greetingKey),
      text_template: remapAdminGreeting(source.text_template, greetingKey),
      title_template: source.title_template != null ? source.title_template : null,
      message_template: source.message_template != null ? source.message_template : null,
      is_active: true,
      is_default: false,
      organization_id: null,
      created_at: now,
      updated_at: now,
      _class: null,
    });
    inserted++;
  });

  function roleGreetingPlaceholder(role) {
    if (role === "CLIENT_ADMIN") return "clientAdminName";
    if (role === "MSP") return "mspName";
    if (role === "ASPIRE_ADMIN") return "aspireAdminName";
    return "adminName";
  }

  function remapAdminGreeting(content, greetingKey) {
    if (content == null || greetingKey === "adminName") return content;
    return String(content).split("{{adminName}}").join("{{" + greetingKey + "}}");
  }

  print(
    "Phase 1 (role variants): inserted=" +
      inserted +
      " skippedExisting=" +
      skippedExisting +
      " missingSource=" +
      missingSource
  );

  // Phase 2: "default" means the role-agnostic base template for a (type, channel) pair.
  const defaultsCleared = db[COLLECTION].updateMany(
    { recipient_role: { $ne: null }, is_default: true },
    { $set: { is_default: false } }
  );
  print(
    "Phase 2 (normalize is_default): matched=" +
      defaultsCleared.matchedCount +
      " modified=" +
      defaultsCleared.modifiedCount
  );

  // Phase 3: unique index so a second variant for the same role cannot be inserted.
  const duplicates = db[COLLECTION]
    .aggregate([
      {
        $group: {
          _id: {
            notification_type: "$notification_type",
            channel: "$channel",
            recipient_role: "$recipient_role",
          },
          count: { $sum: 1 },
          ids: { $push: "$_id" },
        },
      },
      { $match: { count: { $gt: 1 } } },
    ])
    .toArray();

  if (duplicates.length > 0) {
    print(
      "Phase 3 (unique index): SKIPPED — found " +
        duplicates.length +
        " duplicate (notification_type, channel, recipient_role) key(s):"
    );
    duplicates.forEach((dup) => {
      print(
        "  " +
          JSON.stringify(dup._id) +
          " count=" +
          dup.count +
          " ids=" +
          JSON.stringify(dup.ids)
      );
    });
  } else {
    db[COLLECTION].createIndex(
      { notification_type: 1, channel: 1, recipient_role: 1 },
      { unique: true, name: "uniq_notification_type_channel_recipient_role" }
    );
    print(
      "Phase 3 (unique index): created uniq_notification_type_channel_recipient_role"
    );
  }

  print(
    "Migration complete. inserted=" +
      inserted +
      " skippedExisting=" +
      skippedExisting +
      " missingSource=" +
      missingSource
  );
})();
