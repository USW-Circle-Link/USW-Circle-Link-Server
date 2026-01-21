package com.USWCicrcleLink.server.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;

@Setter
@Getter
@AllArgsConstructor
public class ApiResponse<T>{
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public ApiResponse(String message) {
        this.message = message;
    }
}