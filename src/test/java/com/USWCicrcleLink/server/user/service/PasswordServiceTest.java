package com.USWCicrcleLink.server.user.service;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.ClubMemberAccountStatusException;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberAccountStatus;
import com.USWCicrcleLink.server.user.domain.ExistingMember.ClubMemberTemp;
import com.USWCicrcleLink.server.user.dto.ClubDTO;
import com.USWCicrcleLink.server.user.dto.ExistingMemberSignUpRequest;
import com.USWCicrcleLink.server.user.repository.ClubMemberAccountStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordServiceTest {

    @Mock
    private ClubMemberAccountStatusRepository repository;

    @InjectMocks
    private ClubMemberAccountStatusService service;

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordService = new PasswordService();
    }

    @Test
    @DisplayName("clubMemberAccountStatus 생성 성공")
    void createAccountStatus_success() {
        Club club = mock(Club.class);
        ClubMemberTemp temp = mock(ClubMemberTemp.class);
        ClubMemberAccountStatus status = mock(ClubMemberAccountStatus.class);

        when(club.getClubId()).thenReturn(1L);
        when(temp.getClubMemberTempId()).thenReturn(1L);
        mockStatic(ClubMemberAccountStatus.class).when(() -> ClubMemberAccountStatus.createClubMemberAccountStatus(club, temp)).thenReturn(status);

        assertDoesNotThrow(() -> service.createAccountStatus(club, temp));
        verify(repository, times(1)).save(status);
    }

    @Test
    @DisplayName("가입 요청 검증 성공 - 개수와 UUID 모두 일치")
    void checkRequest_success() {
        ClubMemberTemp temp = mock(ClubMemberTemp.class);
        when(temp.getClubMemberTempId()).thenReturn(1L);

        ClubDTO club1 = new ClubDTO(UUID.randomUUID());
        ClubDTO club2 = new ClubDTO(UUID.randomUUID());

        ExistingMemberSignUpRequest request = mock(ExistingMemberSignUpRequest.class);
        when(request.getClubs()).thenReturn(List.of(club1, club2));

        when(repository.countByClubMemberTemp_ClubMemberTempId(1L)).thenReturn(2L);

        ClubMemberAccountStatus s1 = mock(ClubMemberAccountStatus.class);
        ClubMemberAccountStatus s2 = mock(ClubMemberAccountStatus.class);
        Club c1 = mock(Club.class);
        Club c2 = mock(Club.class);

        when(s1.getClub()).thenReturn(c1);
        when(s2.getClub()).thenReturn(c2);
        when(c1.getClubUUID()).thenReturn(club1.getClubUUID());
        when(c2.getClubUUID()).thenReturn(club2.getClubUUID());

        when(repository.findAllByClubMemberTemp_ClubMemberTempId(1L)).thenReturn(List.of(s1, s2));

        assertDoesNotThrow(() -> service.checkRequest(request, temp));
    }

    @Test
    @DisplayName("가입 요청 검증 실패 - 저장된 개수 불일치")
    void checkRequest_countMismatch_throwsException() {
        ClubMemberTemp temp = mock(ClubMemberTemp.class);
        when(temp.getClubMemberTempId()).thenReturn(1L);

        ExistingMemberSignUpRequest request = mock(ExistingMemberSignUpRequest.class);
        when(request.getClubs()).thenReturn(List.of(new ClubDTO(UUID.randomUUID())));

        when(repository.countByClubMemberTemp_ClubMemberTempId(1L)).thenReturn(0L);

        ClubMemberAccountStatusException ex = assertThrows(ClubMemberAccountStatusException.class,
                () -> service.checkRequest(request, temp));
        assertEquals(ExceptionType.CLUB_MEMBER_ACCOUNTSTATUS_COUNT_NOT_MATCH, ex.getExceptionType());
    }

    @Test
    @DisplayName("clubMemberTemp에 대한 연관된 상태 정보 삭제 성공")
    void deleteAccountStatus_deletesSuccessfully() {
        ClubMemberTemp temp = mock(ClubMemberTemp.class);
        when(temp.getClubMemberTempId()).thenReturn(1L);

        List<ClubMemberAccountStatus> statuses = List.of(mock(ClubMemberAccountStatus.class));
        when(repository.findAllByClubMemberTemp_ClubMemberTempId(1L)).thenReturn(statuses);

        service.deleteAccountStatus(temp);

        verify(repository, times(1)).deleteAll(statuses);
    }

    // PasswordService Tests

    @Test
    @DisplayName("비밀번호 유효성 검사 - 성공")
    void validatePassword_success() {
        String password = "Test1234!";
        assertDoesNotThrow(() -> passwordService.validatePassword(password, password));
    }

    @Test
    @DisplayName("비밀번호 유효성 검사 - 빈 값 입력")
    void validatePassword_blank_fail() {
        String password = " ";
        String confirm = " ";
        UserException ex = assertThrows(UserException.class, () -> passwordService.validatePassword(password, confirm));
        assertEquals(ExceptionType.USER_PASSWORD_NOT_INPUT, ex.getExceptionType());
    }

    @Test
    @DisplayName("비밀번호 유효성 검사 - 조건 불일치")
    void validatePassword_condition_fail() {
        String password = "password";
        UserException ex = assertThrows(UserException.class, () -> passwordService.validatePassword(password, password));
        assertEquals(ExceptionType.USER_PASSWORD_CONDITION_FAILED, ex.getExceptionType());
    }

    @Test
    @DisplayName("비밀번호 유효성 검사 - 비밀번호 불일치")
    void validatePassword_mismatch_fail() {
        String password = "Test1234!";
        String confirm = "Wrong1234!";
        UserException ex = assertThrows(UserException.class, () -> passwordService.validatePassword(password, confirm));
        assertEquals(ExceptionType.USER_NEW_PASSWORD_NOT_MATCH, ex.getExceptionType());
    }
}
