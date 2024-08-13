package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.service.EmailTokenService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.security.dto.TokenDto;
import com.USWCicrcleLink.server.user.domain.AuthToken;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.*;
import com.USWCicrcleLink.server.user.service.AuthTokenService;
import com.USWCicrcleLink.server.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    // 임시 회원 등록 및 인증 메일 전송
    @PostMapping("/temporary")
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> registerTemporaryUser(@Valid @RequestBody SignUpRequest request) throws MessagingException {

        UserTemp userTemp = userService.registerUserTemp(request);
        EmailToken emailToken = emailTokenService.createEmailToken(userTemp);
        userService.sendSignUpMail(userTemp,emailToken);

        ApiResponse<VerifyEmailResponse> verifyEmailResponse = new ApiResponse<>("인증 메일 전송 완료",
                new VerifyEmailResponse(emailToken.getUuid(), userTemp.getTempAccount()));

        return new ResponseEntity<>(verifyEmailResponse, HttpStatus.OK);
    }

    // 이메일 인증 확인 후 자동 회원가입
    @GetMapping("/email/verify-token")
    public ResponseEntity<ApiResponse<Boolean>> verifySignUpMail (@RequestParam UUID emailToken_uuid) {

        UserTemp userTemp = userService.verifyEmailToken(emailToken_uuid);
        userService.signUp(userTemp);
        ApiResponse<Boolean> response= new ApiResponse<>("이메일 인증 완료, 회원가입 완료 버튼을 눌러주세요",true);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 회원 가입 완료 처리
    @PostMapping("/finish-signup")
    public ResponseEntity<ApiResponse<String>> signUpFinish(@RequestBody VerifyEmailRequest request) {
        ApiResponse<String> apiResponse = new ApiResponse<>(userService.signUpFinish(request.getAccount()), "회원가입 완료");
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
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

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenDto>> logIn(@RequestBody @Valid LogInRequest request, HttpServletResponse response) {
        TokenDto tokenDto = userService.logIn(request, response);
        ApiResponse<TokenDto> apiResponse = new ApiResponse<>("로그인 성공", tokenDto);
        return ResponseEntity.ok(apiResponse);
    }

    // 아이디 찾기
    @GetMapping ("/find-account/{email}")
    ResponseEntity<ApiResponse<String>> findUserAccount(@PathVariable String email) throws MessagingException {

        User findUser= userService.findUser(email);
        userService.sendAccountInfoMail(findUser);

        ApiResponse<String> response = new ApiResponse<>("계정 정보 전송 완료", findUser.getUserAccount());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 인증 코드 전송
    @PostMapping("/auth/send-code")
    ResponseEntity<ApiResponse<UUID>> sendAuthCode (@Valid @RequestBody UserInfoDto request) throws MessagingException {

        User user = userService.validateAccountAndEmail(request);
        AuthToken authToken = authTokenService.createAuthToken(user);
        userService.sendAuthCodeMail(user,authToken);

        ApiResponse<UUID> response = new ApiResponse<>("인증코드가 전송 되었습니다",user.getUserUUID());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // 인증 코드 검증
    @PostMapping("/auth/verify-token")
    public ResponseEntity<ApiResponse<String>> verifyAuthToken(@RequestHeader UUID uuid, @RequestBody UserInfoDto request) {

        authTokenService.verifyAuthToken(uuid, request);
        authTokenService.deleteAuthToken(uuid);

        ApiResponse<String> response = new ApiResponse<>("인증 코드 검증이 완료되었습니다",request.getUserAccount());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // 비밀번호 재설정
    @PatchMapping("/reset-password")
    public ApiResponse<String> resetUserPw(@RequestHeader UUID uuid, @RequestBody PasswordRequest request) {

        User user = userService.findByUuid(uuid);
        userService.resetPW(user,request);

        return new ApiResponse<>("비밀번호가 변경되었습니다.");
    }

    // 이메일 재인증
    @PostMapping("/email/resend-confirmation")
    public ResponseEntity<ApiResponse<UUID>> resendConfirmEmail(@RequestHeader UUID emailToken_uuid) throws MessagingException {

        EmailToken emailToken = emailTokenService.updateCertificationTime(emailToken_uuid);
        userService.sendSignUpMail(emailToken.getUserTemp(),emailToken);

        ApiResponse<UUID> response = new ApiResponse<>("이메일 재인증을 해주세요", emailToken_uuid);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

}