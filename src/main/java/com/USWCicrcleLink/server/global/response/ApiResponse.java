package com.USWCicrcleLink.server.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) //null 값이 있는 필드는 JSON에서 제외
public class ApiResponse {
    private String message;
    private Object data;

    public ApiResponse(String message) {
        this.message = message;
    }

    //데이터를 포함하는 경우
    public ApiResponse(String message, Object data) {
        this.message = message;
        this.data = data;
    }
}
