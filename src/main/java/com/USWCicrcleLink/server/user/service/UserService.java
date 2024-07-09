package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.email.domain.EmailToken;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.dto.SignUpRequest;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import com.USWCicrcleLink.server.user.repository.UserTempRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserTempRepository userTempRepository;


    // 임시 회원 가입
    public UserTemp signUpUserTemp(SignUpRequest request){

        UserTemp userTemp = request.toEntity();

        userTempRepository.save(userTemp);

       return userTempRepository.findByTempEmail(userTemp.getTempEmail());

    }

}
