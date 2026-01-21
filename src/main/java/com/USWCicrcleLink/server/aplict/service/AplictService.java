package com.USWCicrcleLink.server.aplict.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.aplict.domain.ApplicationAnswer;
import com.USWCicrcleLink.server.aplict.dto.AplictDetailResponse;
import com.USWCicrcleLink.server.aplict.dto.SubmitAplictRequest;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.club.form.domain.ClubForm;
import com.USWCicrcleLink.server.club.form.domain.FormQuestion;
import com.USWCicrcleLink.server.club.form.domain.FormQuestionOption;
import com.USWCicrcleLink.server.club.form.repository.ClubFormRepository;
import com.USWCicrcleLink.server.club.form.repository.FormQuestionOptionRepository;
import com.USWCicrcleLink.server.club.form.repository.FormQuestionRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.AplictException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AplictService {
    private final AplictRepository aplictRepository;
    private final ClubRepository clubRepository;
    private final ProfileRepository profileRepository;
    private final ClubIntroRepository clubIntroRepository;
    private final ClubMembersRepository clubMembersRepository;

    // 폼 관련 리포지토리
    private final ClubFormRepository clubFormRepository;
    private final FormQuestionRepository questionRepository;
    private final FormQuestionOptionRepository optionRepository;

    /**
     * 동아리 지원 가능 여부 확인 (ANYONE)
     */
    @Transactional(readOnly = true)
    public void checkIfCanApply(UUID clubUUID) {
        Profile profile = getAuthenticatedProfile();

        if (aplictRepository.existsByProfileAndClubUUID(profile, clubUUID)) {
            throw new AplictException(ExceptionType.ALREADY_APPLIED);
        }

        if (clubMembersRepository.existsByProfileAndClubUUID(profile, clubUUID)) {
            throw new AplictException(ExceptionType.ALREADY_MEMBER);
        }

        List<Profile> clubMembers = clubMembersRepository.findProfilesByClubUUID(clubUUID);

        for (Profile member : clubMembers) {
            if (profile.getUserHp().equals(member.getUserHp())) {
                throw new AplictException(ExceptionType.PHONE_NUMBER_ALREADY_REGISTERED);
            }
        }

        for (Profile member : clubMembers) {
            if (profile.getStudentNumber().equals(member.getStudentNumber())) {
                throw new AplictException(ExceptionType.STUDENT_NUMBER_ALREADY_REGISTERED);
            }
        }

        log.debug("동아리 지원 가능 - ClubUUID: {}", clubUUID);
    }

    /**
     * 동아리 지원 가능 여부(불리언) 반환 (USER)
     */
    @Transactional(readOnly = true)
    public boolean canApply(UUID clubUUID) {
        Profile profile = getAuthenticatedProfile();

        if (aplictRepository.existsByProfileAndClubUUID(profile, clubUUID)) {
            return false;
        }

        if (clubMembersRepository.existsByProfileAndClubUUID(profile, clubUUID)) {
            return false;
        }

        List<Profile> clubMembers = clubMembersRepository.findProfilesByClubUUID(clubUUID);

        for (Profile member : clubMembers) {
            if (profile.getUserHp().equals(member.getUserHp())) {
                return false;
            }
        }

        for (Profile member : clubMembers) {
            if (profile.getStudentNumber().equals(member.getStudentNumber())) {
                return false;
            }
        }

        return true;
    }

    /**
     * (Legacy) 구글 폼 URL 조회
     */
    @Transactional(readOnly = true)
    public String getGoogleFormUrlByClubUUID(UUID clubUUID) {
        ClubIntro clubIntro = clubIntroRepository.findByClubUUID(clubUUID)
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_INTRO_NOT_EXISTS));

        String googleFormUrl = clubIntro.getGoogleFormUrl();
        if (googleFormUrl == null || googleFormUrl.isEmpty()) {
            throw new ClubException(ExceptionType.GOOGLE_FORM_URL_NOT_EXISTS);
        }

        log.debug("구글 폼 URL 조회 성공 - ClubUUID: {}", clubUUID);
        return googleFormUrl;
    }

    /**
     * 동아리 지원서 제출 (자체 폼 방식)
     */
    public void submitAplict(UUID clubUUID, Long formId, SubmitAplictRequest request) {
        Profile profile = getAuthenticatedProfile();
        checkIfCanApply(clubUUID);

        Club club = clubRepository.findByClubUUID(clubUUID)
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_NOT_EXISTS));

        ClubForm form = clubFormRepository.findById(formId)
                .orElseThrow(() -> new IllegalArgumentException("지원 양식을 찾을 수 없습니다."));

        Aplict aplict = Aplict.builder()
                .profile(profile)
                .club(club)
                .clubForm(form)
                .submittedAt(LocalDateTime.now())
                .aplictStatus(AplictStatus.WAIT)
                .build();

        for (SubmitAplictRequest.AnswerDto answerDto : request.getAnswers()) {
            FormQuestion question = questionRepository.findById(answerDto.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("질문 정보를 찾을 수 없습니다."));

            FormQuestionOption option = null;
            if (answerDto.getOptionId() != null) {
                option = optionRepository.findById(answerDto.getOptionId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옵션입니다."));

                if (!option.getQuestion().getQuestionId().equals(question.getQuestionId())) {
                    throw new IllegalArgumentException("잘못된 옵션 선택입니다.");
                }
            }

            ApplicationAnswer answer = ApplicationAnswer.builder()
                    .question(question)
                    .option(option)
                    .answerText(answerDto.getAnswerText())
                    .build();

            aplict.addAnswer(answer);
        }

        try {
            aplictRepository.save(aplict);
        } catch (DataIntegrityViolationException e) {
            throw new AplictException(ExceptionType.ALREADY_APPLIED);
        }
        log.debug("동아리 지원서 제출 성공 - ClubUUID: {}, User: {}", clubUUID, profile.getUserName());
    }



    private Profile getAuthenticatedProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.user();

        return profileRepository.findByUser_UserUUID(user.getUserUUID())
                .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));
    }
}