package com.USWCicrcleLink.server.global;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMembers;
import com.USWCicrcleLink.server.club.club.domain.Department;
import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroPhotoRepository;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.security.domain.Role;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.domain.UserTemp;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import com.USWCicrcleLink.server.user.repository.UserTempRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Transactional
@RequiredArgsConstructor
public class DummyData {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final UserTempRepository userTempRepository;
    private final ClubRepository clubRepository;
    private final ClubMembersRepository clubMembersRepository;
    private final AplictRepository aplictRepository;
    private final ClubIntroRepository clubIntroRepository;
    private final ClubIntroPhotoRepository clubIntroPhotoRepository;
    private final LeaderRepository leaderRepository;

    @PostConstruct
    public void init() {
        initUser1();
        initUser2();
        initUser3();
        initAdmin();
        initUserTemp();
    }

    //user1 , flag 데이터
    public void initUser1() {

        User user1 = User.builder()
                .userUUID(UUID.randomUUID())
                .userAccount("user1")
                .userPw("1234")
                .email("user1")
                .userCreatedAt(LocalDateTime.now())
                .userUpdatedAt(LocalDateTime.now())
                .role(Role.USER)
                .build();

        userRepository.save(user1);

        User user2 = User.builder()
                .userUUID(UUID.randomUUID())
                .userAccount("user222")
                .userPw("1234")
                .email("user222")
                .userCreatedAt(LocalDateTime.now())
                .userUpdatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user2);

        User user3 = User.builder()
                .userUUID(UUID.randomUUID())
                .userAccount("user333")
                .userPw("1234")
                .email("user333")
                .userCreatedAt(LocalDateTime.now())
                .userUpdatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user3);

        User user4 = User.builder()
                .userUUID(UUID.randomUUID())
                .userAccount("user444")
                .userPw("1234")
                .email("user444")
                .userCreatedAt(LocalDateTime.now())
                .userUpdatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user4);

        Profile profile1 = Profile.builder()
                .user(user1)
                .userName("김땡떙")
                .studentNumber("1234")
                .userHp("01012345678")
                .major("정보보호")
                .profileCreatedAt(LocalDateTime.now())
                .profileUpdatedAt(LocalDateTime.now())
                .build();

        profileRepository.save(profile1);

        Profile profile2 = Profile.builder()
                .user(user2)
                .userName("김빵빵")
                .studentNumber("1234")
                .userHp("01012345678")
                .major("정뽀호")
                .profileCreatedAt(LocalDateTime.now())
                .profileUpdatedAt(LocalDateTime.now())
                .build();

        profileRepository.save(profile2);

        Profile profile3 = Profile.builder()
                .user(user3)
                .userName("user3")
                .studentNumber("1234")
                .userHp("01012345678")
                .major("정보보호")
                .profileCreatedAt(LocalDateTime.now())
                .profileUpdatedAt(LocalDateTime.now())
                .build();

        profileRepository.save(profile3);

        Profile profile4 = Profile.builder()
                .user(user4)
                .userName("user4")
                .studentNumber("1234")
                .userHp("01012345678")
                .major("정뽀호")
                .profileCreatedAt(LocalDateTime.now())
                .profileUpdatedAt(LocalDateTime.now())
                .build();

        profileRepository.save(profile4);

        Club club = Club.builder()
                .clubName("FLAG")
                .leaderName("")
                .mainPhotoPath("")
                .leaderHp("")
                .department(Department.ACADEMIC)
                .clubInsta("")
                .build();

        clubRepository.save(club);

        Leader leader = Leader.builder()
                .leaderAccount("flag")
                .leaderPw("1234")
                .club(club)
                .role(Role.LEADER)
                .build();

        leaderRepository.save(leader);

        ClubIntro clubIntro = ClubIntro.builder()
                .club(club)
                .clubIntro("")
                .googleFormUrl("")
                .build();
        clubIntroRepository.save(clubIntro);

        // ClubIntroPhoto 객체 초기화 (order 1~5)
        for (int i = 1; i <= 5; i++) {
            ClubIntroPhoto clubIntroPhoto = ClubIntroPhoto.builder()
                    .clubIntro(clubIntro)
                    .clubIntroPhotoPath("") // 초기값으로 빈 문자열 설정
                    .order(i) // 순서를 1부터 5까지 설정
                    .build();
            clubIntroPhotoRepository.save(clubIntroPhoto);
        }
        clubIntroRepository.save(clubIntro);

        ClubMembers clubMembers1 = ClubMembers.builder()
                .club(club)
                .profile(profile1)
                .build();
        clubMembersRepository.save(clubMembers1);

        ClubMembers clubMembers2 = ClubMembers.builder()
                .club(club)
                .profile(profile2)
                .build();

        clubMembersRepository.save(clubMembers2);

        // 일반 지원자
        Aplict aplict1 = Aplict.builder()
                .profile(profile1)
                .club(club)
                .aplictGoogleFormUrl("flag_google_url1")
                .submittedAt(LocalDateTime.now())
                .build();

        aplictRepository.save(aplict1);

        Aplict aplict2 = Aplict.builder()
                .profile(profile2)
                .club(club)
                .aplictGoogleFormUrl("flag_google_url1")
                .submittedAt(LocalDateTime.now())
                .build();

        aplictRepository.save(aplict2);

        // 불합격자
        Aplict aplict3 = Aplict.builder()
                .profile(profile3)
                .club(club)
                .aplictGoogleFormUrl("flag_google_url1")
                .submittedAt(LocalDateTime.now())
                .checked(true)
                .aplictStatus(AplictStatus.FAIL)
                .build();

        aplictRepository.save(aplict3);

        Aplict aplict4 = Aplict.builder()
                .profile(profile4)
                .club(club)
                .aplictGoogleFormUrl("flag_google_url1")
                .submittedAt(LocalDateTime.now())
                .checked(true)
                .aplictStatus(AplictStatus.FAIL)
                .build();

        aplictRepository.save(aplict4);
    }

    //user2, 올어바웃 데이터
    public void initUser2() {
        //유저 데이터
        User user = User.builder()
                .userUUID(UUID.randomUUID())
                .userAccount("user2")
                .userPw("1234")
                .email("user2")
                .userCreatedAt(LocalDateTime.now())
                .userUpdatedAt(LocalDateTime.now())
                .role(Role.USER)
                .build();

        userRepository.save(user);

        Profile profile = Profile.builder()
                .user(user)
                .userName("이댕댕")
                .studentNumber("1234")
                .userHp("01012345678")
                .major("컴퓨터SW")
                .profileCreatedAt(LocalDateTime.now())
                .profileUpdatedAt(LocalDateTime.now())
                .build();

        profileRepository.save(profile);

        Club club = Club.builder()
                .clubName("올어바웃")
                .leaderName("춤짱")
                .mainPhotoPath("http://43.200.140.186:8080/mainPhoto/allabout.jpg")
                .department(Department.SHOW)
                .clubInsta("allabout_insta")
                .build();

        clubRepository.save(club);

        ClubMembers clubMembers = ClubMembers.builder()
                .club(club)
                .profile(profile)
                .build();

        clubMembersRepository.save(clubMembers);

        Aplict aplict = Aplict.builder()
                .profile(profile)
                .club(club)
                .aplictGoogleFormUrl("allabout_google_url2")
                .submittedAt(LocalDateTime.now())
                .aplictStatus(AplictStatus.PASS)
                .build();

        aplictRepository.save(aplict);
    }

    //user3, 굴리세 데이터
    public void initUser3() {

        User user = User.builder()
                .userUUID(UUID.randomUUID())
                .userAccount("user3")
                .userPw("1234")
                .email("user3")
                .userCreatedAt(LocalDateTime.now())
                .userUpdatedAt(LocalDateTime.now())
                .role(Role.USER)
                .build();

        userRepository.save(user);

        Profile profile = Profile.builder()
                .user(user)
                .userName("박둥둥")
                .studentNumber("1234")
                .userHp("01012345678")
                .major("데이터과학")
                .profileCreatedAt(LocalDateTime.now())
                .profileUpdatedAt(LocalDateTime.now())
                .build();

        profileRepository.save(profile);

        Club club = Club.builder()
                .clubName("굴리세")
                .leaderName("볼링짱")
                .mainPhotoPath("http://43.200.140.186:8080/mainPhoto/gullisae.jpg")
                .department(Department.SPORT)
                .clubInsta("gullisae_insta")
                .build();

        clubRepository.save(club);

        ClubMembers clubMembers = ClubMembers.builder()
                .club(club)
                .profile(profile)
                .build();

        clubMembersRepository.save(clubMembers);

        Aplict aplict = Aplict.builder()
                .profile(profile)
                .club(club)
                .aplictGoogleFormUrl("gullisae_google_url3")
                .submittedAt(LocalDateTime.now())
                .aplictStatus(AplictStatus.PASS)
                .build();

        aplictRepository.save(aplict);
    }

    //유저템프 데이터
    public void initUserTemp() {
        UserTemp userTemp = UserTemp.builder()
                .tempAccount("account")
                .tempPw("password")
                .tempHp("01012345678")
                .tempName("수원대")
                .tempStudentNumber("12345678")
                .tempMajor("컴정데")
                .tempEmail("suwon")
                .build();

        userTempRepository.save(userTemp);
    }

    //어드민 데이터
    public void initAdmin() {
        Admin admin = Admin.builder()
                .adminUUID(UUID.randomUUID())
                .adminAccount("admin")
                .adminPw("1234")
                .adminName("관리자")
                .role(Role.ADMIN)
                .build();

        adminRepository.save(admin);
    }
}
