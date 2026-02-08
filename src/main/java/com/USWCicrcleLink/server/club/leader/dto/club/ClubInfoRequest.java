package com.USWCicrcleLink.server.club.leader.dto.club;

import com.USWCicrcleLink.server.club.domain.RecruitmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ClubInfoRequest {

    @Size(max = 3000, message = "소개글은 최대 3000자까지 입력 가능합니다.")
    private String clubInfo;

    private RecruitmentStatus recruitmentStatus;

    @Size(max = 3000, message = "모집글은 최대 3000자까지 입력 가능합니다.")
    private String clubRecruitment;

    @Pattern(regexp = "^(https://[a-zA-Z0-9._-]+(?:\\.[a-zA-Z]{2,})+.*)?$", message = "유효한 HTTPS 링크를 입력해주세요.")
    private String googleFormUrl;

    private List<Integer> orders;

    private List<Integer> deletedOrders;

}
