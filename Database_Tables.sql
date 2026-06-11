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


