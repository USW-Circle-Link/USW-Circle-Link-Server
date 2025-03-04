package com.USWCicrcleLink.server.user.dto;

import com.USWCicrcleLink.server.global.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDTO {

    @NotBlank(message = "이메일을 입력해주세요")
    @Size(min = 1, max = 30, message = "이메일은 1~30자 이내여야 합니다.",groups = ValidationGroups.SizeGroup.class )
    private String email;

}
