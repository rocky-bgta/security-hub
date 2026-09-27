package com.aspire.asat.registration.service.microsoft;


import com.aspire.asat.registration.data.microsoft.MicrosoftGroupDto;
import com.aspire.asat.registration.data.microsoft.MicrosoftUserDto;

import java.util.List;

public interface MicrosoftGraphService {

    List<MicrosoftGroupDto> getAllGroups(String accessToken);

    MicrosoftGroupDto getGroupById(String groupId, String accessToken);

    List<MicrosoftUserDto> getGroupMembers(String groupId, String accessToken);

    List<MicrosoftUserDto> getAllUsers(String accessToken);

    MicrosoftUserDto getUserById(String userId, String accessToken);
}

