package com.USWCicrcleLink.server.club.leader.service;

import com.USWCicrcleLink.server.club.application.domain.Aplict;
import com.USWCicrcleLink.server.club.application.domain.AplictStatus;
import com.USWCicrcleLink.server.club.leader.dto.FcmTokenRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface FcmService {
    // 메시지 구성, 토큰 받고 메시지 처리
    int sendMessageTo(Aplict aplict, AplictStatus aplictResult) throws IOException;

    // fcm token 갱신
    public void refreshFcmToken(FcmTokenRequest fcmTokenRequest);

}


