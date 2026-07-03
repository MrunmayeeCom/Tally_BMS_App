package com.example.feature.security.data.db

import androidx.room.TypeConverter
import com.example.feature.security.domain.models.ModulePermission
import com.example.feature.security.domain.models.PermissionAction
import com.example.feature.security.domain.models.SecurityModule

class SecurityConverters {

    @TypeConverter
    fun fromModulePermissions(permissions: List<ModulePermission>?): String? {
        if (permissions == null) return null
        return permissions.joinToString("|") { mp ->
            val actionsStr = mp.actions.joinToString(",") { it.name }
            "${mp.module.name}=$actionsStr"
        }
    }

    @TypeConverter
    fun toModulePermissions(data: String?): List<ModulePermission>? {
        if (data == null) return null
        if (data.isBlank()) return emptyList()

        return data.split("|").map { item ->
            val parts = item.split("=")
            val module = SecurityModule.valueOf(parts[0])
            val actions = if (parts.size > 1 && parts[1].isNotBlank()) {
                parts[1].split(",").map { PermissionAction.valueOf(it) }.toSet()
            } else {
                emptySet()
            }
            ModulePermission(module, actions)
        }
    }
}
