package com.USWCicrcleLink.server.profile.major.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MajorListResponse {
    private Long collegeId;
    private String collegeName;
    private List<MajorResponse> majors;
}
