package com.litroenade.yunjiweather.data.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.litroenade.yunjiweather.data.model.LifeIndexItem;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LifeIndexRepositoryTest {

    @Test
    public void remoteIndicesRequireConfiguredQWeatherLocation() {
        assertFalse(LifeIndexRepository.canFetchRemoteIndices(false, "101010100"));
        assertFalse(LifeIndexRepository.canFetchRemoteIndices(true, "openmeteo:39.9042,116.4074"));
        assertTrue(LifeIndexRepository.canFetchRemoteIndices(true, "101010100"));
    }

    @Test
    public void loadUsesRemoteGatewayAndWritesCompletedItemsToStore() {
        FakeRemoteGateway remoteGateway = new FakeRemoteGateway(Arrays.asList(
                new LifeIndexItem("穿衣", "炎热", "建议短袖。", "建议短袖。")
        ));
        FakeStore store = new FakeStore();
        LifeIndexRepository repository = new LifeIndexRepository(remoteGateway, store);

        LifeIndexRepository.LoadResult result = repository.load("101010100", "北京", 1_000L);

        assertEquals(LifeIndexRepository.LoadSource.REMOTE, result.getSource());
        assertEquals("穿衣", result.getItems().get(0).getName());
        assertEquals(10, store.savedItems.size());
        assertEquals(1_000L, store.savedUpdateTime);
    }

    @Test
    public void loadFallsBackToStoreWhenRemoteFails() {
        FakeRemoteGateway remoteGateway = new FakeRemoteGateway(new IOException("boom"));
        FakeStore store = new FakeStore();
        store.cacheRecord = new LifeIndexStore.CacheRecord(
                Arrays.asList(new LifeIndexItem("出行", "适宜", "适合出行。", "适合出行。")),
                800L
        );
        LifeIndexRepository repository = new LifeIndexRepository(remoteGateway, store);

        LifeIndexRepository.LoadResult result = repository.load("101010100", "北京", 1_000L);

        assertEquals(LifeIndexRepository.LoadSource.CACHE_ERROR, result.getSource());
        assertEquals("出行", result.getItems().get(0).getName());
        assertEquals(800L, result.getCacheUpdateTime());
    }

    private static final class FakeRemoteGateway implements LifeIndexRemoteGateway {
        private final List<LifeIndexItem> items;
        private final IOException exception;

        FakeRemoteGateway(List<LifeIndexItem> items) {
            this.items = items;
            this.exception = null;
        }

        FakeRemoteGateway(IOException exception) {
            this.items = null;
            this.exception = exception;
        }

        @Override
        public List<LifeIndexItem> fetch(String locationId) throws IOException {
            if (exception != null) {
                throw exception;
            }
            return items;
        }
    }

    private static final class FakeStore implements LifeIndexStore {
        private LifeIndexStore.CacheRecord cacheRecord;
        private List<LifeIndexItem> savedItems;
        private long savedUpdateTime;

        @Override
        public void save(String locationId, String cityName, List<LifeIndexItem> items, long updateTime, long expireTime) {
            savedItems = items;
            savedUpdateTime = updateTime;
        }

        @Override
        public LifeIndexStore.CacheRecord readValid(String locationId, long nowTime) {
            return cacheRecord;
        }
    }
}
