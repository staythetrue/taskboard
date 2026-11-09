-- 表结构由应用启动时幂等执行,不依赖任何 ORM 自动建表。
-- 生产环境建议换成 Flyway / Liquibase 做版本化管理。

CREATE TABLE IF NOT EXISTS categories (
  id         BIGINT      NOT NULL AUTO_INCREMENT,
  name       VARCHAR(50) NOT NULL,
  sort_no    INT         NOT NULL DEFAULT 0,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS tasks (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  title       VARCHAR(200)  NOT NULL,
  description VARCHAR(1000) DEFAULT NULL,
  status      VARCHAR(20)   NOT NULL DEFAULT 'TODO',
  priority    INT           NOT NULL DEFAULT 0,
  category_id BIGINT        DEFAULT NULL,
  created_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_status (status),
  KEY idx_created_at (created_at),
  KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 老库补列:MySQL 不支持 ADD COLUMN IF NOT EXISTS,用 information_schema 判断后动态执行
SET @col_exists = (
  SELECT COUNT(*) FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'tasks' AND column_name = 'category_id'
);
SET @ddl = IF(@col_exists = 0,
  'ALTER TABLE tasks ADD COLUMN category_id BIGINT DEFAULT NULL',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 初始分类(靠 uk_name 保证幂等)
INSERT IGNORE INTO categories (name, sort_no) VALUES
  ('工作', 1),
  ('学习', 2),
  ('生活', 3);

-- 没有分类的历史任务统一归到"学习"
UPDATE tasks SET category_id = (SELECT id FROM categories WHERE name = '学习')
WHERE category_id IS NULL;
