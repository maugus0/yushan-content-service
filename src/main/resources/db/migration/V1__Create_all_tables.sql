-- Create all tables for Content Service
-- This migration creates category and novel tables with all necessary indexes and constraints

-- =============================================
-- 1. CREATE CATEGORY TABLE
-- =============================================

CREATE TABLE IF NOT EXISTS category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    slug VARCHAR(100) UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default categories with all columns
INSERT INTO category (name, description, slug, is_active, create_time, update_time) VALUES 
('Fantasy', 'Fantasy novels with magical elements', 'fantasy', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Romance', 'Romance novels focusing on love stories', 'romance', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Mystery', 'Mystery and detective novels', 'mystery', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sci-Fi', 'Science fiction novels', 'sci-fi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Horror', 'Horror and thriller novels', 'horror', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Adventure', 'Adventure and action novels', 'adventure', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Drama', 'Drama and literary fiction', 'drama', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Comedy', 'Comedy and humorous novels', 'comedy', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;

-- Add comments for documentation
COMMENT ON TABLE category IS 'Novel categories and genres';
COMMENT ON COLUMN category.slug IS 'URL-friendly slug for SEO';
COMMENT ON COLUMN category.is_active IS 'Whether the category is active and visible';
COMMENT ON COLUMN category.create_time IS 'When the category was created';
COMMENT ON COLUMN category.update_time IS 'When the category was last updated';

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

-- Category table indexes
CREATE INDEX IF NOT EXISTS idx_category_slug ON category(slug);
CREATE INDEX IF NOT EXISTS idx_category_is_active ON category(is_active);
CREATE INDEX IF NOT EXISTS idx_category_name ON category(name);

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

-- =============================================
-- 5. INSERT SAMPLE DATA FOR TESTING
-- =============================================

-- Insert sample novels for testing APIs
INSERT INTO novel (
    uuid, title, author_id, author_name, category_id, synopsis, 
    status, is_completed, chapter_cnt, word_cnt, avg_rating, 
    review_cnt, view_cnt, vote_cnt, yuan_cnt, 
    create_time, update_time, publish_time
) VALUES 
-- Fantasy novels
(gen_random_uuid(), 'The Dragon Chronicles', '550e8400-e29b-41d4-a716-446655440001', 'John Fantasy', 1, 'An epic tale of dragons and magic in a mystical realm', 2, false, 15, 150000, 4.5, 25, 1200, 45, 150.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Mage Academy', '550e8400-e29b-41d4-a716-446655440001', 'John Fantasy', 1, 'A young mage discovers his powers at the prestigious academy', 1, false, 8, 80000, 4.2, 12, 800, 30, 75.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Romance novels  
(gen_random_uuid(), 'Love in Paris', '550e8400-e29b-41d4-a716-446655440002', 'Jane Romance', 2, 'A heartwarming love story set in the romantic city of Paris', 2, true, 20, 200000, 4.8, 35, 2000, 60, 200.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Summer Romance', '550e8400-e29b-41d4-a716-446655440002', 'Jane Romance', 2, 'A summer fling that turns into something more', 0, false, 5, 50000, 0.0, 0, 0, 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Mystery novels
(gen_random_uuid(), 'The Missing Heir', '550e8400-e29b-41d4-a716-446655440003', 'Detective Smith', 3, 'A detective investigates the mysterious disappearance of a wealthy heir', 2, true, 12, 120000, 4.3, 18, 1500, 40, 120.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Midnight Murder', '550e8400-e29b-41d4-a716-446655440003', 'Detective Smith', 3, 'A murder mystery that unfolds in the dead of night', 1, false, 6, 60000, 0.0, 0, 0, 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Sci-Fi novels
(gen_random_uuid(), 'Galaxy Wars', '550e8400-e29b-41d4-a716-446655440004', 'Space Writer', 4, 'An epic space opera about galactic conflicts and alien civilizations', 2, false, 25, 300000, 4.6, 42, 3000, 80, 250.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Time Traveler', '550e8400-e29b-41d4-a716-446655440004', 'Space Writer', 4, 'A scientist discovers the secret of time travel', 0, false, 3, 30000, 0.0, 0, 0, 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Horror novels
(gen_random_uuid(), 'The Haunted Mansion', '550e8400-e29b-41d4-a716-446655440005', 'Horror Author', 5, 'A terrifying tale of supernatural events in an old mansion', 2, true, 10, 100000, 4.1, 15, 900, 25, 80.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Nightmare Alley', '550e8400-e29b-41d4-a716-446655440005', 'Horror Author', 5, 'A psychological horror story about nightmares coming to life', 1, false, 4, 40000, 0.0, 0, 0, 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Adventure novels
(gen_random_uuid(), 'Treasure Island', '550e8400-e29b-41d4-a716-446655440006', 'Adventure Writer', 6, 'A classic adventure tale of pirates and hidden treasure', 2, true, 18, 180000, 4.7, 28, 1800, 50, 180.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Mountain Expedition', '550e8400-e29b-41d4-a716-446655440006', 'Adventure Writer', 6, 'An expedition to climb the world''s highest mountain', 0, false, 7, 70000, 0.0, 0, 0, 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Drama novels
(gen_random_uuid(), 'Family Secrets', '550e8400-e29b-41d4-a716-446655440007', 'Drama Author', 7, 'A family drama exploring secrets and relationships', 2, true, 14, 140000, 4.4, 22, 1100, 35, 130.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'The Last Goodbye', '550e8400-e29b-41d4-a716-446655440007', 'Drama Author', 7, 'A poignant story about saying goodbye to loved ones', 1, false, 6, 60000, 0.0, 0, 0, 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Comedy novels
(gen_random_uuid(), 'The Office Comedy', '550e8400-e29b-41d4-a716-446655440008', 'Comedy Writer', 8, 'A hilarious workplace comedy about office life', 2, true, 16, 160000, 4.2, 20, 1300, 38, 110.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Funny Farm', '550e8400-e29b-41d4-a716-446655440008', 'Comedy Writer', 8, 'A comedy about life on a quirky farm', 0, false, 5, 50000, 0.0, 0, 0, 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL)

ON CONFLICT (uuid) DO NOTHING;
