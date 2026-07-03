package com.example.feature.approval.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.feature.approval.domain.models.ApprovalChainStep
import com.example.feature.approval.domain.models.WorkflowStep
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "approvals")
data class LocalApproval(
    @PrimaryKey val id: String,
    val companyId: String,
    val tenantId: String,
    val requesterId: String,
    val requesterName: String,
    val type: String,
    val title: String,
    val description: String,
    val date: String,
    val status: String,
    val amount: Double?,
    val refId: String?,
    val remarks: String?,
    val history: List<WorkflowStep>,
    val chain: List<ApprovalChainStep>,
    val pendingSync: Boolean = false
)

class ApprovalConverters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromHistoryList(list: List<WorkflowStep>?): String? {
        if (list == null) return null
        val type = Types.newParameterizedType(List::class.java, WorkflowStep::class.java)
        return moshi.adapter<List<WorkflowStep>>(type).toJson(list)
    }

    @TypeConverter
    fun toHistoryList(json: String?): List<WorkflowStep>? {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, WorkflowStep::class.java)
        return moshi.adapter<List<WorkflowStep>>(type).fromJson(json)
    }

    @TypeConverter
    fun fromChainList(list: List<ApprovalChainStep>?): String? {
        if (list == null) return null
        val type = Types.newParameterizedType(List::class.java, ApprovalChainStep::class.java)
        return moshi.adapter<List<ApprovalChainStep>>(type).toJson(list)
    }

    @TypeConverter
    fun toChainList(json: String?): List<ApprovalChainStep>? {
        if (json.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, ApprovalChainStep::class.java)
        return moshi.adapter<List<ApprovalChainStep>>(type).fromJson(json)
    }
}
