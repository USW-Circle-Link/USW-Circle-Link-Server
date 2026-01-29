package com.USWCicrcleLink.server.global.email.service;

import com.USWCicrcleLink.server.global.email.config.EmailConfig;
import com.USWCicrcleLink.server.global.email.domain.EmailToken;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.EmailException;
import com.USWCicrcleLink.server.user.domain.*;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final EmailConfig emailConfig;
    private final JavaMailSender javaMailSender;

    //이메일 인증 경로
    private static final String VERIFY_EMAIL_PATH = "/users/email/verify-token";

    @Value("${spring.mail.username}")
    private String email_user;

    @Async
    public void sendEmail(MimeMessage mimeMessage) {
        javaMailSender.send(mimeMessage);
    }

    // 회원가입 링크 생성
    public MimeMessage createSignUpLink(EmailToken emailToken) {
        try {
            // 회원 가입 인증 메일 생성
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true,"utf-8");
            helper.setTo(emailToken.getEmail() + "@suwon.ac.kr");
            helper.setSubject("동구라미 회원가입 인증 메일");
            helper.setFrom(email_user);

            ClassPathResource resource = new ClassPathResource("templates/main.html");

            String htmlContent;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                htmlContent = reader.lines().collect(Collectors.joining("\n"));
            }

            // 동적 인증 링크 생성
            String verificationLink = emailConfig.getBaseUrl() + VERIFY_EMAIL_PATH + "?emailTokenUUID=" + emailToken.getEmailTokenUUID();

            // HTML 내용에서 인증 링크를 변경
            htmlContent = htmlContent.replace("https://www.naver.com", verificationLink);

            // HTML 이메일 본문으로 설정
            helper.setText(htmlContent, true);

            //템플릿에 들어가는 이미지를 cid로 삽입
            helper.addInline("image", new ClassPathResource("static/images/logo.png"));

           return mimeMessage;

       } catch (MessagingException | IOException e){
           e.printStackTrace();
           throw new EmailException(ExceptionType.SEND_MAIL_FAILED);
      }
    }


   // 아이디 찾기 메일 생성
    public  MimeMessage createAccountInfoMail (User findUser)  {

        try{
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
            helper.setTo(findUser.getEmail() + "@suwon.ac.kr");
            helper.setSubject("동구라미의 아이디를 찾기 위한 메일입니다.");
            helper.setText("회원님의 아이디는  "  + findUser.getUserAccount() + " 입니다.");
            helper.setFrom(email_user);

            return mimeMessage;

        } catch (MessagingException e){
            e.printStackTrace();
            throw new EmailException(ExceptionType.SEND_MAIL_FAILED);
        }

    }

    // 인증 코드 메일 생성
    public  MimeMessage createAuthCodeMail(User user,AuthToken authToken)  {

        try{
            // 메세지 생성
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
            helper.setTo(user.getEmail() + "@suwon.ac.kr");
            helper.setSubject("비밀번호 찾기 메일 입니다.");
            helper.setText("인증코드는  "  + authToken.getAuthCode()+ " 입니다.");
            helper.setFrom(email_user);

            return mimeMessage;

        } catch (MessagingException e){
            e.printStackTrace();
            throw new EmailException(ExceptionType.SEND_MAIL_FAILED);
        }

    }

    // 회원 탈퇴 인증 메일
    public  MimeMessage createWithdrawalCodeMail(User user, WithdrawalToken token)  {

        try{
            // 메세지 생성
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);
            helper.setTo(user.getEmail() + "@suwon.ac.kr");
            helper.setSubject("회원 탈퇴를 위한 인증 메일 입니다");
            helper.setText("인증 코드는  "  + token.getWithdrawalCode()+ " 입니다.");
            helper.setFrom(email_user);

            return mimeMessage;

        } catch (MessagingException e){
            e.printStackTrace();
            throw new EmailException(ExceptionType.SEND_MAIL_FAILED);
        }

    }




}





