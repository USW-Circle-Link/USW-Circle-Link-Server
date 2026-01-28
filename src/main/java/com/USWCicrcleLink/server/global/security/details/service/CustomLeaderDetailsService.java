package com.USWCicrcleLink.server.global.security.details.service;

import com.USWCicrcleLink.server.club.leader.domain.Leader;
import com.USWCicrcleLink.server.club.leader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.details.CustomLeaderDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomLeaderDetailsService implements RoleBasedUserDetailsService {

    private final LeaderRepository leaderRepository;

    @Override
    public UserDetails loadUserByUuid(UUID uuid) {
        Leader leader = leaderRepository.findByLeaderUUID(uuid)
                .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));

        UUID clubuuid = leaderRepository.findClubuuidByLeaderUUID(leader.getLeaderUUID())
                .orElseThrow(() -> new UserException(ExceptionType.USER_NOT_EXISTS));

        return new CustomLeaderDetails(leader, clubuuid);
    }
}
