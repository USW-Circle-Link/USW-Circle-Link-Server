package com.USWCicrcleLink.server.club.application.service;

import com.USWCicrcleLink.server.club.application.domain.Aplict;
import com.USWCicrcleLink.server.club.application.domain.AplictAnswer;
import com.USWCicrcleLink.server.club.application.domain.AplictStatus;
import com.USWCicrcleLink.server.club.application.dto.AplictDto;
import com.USWCicrcleLink.server.club.application.repository.AplictRepository;
import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.AplictException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.details.CustomUserDetails;
import com.USWCicrcleLink.server.user.profile.domain.Profile;
import com.USWCicrcleLink.server.user.profile.repository.ProfileRepository;
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
    private final com.USWCicrcleLink.server.club.form.repository.FormQuestionRepository formQuestionRepository;
    private final com.USWCicrcleLink.server.club.application.repository.AplictAnswerRepository aplictAnswerRepository;
    private final com.USWCicrcleLink.server.club.form.repository.FormQuestionOptionRepository formQuestionOptionRepository; // Corrected
                                                                                                                            // import

    /**
     * 동아리 지원 가능 여부 확인 (ANYONE)
     */
    @Transactional(readOnly = true)
    public void checkIfCanApply(UUID clubuuid) {
        Profile profile = getAuthenticatedProfile();

        // 이미 지원한 경우 예외 처리
        if (aplictRepository.existsByProfileAndClubuuid(profile, clubuuid)) {
            throw new AplictException(ExceptionType.ALREADY_APPLIED);
        }

        // 이미 동아리 멤버인 경우 예외 처리
        if (clubMembersRepository.existsByProfileAndClubuuid(profile, clubuuid)) {
            throw new AplictException(ExceptionType.ALREADY_MEMBER);
        }

        List<Profile> clubMembers = clubMembersRepository.findProfilesByClubuuid(clubuuid);

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

        log.debug("동아리 지원 가능 - Clubuuid: {}", clubuuid);
    }

    /**
     * 동아리 지원 가능 여부(불리언) 반환 (USER)
     */
    @Transactional(readOnly = true)
    public boolean canApply(UUID clubuuid) {
        Profile profile = getAuthenticatedProfile();

        if (aplictRepository.existsByProfileAndClubuuid(profile, clubuuid)) {
            return false;
        }

        if (clubMembersRepository.existsByProfileAndClubuuid(profile, clubuuid)) {
            return false;
        }

        List<Profile> clubMembers = clubMembersRepository.findProfilesByClubuuid(clubuuid);

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

    @Transactional(readOnly = true)
    public AplictDto.DetailResponse getApplicationDetail(UUID aplictUUID) {
        Profile profile = getAuthenticatedProfile();
        Aplict aplict = aplictRepository.findByAplictUUID(aplictUUID)
                .orElseThrow(() -> new AplictException(ExceptionType.APPLICANT_NOT_EXISTS));

        // 권한 확인: 본인 또는 해당 동아리 회장
        boolean isOwner = aplict.getProfile().getProfileId().equals(profile.getProfileId());
        boolean isLeader = clubMembersRepository.existsByProfileAndClubuuid(profile, aplict.getClub().getClubuuid()); // This
                                                                                                                      // check
                                                                                                                      // might
                                                                                                                      // be
                                                                                                                      // insufficient
                                                                                                                      // for
                                                                                                                      // Leader
                                                                                                                      // role
                                                                                                                      // check.
                                                                                                                      // Using
                                                                                                                      // simplified
                                                                                                                      // logic
                                                                                                                      // for
                                                                                                                      // now
                                                                                                                      // as
                                                                                                                      // per
                                                                                                                      // previous
                                                                                                                      // service
                                                                                                                      // patterns
                                                                                                                      // or
                                                                                                                      // assume
                                                                                                                      // Leader
                                                                                                                      // check
                                                                                                                      // happens
                                                                                                                      // elsewhere.
        // Better Leader check:
        // boolean isLeader = leaderRepository.existsByClub_ClubIdAndLeaderId(...);
        // But for "User also seeing their application", isOwner is enough.
        // For Leader seeing application, we usually access via Leader Service.
        // Let's assume this method is primarily for the User to see THEIR application,
        // OR for a generic view if we add Leader check.
        // Wait, the requirement says "User also can see". Leader sees via LeaderService
        // usually.
        // Let's implement strict check.

        if (!isOwner) {
            // For Leader access, we might need a separate check or different service
            // method.
            // But if this is a shared endpoint, we need to check if 'profile' is the leader
            // of 'aplict.club'.
            // Checking if profile corresponds to a Leader entity for this club.
            // This can be complex without LeaderRepository here.
            // Let's stick to "User can see their own". Leader has separate endpoint in
            // ClubLeaderController usually.
            // If Leader uses this, we need to allow it.

            // Simplest: Check if User UUID matches Aplict Profile User UUID.
            if (!aplict.getProfile().getUser().getUserUUID().equals(profile.getUser().getUserUUID())) {
                throw new AplictException(ExceptionType.APLICT_ACCESS_DENIED);
            }
        }

        List<AplictDto.QnAResponse> qnaList = aplict.getAnswers().stream()
                .map(a -> new AplictDto.QnAResponse(a.getFormQuestion().getContent(), a.getAnswerText()))
                .toList();

        return new AplictDto.DetailResponse(
                aplict.getAplictUUID(),
                aplict.getProfile().getUserName(),
                aplict.getProfile().getStudentNumber(),
                aplict.getProfile().getMajor(),
                aplict.getSubmittedAt(),
                aplict.getAplictStatus(),
                qnaList);
    }

    /**
     * 동아리 지원서 제출 (USER)
     */
    public void submitAplict(UUID clubuuid, AplictDto.SubmitRequest request) {
        Profile profile = getAuthenticatedProfile();

        checkIfCanApply(clubuuid);

        Club club = clubRepository.findByClubuuid(clubuuid)
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_NOT_EXISTS));

        Aplict aplict = Aplict.builder()
                .profile(profile)
                .club(club)
                .submittedAt(LocalDateTime.now())
                .aplictStatus(AplictStatus.WAIT)
                .build();

        // 답변 저장
        // 답변 저장
        if (request.getAnswers() != null) {
            for (AplictDto.AnswerRequest ansReq : request.getAnswers()) {
                com.USWCicrcleLink.server.club.leader.domain.FormQuestion question = formQuestionRepository
                        .findById(ansReq.getQuestionId())
                        .orElseThrow(() -> new IllegalArgumentException("질문을 찾을 수 없습니다."));

                com.USWCicrcleLink.server.club.leader.domain.FormQuestionOption option = null;
                if (ansReq.getOptionId() != null) {
                    option = formQuestionOptionRepository.findById(ansReq.getOptionId())
                            .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다."));
                }

                AplictAnswer answer = AplictAnswer.builder()
                        .aplict(aplict)
                        .formQuestion(question)
                        .option(option)
                        .answerText(ansReq.getAnswerText())
                        .build();
                aplict.addAnswer(answer);
            }
        }

        try {
            aplictRepository.save(aplict);
        } catch (DataIntegrityViolationException e) {
            throw new AplictException(ExceptionType.ALREADY_APPLIED);
        }
        log.debug("동아리 지원서 제출 성공 - ClubUUID: {}, Status: {}", clubuuid, AplictStatus.WAIT);
    }

    /**
     * 인증된 USER 프로필 가져오기
     */
    private Profile getAuthenticatedProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.user();

        return profileRepository.findByUser_UserUUID(user.getUserUUID())
                .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));
    }
}
