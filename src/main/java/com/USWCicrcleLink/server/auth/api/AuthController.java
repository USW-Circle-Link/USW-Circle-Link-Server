package com.USWCicrcleLink.server.auth.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.security.jwt.dto.TokenDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.USWCicrcleLink.server.global.bucket4j.RateLimite;
import com.USWCicrcleLink.server.auth.dto.*;
import com.USWCicrcleLink.server.auth.service.AuthService;
import com.USWCicrcleLink.server.user.dto.*;
import com.USWCicrcleLink.server.user.service.*;
import com.USWCicrcleLink.server.user.domain.*;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.exception.errortype.RateLimitExceededException;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.email.domain.EmailToken;
import com.USWCicrcleLink.server.global.email.service.EmailTokenService;
import com.USWCicrcleLink.server.global.validation.ValidationSequence;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final EmailTokenService emailTokenService;
    private final AuthTokenService authTokenService;
    private final WithdrawalTokenService withdrawalTokenService;

    /**
     * 로그인 (유저, 회장, 동연회 통합)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UnifiedLoginResponse>> login(
            @RequestBody @Validated(ValidationSequence.class) UnifiedLoginRequest request,
            HttpServletResponse response) {

        // 1. Try User Login
        try {
            return ResponseEntity.ok(new ApiResponse<>("유저 로그인 성공", authService.userLogin(request, response)));
        } catch (Exception e) {
            log.info("User Login Failed: {}", e.getMessage());
        }

        // 2. Try Club Leader Login
        try {
            return ResponseEntity.ok(new ApiResponse<>("동아리 회장 로그인 성공", authService.leaderLogin(request, response)));
        } catch (RateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            log.info("Leader Login Failed: {}", e.getMessage());
        }

        // 3. Try Admin Login
        try {
            return ResponseEntity.ok(new ApiResponse<>("관리자 로그인 성공", authService.adminLogin(request, response)));
        } catch (RateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            log.info("Admin Login Failed: {}", e.getMessage());
        }

        throw new UserException(ExceptionType.USER_AUTHENTICATION_FAILED);
    }

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(
            @Validated(ValidationSequence.class) @RequestBody SignUpRequest request,
            @RequestHeader("emailTokenUUID") UUID emailTokenUUID, @RequestHeader("signupUUID") UUID signupUUID) {
        String email = userService.isEmailVerified(emailTokenUUID, signupUUID);
        userService.checkNewSignupCondition(request);
        userService.signUpUser(request, email);
        return ResponseEntity.ok(new ApiResponse<>("회원가입이 정상적으로 완료되어 로그인이 가능합니다."));
    }

    /**
     * 아이디 중복 확인
     */
    @GetMapping("/check-Id")
    public ResponseEntity<ApiResponse<String>> verifyAccountDuplicate(@RequestParam("Id") String account) {
        userService.verifyAccountDuplicate(account);
        return ResponseEntity.ok(new ApiResponse<>("사용 가능한 ID 입니다."));
    }

    /**
     * 회원가입/인증용 이메일 코드 발송
     */
    @PostMapping("/signup/verification-mail")
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> registerTemporaryUser(
            @Validated @RequestBody EmailDTO request) {
        EmailToken emailToken = userService.checkEmailDuplication(request.getEmail());
        userService.sendSignUpMail(emailToken);
        return ResponseEntity.ok(new ApiResponse<>("인증 메일 전송 완료",
                new VerifyEmailResponse(emailToken.getEmailTokenUUID(), emailToken.getEmail())));
    }

    /**
     * 이메일 코드 인증 확인
     */
    @PostMapping("/signup/verify")
    public ResponseEntity<ApiResponse<SignUpuuidResponse>> emailVerification(@Validated @RequestBody EmailDTO request) {
        EmailToken emailToken = emailTokenService.checkEmailIsVerified(request.getEmail());
        return ResponseEntity.ok(new ApiResponse<>("인증 확인 버튼 클릭 후, 이메일 인증 완료",
                new SignUpuuidResponse(emailToken.getEmailTokenUUID(), emailToken.getSignupUUID())));
    }

    /**
     * 비밀번호 찾기 인증코드 전송
     */
    @PostMapping("/password/reset-code")
    @RateLimite(action = "PW_FOUND_EMAIL")
    public ResponseEntity<ApiResponse<UUID>> sendAuthCode(@Valid @RequestBody UserInfoDto request) {
        User user = userService.validateAccountAndEmail(request);
        AuthToken authToken = authTokenService.createOrUpdateAuthToken(user);
        userService.sendAuthCodeMail(user, authToken);
        return ResponseEntity.ok(new ApiResponse<>("인증코드가 전송 되었습니다", user.getUserUUID()));
    }

    /**
     * 비밀번호 찾기 인증코드 확인
     */
    @PostMapping("/password/verify")
    @RateLimite(action = "VALIDATE_CODE")
    public ApiResponse<String> verifyAuthToken(@RequestHeader("uuid") UUID uuid,
            @Valid @RequestBody AuthCodeRequest request) {
        authTokenService.verifyAuthToken(uuid, request);
        authTokenService.deleteAuthToken(uuid);
        return new ApiResponse<>("인증 코드 검증이 완료되었습니다");
    }

    /**
     * 비밀번호 초기화 (인증 후)
     */
    @PatchMapping("/password/reset")
    public ApiResponse<String> resetUserPw(@RequestHeader("uuid") UUID uuid, @RequestBody PasswordRequest request) {
        userService.resetPW(uuid, request);
        return new ApiResponse<>("비밀번호가 변경되었습니다.");
    }

    /**
     * 아이디 찾기 (이메일로 전송)
     */
    @PostMapping("/find-id")
    public ResponseEntity<ApiResponse<String>> findUserAccount(@RequestBody EmailDTO request) {
        User findUser = userService.findUser(request.getEmail());
        userService.sendAccountInfoMail(findUser);
        return ResponseEntity.ok(new ApiResponse<>("계정 정보 전송 완료"));
    }

    /**
     * 회원 탈퇴 인증코드 전송
     */
    @PostMapping("/withdrawal/code")
    @RateLimite(action = "WITHDRAWAL_EMAIL")
    public ApiResponse<String> sendWithdrawalCode() {
        WithdrawalToken token = withdrawalTokenService.createOrUpdateWithdrawalToken();
        userService.sendWithdrawalCodeMail(token);
        return new ApiResponse<>("탈퇴를 위한 인증 메일이 전송 되었습니다");
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(new ApiResponse<>("로그아웃 성공"));
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenDto>> refreshToken(HttpServletRequest request,
            HttpServletResponse response) {
        TokenDto tokenDto = authService.refreshToken(request, response);

        if (tokenDto == null) {
            return ResponseEntity.status(401).body(new ApiResponse<>("리프레시 토큰이 유효하지 않습니다. 로그아웃됐습니다.", null));
        }
        return ResponseEntity.ok(new ApiResponse<>("새로운 엑세스 토큰과 리프레시 토큰이 발급됐습니다. 로그인됐습니다.", tokenDto));
    }
}
