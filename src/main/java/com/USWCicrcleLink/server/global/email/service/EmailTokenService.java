package com.USWCicrcleLink.server.global.email.service;

import com.USWCicrcleLink.server.global.email.domain.EmailToken;
import com.USWCicrcleLink.server.global.email.repository.EmailTokenRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.EmailException;
import com.USWCicrcleLink.server.global.exception.errortype.EmailTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTokenService {

    private  final EmailTokenRepository emailTokenRepository;

    // 토큰 생성
    @Transactional
    public EmailToken createEmailToken(String email) {
        try{
            log.debug("이메일 토큰 생성 완료 email= {}", email);
            EmailToken emailToken = EmailToken.createEmailToken(email);
            return emailTokenRepository.save(emailToken);
        } catch (Exception e){
            log.error("이메일 토큰 생성시 오류 발생");
            throw new EmailException(ExceptionType.EMAIL_TOKEN_CREATION_FALILED);
        }
    }

    // 유효한 토큰 검증
    public EmailToken verifyEmailToken (UUID emailTokenUUID) {

        log.debug("emailToken_uuid에 해당하는 이메일 토큰이 있는지 확인");
        EmailToken emailToken= getEmailTokenByEmailTokenUUID(emailTokenUUID);


        log.debug("토큰 만료시간 검증 시작");
        if(emailToken.isExpired()){ // 이메일 토큰이 만료된 경우
            throw new EmailTokenException(ExceptionType.EMAIL_TOKEN_IS_EXPIRED);
        }

        return emailToken; // 인증 완료된 이메일 토큰
    }

    // 임시 회원 정보 삭제
    @Transactional
    public void deleteEmailToken(String email){
        Optional<EmailToken> emailToken= emailTokenRepository.findByEmail(email);
        if(emailToken.isPresent()){
            emailTokenRepository.delete(emailToken.get());
        }else{
            throw new EmailException(ExceptionType.EMAIL_TOKEN_NOT_FOUND);
        }
    }

    // emailTokenUUID로 이메일 토큰 조회
    @Transactional(readOnly = true)
    public EmailToken getEmailTokenByEmailTokenUUID(UUID emailTokenUUID){

        if(emailTokenUUID==null){
            log.error("getEmailTokenByEmailTokenUUID 메서드에서 emailTokenUUID가 널값임");
        }
        return emailTokenRepository.findByEmailTokenUUID(emailTokenUUID)
                .orElseThrow(() -> new EmailException(ExceptionType.EMAIL_TOKEN_NOT_FOUND));
    }

    // email로 이메일 토큰 조회
    @Transactional(readOnly = true)
    public EmailToken getEmailTokenByEmail(String email){
        if(email==null){
            log.error("getEmailTokenByEmail 메서드에서 email이 널값임");
        }
        return emailTokenRepository.findByEmail(email)
                    .orElseThrow(() -> new EmailException(ExceptionType.EMAIL_TOKEN_NOT_FOUND));
    }

    // 이메일 인증 토큰 업데이트
   public EmailToken updateCertificationTime (EmailToken emailToken) {
        log.debug("이메일 만료시간 업데이트 메서드 시작");

       // 이메일 토큰의 만료시간 갱신
       try{
           emailToken.extendExpirationTime();
           emailTokenRepository.save(emailToken);
       } catch (Exception e){
           log.error("이메일 토큰의 만료시간 업데이트 후, 저장하는 과정에서 오류 발생");
           throw new EmailException(ExceptionType.EMAIL_TOKEN_STATUS_UPATE_FALIED);
       }

       log.debug("이메일 토큰의 만료시간 갱신 완료");

        return emailToken;
    }

    // 이메일의 인증 여부를 확인
    public EmailToken checkEmailIsVerified(String email) {
        // 이메일에 해당하는 이메일 토큰 조회
        EmailToken emailToken = getEmailTokenByEmail(email);
        // 해당 이메일 토큰의 인증 여부 필드 확인
        if(!emailToken.isVerified()){
            throw new EmailException(ExceptionType.EMAIL_TOKEN_NOT_VERIFIED);
        }
        return emailToken;
    }
}

