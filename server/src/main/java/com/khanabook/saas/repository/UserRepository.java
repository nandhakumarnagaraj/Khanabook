package com.khanabook.saas.repository;

import com.khanabook.saas.entity.User;
import com.khanabook.saas.sync.repository.SyncRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends SyncRepository<User, Long> {
}
