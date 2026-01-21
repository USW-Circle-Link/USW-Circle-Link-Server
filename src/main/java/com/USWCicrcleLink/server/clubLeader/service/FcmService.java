package com.USWCicrcleLink.server.clubLeader.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.clubLeader.dto.FcmTokenRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface FcmService {
    // 메시지 구성, 토큰 받고 메시지 처리
    int sendMessageTo(Aplict aplict, AplictStatus aplictResult) throws IOException;

    // fcm token 갱신
    public void refreshFcmToken(FcmTokenRequest fcmTokenRequest);

}
