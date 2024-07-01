package com.USWCicrcleLink.server.aplict.dto;

import lombok.Data;

@Data
public class AplictRequest {
    private Long profileId;
    private Long clubId;
    private String aplictGoogleFormUrl;
}
