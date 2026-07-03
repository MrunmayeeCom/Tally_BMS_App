package com.bmstally.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bmstally.app.model.Company

@Entity(tableName = "companies")
data class CompanyEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val name: String,
    val gstin: String?
) {
    fun toModel() = Company(id, name, gstin)

    companion object {
        fun fromModel(tenantId: String, company: Company) = CompanyEntity(
            id = company.id,
            tenantId = tenantId,
            name = company.name,
            gstin = company.gstin
        )
    }
}
