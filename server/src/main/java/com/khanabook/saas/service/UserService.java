package com.khanabook.saas.service;

import com.khanabook.saas.entity.User;
import java.util.List;

public interface UserService {
    List<Integer> pushData(Long tenantId, List<User> payload);
    List<User> pullData(Long tenantId, Long lastSyncTimestamp, String deviceId);
}
