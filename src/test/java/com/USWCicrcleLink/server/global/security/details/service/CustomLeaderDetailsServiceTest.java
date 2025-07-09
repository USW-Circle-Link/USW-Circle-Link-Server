package com.USWCicrcleLink.server.global.security.details.service;

import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CustomLeaderDetailsServiceTest {

    @Mock
    LeaderRepository leaderRepository;

    @InjectMocks
    CustomLeaderDetailsService customLeaderDetailsService;

    @Nested
    @DisplayName("loadUserByUuid 메서드")
    class LoadUserByUuid {

        @Test
        @DisplayName("정상적으로 Leader와 ClubUUID 조회되면 CustomLeaderDetails를 반환")
        void loadUserByUuid_정상() {
            // given
            UUID leaderUuid = UUID.randomUUID();
            UUID clubUuid = UUID.randomUUID();
            Leader leader = Leader.builder()
                    .leaderUUID(leaderUuid)
                    .leaderAccount("leader@company.com")
                    .leaderPw("encoded_pw")
                    .role(Role.LEADER)
                    .build();

            given(leaderRepository.findByLeaderUUID(leaderUuid)).willReturn(Optional.of(leader));
            given(leaderRepository.findClubUUIDByLeaderUUID(leaderUuid)).willReturn(Optional.of(clubUuid));

            // when
            UserDetails result = customLeaderDetailsService.loadUserByUuid(leaderUuid);

            // then
            assertThat(result).isInstanceOf(CustomLeaderDetails.class);
            CustomLeaderDetails details = (CustomLeaderDetails) result;
            assertThat(details.leader()).isEqualTo(leader);
            assertThat(details.clubUUID()).isEqualTo(clubUuid);
        }

        @Test
        @DisplayName("리더가 존재하지 않으면 UserException 발생")
        void loadUserByUuid_리더없음() {
            // given
            UUID leaderUuid = UUID.randomUUID();
            given(leaderRepository.findByLeaderUUID(leaderUuid)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> customLeaderDetailsService.loadUserByUuid(leaderUuid))
                    .isInstanceOf(UserException.class)
                    .hasMessageContaining(ExceptionType.USER_NOT_EXISTS.getMessage());
        }

        @Test
        @DisplayName("클럽 UUID가 없으면 UserException 발생")
        void loadUserByUuid_클럽UUID없음() {
            // given
            UUID leaderUuid = UUID.randomUUID();
            Leader leader = Leader.builder()
                    .leaderUUID(leaderUuid)
                    .leaderAccount("leader@company.com")
                    .leaderPw("encoded_pw")
                    .build();

            given(leaderRepository.findByLeaderUUID(leaderUuid)).willReturn(Optional.of(leader));
            given(leaderRepository.findClubUUIDByLeaderUUID(leaderUuid)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> customLeaderDetailsService.loadUserByUuid(leaderUuid))
                    .isInstanceOf(UserException.class)
                    .hasMessageContaining(ExceptionType.USER_NOT_EXISTS.getMessage());
        }
    }
}
