package com.USWCicrcleLink.server.aplict.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.clubLeader.domain.ClubForm;
import com.USWCicrcleLink.server.clubLeader.domain.FormQuestion;
import com.USWCicrcleLink.server.clubLeader.domain.FormQuestionOption;
import com.USWCicrcleLink.server.clubLeader.domain.QuestionType;
import com.USWCicrcleLink.server.club.form.repository.ClubFormRepository;
import com.USWCicrcleLink.server.aplict.domain.AplictAnswer;
import com.USWCicrcleLink.server.aplict.dto.AplictSubmitRequest;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final ClubFormRepository clubFormRepository;

    /**
     * 동아리 지원 가능 여부 확인 (ANYONE)
     */
    @Transactional(readOnly = true)
    public void checkIfCanApply(UUID clubUUID) {
        Profile profile = getAuthenticatedProfile();

        // 이미 지원한 경우 예외 처리
        if (aplictRepository.existsByProfileAndClubUUID(profile, clubUUID)) {
            throw new AplictException(ExceptionType.ALREADY_APPLIED);
        }

        // 이미 동아리 멤버인 경우 예외 처리
        if (clubMembersRepository.existsByProfileAndClubUUID(profile, clubUUID)) {
            throw new AplictException(ExceptionType.ALREADY_MEMBER);
        }

        List<Profile> clubMembers = clubMembersRepository.findProfilesByClubUUID(clubUUID);

        // 등록된 전화번호
        for (Profile member : clubMembers) {
            if (profile.getUserHp().equals(member.getUserHp())) {
                throw new AplictException(ExceptionType.PHONE_NUMBER_ALREADY_REGISTERED);
            }
        }

        // 등록된 학번
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
     * 지원서 작성하기 버튼 (USER)
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
     * 동아리 지원서 제출 (USER)
     */
    public void submitAplict(UUID clubUUID) {
        Profile profile = getAuthenticatedProfile();

        checkIfCanApply(clubUUID);

        Club club = clubRepository.findByClubUUID(clubUUID)
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_NOT_EXISTS));

        Aplict aplict = Aplict.builder()
                .profile(profile)
                .club(club)
                .submittedAt(LocalDateTime.now())
                .aplictStatus(AplictStatus.WAIT)
                .build();

        try {
            aplictRepository.save(aplict);
        } catch (DataIntegrityViolationException e) {
            throw new AplictException(ExceptionType.ALREADY_APPLIED);
        }
        log.debug("동아리 지원서 제출 성공 - ClubUUID: {}, Status: {}", clubUUID, AplictStatus.WAIT);
    }

    /**
     * 동아리 지원서 제출 (with Answers) (USER)
     */
    public void submitAplictWithAnswers(UUID clubUUID, Long formId, AplictSubmitRequest request) {
        Profile profile = getAuthenticatedProfile();

        checkIfCanApply(clubUUID);

        Club club = clubRepository.findByClubUUID(clubUUID)
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_NOT_EXISTS));

        ClubForm clubForm = clubFormRepository.findById(formId)
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_FORM_NOT_EXISTS)); // Need ERROR TYPE

        // 폼이 해당 동아리의 것인지 확인
        if (!clubForm.getClub().getClubUUID().equals(clubUUID)) {
            throw new ClubException(ExceptionType.CLUB_FORM_NOT_EXISTS); // Mismatch
        }

        // 폼 상태 확인 (PUBLISHED 인지) - Logic TBD, assuming valid if exists for now or add check
        // if (clubForm.getStatus() != FormStatus.PUBLISHED) ...

        Aplict aplict = Aplict.builder()
                .profile(profile)
                .club(club)
                .clubForm(clubForm)
                .submittedAt(LocalDateTime.now())
                .aplictStatus(AplictStatus.WAIT)
                .build();

        // 질문 매핑
        Map<Long, FormQuestion> questionMap = clubForm.getQuestions().stream()
                .collect(Collectors.toMap(FormQuestion::getQuestionId, Function.identity()));

        // 답변 처리
        for (AplictSubmitRequest.AplictAnswerRequest answerDTO : request.getAnswers()) {
            FormQuestion question = questionMap.get(answerDTO.getQuestionId());
            if (question == null) {
                // 폼에 없는 질문에 대한 답변은 무시하거나 에러 (여기선 무시 or 에러)
                // throw new AplictException(ExceptionType.INVALID_QUESTION);
                continue; 
            }
            
            // 필수 질문 체크는 별도 로직으로 하거나 여기서 체크
            
            // 옵션 검증 (객관식 문항 등)
            if (hasOptions(question.getType())) {
                boolean isValidOption = question.getOptions().stream()
                        .anyMatch(opt -> opt.getContent().equals(answerDTO.getAnswerContent()));
                if (!isValidOption) {
                    throw new AplictException(ExceptionType.INVALID_INPUT); // 혹은 INVALID_OPTION 추가
                }
            }

            AplictAnswer answer = AplictAnswer.createAnswer(aplict, question, answerDTO.getAnswerContent());
            aplict.getAnswers().add(answer);
        }

        // 필수 질문 누락 확인
        for (FormQuestion question : clubForm.getQuestions()) {
            if (question.isRequired()) {
                boolean answered = aplict.getAnswers().stream()
                        .anyMatch(a -> a.getFormQuestion().getQuestionId().equals(question.getQuestionId()));
                if (!answered) {
                     throw new AplictException(ExceptionType.LACK_OF_INFORMATION); 
                }
            }
        }

        try {
            aplictRepository.save(aplict);
        } catch (DataIntegrityViolationException e) {
            throw new AplictException(ExceptionType.ALREADY_APPLIED);
        }
        log.debug("동아리 지원서(Form) 제출 성공 - ClubUUID: {}, FormId: {}", clubUUID, formId);
    }

    /**
     * 인증된 USER 프로필 가져오기
     */
    private Profile getAuthenticatedProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            throw new UserException(ExceptionType.UNAUTHENTICATED_USER);
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;
        User user = userDetails.user();

        return profileRepository.findByUser_UserUUID(user.getUserUUID())
                .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));
    }

    private boolean hasOptions(QuestionType type) {
        return type == QuestionType.DROPDOWN || type == QuestionType.RADIO || type == QuestionType.CHECKBOX;
    }
}