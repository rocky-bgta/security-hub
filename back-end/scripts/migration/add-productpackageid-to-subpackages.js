const CMS_DB_NAME = "cms";                    // Database containing sub_packages
const REGISTRATION_DB_NAME = "registration";  // Database containing client_products

print("==========================================");
print("Migration: Add productPackageId to sub_packages");
print("Matching by: clientAdminId + productId + packageId + licenseStatus");
print("==========================================");
print("CMS Database (sub_packages): " + CMS_DB_NAME);
print("Registration Database (client_products): " + REGISTRATION_DB_NAME);
print("Started at: " + new Date().toISOString());
print("==========================================\n");

// Get database references
const cmsDb = db.getSiblingDB(CMS_DB_NAME);
const registrationDb = db.getSiblingDB(REGISTRATION_DB_NAME);

// Verify collections exist
let subPackageCount = cmsDb.sub_packages.countDocuments();
let clientProductCount = registrationDb.client_products.countDocuments();

print("Verification:");
print("  - sub_packages collection in " + CMS_DB_NAME + ": " + subPackageCount + " documents");
print("  - client_products collection in " + REGISTRATION_DB_NAME + ": " + clientProductCount + " documents");
print("");

if (subPackageCount === 0) {
    print("[ERROR] No documents found in sub_packages collection. Please verify database name.");
    print("==========================================\n");
} else if (clientProductCount === 0) {
    print("[ERROR] No documents found in client_products collection. Please verify database name.");
    print("==========================================\n");
} else {

    // Statistics tracking
    let stats = {
        total: 0,
        updated: 0,
        skippedNoMatch: 0,
        skippedDuplicate: 0,
        skippedAlreadySet: 0,
        skippedNoClientAdminId: 0,
        errors: 0,
        details: {
            noMatch: [],
            duplicates: [],
            noClientAdminId: [],
            errors: []
        }
    };

    let processed = 0;
    let batchSize = 100;

    // License status priority order
    let statusPriority = ["ACTIVE", "PENDING"];

    try {
        // Find all SubPackage documents that need productPackageId (from CMS database)
        let subPackages = cmsDb.sub_packages.find({
            $or: [
                { productPackageId: { $exists: false } },
                { productPackageId: null },
                { productPackageId: "" }
            ]
        }).toArray();
        
        stats.total = subPackages.length;
        print("Found " + stats.total + " SubPackage records to process\n");
        
        if (stats.total === 0) {
            print("No records to migrate. All SubPackages already have productPackageId set.");
            print("==========================================\n");
        } else {
            // Process each SubPackage
            subPackages.forEach(function(subPackage) {
                processed++;
                
                try {
                    // Skip if productPackageId is already set
                    if (subPackage.productPackageId && subPackage.productPackageId.toString().trim() !== "") {
                        stats.skippedAlreadySet++;
                        return;
                    }
                    
                    // Use clientAdminId if available, otherwise fallback to clientId
                    // (In sub_packages table, clientAdminId and clientId are the same)
                    let effectiveClientAdminId = null;
                    if (subPackage.clientAdminId && subPackage.clientAdminId.toString().trim() !== "") {
                        effectiveClientAdminId = subPackage.clientAdminId;
                    } else if (subPackage.clientId && subPackage.clientId.toString().trim() !== "") {
                        effectiveClientAdminId = subPackage.clientId;
                    }

                    // Skip if both clientAdminId and clientId are missing
                    if (!effectiveClientAdminId) {
                        stats.skippedNoClientAdminId++;
                        stats.details.noClientAdminId.push({
                            subPackageId: subPackage._id,
                            subPackageName: subPackage.name || "unnamed",
                            productId: subPackage.productId,
                            packageId: subPackage.packageId
                        });
                        return;
                    }

                    let selectedMatch = null;
                    let isDuplicate = false;
                    let duplicateInfo = null;

                    // Try each licenseStatus in priority order
                    for (let i = 0; i < statusPriority.length && !selectedMatch && !isDuplicate; i++) {
                        let currentStatus = statusPriority[i];

                        // Find ClientProducts from REGISTRATION database
                        // Matching: clientAdminId + productId + packageId + licenseStatus
                        // Using effectiveClientAdminId (clientAdminId or clientId from SubPackage)
                        let matches = registrationDb.client_products.find({
                            clientAdminId: effectiveClientAdminId,
                            productId: subPackage.productId,
                            packageId: subPackage.packageId,
                            licenseStatus: currentStatus
                        }).toArray();

                        if (matches.length === 1) {
                            // Exactly one match - use it
                            selectedMatch = matches[0];
                        } else if (matches.length > 1) {
                            // Multiple matches with same clientAdminId + productId + packageId + licenseStatus = DUPLICATE
                            isDuplicate = true;
                            duplicateInfo = {
                                subPackageId: subPackage._id,
                                subPackageName: subPackage.name || "unnamed",
                                clientAdminId: effectiveClientAdminId,
                                productId: subPackage.productId,
                                packageId: subPackage.packageId,
                                matchCount: matches.length,
                                licenseStatus: currentStatus,
                                clientProductIds: matches.map(function(m) { return m._id; })
                            };
                        }
                        // If matches.length === 0, continue to next status
                    }

                    // Handle result
                    if (isDuplicate) {
                        stats.skippedDuplicate++;
                        stats.details.duplicates.push(duplicateInfo);
                        print("[WARNING] Skipped SubPackage " + subPackage._id + ": Found " + duplicateInfo.matchCount + " ClientProducts with:");
                        print("          clientAdminId=" + effectiveClientAdminId);
                        print("          productId=" + subPackage.productId);
                        print("          packageId=" + subPackage.packageId);
                        print("          licenseStatus=" + duplicateInfo.licenseStatus);
                        print("          ClientProduct IDs: " + duplicateInfo.clientProductIds.join(", "));
                    } else if (selectedMatch === null) {
                        // No match found for any status
                        stats.skippedNoMatch++;
                        stats.details.noMatch.push({
                            subPackageId: subPackage._id,
                            subPackageName: subPackage.name || "unnamed",
                            clientAdminId: effectiveClientAdminId,
                            productId: subPackage.productId,
                            packageId: subPackage.packageId
                        });
                } else {
                    // Exactly one match - update the SubPackage in CMS database
                    // Using aggregation pipeline to place productPackageId after status field
                    let newProductPackageId = selectedMatch._id;
                    let now = new Date();
                    
                    // Reconstruct document with desired field order (productPackageId after status)
                    let updateResult = cmsDb.sub_packages.updateOne(
                        { _id: subPackage._id },
                        [{
                            $replaceRoot: {
                                newRoot: {
                                    $mergeObjects: [
                                        // Fields before productPackageId (in order)
                                        {
                                            _id: "$_id",
                                            name: "$name",
                                            description: "$description",
                                            productId: "$productId",
                                            packageId: "$packageId",
                                            clientId: "$clientId",
                                            clientAdminId: "$clientAdminId",
                                            topicId: "$topicId",
                                            createdBy: "$createdBy",
                                            status: "$status",
                                            productPackageId: newProductPackageId
                                        },
                                        // Fields after productPackageId (in order)
                                        {
                                            createdAt: "$createdAt",
                                            updatedAt: now,
                                            deleted: "$deleted",
                                            isTrial: "$isTrial",
                                            showInSite: "$showInSite",
                                            isAlreadyAssigned: "$isAlreadyAssigned",
                                            _class: "$_class"
                                        }
                                    ]
                                }
                            }
                        }]
                    );
                        
                    if (updateResult.modifiedCount === 1) {
                        stats.updated++;
                        if (processed % 10 === 0 || processed === stats.total) {
                            print("[" + processed + "/" + stats.total + "] Updated SubPackage " + subPackage._id + " (" + (subPackage.name || "unnamed") + ")");
                            print("          productPackageId: " + newProductPackageId);
                            print("          licenseStatus: " + selectedMatch.licenseStatus);
                        }
                    } else {
                        stats.errors++;
                        stats.details.errors.push({
                            subPackageId: subPackage._id,
                            error: "Update operation did not modify document"
                        });
                    }
                }
                
                // Progress indicator
                if (processed % batchSize === 0) {
                    print("Progress: " + processed + "/" + stats.total + " (updated: " + stats.updated + ", no match: " + stats.skippedNoMatch + ", duplicates: " + stats.skippedDuplicate + ")");
                }
                
            } catch (error) {
                stats.errors++;
                stats.details.errors.push({
                    subPackageId: subPackage._id,
                    error: error.message || String(error)
                });
                print("[ERROR] Error processing SubPackage " + subPackage._id + ": " + (error.message || String(error)));
            }
        });
        }
        
        // Print summary
        print("\n==========================================");
        print("Migration Summary");
        print("Completed at: " + new Date().toISOString());
        print("==========================================");
        print("Total SubPackages processed: " + stats.total);
        print("Successfully updated: " + stats.updated);
        print("Skipped (no match found): " + stats.skippedNoMatch);
        print("Skipped (duplicate matches): " + stats.skippedDuplicate);
        print("Skipped (already set): " + stats.skippedAlreadySet);
        print("Skipped (no clientAdminId): " + stats.skippedNoClientAdminId);
        print("Errors: " + stats.errors);
        print("==========================================\n");
        
        // Print no-match details
        if (stats.details.noMatch.length > 0) {
            print("\nSubPackages with no matching ClientProduct (" + stats.details.noMatch.length + "):");
            stats.details.noMatch.slice(0, 10).forEach(function(item) {
                print("  - ID: " + item.subPackageId + ", Name: " + item.subPackageName);
                print("    clientAdminId: " + item.clientAdminId);
                print("    productId: " + item.productId + ", packageId: " + item.packageId);
            });
            if (stats.details.noMatch.length > 10) {
                print("  ... and " + (stats.details.noMatch.length - 10) + " more");
            }
        }
        
        // Print duplicate details
        if (stats.details.duplicates.length > 0) {
            print("\nSubPackages with DUPLICATE ClientProduct matches (" + stats.details.duplicates.length + "):");
            stats.details.duplicates.slice(0, 10).forEach(function(item) {
                print("  - SubPackage ID: " + item.subPackageId + ", Name: " + item.subPackageName);
                print("    clientAdminId: " + item.clientAdminId);
                print("    productId: " + item.productId + ", packageId: " + item.packageId);
                print("    licenseStatus: " + item.licenseStatus);
                print("    Duplicate Count: " + item.matchCount);
                print("    ClientProduct IDs: " + item.clientProductIds.join(", "));
            });
            if (stats.details.duplicates.length > 10) {
                print("  ... and " + (stats.details.duplicates.length - 10) + " more");
            }
        }
        
        // Print no clientAdminId details
        if (stats.details.noClientAdminId.length > 0) {
            print("\nSubPackages without clientAdminId (" + stats.details.noClientAdminId.length + "):");
            stats.details.noClientAdminId.slice(0, 10).forEach(function(item) {
                print("  - ID: " + item.subPackageId + ", Name: " + item.subPackageName);
                print("    productId: " + item.productId + ", packageId: " + item.packageId);
            });
            if (stats.details.noClientAdminId.length > 10) {
                print("  ... and " + (stats.details.noClientAdminId.length - 10) + " more");
            }
        }
        
        // Print errors
        if (stats.details.errors.length > 0) {
            print("\nErrors encountered (" + stats.details.errors.length + "):");
            stats.details.errors.slice(0, 10).forEach(function(item) {
                print("  - SubPackage ID: " + item.subPackageId + ", Error: " + item.error);
            });
        }
        
        print("\n==========================================");
        print("Migration completed!");
        print("==========================================\n");
        
    } catch (error) {
        print("\n==========================================");
        print("FATAL ERROR during migration:");
        print(error.message || String(error));
        print("==========================================\n");
    }
}
