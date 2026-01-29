package com.USWCicrcleLink.server.global.data;

import com.USWCicrcleLink.server.admin.domain.Admin;
import com.USWCicrcleLink.server.admin.repository.AdminRepository;
import com.USWCicrcleLink.server.club.application.domain.Aplict;
import com.USWCicrcleLink.server.club.application.domain.AplictStatus;
import com.USWCicrcleLink.server.club.application.repository.AplictRepository;
import com.USWCicrcleLink.server.club.domain.*;
import com.USWCicrcleLink.server.club.repository.*;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroPhotoRepository;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
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
        private final ClubIntroRepository clubIntroRepository;
        private final ClubIntroPhotoRepository clubIntroPhotoRepository;
        private final LeaderRepository leaderRepository;
        private final PasswordEncoder passwordEncoder;
        private final ClubMainPhotoRepository clubMainPhotoRepository;
        private final ClubHashtagRepository clubHashtagRepository;
        private final ClubCategoryRepository clubCategoryRepository;
        private final ClubCategoryMappingRepository clubCategoryMappingRepository;

        @PostConstruct
        public void init() {
                initAdmin();
                initUser1();
                initUser2();
                initUser3();
                initclub();
        }

        // 관리자 동연회 데이터
        public void initAdmin() {
                // 동아리 연합회 관리자 계정
                Admin clubUnion = Admin.builder()
                                .adminUUID(UUID.randomUUID())
                                .adminAccount("clubUnion")
                                .adminPw(passwordEncoder.encode("hpsEetcTf7ymgy6!")) // 비밀번호 암호화
                                .adminName("동아리 연합회")
                                .role(Role.ADMIN)
                                .build();

                // 개발자 계정
                Admin developer = Admin.builder()
                                .adminUUID(UUID.randomUUID())
                                .adminAccount("developer")
                                .adminPw(passwordEncoder.encode("5MYcg7Cuvrh50fS")) // 비밀번호 암호화
                                .adminName("운영자")
                                .role(Role.ADMIN)
                                .build();

                // 데이터 저장
                adminRepository.save(clubUnion);
                adminRepository.save(developer);
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

                Leader leader = Leader.builder()
                                .leaderAccount("flag1")
                                .leaderPw(passwordEncoder.encode("a123456!"))
                                .club(flagClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(leader);

                ClubIntro clubIntro = ClubIntro.builder()
                                .club(flagClub)
                                .clubIntro("플래그입니다.")
                                .clubRecruitment("플래그 모집글입니다.")
                                .googleFormUrl("flag_google_url")
                                .recruitmentStatus(RecruitmentStatus.OPEN)
                                .build();
                clubIntroRepository.save(clubIntro);

                Leader leader1 = Leader.builder()
                                .leaderAccount("badmintonClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(badmintonClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(leader1);

                ClubIntro clubIntro1 = ClubIntro.builder()
                                .club(badmintonClub)
                                .clubIntro("배드민턴 동아리입니다.")
                                .clubRecruitment("배드민턴 모집글입니다.")
                                .googleFormUrl("badmintonClub_google_url")
                                .recruitmentStatus(RecruitmentStatus.OPEN)
                                .build();
                clubIntroRepository.save(clubIntro1);

                ClubMainPhoto badmintonMainPhoto = ClubMainPhoto.builder()
                                .club(badmintonClub)
                                .clubMainPhotoName("")
                                .clubMainPhotoS3Key("")
                                .build();
                clubMainPhotoRepository.save(badmintonMainPhoto);

                for (int i = 1; i <= 5; i++) {
                        ClubIntroPhoto badmintonIntroPhoto = ClubIntroPhoto.builder()
                                        .clubIntro(clubIntro1)
                                        .clubIntroPhotoName("")
                                        .clubIntroPhotoS3Key("")
                                        .order(i)
                                        .build();
                        clubIntroPhotoRepository.save(badmintonIntroPhoto);
                }

                Leader leader2 = Leader.builder()
                                .leaderAccount("volunteerClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(volunteerClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(leader2);

                ClubIntro clubIntro2 = ClubIntro.builder()
                                .club(volunteerClub)
                                .clubIntro("봉사동아리입니다.")
                                .clubRecruitment("봉사 모집글입니다.")
                                .googleFormUrl("volunteerClub_google_url")
                                .recruitmentStatus(RecruitmentStatus.CLOSE)
                                .build();
                clubIntroRepository.save(clubIntro2);

                ClubMainPhoto volunteerMainPhoto = ClubMainPhoto.builder()
                                .club(volunteerClub)
                                .clubMainPhotoName("")
                                .clubMainPhotoS3Key("")
                                .build();
                clubMainPhotoRepository.save(volunteerMainPhoto);

                for (int i = 1; i <= 5; i++) {
                        ClubIntroPhoto volunteerIntroPhoto = ClubIntroPhoto.builder()
                                        .clubIntro(clubIntro2)
                                        .clubIntroPhotoName("")
                                        .clubIntroPhotoS3Key("")
                                        .order(i)
                                        .build();
                        clubIntroPhotoRepository.save(volunteerIntroPhoto);
                }

                for (int i = 1; i <= 5; i++) {
                        ClubIntroPhoto clubIntroPhoto = ClubIntroPhoto.builder()
                                        .clubIntro(clubIntro)
                                        .clubIntroPhotoName("")
                                        .clubIntroPhotoS3Key("")
                                        .order(i)
                                        .build();
                        clubIntroPhotoRepository.save(clubIntroPhoto);
                }

                // FLAG 동아리 지원자
                Aplict aplict1 = Aplict.builder()
                                .profile(profile1)
                                .club(flagClub)
                                .submittedAt(LocalDateTime.now())
                                .build();
                aplictRepository.save(aplict1);

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
                                .checked(true)
                                .aplictStatus(AplictStatus.FAIL)
                                .build();
                aplictRepository.save(aplict3);

                Aplict aplict4 = Aplict.builder()
                                .profile(profile4)
                                .club(flagClub)
                                .submittedAt(LocalDateTime.now())
                                .checked(true)
                                .aplictStatus(AplictStatus.FAIL)
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
                                .aplictStatus(AplictStatus.PASS)
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
                                .aplictStatus(AplictStatus.FAIL)
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

                Leader allaboutLeader = Leader.builder()
                                .leaderAccount("allaboutClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(allaboutClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(allaboutLeader);

                ClubIntro allaboutIntro = ClubIntro.builder()
                                .club(allaboutClub)
                                .clubIntro("올어바웃 동아리입니다.")
                                .clubRecruitment("올어바웃 모집글입니다.")
                                .googleFormUrl("allaboutClub_google_url")
                                .recruitmentStatus(RecruitmentStatus.OPEN)
                                .build();
                clubIntroRepository.save(allaboutIntro);

                ClubMembers clubMembers = ClubMembers.builder()
                                .club(allaboutClub)
                                .profile(profile)
                                .build();

                clubMembersRepository.save(clubMembers);

                Aplict aplict = Aplict.builder()
                                .profile(profile)
                                .club(allaboutClub)
                                .submittedAt(LocalDateTime.now())
                                .aplictStatus(AplictStatus.PASS)
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

                Leader gullisaeLeader = Leader.builder()
                                .leaderAccount("gullisaeClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(gullisaeClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(gullisaeLeader);

                ClubMembers clubMembers = ClubMembers.builder()
                                .club(gullisaeClub)
                                .profile(profile)
                                .build();

                clubMembersRepository.save(clubMembers);

                ClubIntro gullisaeIntro = ClubIntro.builder()
                                .club(gullisaeClub)
                                .clubIntro("굴리세 동아리입니다.")
                                .clubRecruitment("굴리세 모집글입니다.")
                                .googleFormUrl("gullisaeClub_google_url")
                                .recruitmentStatus(RecruitmentStatus.OPEN)
                                .build();
                clubIntroRepository.save(gullisaeIntro);

                Aplict aplict = Aplict.builder()
                                .profile(profile)
                                .club(gullisaeClub)
                                .submittedAt(LocalDateTime.now())
                                .aplictStatus(AplictStatus.PASS)
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

                ClubIntro tennisIntro = ClubIntro.builder()
                                .club(tennisclub)
                                .clubIntro("테니스 동아리입니다.")
                                .clubRecruitment("테니스 모집글입니다.")
                                .googleFormUrl("tennisClub_google_url")
                                .recruitmentStatus(RecruitmentStatus.CLOSE)
                                .build();
                clubIntroRepository.save(tennisIntro);

                Leader tennisLeader = Leader.builder()
                                .leaderAccount("tennisClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(tennisclub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(tennisLeader);

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

                ClubIntro basketballIntro = ClubIntro.builder()
                                .club(basketballClub)
                                .clubIntro("농구 동아리입니다.")
                                .clubRecruitment("농구 모집글입니다.")
                                .googleFormUrl("basketball_google_url")
                                .recruitmentStatus(RecruitmentStatus.CLOSE)
                                .build();
                clubIntroRepository.save(basketballIntro);

                Leader basketballLeader = Leader.builder()
                                .leaderAccount("basketballClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(basketballClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(basketballLeader);

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

                ClubIntro argIntro = ClubIntro.builder()
                                .club(argClub)
                                .clubIntro("토론 동아리입니다.")
                                .clubRecruitment("토론 모집글입니다.")
                                .googleFormUrl("arg_google_url")
                                .recruitmentStatus(RecruitmentStatus.CLOSE)
                                .build();
                clubIntroRepository.save(argIntro);

                Leader argLeader = Leader.builder()
                                .leaderAccount("argClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(argClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(argLeader);

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

                ClubIntro hamsterIntro = ClubIntro.builder()
                                .club(hamsterClub)
                                .clubIntro("햄스터 동아리입니다.")
                                .clubRecruitment("햄스터 모집글입니다.")
                                .googleFormUrl("hamster_google_url")
                                .recruitmentStatus(RecruitmentStatus.OPEN)
                                .build();
                clubIntroRepository.save(hamsterIntro);

                Leader hamsterLeader = Leader.builder()
                                .leaderAccount("hamsterClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(hamsterClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(hamsterLeader);

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

                ClubIntro hsunmoonIntro = ClubIntro.builder()
                                .club(sunmoonClub)
                                .clubIntro("해달 동아리입니다.")
                                .clubRecruitment("해달 모집글입니다.")
                                .googleFormUrl("sunmoon_google_url")
                                .recruitmentStatus(RecruitmentStatus.OPEN)
                                .build();
                clubIntroRepository.save(hsunmoonIntro);

                Leader sunmoonLeader = Leader.builder()
                                .leaderAccount("sunmoonClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(sunmoonClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(sunmoonLeader);

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

                ClubIntro pigIntro = ClubIntro.builder()
                                .club(pigClub)
                                .clubIntro("돼지 동아리입니다.")
                                .clubRecruitment("돼지 모집글입니다.")
                                .googleFormUrl("pig_google_url")
                                .recruitmentStatus(RecruitmentStatus.OPEN)
                                .build();
                clubIntroRepository.save(pigIntro);

                Leader pigLeader = Leader.builder()
                                .leaderAccount("pigClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(pigClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(pigLeader);

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

                ClubIntro catIntro = ClubIntro.builder()
                                .club(catClub)
                                .clubIntro("고양이 동아리입니다.")
                                .clubRecruitment("고양이 모집글입니다.")
                                .googleFormUrl("cat_google_url")
                                .recruitmentStatus(RecruitmentStatus.CLOSE)
                                .build();
                clubIntroRepository.save(catIntro);

                Leader catLeader = Leader.builder()
                                .leaderAccount("catClub")
                                .leaderPw(passwordEncoder.encode("12345"))
                                .club(catClub)
                                .role(Role.LEADER)
                                .build();
                leaderRepository.save(catLeader);
        }

}
