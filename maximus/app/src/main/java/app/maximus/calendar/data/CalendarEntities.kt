package app.maximus.calendar.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Local wall-clock times (no zone): days as epoch days, times as minute of day. */
@Entity(tableName = "calendar_event")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val location: String,
    val notes: String,
    val allDay: Boolean,
    val startDay: Long,
    val startMinute: Int,
    val endDay: Long,
    val endMinute: Int,
    val frequency: String,
    val interval: Int,
    val weekdayMask: Int,
    val untilDay: Long?,
    val count: Int?,
    val color: Int,
    val createdAt: Long
)

@Entity(
    tableName = "calendar_exdate",
    primaryKeys = ["eventId", "day"],
    foreignKeys = [ForeignKey(entity = CalendarEventEntity::class, parentColumns = ["id"], childColumns = ["eventId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("eventId")]
)
data class CalendarExdateEntity(val eventId: Long, val day: Long)

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_event")
    fun observeEvents(): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_exdate")
    fun observeExdates(): Flow<List<CalendarExdateEntity>>

    @Query("SELECT * FROM calendar_event WHERE id = :id")
    suspend fun get(id: Long): CalendarEventEntity?

    @Insert
    suspend fun insert(e: CalendarEventEntity): Long

    @Update
    suspend fun update(e: CalendarEventEntity)

    @Query("DELETE FROM calendar_event WHERE id = :id")
    suspend fun delete(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExdate(x: CalendarExdateEntity)
}
