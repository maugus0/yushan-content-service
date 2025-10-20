-- Create all tables for Content Service
-- This migration creates category, novel, and chapter tables with all necessary indexes and constraints

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
-- 3. CREATE CHAPTER TABLE
-- =============================================

CREATE TABLE IF NOT EXISTS chapter (
    id SERIAL PRIMARY KEY,
    uuid UUID NOT NULL DEFAULT gen_random_uuid(),
    novel_id INTEGER NOT NULL,
    chapter_number INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    word_cnt INTEGER DEFAULT 0,
    is_premium BOOLEAN DEFAULT FALSE,
    yuan_cost REAL DEFAULT 0.0,
    view_cnt BIGINT DEFAULT 0,
    is_valid BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    publish_time TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_chapter_novel FOREIGN KEY (novel_id) REFERENCES novel(id) ON DELETE CASCADE,
    CONSTRAINT unique_novel_chapter_number UNIQUE (novel_id, chapter_number),
    CONSTRAINT unique_chapter_uuid UNIQUE (uuid)
);

-- =============================================
-- 4. CREATE INDEXES FOR PERFORMANCE
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

-- Chapter table indexes
CREATE INDEX IF NOT EXISTS idx_chapter_novel_id ON chapter(novel_id);
CREATE INDEX IF NOT EXISTS idx_chapter_uuid ON chapter(uuid);
CREATE INDEX IF NOT EXISTS idx_chapter_number ON chapter(chapter_number);
CREATE INDEX IF NOT EXISTS idx_chapter_title ON chapter(title);
CREATE INDEX IF NOT EXISTS idx_chapter_is_valid ON chapter(is_valid);
CREATE INDEX IF NOT EXISTS idx_chapter_is_premium ON chapter(is_premium);
CREATE INDEX IF NOT EXISTS idx_chapter_create_time ON chapter(create_time);
CREATE INDEX IF NOT EXISTS idx_chapter_update_time ON chapter(update_time);
CREATE INDEX IF NOT EXISTS idx_chapter_publish_time ON chapter(publish_time);
CREATE INDEX IF NOT EXISTS idx_chapter_view_cnt ON chapter(view_cnt);
CREATE INDEX IF NOT EXISTS idx_chapter_word_cnt ON chapter(word_cnt);

-- Create composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_chapter_novel_valid ON chapter(novel_id, is_valid);
CREATE INDEX IF NOT EXISTS idx_chapter_novel_published ON chapter(novel_id, is_valid, publish_time) WHERE is_valid = true;
CREATE INDEX IF NOT EXISTS idx_chapter_novel_number ON chapter(novel_id, chapter_number);
CREATE INDEX IF NOT EXISTS idx_chapter_published_ranking ON chapter(novel_id, view_cnt DESC) WHERE is_valid = true;

-- =============================================
-- 5. ADD COMMENTS FOR DOCUMENTATION
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

COMMENT ON TABLE chapter IS 'Stores chapter information including content, metadata, and publishing status';
COMMENT ON COLUMN chapter.id IS 'Primary key - auto-incrementing integer';
COMMENT ON COLUMN chapter.uuid IS 'Unique identifier for external references';
COMMENT ON COLUMN chapter.novel_id IS 'Foreign key to novel table - parent novel';
COMMENT ON COLUMN chapter.chapter_number IS 'Chapter number within the novel (1-based)';
COMMENT ON COLUMN chapter.title IS 'Title of the chapter';
COMMENT ON COLUMN chapter.content IS 'Full text content of the chapter';
COMMENT ON COLUMN chapter.word_cnt IS 'Word count of the chapter content';
COMMENT ON COLUMN chapter.is_premium IS 'Whether this is a premium chapter requiring payment';
COMMENT ON COLUMN chapter.yuan_cost IS 'Cost in yuan for premium chapters';
COMMENT ON COLUMN chapter.view_cnt IS 'Total view count for this chapter';
COMMENT ON COLUMN chapter.is_valid IS 'Whether the chapter is published/visible (soft delete flag)';
COMMENT ON COLUMN chapter.create_time IS 'When the chapter was created';
COMMENT ON COLUMN chapter.update_time IS 'When the chapter was last updated';
COMMENT ON COLUMN chapter.publish_time IS 'When the chapter was/will be published (for scheduling)';

-- =============================================
-- 6. INSERT SAMPLE DATA FOR TESTING
-- =============================================

-- Insert sample novels for testing APIs
INSERT INTO novel (
    uuid, title, author_id, author_name, category_id, synopsis, 
    status, is_completed, chapter_cnt, word_cnt, avg_rating, 
    review_cnt, view_cnt, vote_cnt, yuan_cnt, 
    create_time, update_time, publish_time
) VALUES 
-- Fantasy novels
(gen_random_uuid(), 'The Dragon Chronicles', '550e8400-e29b-41d4-a716-446655440001', 'John Fantasy', 1, 'An epic tale of dragons and magic in a mystical realm', 2, false, 5, 5940, 4.5, 25, 1200, 45, 150.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Mage Academy', '550e8400-e29b-41d4-a716-446655440001', 'John Fantasy', 1, 'A young mage discovers his powers at the prestigious academy', 1, false, 0, 0, 4.2, 12, 800, 30, 75.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Romance novels  
(gen_random_uuid(), 'Love in Paris', '550e8400-e29b-41d4-a716-446655440002', 'Jane Romance', 2, 'A heartwarming love story set in the romantic city of Paris', 2, true, 0, 0, 4.8, 35, 2000, 60, 200.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Summer Romance', '550e8400-e29b-41d4-a716-446655440002', 'Jane Romance', 2, 'A summer fling that turns into something more', 0, false, 0, 0, 0.0, 0, 0, 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Mystery novels
(gen_random_uuid(), 'The Missing Heir', '550e8400-e29b-41d4-a716-446655440003', 'Detective Smith', 3, 'A detective investigates the mysterious disappearance of a wealthy heir', 2, true, 0, 0, 4.3, 18, 1500, 40, 120.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Midnight Murder', '550e8400-e29b-41d4-a716-446655440003', 'Detective Smith', 3, 'A murder mystery that unfolds in the dead of night', 1, false, 0, 0, 0.0, 0, 0, 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Sci-Fi novels
(gen_random_uuid(), 'Galaxy Wars', '550e8400-e29b-41d4-a716-446655440004', 'Space Writer', 4, 'An epic space opera about galactic conflicts and alien civilizations', 2, false, 0, 0, 4.6, 42, 3000, 80, 250.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Time Traveler', '550e8400-e29b-41d4-a716-446655440004', 'Space Writer', 4, 'A scientist discovers the secret of time travel', 0, false, 0, 0, 0.0, 0, 0, 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Horror novels
(gen_random_uuid(), 'The Haunted Mansion', '550e8400-e29b-41d4-a716-446655440005', 'Horror Author', 5, 'A terrifying tale of supernatural events in an old mansion', 2, true, 0, 0, 4.1, 15, 900, 25, 80.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Nightmare Alley', '550e8400-e29b-41d4-a716-446655440005', 'Horror Author', 5, 'A psychological horror story about nightmares coming to life', 1, false, 0, 0, 0.0, 0, 0, 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Adventure novels
(gen_random_uuid(), 'Treasure Island', '550e8400-e29b-41d4-a716-446655440006', 'Adventure Writer', 6, 'A classic adventure tale of pirates and hidden treasure', 2, true, 0, 0, 4.7, 28, 1800, 50, 180.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Mountain Expedition', '550e8400-e29b-41d4-a716-446655440006', 'Adventure Writer', 6, 'An expedition to climb the world''s highest mountain', 0, false, 0, 0, 0.0, 0, 0, 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Drama novels
(gen_random_uuid(), 'Family Secrets', '550e8400-e29b-41d4-a716-446655440007', 'Drama Author', 7, 'A family drama exploring secrets and relationships', 2, true, 0, 0, 4.4, 22, 1100, 35, 130.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'The Last Goodbye', '550e8400-e29b-41d4-a716-446655440007', 'Drama Author', 7, 'A poignant story about saying goodbye to loved ones', 1, false, 0, 0, 0.0, 0, 0, 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),

-- Comedy novels
(gen_random_uuid(), 'The Office Comedy', '550e8400-e29b-41d4-a716-446655440008', 'Comedy Writer', 8, 'A hilarious workplace comedy about office life', 2, true, 0, 0, 4.2, 20, 1300, 38, 110.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'Funny Farm', '550e8400-e29b-41d4-a716-446655440008', 'Comedy Writer', 8, 'A comedy about life on a quirky farm', 0, false, 0, 0, 0.0, 0, 0, 0, 0.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL)

ON CONFLICT (uuid) DO NOTHING;

-- =============================================
-- 7. INSERT SAMPLE CHAPTERS FOR TESTING
-- =============================================

-- Insert sample chapters for "The Dragon Chronicles" (novel_id = 1)
INSERT INTO chapter (
    uuid, novel_id, chapter_number, title, content, word_cnt, 
    is_premium, yuan_cost, view_cnt, is_valid, 
    create_time, update_time, publish_time
) VALUES 
-- Chapter 1: The Beginning
(gen_random_uuid(), 1, 1, 'The Prophecy', 
'In the ancient kingdom of Eldoria, where magic flowed like rivers and dragons soared through the clouds, a prophecy had been foretold for centuries. The old seer, Master Elrond, had spoken of a chosen one who would unite the realms and bring peace to the warring lands. As the sun set behind the towering mountains, casting long shadows across the valley, a young mage named Aria stood at the edge of the cliff, her silver hair dancing in the wind. She could feel the ancient power coursing through her veins, a power that had been dormant for years but was now awakening. The time had come for the prophecy to unfold, and she was at its center.', 
1200, false, 0.0, 150, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Chapter 2: The Discovery
(gen_random_uuid(), 1, 2, 'The Hidden Cave', 
'Aria had always been drawn to the mysterious cave hidden behind the waterfall near her village. The elders had warned her to stay away, speaking of ancient curses and dangerous creatures that lurked within. But something called to her, a voice that seemed to echo from the depths of the earth itself. As she stepped through the cascading water, the cave revealed its secrets. Glowing crystals lined the walls, pulsing with an otherworldly light. In the center of the chamber stood an ancient altar, carved with symbols that seemed to shift and move in the flickering light. Aria reached out to touch the cold stone, and suddenly, the entire cave was filled with a brilliant golden light. The prophecy was beginning to take shape.', 
1150, false, 0.0, 120, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Chapter 3: The Dragon''s Call
(gen_random_uuid(), 1, 3, 'Meeting the Guardian', 
'The golden light faded, and Aria found herself standing in a vast chamber that seemed to stretch into infinity. Before her stood the most magnificent creature she had ever seen - a dragon with scales that shimmered like liquid gold and eyes that held the wisdom of ages. "I have been waiting for you, young one," the dragon spoke, its voice like rolling thunder. "I am Valdris, Guardian of the Ancient Powers. The prophecy speaks of your coming, but the path ahead will not be easy. Dark forces gather in the shadows, seeking to prevent the fulfillment of the ancient words." Aria felt both fear and determination coursing through her. She had always known she was different, but now she understood the true weight of her destiny.', 
1180, false, 0.0, 135, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Chapter 4: The First Trial
(gen_random_uuid(), 1, 4, 'The Test of Courage', 
'Valdris led Aria to a chamber filled with floating platforms that seemed to exist in mid-air. "To prove yourself worthy of the ancient powers, you must complete three trials," the dragon explained. "The first is the Test of Courage. You must cross these platforms without falling into the abyss below. But beware - the platforms will shift and move, testing not just your physical abilities, but your inner strength as well." Aria took a deep breath and stepped onto the first platform. As soon as her foot touched the surface, the entire chamber began to move. Platforms rose and fell, twisted and turned, creating a maze that seemed impossible to navigate. But Aria focused her mind and let her instincts guide her, leaping from platform to platform with growing confidence.', 
1220, false, 0.0, 110, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Chapter 5: The Awakening
(gen_random_uuid(), 1, 5, 'Power Unleashed', 
'As Aria completed the final leap across the platforms, she felt something incredible happening within her. The dormant power that had been sleeping in her veins suddenly erupted, filling her entire being with a warmth and strength she had never known. Her hands glowed with a soft blue light, and she could feel the very air around her responding to her presence. "Excellent," Valdris said with approval. "You have passed the first trial. Your powers are awakening, but this is only the beginning. The dark sorcerer Malachar has learned of your existence and will stop at nothing to prevent you from fulfilling the prophecy. You must be ready for the battles ahead." Aria nodded, feeling both the weight of responsibility and the thrill of her newfound abilities. The journey was just beginning, and she was ready to face whatever challenges lay ahead.', 
1190, false, 0.0, 125, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

ON CONFLICT (uuid) DO NOTHING;
