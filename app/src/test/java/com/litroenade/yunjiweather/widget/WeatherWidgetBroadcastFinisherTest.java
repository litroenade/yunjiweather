package com.litroenade.yunjiweather.widget;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WeatherWidgetBroadcastFinisherTest {

    @Test
    public void markFinishedInvokesFinishAfterAllWidgetUpdatesComplete() {
        int[] finishCount = {0};
        WeatherWidgetBroadcastFinisher finisher = WeatherWidgetBroadcastFinisher.create(
                2,
                () -> finishCount[0]++
        );

        finisher.markFinished();
        assertEquals(0, finishCount[0]);

        finisher.markFinished();
        assertEquals(1, finishCount[0]);
    }

    @Test
    public void createFinishesImmediatelyWhenThereAreNoWidgetIds() {
        int[] finishCount = {0};

        WeatherWidgetBroadcastFinisher.create(0, () -> finishCount[0]++);

        assertEquals(1, finishCount[0]);
    }
}
