-- Create all tables for Content Service
-- This migration creates category and novel tables with all necessary indexes and constraints

-- =============================================
-- 1. CREATE CATEGORY TABLE
-- =============================================

CREATE TABLE IF NOT EXISTS category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default categories
INSERT INTO category (name, description) VALUES 
('Fantasy', 'Fantasy novels with magical elements'),
('Romance', 'Romance novels focusing on love stories'),
('Mystery', 'Mystery and detective novels'),
('Sci-Fi', 'Science fiction novels'),
('Horror', 'Horror and thriller novels'),
('Adventure', 'Adventure and action novels'),
('Drama', 'Drama and literary fiction'),
('Comedy', 'Comedy and humorous novels')
ON CONFLICT (name) DO NOTHING;

-- Add comments for documentation
COMMENT ON TABLE category IS 'Novel categories and genres';

-- =============================================
-- 2. CREATE NOVEL TABLE
-- =============================================

CREATE TABLE IF NOT EXISTS novel (
    id SERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    author_id UUID NOT NULL,
    author_name VARCHAR(100),
    category_id INTEGER NOT NULL,
    synopsis TEXT,
    cover_img_url TEXT,
    status INTEGER NOT NULL DEFAULT 0,
    is_completed BOOLEAN DEFAULT FALSE,
    chapter_cnt INTEGER DEFAULT 0,
    word_cnt BIGINT DEFAULT 0,
    avg_rating REAL DEFAULT 0.0,
    review_cnt INTEGER DEFAULT 0,
    view_cnt BIGINT DEFAULT 0,
    vote_cnt INTEGER DEFAULT 0,
    yuan_cnt REAL DEFAULT 0.0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    publish_time TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_novel_category FOREIGN KEY (category_id) REFERENCES category(id),
    
    -- Indexes for performance
    CONSTRAINT unique_novel_uuid UNIQUE (uuid)
);

-- =============================================
-- 3. CREATE INDEXES FOR PERFORMANCE
-- =============================================

-- Novel table indexes
CREATE INDEX IF NOT EXISTS idx_novel_author_id ON novel(author_id);
CREATE INDEX IF NOT EXISTS idx_novel_category_id ON novel(category_id);
CREATE INDEX IF NOT EXISTS idx_novel_status ON novel(status);
CREATE INDEX IF NOT EXISTS idx_novel_uuid ON novel(uuid);
CREATE INDEX IF NOT EXISTS idx_novel_title ON novel(title);
CREATE INDEX IF NOT EXISTS idx_novel_create_time ON novel(create_time);
CREATE INDEX IF NOT EXISTS idx_novel_update_time ON novel(update_time);
CREATE INDEX IF NOT EXISTS idx_novel_publish_time ON novel(publish_time);
CREATE INDEX IF NOT EXISTS idx_novel_view_cnt ON novel(view_cnt);
CREATE INDEX IF NOT EXISTS idx_novel_vote_cnt ON novel(vote_cnt);
CREATE INDEX IF NOT EXISTS idx_novel_avg_rating ON novel(avg_rating);

-- Create composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_novel_status_category ON novel(status, category_id);
CREATE INDEX IF NOT EXISTS idx_novel_author_status ON novel(author_id, status);
CREATE INDEX IF NOT EXISTS idx_novel_published_ranking ON novel(status, view_cnt DESC) WHERE status = 2;

-- =============================================
-- 4. ADD COMMENTS FOR DOCUMENTATION
-- =============================================

COMMENT ON TABLE novel IS 'Stores novel information including metadata, statistics, and publishing status';
COMMENT ON COLUMN novel.id IS 'Primary key - auto-incrementing integer';
COMMENT ON COLUMN novel.uuid IS 'Unique identifier for external references';
COMMENT ON COLUMN novel.title IS 'Title of the novel';
COMMENT ON COLUMN novel.author_id IS 'Author UUID - managed by User Service';
COMMENT ON COLUMN novel.author_name IS 'Cached author name for performance';
COMMENT ON COLUMN novel.category_id IS 'Foreign key to category table - genre/category of the novel';
COMMENT ON COLUMN novel.synopsis IS 'Short description/summary of the novel';
COMMENT ON COLUMN novel.cover_img_url IS 'URL to the cover image';
COMMENT ON COLUMN novel.status IS 'Publishing status: 0=DRAFT, 1=UNDER_REVIEW, 2=PUBLISHED, 3=HIDDEN, 4=ARCHIVED';
COMMENT ON COLUMN novel.is_completed IS 'Whether the novel is completed by the author';
COMMENT ON COLUMN novel.chapter_cnt IS 'Total number of chapters';
COMMENT ON COLUMN novel.word_cnt IS 'Total word count across all chapters';
COMMENT ON COLUMN novel.avg_rating IS 'Average rating from reviews';
COMMENT ON COLUMN novel.review_cnt IS 'Total number of reviews';
COMMENT ON COLUMN novel.view_cnt IS 'Total view count';
COMMENT ON COLUMN novel.vote_cnt IS 'Total vote count';
COMMENT ON COLUMN novel.yuan_cnt IS 'Total yuan (currency) earned';
COMMENT ON COLUMN novel.create_time IS 'When the novel was created';
COMMENT ON COLUMN novel.update_time IS 'When the novel was last updated';
COMMENT ON COLUMN novel.publish_time IS 'When the novel was published (status changed to PUBLISHED)';
