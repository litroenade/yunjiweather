package com.litroenade.yunjiweather.data.local;

import com.google.gson.Gson;
import com.litroenade.yunjiweather.data.entity.WeatherCacheEntity;
import com.litroenade.yunjiweather.ui.index.LifeIndexItem;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LifeIndexCacheGatewayTest {

    @Test
    public void saveAndReadValidCache_returnsCachedLifeIndexItems() {
        FakeWeatherCacheDao dao = new FakeWeatherCacheDao();
        LifeIndexCacheGateway gateway = new LifeIndexCacheGateway(dao, new Gson());
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
        LifeIndexCacheGateway gateway = new LifeIndexCacheGateway(dao, new Gson());
        gateway.save("101010100", "北京", Arrays.asList(
                new LifeIndexItem("穿衣", "舒适", "建议穿短袖。", "天气较热。")
        ), 1_000L, 2_000L);

        assertNull(gateway.readValid("101010100", 2_000L));
    }

    @Test
    public void readValid_returnsNullWhenJsonBroken() {
        FakeWeatherCacheDao dao = new FakeWeatherCacheDao();
        dao.entity = new WeatherCacheEntity("101010100", "北京", "INDEX", "{broken", 1_000L, 3_000L);
        LifeIndexCacheGateway gateway = new LifeIndexCacheGateway(dao, new Gson());

        assertNull(gateway.readValid("101010100", 2_000L));
    }

    @Test
    public void indexCacheDoesNotOverwriteHomeCache() {
        FakeWeatherCacheDao dao = new FakeWeatherCacheDao();
        LifeIndexCacheGateway gateway = new LifeIndexCacheGateway(dao, new Gson());

        gateway.save("101010100", "北京", Arrays.asList(
                new LifeIndexItem("穿衣", "舒适", "建议穿短袖。", "天气较热。")
        ), 1_000L, 3_000L);

        assertEquals("INDEX", dao.entity.weatherType);
    }

    private static final class FakeWeatherCacheDao implements WeatherCacheDao {
        private WeatherCacheEntity entity;

        @Override
        public void insert(WeatherCacheEntity entity) {
            this.entity = entity;
        }

        @Override
        public WeatherCacheEntity findByLocationAndType(String locationId, String weatherType) {
            if (entity == null) {
                return null;
            }
            if (!entity.locationId.equals(locationId) || !entity.weatherType.equals(weatherType)) {
                return null;
            }
            return entity;
        }

        @Override
        public Long findLatestUpdateTime() {
            return entity == null ? null : entity.updateTime;
        }

        @Override
        public void clearAll() {
            entity = null;
        }
    }
}
