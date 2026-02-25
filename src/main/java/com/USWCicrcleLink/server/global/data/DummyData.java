package com.USWCicrcleLink.server.global.data;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.club.application.domain.Aplict;
import com.USWCicrcleLink.server.club.application.domain.AplictStatus;
import com.USWCicrcleLink.server.club.application.repository.AplictRepository;
import com.USWCicrcleLink.server.club.application.domain.AplictAnswer;
import com.USWCicrcleLink.server.club.application.repository.AplictAnswerRepository;
import com.USWCicrcleLink.server.club.domain.*;
import com.USWCicrcleLink.server.club.repository.*;
import com.USWCicrcleLink.server.club.form.repository.ClubFormRepository;
import com.USWCicrcleLink.server.club.form.repository.FormQuestionOptionRepository;
import com.USWCicrcleLink.server.club.form.repository.FormQuestionRepository;
import com.USWCicrcleLink.server.club.leader.domain.*;
import com.USWCicrcleLink.server.category.domain.ClubCategory;
import com.USWCicrcleLink.server.category.domain.ClubCategoryMapping;
import com.USWCicrcleLink.server.category.repository.ClubCategoryRepository;
import com.USWCicrcleLink.server.category.repository.ClubCategoryMappingRepository;
import com.USWCicrcleLink.server.club.clubInfo.domain.ClubInfo;
import com.USWCicrcleLink.server.club.clubInfo.domain.ClubInfoPhoto;
import com.USWCicrcleLink.server.club.clubInfo.repository.ClubInfoPhotoRepository;
import com.USWCicrcleLink.server.club.clubInfo.repository.ClubInfoRepository;
import com.USWCicrcleLink.server.club.leader.domain.Leader;
import com.USWCicrcleLink.server.club.leader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.security.jwt.domain.Role;
import com.USWCicrcleLink.server.user.profile.domain.MemberType;
import com.USWCicrcleLink.server.user.profile.domain.Profile;
import com.USWCicrcleLink.server.user.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Transactional
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile({ "test", "local" })
public class DummyData {

        private final ProfileRepository profileRepository;
        private final UserRepository userRepository;
        private final AdminRepository adminRepository;
        private final ClubRepository clubRepository;
        private final ClubMembersRepository clubMembersRepository;
        private final AplictRepository aplictRepository;
        private final ClubInfoRepository clubInfoRepository;
        private final ClubInfoPhotoRepository clubInfoPhotoRepository;
        private final LeaderRepository leaderRepository;
        private final PasswordEncoder passwordEncoder;
        private final ClubMainPhotoRepository clubMainPhotoRepository;
        private final ClubHashtagRepository clubHashtagRepository;
        private final ClubCategoryRepository clubCategoryRepository;
        private final ClubCategoryMappingRepository clubCategoryMappingRepository;
        private final ClubFormRepository clubFormRepository;
        private final FormQuestionRepository formQuestionRepository;
        private final FormQuestionOptionRepository formQuestionOptionRepository;
        private final AplictAnswerRepository aplictAnswerRepository;

        @PostConstruct
        public void init() {
                initAdmin();
                initUser1();
                initUser2();
                initUser3();
                initclub();
                initAplictFlow();
        }

        // 관리자 동연회 데이터
        public void initAdmin() {
                UUID clubUnionUUID = UUID.randomUUID();
                // 동아리 연합회 관리자 계정
                Admin clubUnion = Admin.builder()
                                .adminUUID(clubUnionUUID)
                                .adminAccount("clubUnion")
                                .adminPw(passwordEncoder.encode("hpsEetcTf7ymgy6!")) // 비밀번호 암호화
                                .adminName("동아리 연합회")
                                .role(Role.ADMIN)
                                .build();

                UUID developerUUID = UUID.randomUUID();
                // 개발자 계정
                Admin developer = Admin.builder()
                                .adminUUID(developerUUID)
                                .adminAccount("developer")
                                .adminPw(passwordEncoder.encode("5MYcg7Cuvrh50fS")) // 비밀번호 암호화
                                .adminName("운영자")
                                .role(Role.ADMIN)
                                .build();

                // 데이터 저장
                adminRepository.save(clubUnion);
                adminRepository.save(developer);

                // User 테이블 동기화
                saveUserSync(clubUnionUUID, "clubUnion", clubUnion.getAdminPw(), "admin@club.union", Role.ADMIN);
                saveUserSync(developerUUID, "developer", developer.getAdminPw(), "developer@club.union", Role.ADMIN);
        }

        // user1
        public void initUser1() {

                User user1 = User.builder()
                                .userUUID(UUID.randomUUID())
                                .userAccount("user11")
                                .userPw(passwordEncoder.encode("qwer1234!"))
                                .email("user111")
                                .userCreatedAt(LocalDateTime.now())
                                .userUpdatedAt(LocalDateTime.now())
                                .role(Role.USER)
                                .build();
                userRepository.save(user1);

                User user2 = User.builder()
                                .userUUID(UUID.randomUUID())
                                .userAccount("user222")
                                .userPw(passwordEncoder.encode("qwer1234!"))
                                .email("user222")
                                .userCreatedAt(LocalDateTime.now())
                                .userUpdatedAt(LocalDateTime.now())
                                .role(Role.USER)
                                .build();
                userRepository.save(user2);

                User user3 = User.builder()
                                .userUUID(UUID.randomUUID())
                                .userAccount("user333")
                                .userPw(passwordEncoder.encode("qwer1234!"))
                                .email("user333")
                                .userCreatedAt(LocalDateTime.now())
                                .userUpdatedAt(LocalDateTime.now())
                                .role(Role.USER)
                                .build();
                userRepository.save(user3);

                User user4 = User.builder()
                                .userUUID(UUID.randomUUID())
                                .userAccount("user444")
                                .userPw(passwordEncoder.encode("qwer1234!"))
                                .email("user444")
                                .userCreatedAt(LocalDateTime.now())
                                .userUpdatedAt(LocalDateTime.now())
                                .role(Role.USER)
                                .build();
                userRepository.save(user4);

                Profile profile1 = Profile.builder()
                                .user(user1)
                                .userName("김땡떙")
                                .studentNumber("00001001")
                                .userHp("01012345678")
                                .major("정보보호학과")
                                .profileCreatedAt(LocalDateTime.now())
                                .profileUpdatedAt(LocalDateTime.now())
                                .memberType(MemberType.REGULARMEMBER)
                                .build();
                profileRepository.save(profile1);

                Profile profile2 = Profile.builder()
                                .user(user2)
                                .userName("김빵빵")
                                .studentNumber("00001002")
                                .userHp("01012345678")
                                .major("정보보호학과")
                                .profileCreatedAt(LocalDateTime.now())
                                .profileUpdatedAt(LocalDateTime.now())
                                .memberType(MemberType.REGULARMEMBER)
                                .build();
                profileRepository.save(profile2);

                Profile profile3 = Profile.builder()
                                .user(user3)
                                .userName("user3")
                                .studentNumber("00001003")
                                .userHp("01012345678")
                                .major("정보보호학과")
                                .profileCreatedAt(LocalDateTime.now())
                                .profileUpdatedAt(LocalDateTime.now())
                                .memberType(MemberType.REGULARMEMBER)
                                .build();
                profileRepository.save(profile3);

                Profile profile4 = Profile.builder()
                                .user(user4)
                                .userName("user4")
                                .studentNumber("00001004")
                                .userHp("01012345678")
                                .major("정보보호학과")
                                .profileCreatedAt(LocalDateTime.now())
                                .profileUpdatedAt(LocalDateTime.now())
                                .memberType(MemberType.REGULARMEMBER)
                                .build();
                profileRepository.save(profile4);

                // flag 데이터
                Club flagClub = Club.builder()
                                .clubName("FLAG")
                                .leaderName("flag")
                                .leaderHp("01012345678")
                                .department(Department.ACADEMIC)
                                .clubInsta("https://www.instagram.com/usw1982/")
                                .clubRoomNumber("208")
                                .build();
                clubRepository.save(flagClub);

                Club badmintonClub = Club.builder()
                                .clubName("배드민턴동아리")
                                .leaderName("배드민턴")
                                .leaderHp("00000000000")
                                .department(Department.SPORT)
                                .clubInsta("https://www.instagram.com/usw1982/")
                                .clubRoomNumber("B101")
                                .build();
                clubRepository.save(badmintonClub);

                Club volunteerClub = Club.builder()
                                .clubName("봉사동아리")
                                .leaderName("봉사")
                                .leaderHp("00000000000")
                                .department(Department.ACADEMIC)
                                .clubInsta("https://www.instagram.com/usw1982/")
                                .clubRoomNumber("108")
                                .build();
                clubRepository.save(volunteerClub);

                // 플래그, 배드민턴, 봉사 해시태그 데이터
                ClubHashtag flagHashtag1 = ClubHashtag.builder()
                                .club(flagClub)
                                .clubHashtag("IT")
                                .build();
                ClubHashtag flagHashtag2 = ClubHashtag.builder()
                                .club(flagClub)
                                .clubHashtag("개발")
                                .build();
                clubHashtagRepository.save(flagHashtag1);
                clubHashtagRepository.save(flagHashtag2);

                // 배드민턴 동아리 해시태그 추가
                ClubHashtag badmintonHashtag1 = ClubHashtag.builder()
                                .club(badmintonClub)
                                .clubHashtag("스포츠")
                                .build();
                ClubHashtag badmintonHashtag2 = ClubHashtag.builder()
                                .club(badmintonClub)
                                .clubHashtag("건강")
                                .build();
                clubHashtagRepository.save(badmintonHashtag1);
                clubHashtagRepository.save(badmintonHashtag2);

                // 봉사 동아리 해시태그 추가
                ClubHashtag volunteerHashtag1 = ClubHashtag.builder()
                                .club(volunteerClub)
                                .clubHashtag("봉사")
                                .build();
                ClubHashtag volunteerHashtag2 = ClubHashtag.builder()
                                .club(volunteerClub)
                                .clubHashtag("공헌")
                                .build();
                clubHashtagRepository.save(volunteerHashtag1);
                clubHashtagRepository.save(volunteerHashtag2);

                ClubMainPhoto clubMainPhoto = ClubMainPhoto.builder()
                                .club(flagClub)
                                .clubMainPhotoName("")
                                .clubMainPhotoS3Key("")
                                .build();
                clubMainPhotoRepository.save(clubMainPhoto);

                UUID flagLeaderUUID = UUID.randomUUID();
                Leader leader = Leader.builder()
                                .leaderUUID(flagLeaderUUID)
                                .leaderAccount("flag1")
                                .leaderPw(passwordEncoder.encode("a123456!"))
                                .club(flagClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(leader);
                saveUserSync(flagLeaderUUID, "flag1", leader.getLeaderPw(), "flag1@leader.club", Role.LEADER);

                ClubInfo clubInfo = ClubInfo.builder()
                                .club(flagClub)
                                .clubInfo("플래그입니다.")
                                .clubRecruitment("플래그 모집글입니다.")
                                .googleFormUrl("flag_google_url")
                                .recruitmentStatus(RecruitmentStatus.OPEN)
                                .build();
                clubInfoRepository.save(clubInfo);

                UUID badmintonLeaderUUID = UUID.randomUUID();
                Leader leader1 = Leader.builder()
                                .leaderUUID(badmintonLeaderUUID)
                                .leaderAccount("badmintonClub")
                                .leaderPw(passwordEncoder.encode("a123456!"))
                                .club(badmintonClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(leader1);
                saveUserSync(badmintonLeaderUUID, "badmintonClub", leader1.getLeaderPw(), "badminton@leader.club",
                                Role.LEADER);

                ClubInfo clubInfo1 = ClubInfo.builder()
                                .club(badmintonClub)
                                .clubInfo("배드민턴 동아리입니다.")
                                .clubRecruitment("배드민턴 모집글입니다.")
                                .googleFormUrl("badmintonClub_google_url")
                                .recruitmentStatus(RecruitmentStatus.OPEN)
                                .build();
                clubInfoRepository.save(clubInfo1);

                ClubMainPhoto badmintonMainPhoto = ClubMainPhoto.builder()
                                .club(badmintonClub)
                                .clubMainPhotoName("")
                                .clubMainPhotoS3Key("")
                                .build();
                clubMainPhotoRepository.save(badmintonMainPhoto);

                for (int i = 1; i <= 5; i++) {
                        ClubInfoPhoto badmintonInfoPhoto = ClubInfoPhoto.builder()
                                        .clubInfo(clubInfo1)
                                        .clubInfoPhotoName("")
                                        .clubInfoPhotoS3Key("")
                                        .order(i)
                                        .build();
                        clubInfoPhotoRepository.save(badmintonInfoPhoto);
                }

                UUID volunteerLeaderUUID = UUID.randomUUID();
                Leader leader2 = Leader.builder()
                                .leaderUUID(volunteerLeaderUUID)
                                .leaderAccount("volunteerClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(volunteerClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(leader2);
                saveUserSync(volunteerLeaderUUID, "volunteerClub", leader2.getLeaderPw(), "volunteer@leader.club",
                                Role.LEADER);

                ClubInfo clubInfo2 = ClubInfo.builder()
                                .club(volunteerClub)
                                .clubInfo("봉사동아리입니다.")
                                .clubRecruitment("봉사 모집글입니다.")
                                .googleFormUrl("volunteerClub_google_url")
                                .recruitmentStatus(RecruitmentStatus.CLOSE)
                                .build();
                clubInfoRepository.save(clubInfo2);

                ClubMainPhoto volunteerMainPhoto = ClubMainPhoto.builder()
                                .club(volunteerClub)
                                .clubMainPhotoName("")
                                .clubMainPhotoS3Key("")
                                .build();
                clubMainPhotoRepository.save(volunteerMainPhoto);

                for (int i = 1; i <= 5; i++) {
                        ClubInfoPhoto volunteerInfoPhoto = ClubInfoPhoto.builder()
                                        .clubInfo(clubInfo2)
                                        .clubInfoPhotoName("")
                                        .clubInfoPhotoS3Key("")
                                        .order(i)
                                        .build();
                        clubInfoPhotoRepository.save(volunteerInfoPhoto);
                }

                for (int i = 1; i <= 5; i++) {
                        ClubInfoPhoto clubInfoPhoto = ClubInfoPhoto.builder()
                                        .clubInfo(clubInfo)
                                        .clubInfoPhotoName("")
                                        .clubInfoPhotoS3Key("")
                                        .order(i)
                                        .build();
                        clubInfoPhotoRepository.save(clubInfoPhoto);
                }

                // FLAG 동아리 지원자

                Aplict aplict2 = Aplict.builder()
                                .profile(profile2)
                                .club(flagClub)
                                .submittedAt(LocalDateTime.now())
                                .build();
                aplictRepository.save(aplict2);

                Aplict aplict3 = Aplict.builder()
                                .profile(profile3)
                                .club(flagClub)
                                .submittedAt(LocalDateTime.now())

                                .privateStatus(AplictStatus.FAIL)
                                .publicStatus(AplictStatus.WAIT)
                                .build();
                aplictRepository.save(aplict3);

                Aplict aplict4 = Aplict.builder()
                                .profile(profile4)
                                .club(flagClub)
                                .submittedAt(LocalDateTime.now())

                                .privateStatus(AplictStatus.FAIL)
                                .publicStatus(AplictStatus.WAIT)
                                .build();
                aplictRepository.save(aplict4);

                // 배드민턴동아리 소속 및 지원
                ClubMembers badmintonMember = ClubMembers.builder()
                                .club(badmintonClub)
                                .profile(profile1)
                                .build();
                clubMembersRepository.save(badmintonMember);

                Aplict badmintonAplict = Aplict.builder()
                                .profile(profile1)
                                .club(badmintonClub)
                                .submittedAt(LocalDateTime.now())
                                .privateStatus(AplictStatus.PASS)
                                .publicStatus(AplictStatus.WAIT)
                                .build();
                aplictRepository.save(badmintonAplict);

                // 봉사동아리 소속 및 지원
                ClubMembers volunteerMember = ClubMembers.builder()
                                .club(volunteerClub)
                                .profile(profile1)
                                .build();
                clubMembersRepository.save(volunteerMember);

                Aplict volunteerAplict = Aplict.builder()
                                .profile(profile1)
                                .club(volunteerClub)
                                .submittedAt(LocalDateTime.now())
                                .privateStatus(AplictStatus.FAIL)
                                .publicStatus(AplictStatus.WAIT)
                                .build();
                aplictRepository.save(volunteerAplict);

                // 클럽 카테고리 더미 데이터 추가
                ClubCategory clubCategory1 = ClubCategory.builder()
                                .clubCategoryName("운동")
                                .build();
                clubCategoryRepository.save(clubCategory1);

                ClubCategory clubCategory2 = ClubCategory.builder()
                                .clubCategoryName("학술")
                                .build();
                clubCategoryRepository.save(clubCategory2);

                ClubCategory clubCategory3 = ClubCategory.builder()
                                .clubCategoryName("봉사")
                                .build();
                clubCategoryRepository.save(clubCategory3);

                ClubCategory clubCategory4 = ClubCategory.builder()
                                .clubCategoryName("개발")
                                .build();
                clubCategoryRepository.save(clubCategory4);

                // 클럽-카테고리 매핑 더미 데이터 추가
                ClubCategoryMapping mapping1 = ClubCategoryMapping.builder()
                                .club(flagClub)
                                .clubCategory(clubCategory2)
                                .build();
                clubCategoryMappingRepository.save(mapping1);

                ClubCategoryMapping mapping4 = ClubCategoryMapping.builder()
                                .club(flagClub)
                                .clubCategory(clubCategory4)
                                .build();
                clubCategoryMappingRepository.save(mapping4);

                ClubCategoryMapping mapping2 = ClubCategoryMapping.builder()
                                .club(badmintonClub)
                                .clubCategory(clubCategory1)
                                .build();
                clubCategoryMappingRepository.save(mapping2);

                ClubCategoryMapping mapping3 = ClubCategoryMapping.builder()
                                .club(volunteerClub)
                                .clubCategory(clubCategory3)
                                .build();
                clubCategoryMappingRepository.save(mapping3);

                ClubCategoryMapping mapping5 = ClubCategoryMapping.builder()
                                .club(volunteerClub)
                                .clubCategory(clubCategory2)
                                .build();
                clubCategoryMappingRepository.save(mapping5);

                // Postman Test Users
                User testUser1 = User.builder()
                                .userUUID(UUID.randomUUID())
                                .userAccount("test1")
                                .userPw(passwordEncoder.encode("12345"))
                                .email("test1")
                                .userCreatedAt(LocalDateTime.now())
                                .userUpdatedAt(LocalDateTime.now())
                                .role(Role.USER)
                                .build();
                userRepository.save(testUser1);

                User testUser2 = User.builder()
                                .userUUID(UUID.randomUUID())
                                .userAccount("test2")
                                .userPw(passwordEncoder.encode("12345"))
                                .email("test2")
                                .userCreatedAt(LocalDateTime.now())
                                .userUpdatedAt(LocalDateTime.now())
                                .role(Role.USER)
                                .build();
                userRepository.save(testUser2);

                User testUser3 = User.builder()
                                .userUUID(UUID.randomUUID())
                                .userAccount("test3")
                                .userPw(passwordEncoder.encode("12345"))
                                .email("test3")
                                .userCreatedAt(LocalDateTime.now())
                                .userUpdatedAt(LocalDateTime.now())
                                .role(Role.USER)
                                .build();
                userRepository.save(testUser3);

                Profile testProfile1 = Profile.builder()
                                .user(testUser1)
                                .userName("테스터1")
                                .studentNumber("11111111")
                                .userHp("01011111111")
                                .major("테스트학과")
                                .profileCreatedAt(LocalDateTime.now())
                                .profileUpdatedAt(LocalDateTime.now())
                                .memberType(MemberType.REGULARMEMBER)
                                .build();
                profileRepository.save(testProfile1);

                Profile testProfile2 = Profile.builder()
                                .user(testUser2)
                                .userName("테스터2")
                                .studentNumber("22222222")
                                .userHp("01022222222")
                                .major("테스트학과")
                                .profileCreatedAt(LocalDateTime.now())
                                .profileUpdatedAt(LocalDateTime.now())
                                .memberType(MemberType.REGULARMEMBER)
                                .build();
                profileRepository.save(testProfile2);

                Profile testProfile3 = Profile.builder()
                                .user(testUser3)
                                .userName("테스터3")
                                .studentNumber("33333333")
                                .userHp("01033333333")
                                .major("테스트학과")
                                .profileCreatedAt(LocalDateTime.now())
                                .profileUpdatedAt(LocalDateTime.now())
                                .memberType(MemberType.REGULARMEMBER)
                                .build();
                profileRepository.save(testProfile3);

                // test1, test2 apply to FLAG
                Aplict testAplict1 = Aplict.builder()
                                .profile(testProfile1)
                                .club(flagClub)
                                .submittedAt(LocalDateTime.now())
                                .privateStatus(AplictStatus.WAIT)
                                .publicStatus(AplictStatus.WAIT)
                                .build();
                aplictRepository.save(testAplict1);

                Aplict testAplict2 = Aplict.builder()
                                .profile(testProfile2)
                                .club(flagClub)
                                .submittedAt(LocalDateTime.now())
                                .privateStatus(AplictStatus.WAIT)
                                .publicStatus(AplictStatus.WAIT)
                                .build();
                aplictRepository.save(testAplict2);

        }

        // user2, 올어바웃 데이터
        public void initUser2() {
                // 유저 데이터
                User user = User.builder()
                                .userUUID(UUID.randomUUID())
                                .userAccount("user22")
                                .userPw(passwordEncoder.encode(""))
                                .email("user22")
                                .userCreatedAt(LocalDateTime.now())
                                .userUpdatedAt(LocalDateTime.now())
                                .role(Role.USER)
                                .build();

                userRepository.save(user);

                Profile profile = Profile.builder()
                                .user(user)
                                .userName("이댕댕")
                                .studentNumber("00001008")
                                .userHp("01012345678")
                                .major("컴퓨터SW학과")
                                .profileCreatedAt(LocalDateTime.now())
                                .profileUpdatedAt(LocalDateTime.now())
                                .memberType(MemberType.REGULARMEMBER)
                                .build();

                profileRepository.save(profile);

                Club allaboutClub = Club.builder()
                                .clubName("올어바웃")
                                .leaderName("춤짱")
                                .leaderHp("00012341234")
                                .department(Department.SHOW)
                                .clubInsta("https://www.instagram.com/usw1982/")
                                .clubRoomNumber("B103")
                                .build();

                clubRepository.save(allaboutClub);

                // 올어바웃 해시태그
                ClubHashtag allaboutHashtag1 = ClubHashtag.builder()
                                .club(allaboutClub)
                                .clubHashtag("댄스")
                                .build();

                ClubHashtag allaboutHashtag2 = ClubHashtag.builder()
                                .club(allaboutClub)
                                .clubHashtag("공연")
                                .build();

                clubHashtagRepository.save(allaboutHashtag1);
                clubHashtagRepository.save(allaboutHashtag2);

                UUID allaboutLeaderUUID = UUID.randomUUID();
                Leader allaboutLeader = Leader.builder()
                                .leaderUUID(allaboutLeaderUUID)
                                .leaderAccount("allaboutClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(allaboutClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(allaboutLeader);
                saveUserSync(allaboutLeaderUUID, "allaboutClub", allaboutLeader.getLeaderPw(), "allabout@leader.club",
                                Role.LEADER);

                ClubInfo allaboutInfo = ClubInfo.builder()
                                .club(allaboutClub)
                                .clubInfo("올어바웃 동아리입니다.")
                                .clubRecruitment("올어바웃 모집글입니다.")
                                .googleFormUrl("allaboutClub_google_url")
                                .recruitmentStatus(RecruitmentStatus.OPEN)
                                .build();
                clubInfoRepository.save(allaboutInfo);

                ClubMembers clubMembers = ClubMembers.builder()
                                .club(allaboutClub)
                                .profile(profile)
                                .build();

                clubMembersRepository.save(clubMembers);

                Aplict aplict = Aplict.builder()
                                .profile(profile)
                                .club(allaboutClub)
                                .submittedAt(LocalDateTime.now())
                                .privateStatus(AplictStatus.PASS)
                                .publicStatus(AplictStatus.WAIT)
                                .build();

                aplictRepository.save(aplict);
        }

        // user3, 굴리세 데이터
        public void initUser3() {

                User user = User.builder()
                                .userUUID(UUID.randomUUID())
                                .userAccount("user33")
                                .userPw(passwordEncoder.encode("12345"))
                                .email("user33")
                                .userCreatedAt(LocalDateTime.now())
                                .userUpdatedAt(LocalDateTime.now())
                                .role(Role.USER)
                                .build();

                userRepository.save(user);

                Profile profile = Profile.builder()
                                .user(user)
                                .userName("박둥둥")
                                .studentNumber("00001009")
                                .userHp("01012345678")
                                .major("데이터과학부")
                                .profileCreatedAt(LocalDateTime.now())
                                .profileUpdatedAt(LocalDateTime.now())
                                .memberType(MemberType.REGULARMEMBER)
                                .build();

                profileRepository.save(profile);

                Club gullisaeClub = Club.builder()
                                .clubName("굴리세")
                                .leaderName("볼링짱")
                                .leaderHp("00012341234")
                                .department(Department.SPORT)
                                .clubInsta("https://www.instagram.com/usw1982/")
                                .clubRoomNumber("205")
                                .build();

                clubRepository.save(gullisaeClub);

                UUID gullisaeLeaderUUID = UUID.randomUUID();
                Leader gullisaeLeader = Leader.builder()
                                .leaderUUID(gullisaeLeaderUUID)
                                .leaderAccount("gullisaeClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(gullisaeClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(gullisaeLeader);
                saveUserSync(gullisaeLeaderUUID, "gullisaeClub", gullisaeLeader.getLeaderPw(), "gullisae@leader.club",
                                Role.LEADER);

                ClubMembers clubMembers = ClubMembers.builder()
                                .club(gullisaeClub)
                                .profile(profile)
                                .build();

                clubMembersRepository.save(clubMembers);

                ClubInfo gullisaeInfo = ClubInfo.builder()
                                .club(gullisaeClub)
                                .clubInfo("굴리세 동아리입니다.")
                                .clubRecruitment("굴리세 모집글입니다.")
                                .googleFormUrl("gullisaeClub_google_url")
                                .recruitmentStatus(RecruitmentStatus.OPEN)
                                .build();
                clubInfoRepository.save(gullisaeInfo);

                Aplict aplict = Aplict.builder()
                                .profile(profile)
                                .club(gullisaeClub)
                                .submittedAt(LocalDateTime.now())
                                .privateStatus(AplictStatus.PASS)
                                .publicStatus(AplictStatus.WAIT)
                                .build();

                aplictRepository.save(aplict);
        }

        void initclub() {
                // 테니스 동아리
                Club tennisclub = Club.builder()
                                .clubName("테니스")
                                .leaderName("테니스짱")
                                .leaderHp("00012341234")
                                .department(Department.SPORT)
                                .clubInsta("https://www.instagram.com/usw1982/")
                                .clubRoomNumber("105")
                                .build();

                clubRepository.save(tennisclub);

                ClubInfo tennisInfo = ClubInfo.builder()
                                .club(tennisclub)
                                .clubInfo("테니스 동아리입니다.")
                                .clubRecruitment("테니스 모집글입니다.")
                                .googleFormUrl("tennisClub_google_url")
                                .recruitmentStatus(RecruitmentStatus.CLOSE)
                                .build();
                clubInfoRepository.save(tennisInfo);

                UUID tennisLeaderUUID = UUID.randomUUID();
                Leader tennisLeader = Leader.builder()
                                .leaderUUID(tennisLeaderUUID)
                                .leaderAccount("tennisClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(tennisclub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(tennisLeader);
                saveUserSync(tennisLeaderUUID, "tennisClub", tennisLeader.getLeaderPw(), "tennis@leader.club",
                                Role.LEADER);

                // 농구동아리
                Club basketballClub = Club.builder()
                                .clubName("농구")
                                .leaderName("농구짱")
                                .leaderHp("00012341234")
                                .department(Department.SPORT)
                                .clubInsta("https://www.instagram.com/usw1982/")
                                .clubRoomNumber("201")
                                .build();

                clubRepository.save(basketballClub);

                ClubInfo basketballInfo = ClubInfo.builder()
                                .club(basketballClub)
                                .clubInfo("농구 동아리입니다.")
                                .clubRecruitment("농구 모집글입니다.")
                                .googleFormUrl("basketball_google_url")
                                .recruitmentStatus(RecruitmentStatus.CLOSE)
                                .build();
                clubInfoRepository.save(basketballInfo);

                UUID basketballLeaderUUID = UUID.randomUUID();
                Leader basketballLeader = Leader.builder()
                                .leaderUUID(basketballLeaderUUID)
                                .leaderAccount("basketballClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(basketballClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(basketballLeader);
                saveUserSync(basketballLeaderUUID, "basketballClub", basketballLeader.getLeaderPw(),
                                "basketball@leader.club", Role.LEADER);

                // 토론동아리
                Club argClub = Club.builder()
                                .clubName("토론동아리")
                                .leaderName("토론짱")
                                .leaderHp("00012341234")
                                .department(Department.ACADEMIC)
                                .clubInsta("https://www.instagram.com/usw1982/")
                                .clubRoomNumber("B106")
                                .build();

                clubRepository.save(argClub);

                ClubInfo argInfo = ClubInfo.builder()
                                .club(argClub)
                                .clubInfo("토론 동아리입니다.")
                                .clubRecruitment("토론 모집글입니다.")
                                .googleFormUrl("arg_google_url")
                                .recruitmentStatus(RecruitmentStatus.CLOSE)
                                .build();
                clubInfoRepository.save(argInfo);

                UUID argLeaderUUID = UUID.randomUUID();
                Leader argLeader = Leader.builder()
                                .leaderUUID(argLeaderUUID)
                                .leaderAccount("argClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(argClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(argLeader);
                saveUserSync(argLeaderUUID, "argClub", argLeader.getLeaderPw(), "arg@leader.club", Role.LEADER);

                // 햄스터동아리
                Club hamsterClub = Club.builder()
                                .clubName("햄스터동아리")
                                .leaderName("햄스터짱")
                                .leaderHp("00012341234")
                                .department(Department.ACADEMIC)
                                .clubInsta("https://www.instagram.com/usw1982/")
                                .clubRoomNumber("107")
                                .build();

                clubRepository.save(hamsterClub);

                ClubInfo hamsterInfo = ClubInfo.builder()
                                .club(hamsterClub)
                                .clubInfo("햄스터 동아리입니다.")
                                .clubRecruitment("햄스터 모집글입니다.")
                                .googleFormUrl("hamster_google_url")
                                .recruitmentStatus(RecruitmentStatus.OPEN)
                                .build();
                clubInfoRepository.save(hamsterInfo);

                UUID hamsterLeaderUUID = UUID.randomUUID();
                Leader hamsterLeader = Leader.builder()
                                .leaderUUID(hamsterLeaderUUID)
                                .leaderAccount("hamsterClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(hamsterClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(hamsterLeader);
                saveUserSync(hamsterLeaderUUID, "hamsterClub", hamsterLeader.getLeaderPw(), "hamster@leader.club",
                                Role.LEADER);

                // 해달동아리
                Club sunmoonClub = Club.builder()
                                .clubName("해달 동아리")
                                .leaderName("해달짱")
                                .leaderHp("00012341234")
                                .department(Department.SHOW)
                                .clubInsta("https://www.instagram.com/usw1982/")
                                .clubRoomNumber("210")
                                .build();

                clubRepository.save(sunmoonClub);

                ClubInfo sunmoonInfo = ClubInfo.builder()
                                .club(sunmoonClub)
                                .clubInfo("해달 동아리입니다.")
                                .clubRecruitment("해달 모집글입니다.")
                                .googleFormUrl("sunmoon_google_url")
                                .recruitmentStatus(RecruitmentStatus.OPEN)
                                .build();
                clubInfoRepository.save(sunmoonInfo);

                UUID sunmoonLeaderUUID = UUID.randomUUID();
                Leader sunmoonLeader = Leader.builder()
                                .leaderUUID(sunmoonLeaderUUID)
                                .leaderAccount("sunmoonClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(sunmoonClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(sunmoonLeader);
                saveUserSync(sunmoonLeaderUUID, "sunmoonClub", sunmoonLeader.getLeaderPw(), "sunmoon@leader.club",
                                Role.LEADER);

                // 돼지동아리
                Club pigClub = Club.builder()
                                .clubName("돼지 동아리")
                                .leaderName("돼지짱")
                                .leaderHp("00012341234")
                                .department(Department.SHOW)
                                .clubInsta("https://www.instagram.com/usw1982/")
                                .clubRoomNumber("B109")
                                .build();

                clubRepository.save(pigClub);

                ClubInfo pigInfo = ClubInfo.builder()
                                .club(pigClub)
                                .clubInfo("돼지 동아리입니다.")
                                .clubRecruitment("돼지 모집글입니다.")
                                .googleFormUrl("pig_google_url")
                                .recruitmentStatus(RecruitmentStatus.OPEN)
                                .build();
                clubInfoRepository.save(pigInfo);

                UUID pigLeaderUUID = UUID.randomUUID();
                Leader pigLeader = Leader.builder()
                                .leaderUUID(pigLeaderUUID)
                                .leaderAccount("pigClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(pigClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(pigLeader);
                saveUserSync(pigLeaderUUID, "pigClub", pigLeader.getLeaderPw(), "pig@leader.club", Role.LEADER);

                // 고양이동아리
                Club catClub = Club.builder()
                                .clubName("고양이 동아리")
                                .leaderName("고양이짱")
                                .leaderHp("00012341234")
                                .department(Department.SHOW)
                                .clubInsta("https://www.instagram.com/usw1982/")
                                .clubRoomNumber("203")
                                .build();

                clubRepository.save(catClub);

                ClubInfo catInfo = ClubInfo.builder()
                                .club(catClub)
                                .clubInfo("고양이 동아리입니다.")
                                .clubRecruitment("고양이 모집글입니다.")
                                .googleFormUrl("cat_google_url")
                                .recruitmentStatus(RecruitmentStatus.CLOSE)
                                .build();
                clubInfoRepository.save(catInfo);

                UUID catLeaderUUID = UUID.randomUUID();
                Leader catLeader = Leader.builder()
                                .leaderUUID(catLeaderUUID)
                                .leaderAccount("catClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(catClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(catLeader);
                saveUserSync(catLeaderUUID, "catClub", catLeader.getLeaderPw(), "cat@leader.club", Role.LEADER);
        }

        private void saveUserSync(UUID uuid, String account, String encodedPw, String email, Role role) {
                User user = User.builder()
                                .userUUID(uuid)
                                .userAccount(account)
                                .userPw(encodedPw)
                                .email(email)
                                .role(role)
                                .build();
                userRepository.save(user);
        }

        private void initAplictFlow() {
                // 1. 테스트 유저 생성 (postman-test)
                UUID testUserUUID = UUID.fromString("45c0ef76-9509-4888-bdc3-9679964006ad");
                User testUser = User.builder()
                                .userUUID(testUserUUID)
                                .userAccount("testuser")
                                .userPw(passwordEncoder.encode("test1234!"))
                                .email("test@example.com")
                                .role(Role.USER)
                                .build();
                userRepository.save(testUser);

                Profile testProfile = Profile.builder()
                                .user(testUser)
                                .userName("홍길동")
                                .studentNumber("20240001")
                                .userHp("01011112222")
                                .major("컴퓨터공학과")
                                .memberType(MemberType.REGULARMEMBER)
                                .profileCreatedAt(LocalDateTime.now())
                                .profileUpdatedAt(LocalDateTime.now())
                                .build();
                profileRepository.save(testProfile);

                // 2. 테스트 동아리 생성
                Club testClub = Club.builder()
                                .clubuuid(UUID.fromString("3b35b596-aa83-4e28-b4dd-174d6f7d32d0"))
                                .clubName("테스트동아리")
                                .leaderName("회장님")
                                .leaderHp("01012345678")
                                .department(Department.VOLUNTEER)
                                .clubRoomNumber("101")
                                .build();
                clubRepository.save(testClub);

                ClubInfo testClubInfo = ClubInfo.builder()
                                .club(testClub)
                                .clubInfo("테스트 동아리입니다.")
                                .clubRecruitment("신입 부원을 모집합니다.")
                                .recruitmentStatus(RecruitmentStatus.OPEN)
                                .build();
                clubInfoRepository.save(testClubInfo);

                // 3. 지원 폼 생성
                ClubForm form = ClubForm.builder()
                                .club(testClub)
                                .description("신입 부원 모집")
                                .createdBy(1L)
                                .build();
                clubFormRepository.save(form);

                // 4. 질문 및 옵션 생성
                FormQuestion q1 = FormQuestion.builder()
                                .type(QuestionType.RADIO)
                                .content("사용 가능한 프로그래밍 언어는?")
                                .build();
                form.addQuestion(q1);
                formQuestionRepository.save(q1);

                FormQuestionOption o1 = FormQuestionOption.builder()
                                .content("Java")
                                .build();
                q1.addOption(o1);
                formQuestionOptionRepository.save(o1);

                FormQuestionOption o2 = FormQuestionOption.builder()
                                .content("Python")
                                .build();
                q1.addOption(o2);
                formQuestionOptionRepository.save(o2);

                // 5. 지원서 생성
                Aplict aplict = Aplict.builder()
                                .aplictUUID(UUID.fromString("d9942a31-c748-4bc2-9c69-80d5414604c8"))
                                .profile(testProfile)
                                .club(testClub)
                                .submittedAt(LocalDateTime.now())
                                .publicStatus(AplictStatus.WAIT)
                                .privateStatus(AplictStatus.WAIT)
                                .build();
                aplictRepository.save(aplict);

                // 6. 답변 생성
                AplictAnswer answer = AplictAnswer.builder()
                                .aplict(aplict)
                                .formQuestion(q1)
                                .option(o1) // Java 선택
                                .answerText(null)
                                .build();
                aplictAnswerRepository.save(answer);
        }

}
