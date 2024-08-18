package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.service.EmailTokenService;
import com.USWCicrcleLink.server.global.exception.errortype.EmailException;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.security.dto.TokenDto;
import com.USWCicrcleLink.server.global.validation.ValidationSequence;
import com.USWCicrcleLink.server.user.domain.AuthToken;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.*;
import com.USWCicrcleLink.server.user.service.AuthTokenService;
import com.USWCicrcleLink.server.user.service.UserService;

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

    @PatchMapping("/userpw")
    public ApiResponse<String> updateUserPw(@RequestBody UpdatePwRequest request) {

        userService.updateNewPW(request);
        return new ApiResponse<>("비밀번호가 성공적으로 업데이트 되었습니다.");
    }

    // 회원가입 시의 계정 중복 체크
    @GetMapping("/verify-duplicate/{account}")
    public ResponseEntity<ApiResponse<String>> verifyAccountDuplicate(@PathVariable String account) {

        userService.verifyAccountDuplicate(account);

        ApiResponse<String> response = new ApiResponse<>("사용 가능한 ID 입니다.", account);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 비밀번호 일치 확인
    @PostMapping("/validate-passwords-match")
    public ResponseEntity<ApiResponse<Void>> validatePasswordsMatch(@Valid @RequestBody PasswordRequest request) {

        userService.validatePasswordsMatch(request);

        return ResponseEntity.ok(new ApiResponse<>("비밀번호가 일치합니다"));
    }

    // 임시 회원 등록 및 인증 메일 전송
    @PostMapping("/temporary")
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> registerTemporaryUser(@Validated(ValidationSequence.class) @RequestBody SignUpRequest request)  {

        UserTemp userTemp = userService.registerUserTemp(request);
        EmailToken emailToken = emailTokenService.createEmailToken(userTemp);
        userService.sendSignUpMail(userTemp,emailToken);

        ApiResponse<VerifyEmailResponse> verifyEmailResponse = new ApiResponse<>("인증 메일 전송 완료",
                new VerifyEmailResponse(emailToken.getEmailTokenUUID(), userTemp.getTempAccount()));

        return new ResponseEntity<>(verifyEmailResponse, HttpStatus.OK);
    }

    // 이메일 인증 확인 후 자동 회원가입
     @GetMapping("/email/verify-token")
    public ModelAndView verifySignUpMail (@RequestParam UUID emailToken_uuid) {

        ModelAndView modelAndView = new ModelAndView();

        try {
            UserTemp userTemp = userService.verifyEmailToken(emailToken_uuid);
            userService.signUp(userTemp);
            modelAndView.setViewName("email_verification_success");
            modelAndView.addObject("message", "이메일 인증이 성공했습니다. 앱으로 돌아가 회원가입 완료 버튼을 눌러주세요");
        } catch (EmailException e) {
            modelAndView.setViewName("email_verification_failure");
            modelAndView.addObject("message", "이메일 인증이 실패 했습니다. 이메일을 재전송 해주세요");
        }
        return modelAndView;
    }

    // 이메일 재인증
    @PostMapping("/email/resend-confirmation")
    public ResponseEntity<ApiResponse<UUID>> resendConfirmEmail(@RequestHeader UUID emailToken_uuid)  {

        EmailToken emailToken = emailTokenService.updateCertificationTime(emailToken_uuid);
        userService.sendSignUpMail(emailToken.getUserTemp(),emailToken);

        ApiResponse<UUID> response = new ApiResponse<>("이메일 재인증을 해주세요", emailToken_uuid);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // 회원 가입 완료 처리
    @PostMapping("/finish-signup")
    public ResponseEntity<ApiResponse<String>> signUpFinish(@RequestBody FinishSignupRequest request) {
        ApiResponse<String> apiResponse = new ApiResponse<>(userService.signUpFinish(request.getAccount()), "회원가입 완료");
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenDto>> logIn(@RequestBody @Valid LogInRequest request, HttpServletResponse response) {
        TokenDto tokenDto = userService.logIn(request, response);
        ApiResponse<TokenDto> apiResponse = new ApiResponse<>("로그인 성공", tokenDto);
        return ResponseEntity.ok(apiResponse);
    }

    // 아이디 찾기
    @GetMapping ("/find-account/{email}")
    ResponseEntity<ApiResponse<String>> findUserAccount(@PathVariable String email) {

        User findUser= userService.findUser(email);
        userService.sendAccountInfoMail(findUser);

        ApiResponse<String> response = new ApiResponse<>("계정 정보 전송 완료", findUser.getUserAccount());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 인증 코드 전송
    @PostMapping("/auth/send-code")
    ResponseEntity<ApiResponse<UUID>> sendAuthCode (@Valid @RequestBody UserInfoDto request) {

        User user = userService.validateAccountAndEmail(request);
        AuthToken authToken = authTokenService.createOrUpdateAuthToken(user);
        userService.sendAuthCodeMail(user,authToken);

        ApiResponse<UUID> response = new ApiResponse<>("인증코드가 전송 되었습니다",user.getUserUUID());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // 인증 코드 검증
    @PostMapping("/auth/verify-token")
    public ResponseEntity<ApiResponse<UUID>> verifyAuthToken(@RequestHeader UUID uuid,@Valid @RequestBody AuthCodeRequest request) {

        authTokenService.verifyAuthToken(uuid, request);
        authTokenService.deleteAuthToken(uuid);

        ApiResponse<UUID> response = new ApiResponse<>("인증 코드 검증이 완료되었습니다",uuid);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // 비밀번호 재설정
    @PatchMapping("/reset-password")
    public ApiResponse<String> resetUserPw(@RequestHeader UUID uuid, @RequestBody PasswordRequest request) {

        userService.resetPW(uuid,request);

        return new ApiResponse<>("비밀번호가 변경되었습니다.");
    }
}