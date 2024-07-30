package com.USWCicrcleLink.server.user.api;

import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.security.dto.TokenDto;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.CheckPasswordRequest;
import com.USWCicrcleLink.server.user.dto.LogInRequest;
import com.USWCicrcleLink.server.user.dto.SignUpRequest;
import com.USWCicrcleLink.server.user.dto.UpdatePwRequest;
import com.USWCicrcleLink.server.user.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/{uuid}/userpw")
    public ApiResponse<String> updateUserPw(@PathVariable UUID uuid, @RequestBody UpdatePwRequest request) {

        userService.updateNewPW(uuid, request.getUserPw(),request.getNewPw(), request.getConfirmNewPw());

        return new ApiResponse<>("비밀번호가 성공적으로 업데이트 되었습니다.");
    }

    // 임시 회원 등록 및 인증 메일 전송
    @PostMapping("/temp-sign-up")
    public ResponseEntity<ApiResponse> registerTemporaryUser (@Valid @RequestBody SignUpRequest request) throws MessagingException {

        UserTemp userTemp = userService.registerTempUser(request);
        userService.sendEmail(userTemp);
        ApiResponse response = new ApiResponse("인증 메일 전송 완료");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 이메일 인증 확인 후 회원가입
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse>verifyEmail(@RequestParam @Valid  UUID emailTokenId){
        UserTemp userTemp = userService.validateEmailToken(emailTokenId);
        User signUpUser = userService.signUp(userTemp);
        ApiResponse response = new ApiResponse( "회원 가입 완료",signUpUser);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 회원가입 시의 계정 중복 체크
    @GetMapping("/check-account-duplicate")
    public ResponseEntity<ApiResponse> checkAccountDuplicate(@RequestParam @Valid  String account) {
            userService.checkAccountDuplicate(account);
            return ResponseEntity.ok(new ApiResponse("사용 가능한 ID 입니다."));
    }

    // 비밀번호 일치 확인
    @GetMapping("/check-passwords-match")
    public ResponseEntity<ApiResponse> comparePasswords(@Valid @RequestBody CheckPasswordRequest request ){
        userService.comparePasswords(request);
        return ResponseEntity.ok(new ApiResponse<>("비밀번호가 일치합니다"));
    }

    // 로그인
    @PostMapping("/log-in")
    public ResponseEntity<ApiResponse<TokenDto>> logIn(@Valid @RequestBody LogInRequest request) throws Exception {
        TokenDto tokenDto = userService.logIn(request);
        ApiResponse<TokenDto> response = new ApiResponse<>("로그인 성공", tokenDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 아이디 찾기
    @GetMapping ("/find-user-account")
    ResponseEntity<ApiResponse> findUserAccount(@Valid @RequestParam String email) throws MessagingException {
        User findUser= userService.findUser(email);
        userService.sendEmailInfo(findUser);
        ApiResponse response = new ApiResponse( "Account 정보 전송 완료",findUser.getUserAccount());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }


}