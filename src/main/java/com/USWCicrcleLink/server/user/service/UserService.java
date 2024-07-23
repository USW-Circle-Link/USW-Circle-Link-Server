package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.email.service.EmailService;

import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;

import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.SignUpRequest;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import com.USWCicrcleLink.server.user.repository.UserTempRepository;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;
    private final UserTempRepository userTempRepository;
    private final EmailService emailService;
    private final ProfileRepository profileRepository;

    public void updatePW(UUID uuid, String newPassword, String confirmNewPassword) {

        User user = userRepository.findByUserUUID(uuid);

        if (user == null) {
            throw new IllegalArgumentException("해당 UUID를 가진 사용자를 찾을 수 없습니다: " + uuid);
        }
        if (!confirmNewPassword.equals(user.getUserPw())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }

        user.setUserPw(newPassword);
        userRepository.save(user);
    }


    public UserTemp registerTempUser(SignUpRequest request) {

        // 임시 회원 테이블 이메일 중복 검증
        if (isTemporaryUserDuplicate(request.getEmail())) {
            Optional<UserTemp> userTemp = userTempRepository.findByTempEmail(request.getEmail());
            emailService.deleteTempUserAndToken(userTemp.get());
        }
        // 회원 테이블 이메일 중복 검증
        if (isUserDuplicate(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 회원입니다");
        }

        return userTempRepository.save(request.toEntity());
    }
    
    public boolean isTemporaryUserDuplicate(String email) {
        return userTempRepository.existsByTempEmail(email);
    }

    public boolean isUserDuplicate(String email){
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void sendEmail(UserTemp userTemp) throws MessagingException {
        emailService.createEmailToken(userTemp);
        emailService.sendEmail(emailService.createAuthLink(userTemp));
    }

    public UserTemp validateEmailToken(UUID emailTokenId) {

        // 토큰 검증
        emailService.validateToken(emailTokenId);
        EmailToken token = emailService.getTokenBy(emailTokenId);
        return token.getUserTemp();
    }

    // 회원가입
    @Transactional
    public User signUp(UserTemp userTemp) {

        //User 객체 생성 및 저장
        User user = User.builder()
                .userUUID(UUID.randomUUID())
                .userAccount(userTemp.getTempAccount())
                .userPw(userTemp.getTempPw())
                .email(userTemp.getTempEmail())
                .userCreatedAt(LocalDateTime.now())
                .userUpdatedAt(LocalDateTime.now())
                .build();

        //Profile 객체 생성 및 저장
        Profile profile = Profile.builder()
                .user(user)
                .userName(userTemp.getTempName())
                .studentNumber(userTemp.getTempStudentNumber())
                .userHp(userTemp.getTempHp())
                .major(userTemp.getTempMajor())
                .profileCreatedAt(LocalDateTime.now())
                .profileUpdatedAt(LocalDateTime.now())
                .build();


        // 회원 가입
        userRepository.save(user);
        profileRepository.save(profile);
        // 임시 회원 정보 삭제
        emailService.deleteTempUserAndToken(userTemp);
        return user;
    }

    public void checkAccountDuplicate(String account) {
        if (userRepository.existsByUserAccount(account)) {
            throw new IllegalStateException("중복된 ID 입니다. 새로운 ID를 입력해주세요");
        }
    }
}