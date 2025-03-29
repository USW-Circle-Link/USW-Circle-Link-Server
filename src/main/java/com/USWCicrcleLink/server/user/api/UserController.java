package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.global.bucket4j.RateLimite;
import com.USWCicrcleLink.server.global.exception.errortype.EmailTokenException;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.security.jwt.dto.TokenDto;
import com.USWCicrcleLink.server.global.validation.ValidationSequence;
import com.USWCicrcleLink.server.user.domain.AuthToken;
import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberTemp;
import com.USWCicrcleLink.server.user.domain.SignupToken;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.WithdrawalToken;
import com.USWCicrcleLink.server.user.dto.*;
import com.USWCicrcleLink.server.user.service.AuthTokenService;
import com.USWCicrcleLink.server.user.service.SignupTokenService;
import com.USWCicrcleLink.server.user.service.UserService;
import com.USWCicrcleLink.server.user.service.WithdrawalTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final WithdrawalTokenService withdrawalTokenService;
    private final SignupTokenService signupTokenService;
    @PatchMapping("/userpw")
    public ApiResponse<String> updateUserPw(@Validated(ValidationSequence.class) @RequestBody UpdatePwRequest request) {
        userService.updateNewPW(request);
        return new ApiResponse<>("비밀번호가 성공적으로 업데이트 되었습니다.");
    }

    // 아이디 중복 체크
    @PostMapping("/account/verify-duplicate")
    public ResponseEntity<ApiResponse<String>> verifyAccountDuplicate(@Validated(ValidationSequence.class) @RequestBody AccountDto request) {

        userService.verifyAccountDuplicate(request.getAccount());
        ApiResponse<String> response = new ApiResponse<>("사용 가능한 ID 입니다.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    // 기존회원 가입시 이메일 중복 확인
    @PostMapping("/check/email/duplicate")
    public ResponseEntity<ApiResponse<String>> verifyEmailDuplicate(@Validated(ValidationSequence.class) @RequestBody EmailDTO reqeust) {
        userService.verifyEmailDuplicate(reqeust.getEmail());
        ApiResponse<String> response = new ApiResponse<>("이메일 중복 확인에 성공하였습니다.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 기존 동아리원 회원가입
    @PostMapping("/existing/register")
    public ResponseEntity<ApiResponse<Void>> ExistingMemberSignUp(@RequestBody @Validated(ValidationSequence.class) ExistingMemberSignUpRequest request)  {
        // 기존 회원 가입을 위한 조건 검사
        userService.checkExistingSignupCondition(request);
        // 임시 동아리 회원 생성
        ClubMemberTemp clubMemberTemp = userService.registerClubMemberTemp(request);
        // 입력받은 동아리의 회장들에게 가입신청서 보내기
        userService.sendRequest(request, clubMemberTemp);
        return ResponseEntity.ok(new ApiResponse<>("가입 요청에 성공했습니다"));
    }

    // 신규회원가입 요청 - 인증 메일 전송
    @PostMapping("/temporary/register")
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> registerTemporaryUser(@Validated(ValidationSequence.class) @RequestBody EmailDTO request)  {

        // 이메일 토큰이 존재하는지 확인
        EmailToken emailToken = userService.checkEmailTokenIsExist(request.getEmail());

        // 신규회원가입을 위한 이메일 전송
        userService.sendSignUpMail(emailToken);

        ApiResponse<VerifyEmailResponse> verifyEmailResponse = new ApiResponse<>("인증 메일 전송 완료",
                new VerifyEmailResponse(emailToken.getEmailTokenUUID(), emailToken.getEmail()));

        return new ResponseEntity<>(verifyEmailResponse, HttpStatus.OK);
    }

    // 이메일 인증 여부 검증하기
    @GetMapping("/email/verify-token")
    public ModelAndView verifySignUpMail (@RequestParam("emailTokenUUID") UUID emailTokenUUID) {

        ModelAndView modelAndView = new ModelAndView();

        try {
            // 제한시간 안에 인증에 성공 -> SignupToken 생성
            userService.verifyEmailToken(emailTokenUUID);
            modelAndView.setViewName("success");
        } catch (EmailTokenException e) {
            // 이메일 만료 시간이 지난경우
            modelAndView.setViewName("expired");
        } catch (Exception e){
            // 예상치 못한 다른 예외
            modelAndView.setViewName("failure");
        }
        return modelAndView;
    }

    // 인증 확인 버튼
    @GetMapping("/email/verification")
    public ResponseEntity<ApiResponse<SignUpuuidResponse>> emailVerification(@Validated(ValidationSequence.class) @RequestBody EmailDTO request){

        // signupToken 조회
        SignupToken singUpToken = signupTokenService.getSignUpTokenByEmail(request.getEmail());
        ApiResponse<SignUpuuidResponse> response = new ApiResponse<>("인증 확인 버튼 클릭 후, 이메일 인증 완료",
                new SignUpuuidResponse(singUpToken.getEmailTokenUUID(),singUpToken.getSignupUUID()));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 신규 회원 가입 정보 등록하기 (다음 버튼 누른 후)
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Validated(ValidationSequence.class) @RequestBody  SignUpRequest request,@RequestHeader("emailTokenUUID") UUID emailTokenUUID,@RequestHeader("signupUUID") UUID requestSignupUUID) {

        // 인증을 받은 사용자가 맞는지 검증하기
        SignupToken signupToken =signupTokenService.verifyUser(emailTokenUUID, requestSignupUUID);

        // 신규 회원가입을 위한 조건 검사
        userService.checkNewSignupCondition(request);

        // 회원가입 진행
        userService.signUpUser(request,signupToken.getEmail());

        // signupToken 삭제
        signupTokenService.deleteSignUpTokenFromRedis(signupToken);

        return ResponseEntity.ok(new ApiResponse<>("회원가입이 정상적으로 완료되어 로그인이 가능합니다."));
    }


    // 아이디 찾기
    @PostMapping("/find-account")
    ResponseEntity<ApiResponse<String>> findUserAccount(@Validated(ValidationSequence.class) @RequestBody EmailDTO request) {

        User findUser= userService.findUser(request.getEmail());
        userService.sendAccountInfoMail(findUser);

        ApiResponse<String> response = new ApiResponse<>("계정 정보 전송 완료");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 비밀번호 찾기 - 인증 코드 전송
    @PostMapping("/auth/send-code")
    @RateLimite(action = "PW_FOUND_EMAIL")
    ResponseEntity<ApiResponse<UUID>> sendAuthCode (@Validated(ValidationSequence.class) @RequestBody UserInfoDto request) {

        User user = userService.validateAccountAndEmail(request);
        AuthToken authToken = authTokenService.createOrUpdateAuthToken(user);
        userService.sendAuthCodeMail(user,authToken);

        ApiResponse<UUID> response = new ApiResponse<>("인증코드가 전송 되었습니다",user.getUserUUID());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // 비밀번호 찾기- 인증 코드 검증
    @PostMapping("/auth/verify-token")
    @RateLimite(action = "VALIDATE_CODE")
    public ApiResponse<String> verifyAuthToken(@RequestHeader("uuid") UUID uuid,@Validated(ValidationSequence.class) @RequestBody AuthCodeRequest request) {

        authTokenService.verifyAuthToken(uuid, request);
        authTokenService.deleteAuthToken(uuid);

        return new ApiResponse<>("인증 코드 검증이 완료되었습니다");
    }

    // 비밀번호 찾기 - 비밀번호 재설정
    @PatchMapping("/reset-password")
    public ApiResponse<String> resetUserPw(@RequestHeader("uuid") UUID uuid, @Validated(ValidationSequence.class) @RequestBody PasswordRequest request) {

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

    // 회원 탈퇴 - 이메일 보여주기
    @GetMapping("/exit/email")
    public ApiResponse<String> getWithdrawalEmail() {
        User user = userService.getUserByAuth();
        return new ApiResponse<>(user.getEmail());
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
    public ApiResponse<String> cancelMembership(HttpServletRequest request, HttpServletResponse response,@Validated(ValidationSequence.class) @RequestBody AuthCodeRequest authCodeRequest){

        // 토큰 검증 및 삭제
        UUID userUUID = withdrawalTokenService.verifyWithdrawalToken(authCodeRequest);
        withdrawalTokenService.deleteWithdrawalToken(userUUID);

        // 인증 토큰 존재시 인증 토큰도 삭제
        authTokenService.delete(userUUID);

        // 회원 탈퇴 진행
        userService.cancelMembership(request,response);

        return new ApiResponse<>("회원 탈퇴가 완료되었습니다.");
    }
}