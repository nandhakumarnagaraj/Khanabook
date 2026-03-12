package com.khanabook.saas.repository;

import com.khanabook.saas.entity.MenuItem;
import com.khanabook.saas.sync.repository.SyncRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends SyncRepository<MenuItem, Long> {
}
