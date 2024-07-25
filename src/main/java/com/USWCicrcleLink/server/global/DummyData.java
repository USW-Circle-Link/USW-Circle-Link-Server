package com.USWCicrcleLink.server.global;

import com.USWCicrcleLink.server.admin.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMembers;
import com.USWCicrcleLink.server.club.club.domain.Department;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
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

    @PostConstruct
    public void init(){
        initUser1();
        initUser2();
        initUser3();
        initAdmin();
        initUserTemp();
    }

    //user1 , flag 데이터
    public void initUser1() {

        User user = User.builder()
                .userUUID(UUID.randomUUID())
                .userAccount("user1")
                .userPw("1234")
                .email("user1")
                .userCreatedAt(LocalDateTime.now())
                .userUpdatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        Profile profile = Profile.builder()
                .user(user)
                .userName("김땡떙")
                .studentNumber("1234")
                .userHp("01012345678")
                .major("정보보호")
                .profileCreatedAt(LocalDateTime.now())
                .profileUpdatedAt(LocalDateTime.now())
                .build();

        profileRepository.save(profile);

        Club club = Club.builder()
                .clubName("FLAG")
                .leaderName("개발짱")
                .mainPhotoPath("src/main/resources/static/mainPhoto/flag.jpg")
                .department(Department.ACADEMIC)
                .katalkID("flag_kakao")
                .clubInsta("flag_insta")
//                .chatRoomUrl("http://flag")
                .recruitmentStatus(RecruitmentStatus.CLOSE)
                .build();

        clubRepository.save(club);

        ClubIntro clubIntro = ClubIntro.builder()
                .club(club)
                .clubIntro("플래그입니다.")
                .googleFormUrl("flag_google_url")
                .build();

        clubIntroRepository.save(clubIntro);

        ClubMembers clubMembers = ClubMembers.builder()
                .club(club)
                .profile(profile)
                .build();

        clubMembersRepository.save(clubMembers);

        Aplict aplict = Aplict.builder()
                .profile(profile)
                .club(club)
                .aplictGoogleFormUrl("flag_google_url1")
                .submittedAt(LocalDateTime.now())
                .aplictStatus(AplictStatus.PASS)
                .build();

        aplictRepository.save(aplict);
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
                .mainPhotoPath("src/main/resources/static/mainPhoto/allabout.jpg")
                .department(Department.SHOW)
                .katalkID("allabout_kakao")
                .clubInsta("allabout_insta")
//                .chatRoomUrl("http://allabout")
                .recruitmentStatus(RecruitmentStatus.OPEN)
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
                .mainPhotoPath("src/main/resources/static/mainPhoto/gullisae.jpg")
                .department(Department.SPORT)
                .katalkID("gullisae_kakao")
                .clubInsta("gullisae_insta")
//                .chatRoomUrl("http://smash")
                .recruitmentStatus(RecruitmentStatus.OPEN)
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
    public void initUserTemp(){
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
    public void initAdmin(){
        Admin admin = Admin.builder()
                .adminAccount("admin")
                .adminPw("1234")
                .adminName("관리자")
                .build();

        adminRepository.save(admin);
    }
}
