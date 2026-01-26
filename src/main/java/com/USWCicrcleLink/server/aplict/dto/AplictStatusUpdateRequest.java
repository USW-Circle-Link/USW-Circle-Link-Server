package com.USWCicrcleLink.server.aplict.dto;

import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AplictStatusUpdateRequest {
    @NotNull(message = "변경할 상태값은 필수입니다. (PASS, FAIL, WAIT)")
    private AplictStatus status;
}