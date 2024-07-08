package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @PostConstruct
    @Transactional
    public void init() {
        // Create User
        User user = User.builder()
                .userUUID("0000")
                .userAccount("admin")
                .userPw("1234")
                .email("admin")
                .userCreatedAt(LocalDateTime.now())
                .userUpdatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

        public void updatePW(String uuid, String newPassword, String confirmNewPassword){

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

}
