package com.litroenade.yunjiweather.data.repository;

import java.io.IOException;

public interface AlertRemoteGateway {
    WarningRefreshResult refresh(String locationId) throws IOException;
}
