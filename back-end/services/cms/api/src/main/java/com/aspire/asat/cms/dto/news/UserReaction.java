package com.aspire.asat.cms.dto.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserReaction {

    private String id;
    private String userId;  // User who reacted
    private String newsId;  // News the user reacted to
    private NewsReaction reaction;  // The user's reaction

}