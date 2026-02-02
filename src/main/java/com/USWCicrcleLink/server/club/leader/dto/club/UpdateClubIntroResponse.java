package com.USWCicrcleLink.server.club.leader.dto.club;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClubIntroResponse {
    List<String> presignedUrls;
}

