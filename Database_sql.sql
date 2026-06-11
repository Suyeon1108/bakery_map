CREATE DATABASE IF NOT EXISTS bread_DB
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE bread_DB;
 
CREATE USER IF NOT EXISTS 'bakery_user'@'localhost' IDENTIFIED BY 'your_password_here';
GRANT SELECT, INSERT, UPDATE, DELETE ON bread_DB.* TO 'bakery_user'@'localhost';
FLUSH PRIVILEGES;


-- =============================================
-- BAKERY INFO
-- =============================================
CREATE TABLE IF NOT EXISTS bakery_info (
	id			INT				AUTO_INCREMENT PRIMARY KEY,
	name		VARCHAR(100)	NOT NULL,
	address		VARCHAR(255)	NOT NULL,
    
	description	TEXT,
	website_url VARCHAR(500),
		
	lng			DECIMAL(10,7)	NOT NULL,
	lat			DECIMAL(10,7)	NOT NULL,
	location	POINT GENERATED ALWAYS AS (ST_SRID(POINT(lng, lat), 4326)) STORED NOT NULL SRID 4326
					COMMENT 'lng/lat에서 자동 생성. SRID 4326(WGS84)',

	created_at	TIMESTAMP		NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at	TIMESTAMP		NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

	UNIQUE KEY uq_bakery_name_address (name, address) COMMENT '이름과 주소 중복시 저장 안됨.',
	SPATIAL INDEX idx_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS bakery_score (
	id			INT				AUTO_INCREMENT PRIMARY KEY,
	bakery_id	INT				NOT NULL UNIQUE,
			
	ud_score	DECIMAL(5,2)	NOT NULL DEFAULT 0 CHECK (ud_score  BETWEEN 0 AND 100),
	esg_score	DECIMAL(5,2)	NOT NULL DEFAULT 0 CHECK (esg_score BETWEEN 0 AND 100),

	created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

	INDEX idx_ud_score  (ud_score),
	INDEX idx_esg_score (esg_score),
	FOREIGN KEY (bakery_id) REFERENCES bakery_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS bakery_flag (
	id				INT		AUTO_INCREMENT PRIMARY KEY,
	bakery_id		INT		NOT NULL UNIQUE,

	wheelchair		BOOL	NOT NULL DEFAULT FALSE,
	ramp			BOOL	NOT NULL DEFAULT FALSE,
	barrier_free	BOOL	NOT NULL DEFAULT FALSE,
	disabled		BOOL	NOT NULL DEFAULT FALSE,
	braille			BOOL	NOT NULL DEFAULT FALSE,
	elevator		BOOL	NOT NULL DEFAULT FALSE,
	stroller		BOOL	NOT NULL DEFAULT FALSE,
	nursing_room	BOOL	NOT NULL DEFAULT FALSE,
	kids_zone		BOOL	NOT NULL DEFAULT FALSE,
	parking			BOOL	NOT NULL DEFAULT FALSE,
	restroom		BOOL	NOT NULL DEFAULT FALSE,
	seating			BOOL	NOT NULL DEFAULT FALSE,

	FOREIGN KEY (bakery_id) REFERENCES bakery_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ============================================
-- COURSE INFO
-- =============================================
CREATE TABLE IF NOT EXISTS course_info (
	id			INT				AUTO_INCREMENT PRIMARY KEY,
	name		VARCHAR(100)	NOT NULL,
	description	TEXT, 
    
	is_active	BOOL NOT NULL DEFAULT TRUE,
	is_public	BOOL NOT NULL DEFAULT TRUE,
    
	created_at	TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	updated_at	TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS course_bakery (
	id			INT		AUTO_INCREMENT PRIMARY KEY,
	course_id	INT		NOT NULL,
	bakery_id	INT		NOT NULL,
	visit_no	INT		NOT NULL,

	UNIQUE KEY uq_course_seq    (course_id, visit_no),
	UNIQUE KEY uq_course_bakery (course_id, bakery_id),
    
    INDEX idx_course_bakery_bakery_id (bakery_id),
    
	FOREIGN KEY (course_id) REFERENCES course_info(id) ON DELETE CASCADE,
	FOREIGN KEY (bakery_id) REFERENCES bakery_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;




-- -----------------------------------
--  정보 insert
-- BAKERY
DROP PROCEDURE IF EXISTS sp_insert_bakery;
DELIMITER $$
CREATE PROCEDURE sp_insert_bakery (
	-- bakery info
    IN p_name			VARCHAR(100),
    IN p_address		VARCHAR(255),
    IN p_description	TEXT,
    IN p_website_url	VARCHAR(500),
    IN p_lng			DECIMAL(10,7),
    IN p_lat			DECIMAL(10,7),
    IN p_ud_score		DECIMAL(5,2),
    IN p_esg_score		DECIMAL(5,2),
    -- flag
    IN p_wheelchair		BOOL,
    IN p_ramp			BOOL,
    IN p_barrier_free	BOOL,
    IN p_disabled		BOOL,
    IN p_braille		BOOL,
    IN p_elevator		BOOL,
    IN p_stroller		BOOL,
    IN p_nursing_room	BOOL,
    IN p_kids_zone		BOOL,
    IN p_parking		BOOL,
    IN p_restroom		BOOL,
    IN p_seating		BOOL
)
BEGIN
    DECLARE v_bakery_id INT;
    DECLARE v_dup_count INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    
    DECLARE EXIT HANDLER FOR 1062 

	BEGIN
		ROLLBACK;
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'bakery already exists';
	END;
    
	-- 유효성 검사(공백오류, 유효값처리)
    IF p_name IS NULL OR TRIM(p_name) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'bakery name is required';
    END IF;
    IF p_address IS NULL OR TRIM(p_address) = '' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'bakery address is required';
    END IF;
    IF p_lng IS NULL OR p_lng < -180 OR p_lng > 180 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'invalid longitude';
    END IF;

    IF p_lat IS NULL OR p_lat < -90 OR p_lat > 90 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'invalid latitude';
    END IF;
    IF p_ud_score IS NOT NULL AND (p_ud_score < 0 OR p_ud_score > 100) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'invalid ud_score';
    END IF;

    IF p_esg_score IS NOT NULL AND (p_esg_score < 0 OR p_esg_score > 100) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'invalid esg_score';
    END IF;
 
    SELECT COUNT(*) INTO v_dup_count
    FROM bakery_info
    WHERE name = p_name
      AND address = p_address;

    IF v_dup_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'bakery already exists';
    END IF;
 
    START TRANSACTION;
        INSERT INTO bakery_info (name, description, address, website_url, lng, lat)
			VALUES (p_name, p_description, p_address, p_website_url, p_lng, p_lat);
		-- 생성된 id 저장
        SET v_bakery_id = LAST_INSERT_ID();
        
        -- insert과정
        INSERT INTO bakery_score (bakery_id, ud_score, esg_score)
			VALUES (
				v_bakery_id,
				IFNULL(p_ud_score, 0.00),
				IFNULL(p_esg_score, 0.00)
			);
        INSERT INTO bakery_flag (
            bakery_id,
            wheelchair, ramp, barrier_free, disabled, braille,
            elevator, stroller, nursing_room, kids_zone,
            parking, restroom, seating
        ) VALUES (
            v_bakery_id,
            IFNULL(p_wheelchair, FALSE),
            IFNULL(p_ramp, FALSE),
            IFNULL(p_barrier_free, FALSE),
            IFNULL(p_disabled, FALSE),
            IFNULL(p_braille, FALSE),
            IFNULL(p_elevator, FALSE),
            IFNULL(p_stroller, FALSE),
            IFNULL(p_nursing_room, FALSE),
            IFNULL(p_kids_zone, FALSE),
            IFNULL(p_parking, FALSE),
            IFNULL(p_restroom, FALSE),
            IFNULL(p_seating, FALSE)
        );

    COMMIT;
END$$
DELIMITER ;

-- COURSE
DROP PROCEDURE IF EXISTS sp_insert_course;
DELIMITER $$
CREATE PROCEDURE sp_insert_course (
    IN p_name         VARCHAR(100),
    IN p_description  TEXT, 
    IN p_is_active    BOOL,
    IN p_is_public    BOOL
)
BEGIN  
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END; 
    IF p_name IS NULL OR TRIM(p_name) = '' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'course name is required';
    END IF;

	START TRANSACTION;
    INSERT INTO course_info (
        name, description, is_active, is_public
    ) VALUES (
        p_name,
        p_description, 
        IFNULL(p_is_active, TRUE),
        IFNULL(p_is_public, TRUE)
    );
    COMMIT;
END$$
DELIMITER ;
-- --------------------------------
DROP PROCEDURE IF EXISTS sp_add_bakery_to_course;
DELIMITER $$
CREATE PROCEDURE sp_add_bakery_to_course (
    IN p_course_id   INT,
    IN p_bakery_id   INT,
    IN p_visit_no    INT
)
BEGIN
    DECLARE v_course_cnt INT DEFAULT 0;
    DECLARE v_bakery_cnt INT DEFAULT 0;
    DECLARE v_visit_dup_cnt INT DEFAULT 0;
    DECLARE v_bakery_dup_cnt INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    IF p_course_id IS NULL OR p_course_id <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'invalid course_id';
    END IF;
    IF p_bakery_id IS NULL OR p_bakery_id <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'invalid bakery_id';
    END IF;
    IF p_visit_no IS NULL OR p_visit_no <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'invalid visit_no';
    END IF;
    SELECT COUNT(*) INTO v_course_cnt
    FROM course_info
    WHERE id = p_course_id;
    IF v_course_cnt = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'course not found';
    END IF;
    SELECT COUNT(*) INTO v_bakery_cnt
    FROM bakery_info
    WHERE id = p_bakery_id;
    IF v_bakery_cnt = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'bakery not found';
    END IF;
    START TRANSACTION;
        SELECT COUNT(*) INTO v_visit_dup_cnt
        FROM course_bakery
        WHERE course_id = p_course_id
          AND visit_no = p_visit_no
        FOR UPDATE;
        SELECT COUNT(*) INTO v_bakery_dup_cnt
        FROM course_bakery
        WHERE course_id = p_course_id
          AND bakery_id = p_bakery_id
        FOR UPDATE;
        IF v_visit_dup_cnt > 0 THEN
            ROLLBACK;
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'visit_no already exists in course';
        END IF;
        IF v_bakery_dup_cnt > 0 THEN
            ROLLBACK;
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'bakery already added to course';
        END IF;
        INSERT INTO course_bakery (
            course_id, bakery_id, visit_no
        ) VALUES (
            p_course_id, p_bakery_id, p_visit_no
        );
    COMMIT;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_remove_bakery_from_course;
DELIMITER $$
CREATE PROCEDURE sp_remove_bakery_from_course (
    IN p_course_id   INT,
    IN p_bakery_id   INT
)
BEGIN
    DECLARE v_deleted_rows INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;
    IF p_course_id IS NULL OR p_course_id <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'invalid course_id';
    END IF;
    IF p_bakery_id IS NULL OR p_bakery_id <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'invalid bakery_id';
    END IF;
    START TRANSACTION;
        DELETE FROM course_bakery
        WHERE course_id = p_course_id
          AND bakery_id = p_bakery_id;
        SET v_deleted_rows = ROW_COUNT();
        IF v_deleted_rows = 0 THEN
            ROLLBACK;
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'course bakery mapping not found';
        END IF;
    COMMIT;
END$$
DELIMITER ;