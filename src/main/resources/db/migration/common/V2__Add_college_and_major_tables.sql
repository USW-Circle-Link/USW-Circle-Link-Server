-- college 테이블
CREATE TABLE college_table (
                               college_id BIGINT NOT NULL AUTO_INCREMENT,
                               college_name VARCHAR(20) NOT NULL,
                               PRIMARY KEY (college_id)
);

-- major 테이블
CREATE TABLE major_table (
                             major_id BIGINT NOT NULL AUTO_INCREMENT,
                             major_name VARCHAR(20) NOT NULL,
                             college_id BIGINT NOT NULL,
                             PRIMARY KEY (major_id)
);

-- 인덱스
CREATE INDEX idx_major_college_id ON major_table(college_id);

-- 외래키
ALTER TABLE major_table
    ADD CONSTRAINT fk_major_college_id
        FOREIGN KEY (college_id)
            REFERENCES college_table(college_id);