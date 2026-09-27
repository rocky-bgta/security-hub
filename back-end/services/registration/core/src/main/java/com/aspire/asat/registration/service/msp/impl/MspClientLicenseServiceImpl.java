package com.aspire.asat.registration.service.msp.impl;

import com.aspire.asat.common.dto.activitylog.ClientAdminActivityLogRequestDto;
import com.aspire.asat.common.dto.activitylog.ClientAdminActivityLogResponseDto;
import com.aspire.asat.common.dto.activitylog.ClientAdminActivityLogDto;
import com.aspire.asat.common.enums.ActivityType;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientAdminDetailedResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.ClientProductDetailedDto;
import com.aspire.asat.registration.data.mspUser.response.*;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.model.ClientAdmin;
import com.aspire.asat.registration.model.ClientProduct;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.ClientProductRepository;
import com.aspire.asat.registration.repository.msp.MspUsersRepository;
import com.aspire.asat.registration.service.ActivityLogService;
import com.aspire.asat.registration.service.ClientAdminService;
import com.aspire.asat.registration.constant.MspLicenseConstants;
import com.aspire.asat.registration.service.msp.MspClientLicenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MspClientLicenseServiceImpl implements MspClientLicenseService {

    private final ClientAdminRepository clientAdminRepository;
    private final ClientProductRepository clientProductRepository;
    private final MspUsersRepository mspUsersRepository;
    private final ClientAdminService clientAdminService;
    private final ActivityLogService activityLogService;

    @Override
    public AllResponseDto<List<ClientLicenseSummaryDto>> getClientLicenseList(
            String mspId,
            Integer offset,
            Integer pageSize,
            String search) {

        if (mspId == null || mspId.isBlank()) {
            throw new RegistrationServiceException("MSP ID cannot be null or empty");
        }
        if (mspUsersRepository.findById(mspId).isEmpty()) {
            throw new RegistrationServiceException("MSP not found with ID: " + mspId);
        }

        List<ClientAdmin> allClients = clientAdminRepository.findAllByMspId(mspId);

        // Optional search filter by organization name or email
        Stream<ClientAdmin> stream = allClients.stream();
        if (search != null && !search.isBlank()) {
            String lower = search.toLowerCase();
            stream = stream.filter(c ->
                    (c.getOrganizationName() != null && c.getOrganizationName().toLowerCase().contains(lower))
                            || (c.getEmail() != null && c.getEmail().toLowerCase().contains(lower))
                            || (c.getContactEmail() != null && c.getContactEmail().toLowerCase().contains(lower)));
        }

        List<ClientAdmin> filtered = stream.collect(Collectors.toList());
        long total = filtered.size();

        int effectiveOffset = (offset != null && offset >= 0) ? offset : 0;
        int effectivePageSize = (pageSize != null && pageSize > 0) ? pageSize : 10;
        int from = Math.min(effectiveOffset * effectivePageSize, filtered.size());
        int to = Math.min(from + effectivePageSize, filtered.size());

        List<ClientLicenseSummaryDto> items = new ArrayList<>();
        for (int i = from; i < to; i++) {
            ClientAdmin client = filtered.get(i);
            items.add(buildClientLicenseSummary(client));
        }

        return new AllResponseDto<>(effectiveOffset, effectivePageSize, total, items);
    }

    @Override
    public MspClientLicenseDetailResponseDto getClientLicenseDetail(String mspId, String clientAdminId) {

        ClientAdmin client = clientAdminRepository.findById(clientAdminId)
                .orElseThrow(() -> new RegistrationServiceException("Client admin not found with ID: " + clientAdminId));

        if (!mspId.equals(client.getMspId())) {
            throw new RegistrationServiceException("Client does not belong to the specified MSP");
        }

        ClientAdminDetailedResponseDto detailed =
                clientAdminService.getClientAdminDetailedById(clientAdminId);

        int totalAllocated = 0;
        int totalUsed = 0;
        for (ClientProductDetailedDto p : detailed.getClientProducts()) {
            if (p.getLicenseCount() != null) totalAllocated += p.getLicenseCount();
            if (p.getUsedLicenseCount() != null) totalUsed += p.getUsedLicenseCount();
        }

        return MspClientLicenseDetailResponseDto.builder()
                .clientId(detailed.getId())
                .clientName(detailed.getOrganizationName())
                .contactEmail(detailed.getContactEmail() != null ? detailed.getContactEmail() : detailed.getEmail())
                .status(detailed.getStatus() != null ? detailed.getStatus().name() : null)
                .totalLicensesAllocated(totalAllocated)
                .licensesInUse(totalUsed)
                .remainingLicenses(totalAllocated - totalUsed)
                .productDetails(detailed.getClientProducts())
                .build();
    }

    @Override
    public ClientLicenseUsageResponseDto getClientLicenseUsage(String mspId, String clientAdminId) {

        ClientAdmin client = clientAdminRepository.findById(clientAdminId)
                .orElseThrow(() -> new RegistrationServiceException("Client admin not found with ID: " + clientAdminId));

        if (!mspId.equals(client.getMspId())) {
            throw new RegistrationServiceException("Client does not belong to the specified MSP");
        }

        List<ClientProduct> products = clientProductRepository.findByClientAdminId(clientAdminId);
        int licensesInUse = products.stream().mapToInt(ClientProduct::getUsedLicenseCount).sum();
        int totalAllocated = products.stream().mapToInt(ClientProduct::getLicenseCount).sum();
        int inactiveLicenseCount = totalAllocated - licensesInUse;

        // License usage history: fetch logs for LICENSE_ALLOCATED and LICENSE_DEALLOCATED
        List<LicenseUsageHistoryItemDto> usageHistory = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, MspLicenseConstants.LICENSE_HISTORY_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));

        for (ActivityType type : Arrays.asList(ActivityType.LICENSE_ALLOCATED, ActivityType.LICENSE_DEALLOCATED)) {
            ClientAdminActivityLogRequestDto req = ClientAdminActivityLogRequestDto.builder()
                    .clientAdminId(clientAdminId)
                    .activityType(type)
                    .offset(0)
                    .pageSize(MspLicenseConstants.LICENSE_HISTORY_PAGE_SIZE)
                    .sortBy("createdAt")
                    .order("desc")
                    .build();
            ClientAdminActivityLogResponseDto resp = activityLogService.getClientAdminActivityLogs(req);
            if (resp.getActivityLogs() != null) {
                for (ClientAdminActivityLogDto dto : resp.getActivityLogs()) {
                    usageHistory.add(LicenseUsageHistoryItemDto.builder()
                            .activityId(dto.getActivityId())
                            .actionType(dto.getActionPerformed())
                            .description(dto.getActionDescription() != null ? dto.getActionDescription() : dto.getDetails())
                            .timestamp(dto.getTimestamp())
                            .oldValue(dto.getOldValue())
                            .newValue(dto.getNewValue())
                            .build());
                }
            }
        }
        usageHistory.sort(Comparator.comparing(LicenseUsageHistoryItemDto::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())));

        // Upcoming expirations: products with expiry within UPCOMING_EXPIRY_DAYS
        Instant threshold = Instant.now().plus(MspLicenseConstants.UPCOMING_EXPIRY_DAYS, ChronoUnit.DAYS);
        List<ClientProductExpiryItemDto> upcomingExpirations = products.stream()
                .filter(p -> p.getExpiryDate() != null && !p.getExpiryDate().isBefore(Instant.now()) && !p.getExpiryDate().isAfter(threshold))
                .map(p -> ClientProductExpiryItemDto.builder()
                        .clientProductId(p.getId())
                        .productId(p.getProductId())
                        .packageId(p.getPackageId())
                        .licenseCount(p.getLicenseCount())
                        .expiryDate(p.getExpiryDate())
                        .licenseStatus(p.getLicenseStatus())
                        .build())
                .collect(Collectors.toList());

        return ClientLicenseUsageResponseDto.builder()
                .clientAdminId(clientAdminId)
                .licensesInUse(licensesInUse)
                .usageHistory(usageHistory)
                .inactiveLicenseCount(inactiveLicenseCount)
                .upcomingExpirations(upcomingExpirations)
                .build();
    }

    @Override
    public byte[] exportClientLicenses(String mspId, String format) {

        if (mspId == null || mspId.isBlank()) {
            throw new RegistrationServiceException("MSP ID cannot be null or empty");
        }
        if (mspUsersRepository.findById(mspId).isEmpty()) {
            throw new RegistrationServiceException("MSP not found with ID: " + mspId);
        }

        String normalizedFormat = (format != null && !format.isBlank()) ? format.trim().toLowerCase() : "csv";

        // Get full list (no pagination) for export
        AllResponseDto<List<ClientLicenseSummaryDto>> data = getClientLicenseList(mspId, 0, Integer.MAX_VALUE, null);
        List<ClientLicenseSummaryDto> items = data.getItems() != null ? data.getItems() : Collections.emptyList();

        if ("csv".equals(normalizedFormat)) {
            return buildCsv(items);
        }
        if ("xlsx".equals(normalizedFormat) || "excel".equals(normalizedFormat)) {
            return buildCsv(items); // Use CSV as fallback if no Excel library in registration service
        }
        if ("pdf".equals(normalizedFormat)) {
            return buildCsv(items); // Use CSV as fallback if no PDF library
        }

        return buildCsv(items);
    }

    private ClientLicenseSummaryDto buildClientLicenseSummary(ClientAdmin client) {

        List<ClientProduct> clientProducts = clientProductRepository.findByClientAdminId(client.getId());
        int allocated = clientProducts.stream().mapToInt(ClientProduct::getLicenseCount).sum();
        int inUse = clientProducts.stream().mapToInt(ClientProduct::getUsedLicenseCount).sum();
        Optional<Instant> minExpiry = clientProducts.stream()
                .map(ClientProduct::getExpiryDate)
                .filter(Objects::nonNull)
                .min(Instant::compareTo);

        return ClientLicenseSummaryDto.builder()
                .clientId(client.getId())
                .clientName(client.getOrganizationName())
                .contactEmail(client.getContactEmail() != null ? client.getContactEmail() : client.getEmail())
                .licensesAllocated(allocated)
                .licensesInUse(inUse)
                .remainingLicenses(allocated - inUse)
                .licenseExpiryDate(minExpiry.orElse(null))
                .status(client.getStatus() != null ? client.getStatus().name() : null)
                .createdAt(client.getCreatedAt())
                .build();
    }

    private byte[] buildCsv(List<ClientLicenseSummaryDto> items) {

        StringBuilder sb = new StringBuilder();
        // BOM for UTF-8
        sb.append('\uFEFF');
        sb.append("Client ID,Client Name,Contact Email,Licenses Allocated,Licenses In Use,Remaining Licenses,License Expiry Date,Status,Created At\n");

        for (ClientLicenseSummaryDto row : items) {
            sb.append(escapeCsv(row.getClientId()));
            sb.append(',');
            sb.append(escapeCsv(row.getClientName()));
            sb.append(',');
            sb.append(escapeCsv(row.getContactEmail()));
            sb.append(',');
            sb.append(row.getLicensesAllocated() != null ? row.getLicensesAllocated() : "");
            sb.append(',');
            sb.append(row.getLicensesInUse() != null ? row.getLicensesInUse() : "");
            sb.append(',');
            sb.append(row.getRemainingLicenses() != null ? row.getRemainingLicenses() : "");
            sb.append(',');
            sb.append(row.getLicenseExpiryDate() != null ? row.getLicenseExpiryDate().toString() : "");
            sb.append(',');
            sb.append(escapeCsv(row.getStatus()));
            sb.append(',');
            sb.append(row.getCreatedAt() != null ? row.getCreatedAt().toString() : "");
            sb.append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
