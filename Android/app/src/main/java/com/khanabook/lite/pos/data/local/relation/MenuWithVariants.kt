package com.khanabook.lite.pos.data.local.relation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.room.Embedded
import androidx.room.Relation
import com.khanabook.lite.pos.data.local.entity.MenuItemEntity
import com.khanabook.lite.pos.data.local.entity.ItemVariantEntity

data class MenuWithVariants(
    @Embedded val menuItem: MenuItemEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "menu_item_id"
    )
    val variants: List<ItemVariantEntity>
)


