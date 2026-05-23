package com.litroenade.yunjiweather.data.api;

import com.litroenade.yunjiweather.data.api.model.QWeatherAirQualityResponse;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class QWeatherRemoteGatewayTest {

    @Test
    public void resolvePrimaryPollutantName_returnsNoneWhenApiOmitsPollutant() throws IOException {
        QWeatherAirQualityResponse.AirIndex airIndex = new QWeatherAirQualityResponse.AirIndex();

        assertEquals("无", QWeatherRemoteGateway.resolvePrimaryPollutantName(airIndex));
    }
}
