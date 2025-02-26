package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.service.EmailTokenService;
import com.USWCicrcleLink.server.global.bucket4j.RateLimite;
import com.USWCicrcleLink.server.global.exception.errortype.EmailException;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.security.jwt.dto.TokenDto;
import com.USWCicrcleLink.server.global.validation.ValidationSequence;
import com.USWCicrcleLink.server.user.domain.AuthToken;
import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberTemp;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.domain.WithdrawalToken;
import com.USWCicrcleLink.server.user.dto.*;
import com.USWCicrcleLink.server.user.service.AuthTokenService;
import com.USWCicrcleLink.server.user.service.PasswordService;
import com.USWCicrcleLink.server.user.service.UserService;
import com.USWCicrcleLink.server.user.service.WithdrawalTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthTokenService authTokenService;
    private final EmailTokenService emailTokenService;
    private final WithdrawalTokenService withdrawalTokenService;
    private final PasswordService passwordService;

    @PatchMapping("/userpw")
    public ApiResponse<String> updateUserPw(@Validated(ValidationSequence.class) @RequestBody UpdatePwRequest request) {
        userService.updateNewPW(request);
        return new ApiResponse<>("비밀번호가 성공적으로 업데이트 되었습니다.");
    }

    // 회원가입시 계정 중복 체크
    @GetMapping("/verify-duplicate/{account}")
    public ResponseEntity<ApiResponse<String>> verifyAccountDuplicate(@PathVariable("account") String account) {

        userService.verifyAccountDuplicate(account);

        ApiResponse<String> response = new ApiResponse<>("사용 가능한 ID 입니다.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 비밀번호 유효성 확인
    @PostMapping("/validate-passwords-match")
    public ResponseEntity<ApiResponse<Void>> validatePassword(@Validated(ValidationSequence.class) @RequestBody PasswordRequest request) {
        passwordService.validatePassword(request);

        return ResponseEntity.ok(new ApiResponse<>("비밀번호가 일치합니다"));
    }

    // 신규회원가입
    @PostMapping("/temporary/register")
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> registerTemporaryUser(@Validated(ValidationSequence.class) @RequestBody SignUpRequest request)  {

        UserTemp userTemp = userService.registerUserTemp(request);
        EmailToken emailToken = emailTokenService.createEmailToken(userTemp);
        userService.sendSignUpMail(userTemp,emailToken);

        ApiResponse<VerifyEmailResponse> verifyEmailResponse = new ApiResponse<>("인증 메일 전송 완료",
                new VerifyEmailResponse(emailToken.getEmailTokenUUID(), userTemp.getTempAccount()));

        return new ResponseEntity<>(verifyEmailResponse, HttpStatus.OK);
    }

    // 신규회원 - 이메일 인증 후 회원가입
    @GetMapping("/email/verify-token")
    public ModelAndView verifySignUpMail (@RequestParam("emailToken_uuid") UUID emailToken_uuid) {

        ModelAndView modelAndView = new ModelAndView();

        try {
            UserTemp userTemp = userService.verifyEmailToken(emailToken_uuid);
            userService.signUp(userTemp);
            modelAndView.setViewName("success");
        } catch (EmailException e) {
            modelAndView.setViewName("failure");
        }
        return modelAndView;
    }

    // 이메일 재인증
    @PostMapping("/email/resend-confirmation")
    public ResponseEntity<ApiResponse<UUID>> resendConfirmEmail(@RequestHeader UUID emailToken_uuid) {

        EmailToken emailToken = emailTokenService.updateCertificationTime(emailToken_uuid);
        userService.sendSignUpMail(emailToken.getUserTemp(),emailToken);

        ApiResponse<UUID> response = new ApiResponse<>("이메일 재인증을 해주세요", emailToken_uuid);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // 로그인하러가기 - 회원가입 최종 확인
    @PostMapping("/finish-signup")
    public ResponseEntity<ApiResponse<Void>> signUpFinish(@RequestBody FinishSignupRequest request) {
        userService.signUpFinish(request.getAccount());
        return ResponseEntity.ok(new ApiResponse<>("회원가입이 정상적으로 완료되어 로그인이 가능합니다."));
    }

    // 기존 동아리원 회원가입
    @PostMapping("/existing/register")
    public ResponseEntity<ApiResponse<Void>> ExistingMemberSignUp(@Validated(ValidationSequence.class) @RequestBody ExistingMemberSignUpRequest request)  {
        // 임시 동아리 회원 생성
        ClubMemberTemp clubMemberTemp = userService.registerClubMemberTemp(request);
        // 입력받은 동아리의 회장들에게 가입신청서 보내기
        userService.sendRequest(request, clubMemberTemp);
        return ResponseEntity.ok(new ApiResponse<>("가입 요청에 성공했습니다"));
    }

    // 아이디 찾기
    @GetMapping ("/find-account/{email}")
    ResponseEntity<ApiResponse<String>> findUserAccount(@PathVariable("email") String email) {

        User findUser= userService.findUser(email);
        userService.sendAccountInfoMail(findUser);

        ApiResponse<String> response = new ApiResponse<>("계정 정보 전송 완료");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 비밀번호 찾기 - 인증 코드 전송
    @PostMapping("/auth/send-code")
    @RateLimite(action = "PW_FOUND_EMAIL")
    ResponseEntity<ApiResponse<UUID>> sendAuthCode (@Valid @RequestBody UserInfoDto request) {

        User user = userService.validateAccountAndEmail(request);
        AuthToken authToken = authTokenService.createOrUpdateAuthToken(user);
        userService.sendAuthCodeMail(user,authToken);

        ApiResponse<UUID> response = new ApiResponse<>("인증코드가 전송 되었습니다",user.getUserUUID());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // 인증 코드 검증
    @PostMapping("/auth/verify-token")
    @RateLimite(action = "VALIDATE_CODE")
    public ApiResponse<String> verifyAuthToken(@RequestHeader UUID uuid,@Valid @RequestBody AuthCodeRequest request) {

        authTokenService.verifyAuthToken(uuid, request);
        authTokenService.deleteAuthToken(uuid);

        return new ApiResponse<>("인증 코드 검증이 완료되었습니다");
    }

    // 비밀번호 재설정
    @PatchMapping("/reset-password")
    public ApiResponse<String> resetUserPw(@RequestHeader UUID uuid, @RequestBody PasswordRequest request) {

        userService.resetPW(uuid,request);

        return new ApiResponse<>("비밀번호가 변경되었습니다.");
    }

    /**
     * User 로그인
     */
    @PostMapping("/login")
    @RateLimite(action = "APP_LOGIN")
    public ResponseEntity<ApiResponse<TokenDto>> userLogin(@RequestBody @Validated(ValidationSequence.class) LogInRequest request, HttpServletResponse response) {
        TokenDto tokenDto = userService.userLogin(request, response);
        return ResponseEntity.ok(new ApiResponse<>("로그인 성공", tokenDto));
    }

    // 회원 탈퇴 요청 및 메일 전송
    @PostMapping("/exit/send-code")
    @RateLimite(action = "WITHDRAWAL_EMAIL")
    public ApiResponse<String> sendWithdrawalCode () {

        // 탈퇴 토큰 생성
        WithdrawalToken token = withdrawalTokenService.createOrUpdateWithdrawalToken();
        // 탈퇴 인증 메일 전송
        userService.sendWithdrawalCodeMail(token);

        return new ApiResponse<>("탈퇴를 위한 인증 메일이 전송 되었습니다");
    }

    // 회원 탈퇴 인증 번호 확인
    @DeleteMapping("/exit")
    @RateLimite(action ="WITHDRAWAL_CODE")
    public ApiResponse<String> cancelMembership(HttpServletRequest request, HttpServletResponse response,@Valid @RequestBody AuthCodeRequest authCodeRequest){

        // 토큰 검증 및 삭제
        UUID uuid = withdrawalTokenService.verifyWithdrawalToken(authCodeRequest);
        withdrawalTokenService.deleteWithdrawalToken(uuid);

        // 회원 탈퇴 진행
        userService.cancelMembership(request,response);
        return new ApiResponse<>("회원 탈퇴가 완료되었습니다.");
    }
}