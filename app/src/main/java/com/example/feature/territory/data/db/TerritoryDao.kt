package com.example.feature.territory.data.db

import androidx.room.*

@Dao
interface TerritoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTerritory(territory: LocalTerritory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTerritories(territories: List<LocalTerritory>)

    @Query("DELETE FROM territory_items WHERE id = :id")
    suspend fun deleteTerritory(id: String)

    @Query("SELECT * FROM territory_items WHERE companyId = :companyId")
    suspend fun getTerritories(companyId: String): List<LocalTerritory>

    @Query("SELECT * FROM territory_items WHERE id = :id LIMIT 1")
    suspend fun getTerritoryById(id: String): LocalTerritory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBeat(beat: LocalBeat)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBeats(beats: List<LocalBeat>)

    @Query("DELETE FROM beat_items WHERE id = :id")
    suspend fun deleteBeat(id: String)

    @Query("SELECT * FROM beat_items WHERE companyId = :companyId")
    suspend fun getBeats(companyId: String): List<LocalBeat>

    @Query("SELECT * FROM beat_items WHERE territoryId = :territoryId AND companyId = :companyId")
    suspend fun getBeatsWithTerritory(territoryId: String, companyId: String): List<LocalBeat>

    @Query("SELECT * FROM beat_items WHERE id = :id LIMIT 1")
    suspend fun getBeatById(id: String): LocalBeat?
}
