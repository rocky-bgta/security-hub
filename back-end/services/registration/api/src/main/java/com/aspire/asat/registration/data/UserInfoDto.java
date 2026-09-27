package com.aspire.asat.registration.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserInfoDto {

    private List<String> group;
    private List<String> department;
    private List<String> country;

}
