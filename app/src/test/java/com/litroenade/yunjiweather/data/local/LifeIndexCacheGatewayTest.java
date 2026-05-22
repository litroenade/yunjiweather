package com.litroenade.yunjiweather.data.local;

import com.google.gson.Gson;
import com.litroenade.yunjiweather.data.entity.WeatherCacheEntity;
import com.litroenade.yunjiweather.data.model.LifeIndexItem;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LifeIndexCacheGatewayTest {

    @Test
    public void saveAndReadValidCache_returnsCachedLifeIndexItems() {
        FakeWeatherCacheDao dao = new FakeWeatherCacheDao();
        LifeIndexCacheGateway gateway = new LifeIndexCacheGateway(1L, dao, new Gson());
        List<LifeIndexItem> items = Arrays.asList(
                new LifeIndexItem("穿衣", "舒适", "建议穿短袖或薄外套。", "早晚温差明显时注意加衣。"),
                new LifeIndexItem("出行", "适宜", "天气适合出行。", "出门前关注实时天气变化。")
        );

        gateway.save("101010100", "北京", items, 1_000L, 3_000L);
        LifeIndexCacheGateway.CacheRecord record = gateway.readValid("101010100", 2_000L);

        assertEquals(2, record.getItems().size());
        assertEquals("穿衣", record.getItems().get(0).getName());
        assertEquals("适宜", record.getItems().get(1).getLevel());
        assertEquals(1_000L, record.getUpdateTime());
    }

    @Test
    public void readValid_returnsNullWhenCacheExpired() {
        FakeWeatherCacheDao dao = new FakeWeatherCacheDao();
        LifeIndexCacheGateway gateway = new LifeIndexCacheGateway(1L, dao, new Gson());
        gateway.save("101010100", "北京", Arrays.asList(
                new LifeIndexItem("穿衣", "舒适", "建议穿短袖。", "天气较热。")
        ), 1_000L, 2_000L);

        assertNull(gateway.readValid("101010100", 2_000L));
    }

    @Test
    public void readValid_returnsNullWhenJsonBroken() {
        FakeWeatherCacheDao dao = new FakeWeatherCacheDao();
        dao.entity = new WeatherCacheEntity(1L, "101010100", "北京", "INDEX", "{broken", 1_000L, 3_000L);
        LifeIndexCacheGateway gateway = new LifeIndexCacheGateway(1L, dao, new Gson());

        assertNull(gateway.readValid("101010100", 2_000L));
    }

    @Test
    public void indexCacheDoesNotOverwriteHomeCache() {
        FakeWeatherCacheDao dao = new FakeWeatherCacheDao();
        LifeIndexCacheGateway gateway = new LifeIndexCacheGateway(1L, dao, new Gson());

        gateway.save("101010100", "北京", Arrays.asList(
                new LifeIndexItem("穿衣", "舒适", "建议穿短袖。", "天气较热。")
        ), 1_000L, 3_000L);

        assertEquals("INDEX", dao.entity.weatherType);
    }

    @Test
    public void userCacheDoesNotOverwriteOtherUserCache() {
        FakeWeatherCacheDao dao = new FakeWeatherCacheDao();
        LifeIndexCacheGateway firstGateway = new LifeIndexCacheGateway(1L, dao, new Gson());
        LifeIndexCacheGateway secondGateway = new LifeIndexCacheGateway(2L, dao, new Gson());

        firstGateway.save("101010100", "北京", Arrays.asList(
                new LifeIndexItem("穿衣", "舒适", "用户 A 建议", "用户 A 详情")
        ), 1_000L, 3_000L);
        secondGateway.save("101010100", "北京", Arrays.asList(
                new LifeIndexItem("穿衣", "偏热", "用户 B 建议", "用户 B 详情")
        ), 2_000L, 4_000L);

        assertEquals("舒适", firstGateway.readValid("101010100", 2_500L).getItems().get(0).getLevel());
        assertEquals("偏热", secondGateway.readValid("101010100", 2_500L).getItems().get(0).getLevel());
    }

    private static final class FakeWeatherCacheDao implements WeatherCacheDao {
        private WeatherCacheEntity entity;
        private WeatherCacheEntity secondEntity;

        @Override
        public void insert(WeatherCacheEntity entity) {
            if (entity.ownerUserId == 1L) {
                this.entity = entity;
            } else {
                this.secondEntity = entity;
            }
        }

        @Override
        public WeatherCacheEntity findByLocationAndType(long ownerUserId, String locationId, String weatherType) {
            WeatherCacheEntity candidate = ownerUserId == 1L ? entity : secondEntity;
            if (candidate == null) {
                return null;
            }
            if (!candidate.locationId.equals(locationId) || !candidate.weatherType.equals(weatherType)) {
                return null;
            }
            return candidate;
        }

        @Override
        public Long findLatestUpdateTime(long ownerUserId) {
            WeatherCacheEntity candidate = ownerUserId == 1L ? entity : secondEntity;
            return candidate == null ? null : candidate.updateTime;
        }

        @Override
        public int count(long ownerUserId) {
            WeatherCacheEntity candidate = ownerUserId == 1L ? entity : secondEntity;
            return candidate == null ? 0 : 1;
        }

        @Override
        public void clearAll(long ownerUserId) {
            if (ownerUserId == 1L) {
                entity = null;
            } else {
                secondEntity = null;
            }
        }
    }
}
