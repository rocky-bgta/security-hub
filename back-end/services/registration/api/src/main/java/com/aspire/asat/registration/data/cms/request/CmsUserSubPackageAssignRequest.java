package com.aspire.asat.registration.data.cms.request;

import com.aspire.asat.registration.data.subpackage.SubPackageAssignRequest;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class CmsUserSubPackageAssignRequest {
    
    private List<SubPackageData> subPackageData;

    @Data
    public static class SubPackageData {
        private String subPackageId;
        private List<String> userIdList;
        private String productId;
        private String productPackageId;
        private String clientAdminId;
        private String status;
        private Long validFor;
        private String channel;
        private boolean enableFirstUserNotificationEmail;
        private List<String> secondaryEmails;
        private List<String> thirdLevelEmails;
        private List<String> fourthHREmails;
        private CompletionDays completionDays;
    }

    @Data
    public static class CompletionDays {
        private DurationUnit durationUnit;
        private Integer durationValue;
    }

    public enum DurationUnit {
        DAYS, WEEKS, MONTHS
    }

    /**
     * Converts Registration service SubPackageAssignRequest to CMS service request format
     */
    public static CmsUserSubPackageAssignRequest fromRegistrationRequest(SubPackageAssignRequest registrationRequest) {
        CmsUserSubPackageAssignRequest cmsRequest = new CmsUserSubPackageAssignRequest();
        
        cmsRequest.setSubPackageData(
            registrationRequest.getSubPackageData().stream()
                .map(regData -> {
                    SubPackageData cmsData = new SubPackageData();
                    cmsData.setSubPackageId(regData.getSubPackageId());
                    cmsData.setUserIdList(regData.getUserIdList());
                    cmsData.setProductId(regData.getProductId());
                    cmsData.setProductPackageId(regData.getProductPackageId());
                    cmsData.setClientAdminId(regData.getClientAdminId());
                    cmsData.setStatus(regData.getStatus());
                    cmsData.setValidFor(regData.getValidFor());
                    cmsData.setChannel(regData.getChannel());
                    cmsData.setEnableFirstUserNotificationEmail(regData.isEnableFirstUserNotificationEmail());
                    cmsData.setSecondaryEmails(regData.getSecondaryEmails());
                    cmsData.setThirdLevelEmails(regData.getThirdLevelEmails());
                    cmsData.setFourthHREmails(regData.getFourthHREmails());
                    
                    if (regData.getCompletionDays() != null) {
                        CompletionDays cmsCompletionDays = new CompletionDays();
                        cmsCompletionDays.setDurationUnit(DurationUnit.valueOf(regData.getCompletionDays().getDurationUnit().name()));
                        cmsCompletionDays.setDurationValue(regData.getCompletionDays().getDurationValue());
                        cmsData.setCompletionDays(cmsCompletionDays);
                    }
                    
                    return cmsData;
                })
                .collect(Collectors.toList())
        );
        
        return cmsRequest;
    }
}
