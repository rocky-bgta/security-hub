# Poll/Survey Enhanced Features - Implementation Summary

## Overview
Enhanced the Poll/Survey system with advanced features including date management, result visibility control, multiple submissions, and support for various question types including text-based answers.

---

## New Features Implemented

### 1. Poll/Survey Management Fields

#### Added Fields to PollSurvey Entity:
- **startDate** (LocalDate) - Required start date for the poll/survey
- **endDate** (LocalDate) - End date for the poll/survey
- **showResultsToUsers** (Boolean) - Controls whether users can see voting results
- **allowMultipleSubmissions** (Boolean) - Allows users to vote multiple times

### 2. Question Types (NEW ENUM)

Created `QuestionType` enum with 5 types:
- **RADIO** - Single choice (radio button)
- **MCQ** - Multiple choice (checkboxes)
- **SHORT_TEXT** - Short text input
- **LONG_TEXT** - Long text input (textarea)
- **RATING** - Rating (1-5 or similar, displayed as radio)

### 3. Text-Based Answers

- Added `textAnswer` field to `PollSurveyVoteLog` entity
- Vote requests now support both `answerIds` and `textAnswer` fields
- System validates answer type based on question type

---

## API Changes

### Create Poll/Survey
**Endpoint:** `POST /api/polls`

**New Request Fields:**
```json
{
  "title": "Customer Satisfaction Survey",
  "description": "Help us improve our services",
  "type": "SURVEY",
  "status": "ACTIVE",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "showResultsToUsers": true,
  "allowMultipleSubmissions": false,
  "questions": [
    {
      "questionText": "How satisfied are you?",
      "questionType": "RATING",
      "answers": [
        {"answerText": "Very Satisfied"},
        {"answerText": "Satisfied"},
        {"answerText": "Neutral"},
        {"answerText": "Dissatisfied"},
        {"answerText": "Very Dissatisfied"}
      ]
    },
    {
      "questionText": "What can we improve?",
      "questionType": "LONG_TEXT",
      "answers": []
    },
    {
      "questionText": "Your email",
      "questionType": "SHORT_TEXT",
      "answers": []
    },
    {
      "questionText": "Select your preferences",
      "questionType": "MCQ",
      "answers": [
        {"answerText": "Option 1"},
        {"answerText": "Option 2"},
        {"answerText": "Option 3"}
      ]
    }
  ]
}
```

### Update Poll/Survey
**Endpoint:** `PUT /api/polls/{id}`
- Same structure as create request
- All new fields supported

### Vote Submission
**Endpoint:** `POST /api/polls/{pollId}/votes`

**Request with Multiple Question Types:**
```json
{
  "votes": [
    {
      "questionId": "uuid-1",
      "answerIds": ["answer-uuid"],
      "textAnswer": null
    },
    {
      "questionId": "uuid-2",
      "answerIds": null,
      "textAnswer": "This is my feedback about your service..."
    },
    {
      "questionId": "uuid-3",
      "answerIds": null,
      "textAnswer": "user@example.com"
    },
    {
      "questionId": "uuid-4",
      "answerIds": ["answer-uuid-1", "answer-uuid-2"],
      "textAnswer": null
    }
  ]
}
```

### Get Latest Active Poll with User Votes
**Endpoint:** `GET /api/polls/latest-active`

**Response:**
```json
{
  "message": "Latest active poll/survey retrieved successfully",
  "statusCode": 200,
  "data": {
    "id": "uuid",
    "title": "Customer Satisfaction Survey",
    "description": "Help us improve",
    "type": "SURVEY",
    "status": "ACTIVE",
    "questions": [...],
    "summary": {
      // Only included if showResultsToUsers = true
      // null if showResultsToUsers = false
    },
    "userSelectedAnswerIds": [
      "answer-uuid-1",
      "answer-uuid-2"
    ]
  }
}
```

---

## Validation Rules

### Question Type Validations:

1. **RADIO Questions:**
   - Only ONE answer allowed per question
   - Validation: Throws error if multiple answerIds provided

2. **MCQ Questions:**
   - MULTIPLE answers allowed per question
   - No validation on count

3. **RATING Questions:**
   - Only ONE answer allowed per question
   - Same validation as RADIO

4. **SHORT_TEXT Questions:**
   - Must provide `textAnswer` field
   - `answerIds` field ignored
   - No predefined answers needed

5. **LONG_TEXT Questions:**
   - Must provide `textAnswer` field
   - `answerIds` field ignored
   - No predefined answers needed

### Multiple Submissions Logic:

- **allowMultipleSubmissions = false (default):**
  - Creates a submission record to prevent duplicates
  - User gets error: "You have already voted for this poll/survey"
  
- **allowMultipleSubmissions = true:**
  - No submission record created
  - User can vote unlimited times
  - Each vote is recorded with timestamp

### Show Results Logic:

- **showResultsToUsers = false (default):**
  - Summary is NULL in `/latest-active` endpoint
  - Users cannot see voting statistics
  
- **showResultsToUsers = true:**
  - Summary is included in `/latest-active` endpoint
  - Users can see vote counts and percentages

---

## Database Schema Changes

### PollSurvey Collection:
```javascript
{
  _id: UUID,
  title: String,
  description: String,
  type: "POLL" | "SURVEY",
  status: "ACTIVE" | "INACTIVE" | "DRAFT",
  startDate: ISODate,        // NEW
  endDate: ISODate,          // NEW
  showResultsToUsers: Boolean, // NEW (default: false)
  allowMultipleSubmissions: Boolean, // NEW (default: false)
  createdBy: String,
  createdAt: DateTime,
  updatedBy: String,
  updatedAt: DateTime,
  questions: [
    {
      id: UUID,
      questionText: String,
      questionType: "RADIO" | "MCQ" | "SHORT_TEXT" | "LONG_TEXT" | "RATING", // NEW
      pollSurveyId: UUID,
      answers: [
        {
          id: UUID,
          answerText: String,
          questionId: UUID
        }
      ]
    }
  ]
}
```

### PollSurveyVoteLog Collection:
```javascript
{
  _id: UUID,
  userId: String,
  pollSurveyId: UUID,
  questionId: UUID,
  answerId: UUID,          // For RADIO, MCQ, RATING
  textAnswer: String,      // NEW - For SHORT_TEXT, LONG_TEXT
  createdAt: DateTime,
  updatedAt: DateTime
}
```

---

## Response DTO Changes

### PollSurveyResponse:
```java
{
  id: UUID,
  title: String,
  description: String,
  type: PollSurveyType,
  status: PollSurveyStatus,
  startDate: LocalDate,              // NEW
  endDate: LocalDate,                // NEW
  showResultsToUsers: Boolean,       // NEW
  allowMultipleSubmissions: Boolean, // NEW
  questions: List<PollSurveyQuestionDto>
}
```

### PollSurveyQuestionDto:
```java
{
  id: UUID,
  questionText: String,
  questionType: QuestionType, // NEW
  answers: List<PollSurveyAnswerDto>
}
```

### PollSurveyDetailWithSummaryResponse:
```java
{
  id: UUID,
  title: String,
  description: String,
  type: PollSurveyType,
  status: PollSurveyStatus,
  questions: List<PollSurveyQuestionDto>,
  summary: PollSurveySummaryResponse,  // null if showResultsToUsers=false
  userSelectedAnswerIds: List<UUID>    // NEW - User's vote history
}
```

---

## Files Modified

### New Files Created:
1. `/services/universal/api/src/main/java/com/aspire/asat/enums/QuestionType.java`

### Modified Files:
1. `/services/universal/core/src/main/java/com/aspire/asat/universal/entity/PollSurvey.java`
2. `/services/universal/core/src/main/java/com/aspire/asat/universal/entity/PollSurveyQuestion.java`
3. `/services/universal/core/src/main/java/com/aspire/asat/universal/entity/PollSurveyVoteLog.java`
4. `/services/universal/api/src/main/java/com/aspire/asat/pool/PollSurveyCreateRequest.java`
5. `/services/universal/api/src/main/java/com/aspire/asat/pool/PollSurveyUpdateRequest.java`
6. `/services/universal/api/src/main/java/com/aspire/asat/pool/PollSurveyQuestionCreateDto.java`
7. `/services/universal/api/src/main/java/com/aspire/asat/pool/PollSurveyVoteRequest.java`
8. `/services/universal/api/src/main/java/com/aspire/asat/pool/PollSurveyResponse.java`
9. `/services/universal/api/src/main/java/com/aspire/asat/pool/PollSurveyQuestionDto.java`
10. `/services/universal/api/src/main/java/com/aspire/asat/pool/PollSurveyDetailWithSummaryResponse.java`
11. `/services/universal/core/src/main/java/com/aspire/asat/universal/service/impl/PollSurveyServiceImpl.java`
12. `/services/universal/core/src/main/java/com/aspire/asat/universal/repository/PollSurveyRepository.java`

---

## Example Use Cases

### Use Case 1: Customer Satisfaction Survey with Mixed Question Types
```json
{
  "title": "Q4 Customer Satisfaction Survey",
  "type": "SURVEY",
  "status": "ACTIVE",
  "startDate": "2025-10-01",
  "endDate": "2025-12-31",
  "showResultsToUsers": true,
  "allowMultipleSubmissions": false,
  "questions": [
    {
      "questionText": "Overall satisfaction rating?",
      "questionType": "RATING"
    },
    {
      "questionText": "What features do you use most? (Select all that apply)",
      "questionType": "MCQ"
    },
    {
      "questionText": "What improvements would you suggest?",
      "questionType": "LONG_TEXT"
    }
  ]
}
```

### Use Case 2: Quick Poll with Results Hidden
```json
{
  "title": "Product Feature Vote",
  "type": "POLL",
  "status": "ACTIVE",
  "startDate": "2025-11-01",
  "endDate": "2025-11-30",
  "showResultsToUsers": false,
  "allowMultipleSubmissions": false,
  "questions": [
    {
      "questionText": "Which feature should we prioritize?",
      "questionType": "RADIO",
      "answers": [
        {"answerText": "Dark Mode"},
        {"answerText": "Mobile App"},
        {"answerText": "API Access"},
        {"answerText": "Advanced Analytics"}
      ]
    }
  ]
}
```

### Use Case 3: Continuous Feedback Form
```json
{
  "title": "Continuous Feedback",
  "type": "SURVEY",
  "status": "ACTIVE",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "showResultsToUsers": false,
  "allowMultipleSubmissions": true,  // Users can submit multiple times
  "questions": [
    {
      "questionText": "Today's experience rating",
      "questionType": "RATING"
    },
    {
      "questionText": "Any issues or suggestions?",
      "questionType": "LONG_TEXT"
    }
  ]
}
```

---

## Testing Checklist

- [x] Create poll with startDate and endDate
- [x] Create poll with showResultsToUsers = true/false
- [x] Create poll with allowMultipleSubmissions = true/false
- [x] Create questions with all question types (RADIO, MCQ, SHORT_TEXT, LONG_TEXT, RATING)
- [x] Vote with text answers for SHORT_TEXT and LONG_TEXT questions
- [x] Vote with answer IDs for RADIO, MCQ, RATING questions
- [x] Validate single answer restriction for RADIO and RATING
- [x] Validate multiple answers allowed for MCQ
- [x] Test multiple submissions when allowMultipleSubmissions = true
- [x] Test submission blocking when allowMultipleSubmissions = false
- [x] Test /latest-active endpoint shows summary when showResultsToUsers = true
- [x] Test /latest-active endpoint hides summary when showResultsToUsers = false
- [x] Test userSelectedAnswerIds includes user's previous votes

---

## Summary

✅ All requested features implemented successfully
✅ Backward compatible with existing polls/surveys
✅ Comprehensive validation for different question types
✅ Flexible voting system supporting both choice and text-based answers
✅ User-friendly result visibility control
✅ Multiple submission support for feedback forms
✅ Latest active poll API respects showResultsToUsers flag

