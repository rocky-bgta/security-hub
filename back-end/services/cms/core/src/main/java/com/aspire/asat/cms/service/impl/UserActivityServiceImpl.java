package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.common.MonthlyActivityDurationDTO;
import com.aspire.asat.cms.model.UserActivity;
import com.aspire.asat.cms.repository.UserActivityRepository;
import com.aspire.asat.cms.service.UserActivityService;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class UserActivityServiceImpl implements UserActivityService {

    private UserActivityRepository userActivityRepository;
    private MongoTemplate mongoTemplate;

    public UserActivityServiceImpl(UserActivityRepository userActivityRepository, MongoTemplate mongoTemplate) {
        this.userActivityRepository = userActivityRepository;
        this.mongoTemplate = mongoTemplate;
    }

    // Method to update 'lastAvailableTime' when a request is received
    @Override
    public void updateActivity(String userId, String sessionId) {
        UserActivity activity = userActivityRepository.findByUserIdAndSessionId(userId, sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Update last available time
        activity.setLastAvailableTime(LocalDateTime.now());
        userActivityRepository.save(activity);
    }

    @Scheduled(fixedRate = 1200000)  // Every 20 minutes (1200000 ms)
    public void checkUserActivity() {
        // Calculate the time threshold (10 minutes ago)
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);

        // Convert LocalDateTime to Date (MongoDB stores dates in ISODate format)
        Date thresholdDate = Date.from(threshold.atZone(ZoneId.systemDefault()).toInstant());

        // Query for documents where lastAvailableTime is older than 20 minutes and endTime is null
        Query query = new Query();
        query.addCriteria(Criteria.where("lastAvailableTime").lt(thresholdDate))  // lastAvailableTime is older than 20 minutes
                .addCriteria(Criteria.where("endTime").is(null));  // endTime is null (session is not completed yet)

        // Use MongoDB aggregation to calculate and update fields directly in the database
        // Calculate duration as the difference between lastAvailableTime and startTime (in milliseconds)
        // Using custom AggregationOperation to avoid SpEL parsing issues
        AggregationOperation projectOperation = context -> {
            Document projection = new Document();
            projection.put("endTime", "$lastAvailableTime");
            projection.put("duration", new Document("$subtract", 
                Arrays.asList("$lastAvailableTime", "$startTime")));
            projection.put("sessionId", "$sessionId");
            projection.put("userId", "$userId");
            projection.put("startTime", "$startTime");
            projection.put("lastAvailableTime", "$lastAvailableTime");
            return new Document("$project", projection);
        };
        
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("lastAvailableTime").lt(thresholdDate)), // Match inactive sessions
                projectOperation
        );

        // Execute aggregation and update documents
        AggregationResults<UserActivity> result = mongoTemplate.aggregate(aggregation, UserActivity.class, UserActivity.class);

        // Now update the documents with the calculated fields
        for (UserActivity activity : result.getMappedResults()) {
            Update update = new Update();
            update.set("endTime", activity.getEndTime());  // Set endTime to lastAvailableTime
            update.set("duration", activity.getDuration());  // Set duration in seconds

            // Update the corresponding session in the database
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("sessionId").is(activity.getSessionId())),
                    update,
                    UserActivity.class
            );
        }
    }

    @Override
    public long getTotalDurationForLast7Days(String userId) {
        // Get the current date and time
        LocalDateTime currentDate = LocalDateTime.now();

        // Calculate the date 7 days ago
        LocalDateTime sevenDaysAgo = currentDate.minusDays(7);

        // Query to find activities for the user between current date and 7 days ago
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("startTime").gte(sevenDaysAgo).lte(currentDate));

        // Fetch activities for the user in the last 7 days
        List<UserActivity> activities = mongoTemplate.find(query, UserActivity.class);

        // Sum up the durations of all activities in that period
        long totalDuration = activities.stream().mapToLong(UserActivity::getDuration).sum();

        return totalDuration;
    }

    @Override
    public long getTotalDurationForLast30Days(String userId) {
        // Get the current date and time
        LocalDateTime currentDate = LocalDateTime.now();

        // Calculate the date 30 days ago
        LocalDateTime thirtyDaysAgo = currentDate.minusDays(30);

        // Query to find activities for the user between current date and 30 days ago
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("startTime").gte(thirtyDaysAgo).lte(currentDate));

        // Fetch activities for the user in the last 30 days
        List<UserActivity> activities = mongoTemplate.find(query, UserActivity.class);

        // Sum up the durations of all activities in that period
        long totalDuration = activities.stream().mapToLong(UserActivity::getDuration).sum();

        return totalDuration;
    }

    @Override
    public MonthlyActivityDurationDTO getTotalDurationForYear(String userId) {
        // Get the current year
        int currentYear = LocalDateTime.now().getYear();

        // Create the DTO to store total duration per month
        MonthlyActivityDurationDTO durationDTO = new MonthlyActivityDurationDTO();

        // Loop through each month (JAN to DEC)
        for (int month = 1; month <= 12; month++) {
            // Get the start and end of the month for the current year
            LocalDateTime startOfMonth = LocalDateTime.now()
                    .withMonth(month)
                    .withDayOfMonth(1)
                    .withYear(currentYear)
                    .withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1); // End of the month

            // Query to find activities for the given user and month
            Query query = new Query();
            query.addCriteria(Criteria.where("userId").is(userId));
            query.addCriteria(Criteria.where("startTime").gte(startOfMonth).lte(endOfMonth));

            // Fetch activities for the user in that month
            List<UserActivity> activities = mongoTemplate.find(query, UserActivity.class);

            // Sum up the durations of all activities in that month
            long totalDuration = activities.stream().mapToLong(UserActivity::getDuration).sum();

            // Set the duration for the corresponding month in the DTO
            switch (month) {
                case 1:
                    durationDTO.setJan(totalDuration);
                    break;
                case 2:
                    durationDTO.setFeb(totalDuration);
                    break;
                case 3:
                    durationDTO.setMar(totalDuration);
                    break;
                case 4:
                    durationDTO.setApr(totalDuration);
                    break;
                case 5:
                    durationDTO.setMay(totalDuration);
                    break;
                case 6:
                    durationDTO.setJun(totalDuration);
                    break;
                case 7:
                    durationDTO.setJul(totalDuration);
                    break;
                case 8:
                    durationDTO.setAug(totalDuration);
                    break;
                case 9:
                    durationDTO.setSep(totalDuration);
                    break;
                case 10:
                    durationDTO.setOct(totalDuration);
                    break;
                case 11:
                    durationDTO.setNov(totalDuration);
                    break;
                case 12:
                    durationDTO.setDec(totalDuration);
                    break;
            }
        }

        return durationDTO;
    }

//    // Scheduled task to check for inactive sessions every 10 minutes
//    @Scheduled(fixedRate = 600000) // 10 minutes in milliseconds
//    public void checkUserActivity() {
//        // Calculate the time threshold (10 minutes ago)
//        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
//
//        // Fetch all user activities where lastAvailableTime is older than the threshold
//        List<UserActivity> inactiveSessions = userActivityRepository.findInactiveSessions(threshold);
//
//        // Loop through the sessions and update them
//        for (UserActivity activity : inactiveSessions) {
//            updateSession(activity);
//        }
//    }
//
//    private void updateSession(UserActivity activity) {
//        // Set the endTime to lastAvailableTime and calculate the duration
//        LocalDateTime currentTime = LocalDateTime.now();
//        activity.setEndTime(activity.getLastAvailableTime());
//        activity.setDuration(Duration.between(activity.getStartTime(), activity.getEndTime()).getSeconds());
//
//        // Save the updated session
//        userActivityRepository.save(activity);
//    }

}
