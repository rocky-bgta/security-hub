/**
 * Sets templateType=EMAIL on email_templates missing the field.
 *
 *   mongosh "mongodb://localhost:27017/phishing" --file email-templates-template-type-default-email.js
 */
(function () {
  const result = db.email_templates.updateMany(
    { templateType: { $exists: false } },
    { $set: { templateType: "EMAIL" } }
  );
  print("email-templates-template-type-default-email: matched=" + result.matchedCount + " modified=" + result.modifiedCount);
})();
