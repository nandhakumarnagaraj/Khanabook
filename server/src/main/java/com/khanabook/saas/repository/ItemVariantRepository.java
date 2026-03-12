package com.khanabook.saas.repository;

import com.khanabook.saas.entity.ItemVariant;
import com.khanabook.saas.sync.repository.SyncRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemVariantRepository extends SyncRepository<ItemVariant, Long> {
}
