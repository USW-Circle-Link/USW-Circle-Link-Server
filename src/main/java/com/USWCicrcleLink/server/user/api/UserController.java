package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.service.EmailTokenService;
import com.USWCicrcleLink.server.global.bucket4j.RateLimite;
import com.USWCicrcleLink.server.global.exception.errortype.EmailTokenException;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.security.jwt.dto.TokenDto;
import com.USWCicrcleLink.server.global.validation.ValidationSequence;
import com.USWCicrcleLink.server.user.domain.AuthToken;
import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberTemp;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.WithdrawalToken;
import com.USWCicrcleLink.server.user.dto.*;
import com.USWCicrcleLink.server.user.service.AuthTokenService;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "일반 사용자 관련 API")
public class UserController {

    private final UserService userService;
    private final AuthTokenService authTokenService;
    private final WithdrawalTokenService withdrawalTokenService;
    private final EmailTokenService emailTokenService;

    @Operation(
            summary = "비밀번호 변경",
            description = "현재 비밀번호 확인 후 새 비밀번호로 변경합니다. JWT 인증 필요."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 변경 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 유효성 오류 또는 비밀번호 정책 위반"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/userpw")
    public ApiResponse<String> updateUserPw(@Validated(ValidationSequence.class) @RequestBody UpdatePwRequest request) {
        userService.updateNewPW(request);
        return new ApiResponse<>("비밀번호가 성공적으로 업데이트 되었습니다.");
    }

    // 기존회원 가입시 이메일 중복 확인
    @Operation(summary = "이메일 중복 체크", description = "입력 이메일이 사용자 또는 기존 회원가입 대기자에 중복되는지 확인합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "중복 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복됨")
    })
    @PostMapping("/check/{email}/duplicate")
    public ResponseEntity<ApiResponse<String>> verifyEmailDuplicate(@PathVariable("email") String email) {
        userService.verifyEmailDuplicate(email);
        ApiResponse<String> response = new ApiResponse<>("이메일 중복 확인에 성공하였습니다.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 아이디 중복 체크
    @Operation(summary = "아이디 중복 체크", description = "계정(account)의 사용 가능 여부를 확인합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용 가능"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복됨")
    })
    @GetMapping("/verify-duplicate/{account}")
    public ResponseEntity<ApiResponse<String>> verifyAccountDuplicate(@PathVariable("account") String account) {

        userService.verifyAccountDuplicate(account);

        ApiResponse<String> response = new ApiResponse<>("사용 가능한 ID 입니다.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 신규회원가입 요청 - 인증 메일 전송
    @Operation(
            summary = "신규회원가입: 인증 메일 전송",
            description = "이메일 중복 검사 후 회원가입 인증 메일을 전송합니다. 레이트 리미트 적용.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "이메일 예시", value = "{\n  \"email\": \"user@example.com\"\n}"))
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 메일 전송 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이메일 중복"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "과도한 요청")
    })
    @PostMapping("/temporary/register")
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> registerTemporaryUser(@Validated @RequestBody EmailDTO request)  {

        // 이메일 중복 검증
        EmailToken emailToken = userService.checkEmailDuplication(request.getEmail());

        // 신규회원가입을 위한 이메일 전송
        userService.sendSignUpMail(emailToken);

        ApiResponse<VerifyEmailResponse> verifyEmailResponse = new ApiResponse<>("인증 메일 전송 완료",
                new VerifyEmailResponse(emailToken.getEmailTokenUUID(), emailToken.getEmail()));

        return new ResponseEntity<>(verifyEmailResponse, HttpStatus.OK);
    }

    // 이메일 인증 여부 검증하기
    @Operation(summary = "신규회원가입: 이메일 인증 링크 검증", description = "메일의 인증 링크를 통해 토큰을 검증하고 결과 페이지를 렌더링합니다.")
    @GetMapping("/email/verify-token")
    public ModelAndView verifySignUpMail (@RequestParam("emailTokenUUID") UUID emailTokenUUID) {

        ModelAndView modelAndView = new ModelAndView();

        try {
            // 제한시간 안에 인증에 성공
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
    @Operation(summary = "신규회원가입: 인증 확인 버튼", description = "사용자가 인증 확인 버튼을 눌러 인증 완료 상태를 확인합니다.")
    @PostMapping("/email/verification")
    public ResponseEntity<ApiResponse<SignUpuuidResponse>> emailVerification(@Validated @RequestBody EmailDTO request){

        EmailToken emailToken = emailTokenService.checkEmailIsVerified(request.getEmail());

        ApiResponse<SignUpuuidResponse> response = new ApiResponse<>("인증 확인 버튼 클릭 후, 이메일 인증 완료",
                new SignUpuuidResponse(emailToken.getEmailTokenUUID(),emailToken.getSignupUUID()));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 회원 가입 정보 등록하기 -- 다음 버튼 누른 후
    @Operation(
            summary = "신규회원가입: 회원 정보 등록",
            description = "인증 완료된 사용자가 회원 정보를 제출하여 가입을 완료합니다.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "emailTokenUUID", required = true, description = "인증 토큰 UUID"),
                    @Parameter(in = ParameterIn.HEADER, name = "signupUUID", required = true, description = "회원가입 요청 UUID")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json"))
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복 또는 정책 위반")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Validated(ValidationSequence.class) @RequestBody  SignUpRequest request,@RequestHeader("emailTokenUUID") UUID emailTokenUUID,@RequestHeader("signupUUID") UUID signupUUID) {

        // 인증을 받은 사용자가 맞는지 검증하기
        String email = userService.isEmailVerified(emailTokenUUID, signupUUID);

        // 신규 회원가입을 위한 조건 검사
        userService.checkNewSignupCondition(request);

        // 회원가입 진행
        userService.signUpUser(request,email);
        return ResponseEntity.ok(new ApiResponse<>("회원가입이 정상적으로 완료되어 로그인이 가능합니다."));
    }

    // 기존 동아리원 회원가입
    @Operation(
            summary = "기존 동아리원 회원가입",
            description = "기존 동아리원 정보로 임시 회원을 등록하고 각 동아리장에게 가입 신청을 전송합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json"))
    )
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

    // 아이디 찾기
    @Operation(summary = "아이디 찾기", description = "이메일로 계정을 조회하고 아이디 정보를 메일로 전송합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "전송 완료"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @GetMapping ("/find-account/{email}")
    ResponseEntity<ApiResponse<String>> findUserAccount(@PathVariable("email") String email) {

        User findUser= userService.findUser(email);
        userService.sendAccountInfoMail(findUser);

        ApiResponse<String> response = new ApiResponse<>("계정 정보 전송 완료");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 비밀번호 찾기 - 인증 코드 전송
    @Operation(
            summary = "비밀번호 찾기: 인증 코드 전송",
            description = "아이디/이메일 검증 후 비밀번호 재설정을 위한 인증 코드를 메일로 전송합니다. 레이트 리미트 적용.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "계정/이메일 예시", value = "{\n  \"userAccount\": \"user1\",\n  \"email\": \"user@example.com\"\n}")))
    )
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
    @Operation(
            summary = "비밀번호 찾기: 인증 코드 검증",
            description = "인증 코드를 검증하고 사용된 토큰을 제거합니다.",
            parameters = {@Parameter(in = ParameterIn.HEADER, name = "uuid", required = true, description = "사용자 UUID")},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json"))
    )
    @PostMapping("/auth/verify-token")
    @RateLimite(action = "VALIDATE_CODE")
    public ApiResponse<String> verifyAuthToken(@RequestHeader("uuid") UUID uuid,@Valid @RequestBody AuthCodeRequest request) {

        authTokenService.verifyAuthToken(uuid, request);
        authTokenService.deleteAuthToken(uuid);

        return new ApiResponse<>("인증 코드 검증이 완료되었습니다");
    }

    // 비밀번호 재설정
    @Operation(
            summary = "비밀번호 재설정",
            description = "인증 완료 후 새 비밀번호로 재설정합니다.",
            parameters = {@Parameter(in = ParameterIn.HEADER, name = "uuid", required = true, description = "사용자 UUID")},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json"))
    )
    @PatchMapping("/reset-password")
    public ApiResponse<String> resetUserPw(@RequestHeader("uuid") UUID uuid, @RequestBody PasswordRequest request) {

        userService.resetPW(uuid,request);

        return new ApiResponse<>("비밀번호가 변경되었습니다.");
    }

    /**
     * User 로그인
     */
    @Operation(
            summary = "로그인",
            description = "아이디와 비밀번호를 검증하여 Access/Refresh 토큰을 발급합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "로그인 예시", value = "{\n  \"account\": \"user1\",\n  \"password\": \"P@ssw0rd!\",\n  \"fcmToken\": \"optional-token\"\n}")))
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = TokenDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "과도한 요청")
    })
    @PostMapping("/login")
    @RateLimite(action = "APP_LOGIN")
    public ResponseEntity<ApiResponse<TokenDto>> userLogin(@RequestBody @Validated(ValidationSequence.class) LogInRequest request, HttpServletResponse response) {

        TokenDto tokenDto = userService.userLogin(request, response);
        return ResponseEntity.ok(new ApiResponse<>("로그인 성공", tokenDto));
    }

    // 회원 탈퇴 요청 및 메일 전송
    @Operation(summary = "회원 탈퇴: 인증 메일 전송", description = "탈퇴 인증 코드를 메일로 전송합니다. JWT 인증 필요.")
    @SecurityRequirement(name = "bearerAuth")
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
    @Operation(
            summary = "회원 탈퇴: 인증 코드 검증 및 탈퇴",
            description = "탈퇴 인증 코드를 검증 후 회원 정보를 삭제하고 로그아웃합니다. JWT 인증 필요.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = "application/json"))
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/exit")
    @RateLimite(action ="WITHDRAWAL_CODE")
    public ApiResponse<String> cancelMembership(HttpServletRequest request, HttpServletResponse response,@Valid @RequestBody AuthCodeRequest authCodeRequest){

        // 토큰 검증 및 삭제
        UUID uuid = withdrawalTokenService.verifyWithdrawalToken(authCodeRequest);
        withdrawalTokenService.deleteWithdrawalToken(uuid);

        // 인증 토큰 존재시 인증 토큰도 삭제
        authTokenService.delete(uuid);

        // 회원 탈퇴 진행
        userService.cancelMembership(request,response);

        return new ApiResponse<>("회원 탈퇴가 완료되었습니다.");
    }
}