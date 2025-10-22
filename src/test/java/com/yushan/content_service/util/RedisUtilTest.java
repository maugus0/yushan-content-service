package com.yushan.content_service.util;

import com.yushan.content_service.dto.common.PageResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RedisUtil class
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisUtilTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisUtil redisUtil;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testSetWithDuration() {
        String key = "test:key";
        String value = "test-value";
        Duration ttl = Duration.ofMinutes(30);

        redisUtil.set(key, value, ttl);

        verify(valueOperations).set(key, value, ttl);
    }

    @Test
    void testSetWithSeconds() {
        String key = "test:key";
        String value = "test-value";
        long ttlSeconds = 1800;

        redisUtil.set(key, value, ttlSeconds);

        verify(valueOperations).set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    @Test
    void testGet() {
        String key = "test:key";
        String expectedValue = "test-value";
        when(valueOperations.get(key)).thenReturn(expectedValue);

        Object result = redisUtil.get(key);

        assertEquals(expectedValue, result);
        verify(valueOperations).get(key);
    }

    @Test
    void testGetWithTypeCasting() {
        String key = "test:key";
        String expectedValue = "test-value";
        when(valueOperations.get(key)).thenReturn(expectedValue);

        String result = redisUtil.get(key, String.class);

        assertEquals(expectedValue, result);
        verify(valueOperations).get(key);
    }

    @Test
    void testGetWithTypeCastingNullValue() {
        String key = "test:key";
        when(valueOperations.get(key)).thenReturn(null);

        String result = redisUtil.get(key, String.class);

        assertNull(result);
        verify(valueOperations).get(key);
    }

    @Test
    void testGetWithTypeCastingWrongType() {
        String key = "test:key";
        Integer wrongTypeValue = 123;
        when(valueOperations.get(key)).thenReturn(wrongTypeValue);

        String result = redisUtil.get(key, String.class);

        assertNull(result);
        verify(valueOperations).get(key);
    }

    @Test
    void testDelete() {
        String key = "test:key";
        when(redisTemplate.delete(key)).thenReturn(true);

        redisUtil.delete(key);

        verify(redisTemplate).delete(key);
    }

    @Test
    void testDeleteMultipleKeys() {
        Set<String> keys = new HashSet<>();
        keys.add("key1");
        keys.add("key2");
        when(redisTemplate.delete(keys)).thenReturn(2L);

        redisUtil.delete(keys);

        verify(redisTemplate).delete(keys);
    }

    @Test
    void testExists() {
        String key = "test:key";
        when(redisTemplate.hasKey(key)).thenReturn(true);

        boolean result = redisUtil.exists(key);

        assertTrue(result);
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void testExistsFalse() {
        String key = "test:key";
        when(redisTemplate.hasKey(key)).thenReturn(false);

        boolean result = redisUtil.exists(key);

        assertFalse(result);
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void testIncrement() {
        String key = "test:key";
        Long expectedValue = 1L;
        when(valueOperations.increment(key)).thenReturn(expectedValue);

        Long result = redisUtil.increment(key);

        assertEquals(expectedValue, result);
        verify(valueOperations).increment(key);
    }

    @Test
    void testIncrementWithDelta() {
        String key = "test:key";
        long delta = 5L;
        Long expectedValue = 5L;
        when(valueOperations.increment(key, delta)).thenReturn(expectedValue);

        Long result = redisUtil.increment(key, delta);

        assertEquals(expectedValue, result);
        verify(valueOperations).increment(key, delta);
    }

    @Test
    void testExpire() {
        String key = "test:key";
        Duration ttl = Duration.ofMinutes(30);
        when(redisTemplate.expire(key, ttl)).thenReturn(true);

        redisUtil.expire(key, ttl);

        verify(redisTemplate).expire(key, ttl);
    }

    @Test
    void testKeys() {
        String pattern = "test:*";
        Set<String> expectedKeys = new HashSet<>();
        expectedKeys.add("test:key1");
        expectedKeys.add("test:key2");
        when(redisTemplate.keys(pattern)).thenReturn(expectedKeys);

        Set<String> result = redisUtil.keys(pattern);

        assertEquals(expectedKeys, result);
        verify(redisTemplate).keys(pattern);
    }

    // Novel-specific cache methods tests

    @Test
    void testCacheNovel() {
        Integer novelId = 1;
        String novelData = "novel-data";

        redisUtil.cacheNovel(novelId, novelData);

        verify(valueOperations).set("novel:1", novelData, Duration.ofHours(1));
    }

    @Test
    void testGetCachedNovel() {
        Integer novelId = 1;
        String expectedData = "novel-data";
        when(valueOperations.get("novel:1")).thenReturn(expectedData);

        Object result = redisUtil.getCachedNovel(novelId);

        assertEquals(expectedData, result);
        verify(valueOperations).get("novel:1");
    }

    @Test
    void testGetCachedNovelWithType() {
        Integer novelId = 1;
        String expectedData = "novel-data";
        when(valueOperations.get("novel:1")).thenReturn(expectedData);

        String result = redisUtil.getCachedNovel(novelId, String.class);

        assertEquals(expectedData, result);
        verify(valueOperations).get("novel:1");
    }

    @Test
    void testDeleteNovelCache() {
        Integer novelId = 1;
        when(redisTemplate.delete("novel:1")).thenReturn(true);

        redisUtil.deleteNovelCache(novelId);

        verify(redisTemplate).delete("novel:1");
    }

    // Chapter-specific cache methods tests

    @Test
    void testCacheChapter() {
        java.util.UUID chapterUuid = java.util.UUID.randomUUID();
        String chapterData = "chapter-data";

        redisUtil.cacheChapter(chapterUuid, chapterData);

        verify(valueOperations).set("chapter:uuid:" + chapterUuid, chapterData, Duration.ofHours(2));
    }

    @Test
    void testGetCachedChapter() {
        java.util.UUID chapterUuid = java.util.UUID.randomUUID();
        String expectedData = "chapter-data";
        when(valueOperations.get("chapter:uuid:" + chapterUuid)).thenReturn(expectedData);

        Object result = redisUtil.getCachedChapter(chapterUuid);

        assertEquals(expectedData, result);
        verify(valueOperations).get("chapter:uuid:" + chapterUuid);
    }

    @Test
    void testGetCachedChapterWithType() {
        java.util.UUID chapterUuid = java.util.UUID.randomUUID();
        String expectedData = "chapter-data";
        when(valueOperations.get("chapter:uuid:" + chapterUuid)).thenReturn(expectedData);

        String result = redisUtil.getCachedChapter(chapterUuid, String.class);

        assertEquals(expectedData, result);
        verify(valueOperations).get("chapter:uuid:" + chapterUuid);
    }

    @Test
    void testCacheChapterByNovelAndNumber() {
        Integer novelId = 1;
        Integer chapterNumber = 5;
        String chapterData = "chapter-data";

        redisUtil.cacheChapterByNovelAndNumber(novelId, chapterNumber, chapterData);

        verify(valueOperations).set("chapter:novel:1:number:5", chapterData, Duration.ofHours(2));
    }

    @Test
    void testGetCachedChapterByNovelAndNumber() {
        Integer novelId = 1;
        Integer chapterNumber = 5;
        String expectedData = "chapter-data";
        when(valueOperations.get("chapter:novel:1:number:5")).thenReturn(expectedData);

        Object result = redisUtil.getCachedChapterByNovelAndNumber(novelId, chapterNumber);

        assertEquals(expectedData, result);
        verify(valueOperations).get("chapter:novel:1:number:5");
    }

    @Test
    void testGetCachedChapterByNovelAndNumberWithType() {
        Integer novelId = 1;
        Integer chapterNumber = 5;
        String expectedData = "chapter-data";
        when(valueOperations.get("chapter:novel:1:number:5")).thenReturn(expectedData);

        String result = redisUtil.getCachedChapterByNovelAndNumber(novelId, chapterNumber, String.class);

        assertEquals(expectedData, result);
        verify(valueOperations).get("chapter:novel:1:number:5");
    }

    @Test
    void testCacheChapterList() {
        Integer novelId = 1;
        String cacheKey = "published";
        String chapterListData = "chapter-list-data";

        redisUtil.cacheChapterList(novelId, cacheKey, chapterListData);

        verify(valueOperations).set("chapter:list:1:published", chapterListData, Duration.ofHours(2));
    }

    @Test
    void testGetCachedChapterList() {
        Integer novelId = 1;
        String cacheKey = "published";
        String expectedData = "chapter-list-data";
        when(valueOperations.get("chapter:list:1:published")).thenReturn(expectedData);

        Object result = redisUtil.getCachedChapterList(novelId, cacheKey);

        assertEquals(expectedData, result);
        verify(valueOperations).get("chapter:list:1:published");
    }

    @Test
    void testGetCachedChapterListWithType() {
        Integer novelId = 1;
        String cacheKey = "published";
        String expectedData = "chapter-list-data";
        when(valueOperations.get("chapter:list:1:published")).thenReturn(expectedData);

        String result = redisUtil.getCachedChapterList(novelId, cacheKey, String.class);

        assertEquals(expectedData, result);
        verify(valueOperations).get("chapter:list:1:published");
    }

    @Test
    void testGetCachedChapterListTyped() {
        Integer novelId = 1;
        String cacheKey = "published";
        PageResponseDTO<String> expectedData = new PageResponseDTO<>();
        when(valueOperations.get("chapter:list:1:published")).thenReturn(expectedData);

        PageResponseDTO<String> result = redisUtil.getCachedChapterListTyped(novelId, cacheKey);

        assertEquals(expectedData, result);
        verify(valueOperations).get("chapter:list:1:published");
    }

    @Test
    void testCacheChapterStatistics() {
        Integer novelId = 1;
        String statisticsData = "statistics-data";

        redisUtil.cacheChapterStatistics(novelId, statisticsData);

        verify(valueOperations).set("chapter:stats:1", statisticsData, Duration.ofHours(2));
    }

    @Test
    void testGetCachedChapterStatistics() {
        Integer novelId = 1;
        String expectedData = "statistics-data";
        when(valueOperations.get("chapter:stats:1")).thenReturn(expectedData);

        Object result = redisUtil.getCachedChapterStatistics(novelId);

        assertEquals(expectedData, result);
        verify(valueOperations).get("chapter:stats:1");
    }

    @Test
    void testGetCachedChapterStatisticsWithType() {
        Integer novelId = 1;
        String expectedData = "statistics-data";
        when(valueOperations.get("chapter:stats:1")).thenReturn(expectedData);

        String result = redisUtil.getCachedChapterStatistics(novelId, String.class);

        assertEquals(expectedData, result);
        verify(valueOperations).get("chapter:stats:1");
    }

    @Test
    void testDeleteChapterCache() {
        java.util.UUID chapterUuid = java.util.UUID.randomUUID();
        when(redisTemplate.delete("chapter:uuid:" + chapterUuid)).thenReturn(true);

        redisUtil.deleteChapterCache(chapterUuid);

        verify(redisTemplate).delete("chapter:uuid:" + chapterUuid);
    }

    @Test
    void testDeleteChapterCacheByNovelAndNumber() {
        Integer novelId = 1;
        Integer chapterNumber = 5;
        when(redisTemplate.delete("chapter:novel:1:number:5")).thenReturn(true);

        redisUtil.deleteChapterCacheByNovelAndNumber(novelId, chapterNumber);

        verify(redisTemplate).delete("chapter:novel:1:number:5");
    }

    @Test
    void testInvalidateChapterCaches() {
        Integer novelId = 1;
        Set<String> listKeys = new HashSet<>();
        listKeys.add("chapter:list:1:published");
        listKeys.add("chapter:list:1:draft");
        
        when(redisTemplate.keys("chapter:list:1:*")).thenReturn(listKeys);
        when(redisTemplate.delete(listKeys)).thenReturn(2L);
        when(redisTemplate.delete("chapter:stats:1")).thenReturn(true);

        redisUtil.invalidateChapterCaches(novelId);

        verify(redisTemplate).keys("chapter:list:1:*");
        verify(redisTemplate).delete(listKeys);
        verify(redisTemplate).delete("chapter:stats:1");
    }

    @Test
    void testInvalidateChapterCachesWithEmptyKeys() {
        Integer novelId = 1;
        Set<String> emptyKeys = new HashSet<>();
        
        when(redisTemplate.keys("chapter:list:1:*")).thenReturn(emptyKeys);
        when(redisTemplate.delete("chapter:stats:1")).thenReturn(true);

        redisUtil.invalidateChapterCaches(novelId);

        verify(redisTemplate).keys("chapter:list:1:*");
        verify(redisTemplate, never()).delete(emptyKeys);
        verify(redisTemplate).delete("chapter:stats:1");
    }

    @Test
    void testInvalidateAllChapterCaches() {
        Set<String> chapterKeys = new HashSet<>();
        chapterKeys.add("chapter:uuid:123");
        chapterKeys.add("chapter:novel:1:number:1");
        
        when(redisTemplate.keys("chapter:*")).thenReturn(chapterKeys);
        when(redisTemplate.delete(chapterKeys)).thenReturn(2L);

        redisUtil.invalidateAllChapterCaches();

        verify(redisTemplate).keys("chapter:*");
        verify(redisTemplate).delete(chapterKeys);
    }

    @Test
    void testInvalidateAllChapterCachesWithEmptyKeys() {
        Set<String> emptyKeys = new HashSet<>();
        
        when(redisTemplate.keys("chapter:*")).thenReturn(emptyKeys);

        redisUtil.invalidateAllChapterCaches();

        verify(redisTemplate).keys("chapter:*");
        verify(redisTemplate, never()).delete(emptyKeys);
    }

    // View count cache methods tests

    @Test
    void testCacheViewCount() {
        Integer novelId = 1;
        Long viewCount = 1000L;

        redisUtil.cacheViewCount(novelId, viewCount);

        verify(valueOperations).set("view_count:1", viewCount, Duration.ofMinutes(30));
    }

    @Test
    void testGetCachedViewCount() {
        Integer novelId = 1;
        Long expectedCount = 1000L;
        when(valueOperations.get("view_count:1")).thenReturn(expectedCount);

        Long result = redisUtil.getCachedViewCount(novelId);

        assertEquals(expectedCount, result);
        verify(valueOperations).get("view_count:1");
    }

    @Test
    void testIncrementCachedViewCount() {
        Integer novelId = 1;
        Long expectedCount = 1001L;
        when(valueOperations.increment("view_count:1")).thenReturn(expectedCount);
        when(redisTemplate.expire("view_count:1", Duration.ofMinutes(30))).thenReturn(true);

        Long result = redisUtil.incrementCachedViewCount(novelId);

        assertEquals(expectedCount, result);
        verify(valueOperations).increment("view_count:1");
        verify(redisTemplate).expire("view_count:1", Duration.ofMinutes(30));
    }

    @Test
    void testDeleteViewCountCache() {
        Integer novelId = 1;
        when(redisTemplate.delete("view_count:1")).thenReturn(true);

        redisUtil.deleteViewCountCache(novelId);

        verify(redisTemplate).delete("view_count:1");
    }

    // Popular queries cache methods tests

    @Test
    void testCachePopularNovels() {
        String category = "fantasy";
        String novelsData = "novels-data";

        redisUtil.cachePopularNovels(category, novelsData);

        verify(valueOperations).set("popular:novels:fantasy", novelsData, Duration.ofMinutes(15));
    }

    @Test
    void testGetCachedPopularNovels() {
        String category = "fantasy";
        String expectedData = "novels-data";
        when(valueOperations.get("popular:novels:fantasy")).thenReturn(expectedData);

        Object result = redisUtil.getCachedPopularNovels(category);

        assertEquals(expectedData, result);
        verify(valueOperations).get("popular:novels:fantasy");
    }

    @Test
    void testGetCachedPopularNovelsWithType() {
        String category = "fantasy";
        String expectedData = "novels-data";
        when(valueOperations.get("popular:novels:fantasy")).thenReturn(expectedData);

        String result = redisUtil.getCachedPopularNovels(category, String.class);

        assertEquals(expectedData, result);
        verify(valueOperations).get("popular:novels:fantasy");
    }

    @Test
    void testCacheSearchResults() {
        String searchQuery = "test query";
        String searchResults = "search-results";

        redisUtil.cacheSearchResults(searchQuery, searchResults);

        verify(valueOperations).set("search:query:" + searchQuery.hashCode(), searchResults, Duration.ofMinutes(10));
    }

    @Test
    void testGetCachedSearchResults() {
        String searchQuery = "test query";
        String expectedData = "search-results";
        when(valueOperations.get("search:query:" + searchQuery.hashCode())).thenReturn(expectedData);

        Object result = redisUtil.getCachedSearchResults(searchQuery);

        assertEquals(expectedData, result);
        verify(valueOperations).get("search:query:" + searchQuery.hashCode());
    }

    // Cache invalidation methods tests

    @Test
    void testInvalidateNovelCaches() {
        Integer novelId = 1;
        Set<String> popularKeys = new HashSet<>();
        popularKeys.add("popular:novels:fantasy");
        popularKeys.add("popular:novels:romance");
        
        when(redisTemplate.delete("novel:1")).thenReturn(true);
        when(redisTemplate.delete("view_count:1")).thenReturn(true);
        when(redisTemplate.keys("chapter:list:1:*")).thenReturn(new HashSet<>());
        when(redisTemplate.delete("chapter:stats:1")).thenReturn(true);
        when(redisTemplate.keys("popular:*")).thenReturn(popularKeys);
        when(redisTemplate.delete(popularKeys)).thenReturn(2L);

        redisUtil.invalidateNovelCaches(novelId);

        verify(redisTemplate).delete("novel:1");
        verify(redisTemplate).delete("view_count:1");
        verify(redisTemplate).keys("chapter:list:1:*");
        verify(redisTemplate).delete("chapter:stats:1");
        verify(redisTemplate).keys("popular:*");
        verify(redisTemplate).delete(popularKeys);
    }

    @Test
    void testInvalidateSearchCaches() {
        Set<String> searchKeys = new HashSet<>();
        searchKeys.add("search:query:123");
        searchKeys.add("search:query:456");
        
        when(redisTemplate.keys("search:*")).thenReturn(searchKeys);
        when(redisTemplate.delete(searchKeys)).thenReturn(2L);

        redisUtil.invalidateSearchCaches();

        verify(redisTemplate).keys("search:*");
        verify(redisTemplate).delete(searchKeys);
    }

    @Test
    void testClearAllCaches() {
        Set<String> allKeys = new HashSet<>();
        allKeys.add("novel:1");
        allKeys.add("chapter:uuid:123");
        allKeys.add("search:query:456");
        
        when(redisTemplate.keys("*")).thenReturn(allKeys);
        when(redisTemplate.delete(allKeys)).thenReturn(3L);

        redisUtil.clearAllCaches();

        verify(redisTemplate).keys("*");
        verify(redisTemplate).delete(allKeys);
    }

    // Category-specific cache methods tests

    @Test
    void testCacheCategory() {
        Integer categoryId = 1;
        String categoryData = "category-data";

        redisUtil.cacheCategory(categoryId, categoryData);

        verify(valueOperations).set("category:id:1", categoryData, Duration.ofMinutes(30));
    }

    @Test
    void testGetCachedCategory() {
        Integer categoryId = 1;
        String expectedData = "category-data";
        when(valueOperations.get("category:id:1")).thenReturn(expectedData);

        Object result = redisUtil.getCachedCategory(categoryId);

        assertEquals(expectedData, result);
        verify(valueOperations).get("category:id:1");
    }

    @Test
    void testGetCachedCategoryWithType() {
        Integer categoryId = 1;
        String expectedData = "category-data";
        when(valueOperations.get("category:id:1")).thenReturn(expectedData);

        String result = redisUtil.getCachedCategory(categoryId, String.class);

        assertEquals(expectedData, result);
        verify(valueOperations).get("category:id:1");
    }

    @Test
    void testCacheCategoryBySlug() {
        String slug = "fantasy";
        String categoryData = "category-data";

        redisUtil.cacheCategoryBySlug(slug, categoryData);

        verify(valueOperations).set("category:slug:fantasy", categoryData, Duration.ofMinutes(30));
    }

    @Test
    void testGetCachedCategoryBySlug() {
        String slug = "fantasy";
        String expectedData = "category-data";
        when(valueOperations.get("category:slug:fantasy")).thenReturn(expectedData);

        Object result = redisUtil.getCachedCategoryBySlug(slug);

        assertEquals(expectedData, result);
        verify(valueOperations).get("category:slug:fantasy");
    }

    @Test
    void testGetCachedCategoryBySlugWithType() {
        String slug = "fantasy";
        String expectedData = "category-data";
        when(valueOperations.get("category:slug:fantasy")).thenReturn(expectedData);

        String result = redisUtil.getCachedCategoryBySlug(slug, String.class);

        assertEquals(expectedData, result);
        verify(valueOperations).get("category:slug:fantasy");
    }

    @Test
    void testCacheCategories() {
        String type = "all";
        String categoriesData = "categories-data";

        redisUtil.cacheCategories(type, categoriesData);

        verify(valueOperations).set("category:all", categoriesData, Duration.ofMinutes(30));
    }

    @Test
    void testGetCachedCategories() {
        String type = "all";
        String expectedData = "categories-data";
        when(valueOperations.get("category:all")).thenReturn(expectedData);

        Object result = redisUtil.getCachedCategories(type);

        assertEquals(expectedData, result);
        verify(valueOperations).get("category:all");
    }

    @Test
    void testGetCachedCategoriesWithType() {
        String type = "all";
        String expectedData = "categories-data";
        when(valueOperations.get("category:all")).thenReturn(expectedData);

        String result = redisUtil.getCachedCategories(type, String.class);

        assertEquals(expectedData, result);
        verify(valueOperations).get("category:all");
    }

    @Test
    void testInvalidateCategoryCaches() {
        Set<String> categoryKeys = new HashSet<>();
        categoryKeys.add("category:id:1");
        categoryKeys.add("category:slug:fantasy");
        categoryKeys.add("category:all");
        
        when(redisTemplate.keys("category:*")).thenReturn(categoryKeys);
        when(redisTemplate.delete(categoryKeys)).thenReturn(3L);

        redisUtil.invalidateCategoryCaches();

        verify(redisTemplate).keys("category:*");
        verify(redisTemplate).delete(categoryKeys);
    }
}