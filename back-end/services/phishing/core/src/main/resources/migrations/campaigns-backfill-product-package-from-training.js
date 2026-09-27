/**
 * Idempotent backfill: copy trainingData.productPackageId onto campaign root
 * for campaigns that already have training scope.
 *
 * Also unsets obsolete root productId/packageId fields used by the previous
 * license-scope model.
 *
 * Simulation-only campaigns without trainingData.productPackageId are unchanged
 * and will not contribute to unique-user license statistics until Step 1 is
 * updated with productPackageId (ClientProduct.id).
 *
 * Usage (mongosh against phishing DB):
 *   load('campaigns-backfill-product-package-from-training.js')
 */
(function () {
  const copyResult = db.campaigns.updateMany(
    {
      $or: [
        { productPackageId: { $exists: false } },
        { productPackageId: null },
        { productPackageId: '' }
      ],
      'trainingData.productPackageId': { $exists: true, $nin: [null, ''] }
    },
    [
      {
        $set: {
          productPackageId: '$trainingData.productPackageId'
        }
      }
    ]
  );

  print('Backfill productPackageId — Matched: ' + copyResult.matchedCount
      + ', Modified: ' + copyResult.modifiedCount);

  const unsetResult = db.campaigns.updateMany(
    {
      $or: [
        { productId: { $exists: true } },
        { packageId: { $exists: true } }
      ]
    },
    { $unset: { productId: '', packageId: '' } }
  );

  print('Unset obsolete productId/packageId — Matched: ' + unsetResult.matchedCount
      + ', Modified: ' + unsetResult.modifiedCount);

  try {
    db.campaigns.dropIndex('client_status_product_package_idx');
  } catch (e) {
    print('dropIndex client_status_product_package_idx skipped: ' + e.message);
  }

  db.campaigns.createIndex(
    { clientId: 1, status: 1, productPackageId: 1 },
    { name: 'client_status_product_package_idx', background: true }
  );
  print('Ensured index client_status_product_package_idx on productPackageId');
})();
