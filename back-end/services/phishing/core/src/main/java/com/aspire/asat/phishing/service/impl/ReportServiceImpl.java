package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.dto.response.CampaignPerformanceDto;
import com.aspire.asat.phishing.dto.response.EmailActivityDto;
import com.aspire.asat.phishing.dto.response.UserRiskSummaryDto;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.EmailActivity;
import com.aspire.asat.phishing.model.UserRiskProfile;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.repository.custom.RecipientWindowActivityMetrics;
import com.aspire.asat.phishing.service.ReportService;
import com.aspire.asat.phishing.service.support.CampaignPerformanceSupport;
import com.aspire.asat.phishing.service.support.DashboardChannelScope;
import com.aspire.asat.phishing.utils.CohortMetricsCalculator;
import com.aspire.asat.phishing.utils.RiskScoreUtils;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation for report operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    public static final Set<CampaignStatus> REPORTABLE_CAMPAIGN_STATUSES = Set.of(
            CampaignStatus.RUNNING,
            CampaignStatus.COMPLETED,
            CampaignStatus.EXPIRED
    );

    private final UserCurrentContextService userCurrentContextService;
    private final CampaignRepository campaignRepository;
    private final EmailActivityRepository emailActivityRepository;
    private final UserRiskProfileRepository userRiskProfileRepository;

    @Override
    public List<CampaignPerformanceDto> getCampaignReports(int offset, int pageSize, 
            String sortBy, String sortOrder) {
        return getCampaignReports(offset, pageSize, sortBy, sortOrder, CampaignChannel.EMAIL);
    }

    @Override
    public List<CampaignPerformanceDto> getCampaignReports(int offset, int pageSize, 
            String sortBy, String sortOrder, CampaignChannel channel) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);

        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy != null ? sortBy : "createdAt"
        );

        Pageable pageable = PageRequest.of(offset, pageSize, sort);
        Page<Campaign> campaigns = campaignRepository.findWithStatuses(
                clientId, null, REPORTABLE_CAMPAIGN_STATUSES, effectiveChannel, pageable);

        return campaigns.getContent().stream()
                .map(CampaignPerformanceSupport::toPerformanceDto)
                .toList();
    }

    @Override
    public long countCampaignReports() {
        return countCampaignReports(CampaignChannel.EMAIL);
    }

    @Override
    public long countCampaignReports(CampaignChannel channel) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);
        return campaignRepository.countByClientIdAndChannelAndStatuses(
                clientId, effectiveChannel, REPORTABLE_CAMPAIGN_STATUSES);
    }

    @Override
    public CampaignPerformanceDto getCampaignReportById(String campaignId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();

        Campaign campaign = campaignRepository.findByIdAndClientId(campaignId, clientId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        return CampaignPerformanceSupport.toPerformanceDto(campaign);
    }

    @Override
    public List<EmailActivityDto> getEmailActivityLog(int offset, int pageSize, 
            ActivityType activityType, Instant startTime, Instant endTime, String search,
            CampaignChannel channel) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);
        List<String> campaignIds = campaignRepository.findIdsByClientIdAndChannel(clientId, effectiveChannel);

        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<EmailActivity> activities = emailActivityRepository.findEmailActivityLog(
                clientId, activityType, startTime, endTime,
                StringUtils.hasText(search) ? search.trim() : null,
                campaignIds,
                pageable);

        List<EmailActivity> content = activities.getContent();
        if (content.isEmpty()) {
            return List.of();
        }

        return activities.getContent().stream()
                .map(this::toEmailActivityDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countEmailActivities(ActivityType activityType, Instant startTime, Instant endTime, String search,
            CampaignChannel channel) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);
        List<String> campaignIds = campaignRepository.findIdsByClientIdAndChannel(clientId, effectiveChannel);
        return emailActivityRepository.countEmailActivityLog(
                clientId, activityType, startTime, endTime,
                StringUtils.hasText(search) ? search.trim() : null,
                campaignIds);
    }

    @Override
    public List<UserRiskSummaryDto> getUserRiskReport(int offset, int pageSize, 
            String department, String search, RiskLevel riskLevel, String sortBy, String sortOrder) {
        return getUserRiskReport(offset, pageSize, department, search, riskLevel, sortBy, sortOrder, CampaignChannel.EMAIL);
    }

    @Override
    public List<UserRiskSummaryDto> getUserRiskReport(int offset, int pageSize, 
            String department, String search, RiskLevel riskLevel, String sortBy, String sortOrder,
            CampaignChannel channel) {
        List<UserRiskSummaryDto> filtered = getFilteredAndSortedChannelUserRiskList(
                department, search, riskLevel, sortBy, sortOrder, channel);

        int total = filtered.size();
        if (offset >= total) {
            return List.of();
        }
        int fromIndex = Math.max(0, offset);
        int toIndex = Math.min(offset + pageSize, total);
        return filtered.subList(fromIndex, toIndex);
    }

    @Override
    public long countUsersForRiskReport(String department, String search, RiskLevel riskLevel) {
        return countUsersForRiskReport(department, search, riskLevel, CampaignChannel.EMAIL);
    }

    @Override
    public long countUsersForRiskReport(String department, String search, RiskLevel riskLevel, CampaignChannel channel) {
        List<UserRiskSummaryDto> filtered = getFilteredAndSortedChannelUserRiskList(
                department, search, riskLevel, null, null, channel);
        return filtered.size();
    }

    private List<UserRiskSummaryDto> getFilteredAndSortedChannelUserRiskList(
            String department, String search, RiskLevel riskLevel, String sortBy, String sortOrder,
            CampaignChannel channel) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);

        List<String> campaignIds = campaignRepository.findIdsByClientIdAndChannel(clientId, effectiveChannel);
        if (campaignIds.isEmpty()) {
            return List.of();
        }

        Instant now = Instant.now();
        Instant start = now.minus(30, ChronoUnit.DAYS);
        List<RecipientWindowActivityMetrics> rows = emailActivityRepository.aggregateRecipientMetricsForWindow(
                clientId, start, now, campaignIds, DashboardChannelScope.activityMapping(effectiveChannel));
        if (rows.isEmpty()) {
            return List.of();
        }

        Set<String> emails = rows.stream()
                .map(RecipientWindowActivityMetrics::recipientEmail)
                .filter(email -> email != null && !email.isBlank())
                .collect(Collectors.toSet());

        Map<String, UserRiskProfile> emailToProfile = userRiskProfileRepository.findByClientIdAndEmailIn(clientId, emails).stream()
                .filter(p -> p.getEmail() != null && !p.getEmail().isBlank())
                .collect(Collectors.toMap(UserRiskProfile::getEmail, p -> p, (a, b) -> a));

        List<UserRiskSummaryDto> dtos = rows.stream()
                .map(row -> buildChannelUserRiskSummaryDto(row, emailToProfile.get(row.recipientEmail())))
                .toList();

        String deptFilter = StringUtils.hasText(department) ? department.trim() : null;
        String searchFilter = StringUtils.hasText(search) ? search.trim().toLowerCase() : null;

        List<UserRiskSummaryDto> filtered = dtos.stream()
                .filter(u -> u.getUserId() != null && !u.getUserId().isBlank())
                .filter(u -> {
                    if (deptFilter != null) {
                        if (u.getDepartment() == null || !u.getDepartment().equalsIgnoreCase(deptFilter)) {
                            return false;
                        }
                    }
                    if (searchFilter != null) {
                        boolean match = (u.getEmail() != null && u.getEmail().toLowerCase().contains(searchFilter))
                                || (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(searchFilter))
                                || (u.getLastName() != null && u.getLastName().toLowerCase().contains(searchFilter))
                                || (u.getFullName() != null && u.getFullName().toLowerCase().contains(searchFilter))
                                || (u.getDepartment() != null && u.getDepartment().toLowerCase().contains(searchFilter));
                        if (!match) {
                            return false;
                        }
                    }
                    if (riskLevel != null && u.getRiskLevel() != riskLevel) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        Comparator<UserRiskSummaryDto> comparator = buildUserRiskDtoComparator(sortBy, sortOrder);
        filtered.sort(comparator);

        return filtered;
    }

    private UserRiskSummaryDto buildChannelUserRiskSummaryDto(RecipientWindowActivityMetrics row, UserRiskProfile profile) {
        double score = CohortMetricsCalculator.phishingRiskScoreFromActivityCounts(
                row.opened(), row.clicked(), row.submits(), row.reported());
        RiskLevel calculatedRiskLevel = RiskScoreUtils.fromScore(score);

        String firstName = profile != null ? profile.getFirstName() : null;
        String lastName = profile != null ? profile.getLastName() : null;
        String fullName = "";
        if (firstName != null) fullName += firstName;
        if (lastName != null) fullName += (fullName.isEmpty() ? "" : " ") + lastName;

        String userId = profile != null ? profile.getUserId() : null;
        String department = profile != null ? profile.getDepartment() : null;
        Instant lastActivityAt = profile != null ? profile.getLastActivityAt() : null;
        Instant lastClickedAt = profile != null ? profile.getLastClickedAt() : null;

        return UserRiskSummaryDto.builder()
                .userId(userId)
                .email(row.recipientEmail())
                .firstName(firstName)
                .lastName(lastName)
                .fullName(fullName.trim())
                .department(department)
                .riskLevel(calculatedRiskLevel)
                .riskScore(RiskScoreUtils.roundToTwoDecimals(score))
                .campaignsTargeted(profile != null ? profile.getCampaignsTargeted() : 0)
                .emailsReceived(row.delivered())
                .emailsOpened(row.opened())
                .emailsClicked(row.clicked())
                .dataSubmissions(row.submits())
                .emailsReported(row.reported())
                .breachesInvolved(0)
                .isRepeatOffender(row.clicked() >= 3)
                .lastActivityAt(lastActivityAt)
                .lastClickedAt(lastClickedAt)
                .build();
    }

    private Comparator<UserRiskSummaryDto> buildUserRiskDtoComparator(String sortBy, String sortOrder) {
        String field = sortBy == null ? "riskScore" : sortBy.trim().toLowerCase();
        boolean asc = "asc".equalsIgnoreCase(sortOrder);

        Comparator<UserRiskSummaryDto> comp = switch (field) {
            case "email" -> Comparator.comparing(u -> nullToEmpty(u.getEmail()), String.CASE_INSENSITIVE_ORDER);
            case "firstname" -> Comparator.comparing(u -> nullToEmpty(u.getFirstName()), String.CASE_INSENSITIVE_ORDER);
            case "lastname" -> Comparator.comparing(u -> nullToEmpty(u.getLastName()), String.CASE_INSENSITIVE_ORDER);
            case "department" -> Comparator.comparing(u -> nullToEmpty(u.getDepartment()), String.CASE_INSENSITIVE_ORDER);
            case "risklevel" -> Comparator.comparing(u -> u.getRiskLevel() != null ? u.getRiskLevel().ordinal() : -1);
            case "emailsreceived" -> Comparator.comparingInt(UserRiskSummaryDto::getEmailsReceived);
            case "emailsopened" -> Comparator.comparingInt(UserRiskSummaryDto::getEmailsOpened);
            case "emailsclicked" -> Comparator.comparingInt(UserRiskSummaryDto::getEmailsClicked);
            case "datasubmissions" -> Comparator.comparingInt(UserRiskSummaryDto::getDataSubmissions);
            case "emailsreported" -> Comparator.comparingInt(UserRiskSummaryDto::getEmailsReported);
            default -> Comparator.comparing(u -> u.getRiskScore() != null ? u.getRiskScore() : 0.0);
        };

        return asc ? comp : comp.reversed();
    }

    @Override
    public byte[] exportCampaignReport(String campaignId, String format) {
        // TODO: Implement PDF/Excel export using a library like Apache POI or iText
        log.info("Exporting campaign report {} in format: {}", campaignId, format);
        
        CampaignPerformanceDto report = getCampaignReportById(campaignId);
        
        // Placeholder - return simple CSV for now
        StringBuilder csv = new StringBuilder();
        csv.append("Campaign Report\n");
        csv.append("Name,").append(report.getCampaignName()).append("\n");
        csv.append("Status,").append(report.getStatus()).append("\n");
        csv.append("Recipients,").append(report.getTotalRecipients()).append("\n");
        csv.append("Sent,").append(report.getEmailsSent()).append("\n");
        csv.append("Opened,").append(report.getOpened()).append("\n");
        csv.append("Clicked,").append(report.getClicked()).append("\n");
        csv.append("Submitted,").append(report.getDataSubmitted()).append("\n");
        csv.append("Reported,").append(report.getReported()).append("\n");
        csv.append("Open Rate,").append(report.getOpenRate()).append("%\n");
        csv.append("Click Rate,").append(report.getClickRate()).append("%\n");

        return csv.toString().getBytes();
    }

    @Override
    public byte[] exportUserRiskReport(String format) {
        return exportUserRiskReport(format, CampaignChannel.EMAIL);
    }

    @Override
    public byte[] exportUserRiskReport(String format, CampaignChannel channel) {
        log.info("Exporting user risk report in format: {}, channel: {}", format, channel);

        List<UserRiskSummaryDto> users = getFilteredAndSortedChannelUserRiskList(
                null, null, null, "riskScore", "desc", channel);
        String normalizedFormat = format == null ? "csv" : format.trim().toLowerCase();

        try {
            return switch (normalizedFormat) {
                case "csv" -> generateUserRiskCsv(users);
                case "excel", "xlsx" -> generateUserRiskExcel(users);
                case "pdf" -> generateUserRiskPdf(users);
                default -> throw new IllegalArgumentException("Unsupported export format: " + format);
            };
        } catch (Exception e) {
            log.error("Failed generating user-risk export. format={}, usersCount={}", normalizedFormat, users.size(), e);
            throw e;
        }
    }

    private byte[] generateUserRiskCsv(List<UserRiskSummaryDto> users) {
        StringBuilder csv = new StringBuilder();
        csv.append("Email,Name,Department,Risk Level,Risk Score,Emails Received,Emails Opened,Clicks,Submissions,Emails Reported\n");

        for (UserRiskSummaryDto user : users) {
            csv.append(csvValue(user.getEmail())).append(",");
            csv.append(csvValue(user.getFullName())).append(",");
            csv.append(csvValue(user.getDepartment())).append(",");
            csv.append(csvValue(user.getRiskLevel())).append(",");
            csv.append(csvValue(user.getRiskScore())).append(",");
            csv.append(csvValue(user.getEmailsReceived())).append(",");
            csv.append(csvValue(user.getEmailsOpened())).append(",");
            csv.append(csvValue(user.getEmailsClicked())).append(",");
            csv.append(csvValue(user.getDataSubmissions())).append(",");
            csv.append(csvValue(user.getEmailsReported())).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] generateUserRiskExcel(List<UserRiskSummaryDto> users) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("User Risk Report");
            int rowIdx = 0;

            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Email");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Department");
            header.createCell(3).setCellValue("Risk Level");
            header.createCell(4).setCellValue("Risk Score");
            header.createCell(5).setCellValue("Emails Received");
            header.createCell(6).setCellValue("Emails Opened");
            header.createCell(7).setCellValue("Clicks");
            header.createCell(8).setCellValue("Submissions");
            header.createCell(9).setCellValue("Emails Reported");

            for (UserRiskSummaryDto user : users) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(nullToEmpty(user.getEmail()));
                row.createCell(1).setCellValue(nullToEmpty(user.getFullName()));
                row.createCell(2).setCellValue(nullToEmpty(user.getDepartment()));
                row.createCell(3).setCellValue(user.getRiskLevel() != null ? user.getRiskLevel().name() : "");
                row.createCell(4).setCellValue(user.getRiskScore() != null ? user.getRiskScore() : 0.0);
                row.createCell(5).setCellValue(user.getEmailsReceived());
                row.createCell(6).setCellValue(user.getEmailsOpened());
                row.createCell(7).setCellValue(user.getEmailsClicked());
                row.createCell(8).setCellValue(user.getDataSubmissions());
                row.createCell(9).setCellValue(user.getEmailsReported());
            }

            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            byte[] data = outputStream.toByteArray();
            if (!isLikelyXlsx(data)) {
                throw new RuntimeException("Generated Excel binary is invalid or empty");
            }
            log.info("Generated user risk Excel export successfully. usersCount={}, bytes={}", users.size(), data.length);
            return data;
        } catch (Exception e) {
            log.error("Failed to generate user risk Excel export. usersCount={}", users.size(), e);
            throw new RuntimeException("Failed to generate user risk Excel export", e);
        }
    }

    private byte[] generateUserRiskPdf(List<UserRiskSummaryDto> users) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
                .append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>")
                .append("<meta charset=\"UTF-8\"/>")
                .append("<style>")
                .append("@page{size:A4 landscape;margin:12mm;}")
                .append("body{font-family:Arial,sans-serif;font-size:10px;}")
                .append("h2{margin-bottom:12px;}")
                .append("table{width:100%;border-collapse:collapse;}")
                .append("thead{display:table-header-group;}")
                .append("tbody{display:table-row-group;}")
                .append("th,td{border:1px solid #cccccc;padding:6px;text-align:left;}")
                .append("th{background:#f5f5f5;}")
                .append("</style></head><body>");
        html.append("<h2>User Risk Report</h2>");
        html.append("<table><thead><tr>")
                .append("<th>Email</th><th>Name</th><th>Department</th><th>Risk Level</th>")
                .append("<th>Risk Score</th><th>Emails Received</th><th>Emails Opened</th>")
                .append("<th>Clicks</th><th>Submissions</th><th>Emails Reported</th>")
                .append("</tr></thead><tbody>");

        for (UserRiskSummaryDto user : users) {
            html.append("<tr>")
                    .append("<td>").append(escapeHtml(user.getEmail())).append("</td>")
                    .append("<td>").append(escapeHtml(user.getFullName())).append("</td>")
                    .append("<td>").append(escapeHtml(user.getDepartment())).append("</td>")
                    .append("<td>").append(escapeHtml(user.getRiskLevel() != null ? user.getRiskLevel().name() : "")).append("</td>")
                    .append("<td>").append(escapeHtml(String.valueOf(user.getRiskScore() != null ? user.getRiskScore() : 0.0))).append("</td>")
                    .append("<td>").append(escapeHtml(String.valueOf(user.getEmailsReceived()))).append("</td>")
                    .append("<td>").append(escapeHtml(String.valueOf(user.getEmailsOpened()))).append("</td>")
                    .append("<td>").append(escapeHtml(String.valueOf(user.getEmailsClicked()))).append("</td>")
                    .append("<td>").append(escapeHtml(String.valueOf(user.getDataSubmissions()))).append("</td>")
                    .append("<td>").append(escapeHtml(String.valueOf(user.getEmailsReported()))).append("</td>")
                    .append("</tr>");
        }
        html.append("</tbody></table></body></html>");

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html.toString(), null);
            builder.toStream(outputStream);
            builder.run();
            byte[] data = outputStream.toByteArray();
            if (!isLikelyPdf(data)) {
                throw new RuntimeException("Generated PDF binary is invalid or empty");
            }
            log.info("Generated user risk PDF export successfully. usersCount={}, bytes={}", users.size(), data.length);
            return data;
        } catch (Exception e) {
            log.error("Failed to generate user risk PDF export. usersCount={}", users.size(), e);
            throw new RuntimeException("Failed to generate user risk PDF export", e);
        }
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value);
        String escaped = text.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private boolean isLikelyXlsx(byte[] data) {
        if (data == null || data.length < 4) {
            return false;
        }
        return data[0] == 'P' && data[1] == 'K' && data[2] == 3 && data[3] == 4;
    }

    private boolean isLikelyPdf(byte[] data) {
        if (data == null || data.length < 5) {
            return false;
        }
        return data[0] == '%' && data[1] == 'P' && data[2] == 'D' && data[3] == 'F' && data[4] == '-';
    }

    private EmailActivityDto toEmailActivityDto(EmailActivity activity) {
        return EmailActivityDto.builder()
                .activityId(activity.getId())
                .campaignId(activity.getCampaignId())
                .campaignName(blankToNull(activity.getCampaignName()))
                .recipientId(activity.getRecipientId())
                .recipientEmail(blankToNull(activity.getRecipientEmail()))
                .recipientName(blankToNull(activity.getRecipientName()))
                .activityType(activity.getActivityType())
                .activityLabel(getActivityLabel(activity.getActivityType()))
                .timestamp(activity.getTimestamp())
                .ipAddress(activity.getIpAddress())
                .geoLocation(activity.getGeoLocation())
                .deviceType(activity.getDeviceType())
                .browser(activity.getBrowser())
                .build();
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private UserRiskSummaryDto toUserRiskSummaryDto(UserRiskProfile profile) {
        String fullName = "";
        if (profile.getFirstName() != null) fullName += profile.getFirstName();
        if (profile.getLastName() != null) fullName += " " + profile.getLastName();

        return UserRiskSummaryDto.builder()
                .userId(profile.getUserId())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .fullName(fullName.trim())
                .department(profile.getDepartment())
                .riskLevel(profile.getRiskLevel())
                .riskScore(RiskScoreUtils.roundToTwoDecimals(profile.getRiskScore()))
                .campaignsTargeted(profile.getCampaignsTargeted())
                .emailsReceived(profile.getEmailsReceived())
                .emailsOpened(profile.getEmailsOpened())
                .emailsClicked(profile.getLinksClicked())
                .dataSubmissions(profile.getDataSubmissions())
                .emailsReported(profile.getEmailsReported())
                .breachesInvolved(profile.getBreachesInvolved())
                .isRepeatOffender(profile.isRepeatOffender())
                .lastActivityAt(profile.getLastActivityAt())
                .lastClickedAt(profile.getLastClickedAt())
                .build();
    }

    private String getActivityLabel(ActivityType type) {
        switch (type) {
            case EMAIL_SENT: return "Email Sent";
            case EMAIL_DELIVERED: return "Email Delivered";
            case EMAIL_BOUNCED: return "Email Bounced";
            case EMAIL_OPENED: return "Email Opened";
            case LINK_CLICKED: return "Link Clicked";
            case ATTACHMENT_OPENED: return "Attachment Opened";
            case DATA_SUBMITTED: return "Data Submitted";
            case EMAIL_REPORTED: return "Email Reported";
            default: return type.name();
        }
    }
}
