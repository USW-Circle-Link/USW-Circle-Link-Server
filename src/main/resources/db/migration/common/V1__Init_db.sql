CREATE TABLE admin_table (
                             admin_id BIGINT NOT NULL AUTO_INCREMENT,
                             admin_uuid BINARY(16) NOT NULL,
                             admin_account VARCHAR(20) NOT NULL,
                             admin_name VARCHAR(30) NOT NULL,
                             admin_pw VARCHAR(255) NOT NULL,
                             role ENUM('ADMIN','LEADER','USER') NOT NULL,
                             PRIMARY KEY (admin_id),
                             UNIQUE (admin_uuid),
                             UNIQUE (admin_account)
);

CREATE TABLE aplict_table (
                              aplict_checked BIT(1) DEFAULT NULL,
                              aplict_delete_date DATETIME(6) DEFAULT NULL,
                              aplict_id BIGINT NOT NULL AUTO_INCREMENT,
                              aplict_submitted_at DATETIME(6) NOT NULL,
                              club_id BIGINT NOT NULL,
                              profile_id BIGINT NOT NULL,
                              aplict_uuid BINARY(16) NOT NULL,
                              aplict_status ENUM('FAIL','PASS','WAIT') NOT NULL,
                              PRIMARY KEY (aplict_id),
                              UNIQUE (aplict_uuid)
);

CREATE TABLE auth_token_table (
                                  auth_token_id BIGINT NOT NULL AUTO_INCREMENT,
                                  user_uuid BINARY(16) DEFAULT NULL,
                                  auth_code VARCHAR(255) NOT NULL,
                                  PRIMARY KEY (auth_token_id),
                                  UNIQUE (user_uuid)
);

CREATE TABLE club_category_mapping_table (
                                             club_category_id BIGINT NOT NULL,
                                             club_category_mapping_id BIGINT NOT NULL AUTO_INCREMENT,
                                             club_id BIGINT NOT NULL,
                                             PRIMARY KEY (club_category_mapping_id)
);

CREATE TABLE club_category_table (
                                     club_category_id BIGINT NOT NULL AUTO_INCREMENT,
                                     club_category_uuid BINARY(16) NOT NULL,
                                     club_category_name VARCHAR(20) NOT NULL,
                                     PRIMARY KEY (club_category_id),
                                     UNIQUE (club_category_uuid)
);

CREATE TABLE club_hashtag_table (
                                    club_hashtag_id BIGINT NOT NULL AUTO_INCREMENT,
                                    club_id BIGINT NOT NULL,
                                    club_hashtag VARCHAR(10) NOT NULL,
                                    PRIMARY KEY (club_hashtag_id)
);

CREATE TABLE club_intro_photo_table (
                                        photo_order INT NOT NULL,
                                        club_intro_id BIGINT NOT NULL,
                                        club_intro_photo_id BIGINT NOT NULL AUTO_INCREMENT,
                                        club_intro_photo_name VARCHAR(255) DEFAULT NULL,
                                        club_intro_photo_s3key VARCHAR(255) DEFAULT NULL,
                                        PRIMARY KEY (club_intro_photo_id)
);

CREATE TABLE club_intro_table (
                                  club_id BIGINT NOT NULL,
                                  club_intro_id BIGINT NOT NULL AUTO_INCREMENT,
                                  club_intro VARCHAR(3000) DEFAULT NULL,
                                  club_recruitment VARCHAR(3000) DEFAULT NULL,
                                  google_form_url VARCHAR(255) DEFAULT NULL,
                                  club_intro_recruitment_status ENUM('CLOSE','OPEN') NOT NULL,
                                  PRIMARY KEY (club_intro_id),
                                  UNIQUE (club_id)
);

CREATE TABLE club_main_photo_table (
                                       club_id BIGINT NOT NULL,
                                       club_main_photo_id BIGINT NOT NULL AUTO_INCREMENT,
                                       club_main_photo_name VARCHAR(255) DEFAULT NULL,
                                       club_main_photo_s3key VARCHAR(255) DEFAULT NULL,
                                       PRIMARY KEY (club_main_photo_id),
                                       UNIQUE (club_id)
);

CREATE TABLE club_member_accountstatus_table (
                                                 club_id BIGINT NOT NULL,
                                                 club_member_accountstatus_id BIGINT NOT NULL AUTO_INCREMENT,
                                                 clubmembertemp_id BIGINT NOT NULL,
                                                 clubmember_account_status_uuid BINARY(16) NOT NULL,
                                                 PRIMARY KEY (club_member_accountstatus_id),
                                                 UNIQUE (clubmember_account_status_uuid)
);

CREATE TABLE club_members_table (
                                    club_id BIGINT NOT NULL,
                                    club_member_id BIGINT NOT NULL AUTO_INCREMENT,
                                    profile_id BIGINT NOT NULL,
                                    club_member_uuid BINARY(16) NOT NULL,
                                    PRIMARY KEY (club_member_id),
                                    UNIQUE (club_member_uuid)
);

CREATE TABLE club_membertemp_table (
                                       club_request_count INT NOT NULL,
                                       total_club_request INT NOT NULL,
                                       club_member_temp_expiry_date DATETIME(6) NOT NULL,
                                       club_membertemp_id BIGINT NOT NULL AUTO_INCREMENT,
                                       profile_temp_student_number VARCHAR(8) NOT NULL,
                                       profile_temp_hp VARCHAR(11) NOT NULL,
                                       profile_temp_account VARCHAR(20) NOT NULL,
                                       profile_temp_major VARCHAR(20) NOT NULL,
                                       profile_temp_email VARCHAR(30) NOT NULL,
                                       profile_temp_name VARCHAR(30) NOT NULL,
                                       profile_temp_pw VARCHAR(255) NOT NULL,
                                       PRIMARY KEY (club_membertemp_id)
);

CREATE TABLE club_table (
                            club_room_number VARCHAR(4) NOT NULL,
                            club_id BIGINT NOT NULL AUTO_INCREMENT,
                            club_name VARCHAR(10) NOT NULL,
                            leader_hp VARCHAR(11) NOT NULL,
                            club_uuid BINARY(16) NOT NULL,
                            leader_name VARCHAR(30) NOT NULL,
                            club_insta VARCHAR(255) DEFAULT NULL,
                            department ENUM('ACADEMIC','ART','RELIGION','SHOW','SPORT','VOLUNTEER') NOT NULL,
                            PRIMARY KEY (club_id),
                            UNIQUE (club_name),
                            UNIQUE (club_uuid)
);

CREATE TABLE email_token_table (
                                   is_verified BIT(1) NOT NULL,
                                   email_token_id BIGINT NOT NULL AUTO_INCREMENT,
                                   expiration_time DATETIME(6) NOT NULL,
                                   email_token_uuid BINARY(16) NOT NULL,
                                   signup_uuid BINARY(16) DEFAULT NULL,
                                   email VARCHAR(30) NOT NULL,
                                   PRIMARY KEY (email_token_id),
                                   UNIQUE (email_token_uuid),
                                   UNIQUE (email),
                                   UNIQUE (signup_uuid)
);

CREATE TABLE floor_photo_table (
                                   floor_photo_id BIGINT NOT NULL AUTO_INCREMENT,
                                   floor_photo_name VARCHAR(255) NOT NULL,
                                   floor_photo_s3_key VARCHAR(255) NOT NULL,
                                   floor_photo_floor ENUM('B1','F1','F2') NOT NULL,
                                   PRIMARY KEY (floor_photo_id)
);

CREATE TABLE leader_table (
                              is_agreed_terms BIT(1) NOT NULL,
                              club_id BIGINT DEFAULT NULL,
                              leader_id BIGINT NOT NULL AUTO_INCREMENT,
                              leader_uuid BINARY(16) NOT NULL,
                              leader_account VARCHAR(255) NOT NULL,
                              leader_pw VARCHAR(255) NOT NULL,
                              role ENUM('ADMIN','LEADER','USER') NOT NULL,
                              PRIMARY KEY (leader_id),
                              UNIQUE (leader_account),
                              UNIQUE (club_id)
);

CREATE TABLE notice_photo_table (
                                    photo_order INT NOT NULL,
                                    notice_id BIGINT NOT NULL,
                                    notice_photo_id BIGINT NOT NULL AUTO_INCREMENT,
                                    notice_photo_name VARCHAR(255) DEFAULT NULL,
                                    notice_photo_s3key VARCHAR(255) DEFAULT NULL,
                                    PRIMARY KEY (notice_photo_id)
);

CREATE TABLE notice_table (
                              admin_id BIGINT NOT NULL,
                              notice_created_at DATETIME(6) NOT NULL,
                              notice_id BIGINT NOT NULL AUTO_INCREMENT,
                              notice_uuid BINARY(16) NOT NULL,
                              notice_title VARCHAR(200) NOT NULL,
                              notice_content VARCHAR(3000) NOT NULL,
                              PRIMARY KEY (notice_id),
                              UNIQUE (notice_uuid)
);

CREATE TABLE profile_table (
                               fcm_token_updated_at DATETIME(6) DEFAULT NULL,
                               profile_created_at DATETIME(6) NOT NULL,
                               profile_id BIGINT NOT NULL AUTO_INCREMENT,
                               profile_updated_at DATETIME(6) NOT NULL,
                               student_number VARCHAR(8) NOT NULL,
                               user_id BIGINT DEFAULT NULL,
                               user_hp VARCHAR(11) NOT NULL,
                               major VARCHAR(20) NOT NULL,
                               user_name VARCHAR(30) NOT NULL,
                               fcm_token VARCHAR(255) DEFAULT NULL,
                               member_type ENUM('NONMEMBER','REGULARMEMBER') NOT NULL,
                               PRIMARY KEY (profile_id),
                               UNIQUE (user_id)
);

CREATE TABLE user_table (
                            user_created_at DATETIME(6) NOT NULL,
                            user_id BIGINT NOT NULL AUTO_INCREMENT,
                            user_updated_at DATETIME(6) NOT NULL,
                            uuid BINARY(16) NOT NULL,
                            user_account VARCHAR(20) NOT NULL,
                            email VARCHAR(30) NOT NULL,
                            user_pw VARCHAR(255) NOT NULL,
                            role ENUM('ADMIN','LEADER','USER') NOT NULL,
                            PRIMARY KEY (user_id),
                            UNIQUE (uuid),
                            UNIQUE (user_account),
                            UNIQUE (email)
);

CREATE TABLE withdrawal_token (
                                  withdrawal_id BIGINT NOT NULL AUTO_INCREMENT,
                                  user_uuid BINARY(16) DEFAULT NULL,
                                  withdrawal_code VARCHAR(255) NOT NULL,
                                  PRIMARY KEY (withdrawal_id),
                                  UNIQUE (user_uuid)
);

# 인덱스
CREATE INDEX idx_aplict_club_id ON aplict_table(club_id);
CREATE INDEX idx_aplict_profile_id ON aplict_table(profile_id);

CREATE INDEX idx_ccm_club_id ON club_category_mapping_table(club_id);
CREATE INDEX idx_ccm_category_id ON club_category_mapping_table(club_category_id);

CREATE INDEX idx_hashtag_club_id ON club_hashtag_table(club_id);

CREATE INDEX idx_intro_photo_intro_id ON club_intro_photo_table(club_intro_id);

CREATE INDEX idx_accountstatus_club_id ON club_member_accountstatus_table(club_id);
CREATE INDEX idx_accountstatus_membertemp_id ON club_member_accountstatus_table(clubmembertemp_id);

CREATE INDEX idx_members_club_id ON club_members_table(club_id);
CREATE INDEX idx_members_profile_id ON club_members_table(profile_id);

CREATE INDEX idx_notice_photo_notice_id ON notice_photo_table(notice_id);
CREATE INDEX idx_notice_admin_id ON notice_table(admin_id);

# 외래키
ALTER TABLE aplict_table ADD CONSTRAINT fk_aplict_club_id FOREIGN KEY (club_id) REFERENCES club_table(club_id);
ALTER TABLE aplict_table ADD CONSTRAINT fk_aplict_profile_id FOREIGN KEY (profile_id) REFERENCES profile_table(profile_id);

ALTER TABLE auth_token_table ADD CONSTRAINT fk_auth_token_user_uuid FOREIGN KEY (user_uuid) REFERENCES user_table(uuid);

ALTER TABLE club_category_mapping_table ADD CONSTRAINT fk_ccm_club_id FOREIGN KEY (club_id) REFERENCES club_table(club_id);
ALTER TABLE club_category_mapping_table ADD CONSTRAINT fk_ccm_category_id FOREIGN KEY (club_category_id) REFERENCES club_category_table(club_category_id);

ALTER TABLE club_hashtag_table ADD CONSTRAINT fk_hashtag_club_id FOREIGN KEY (club_id) REFERENCES club_table(club_id);

ALTER TABLE club_intro_photo_table ADD CONSTRAINT fk_intro_photo_intro_id FOREIGN KEY (club_intro_id) REFERENCES club_intro_table(club_intro_id);
ALTER TABLE club_intro_table ADD CONSTRAINT fk_intro_club_id FOREIGN KEY (club_id) REFERENCES club_table(club_id);

ALTER TABLE club_main_photo_table ADD CONSTRAINT fk_main_photo_club_id FOREIGN KEY (club_id) REFERENCES club_table(club_id);

ALTER TABLE club_member_accountstatus_table ADD CONSTRAINT fk_accountstatus_club_id FOREIGN KEY (club_id) REFERENCES club_table(club_id);
ALTER TABLE club_member_accountstatus_table ADD CONSTRAINT fk_accountstatus_membertemp_id FOREIGN KEY (clubmembertemp_id) REFERENCES club_membertemp_table(club_membertemp_id);

ALTER TABLE club_members_table ADD CONSTRAINT fk_members_club_id FOREIGN KEY (club_id) REFERENCES club_table(club_id);
ALTER TABLE club_members_table ADD CONSTRAINT fk_members_profile_id FOREIGN KEY (profile_id) REFERENCES profile_table(profile_id);

ALTER TABLE leader_table ADD CONSTRAINT fk_leader_club_id FOREIGN KEY (club_id) REFERENCES club_table(club_id);

ALTER TABLE notice_photo_table ADD CONSTRAINT fk_notice_photo_notice_id FOREIGN KEY (notice_id) REFERENCES notice_table(notice_id);

ALTER TABLE notice_table ADD CONSTRAINT fk_notice_admin_id FOREIGN KEY (admin_id) REFERENCES admin_table(admin_id);

ALTER TABLE profile_table ADD CONSTRAINT fk_profile_user_id FOREIGN KEY (user_id) REFERENCES user_table(user_id);

ALTER TABLE withdrawal_token ADD CONSTRAINT fk_withdrawal_user_uuid FOREIGN KEY (user_uuid) REFERENCES user_table(uuid);
