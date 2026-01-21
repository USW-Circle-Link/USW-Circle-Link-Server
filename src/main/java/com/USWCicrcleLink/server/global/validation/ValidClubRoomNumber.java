package com.USWCicrcleLink.server.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ClubRoomNumberValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidClubRoomNumber {
    String message() default "유효하지 않은 동아리방입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
