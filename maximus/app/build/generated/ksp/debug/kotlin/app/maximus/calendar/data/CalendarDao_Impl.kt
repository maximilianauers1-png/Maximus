package app.maximus.calendar.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CalendarDao_Impl(
  __db: RoomDatabase,
) : CalendarDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCalendarEventEntity: EntityInsertAdapter<CalendarEventEntity>

  private val __insertAdapterOfCalendarExdateEntity: EntityInsertAdapter<CalendarExdateEntity>

  private val __updateAdapterOfCalendarEventEntity: EntityDeleteOrUpdateAdapter<CalendarEventEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfCalendarEventEntity = object : EntityInsertAdapter<CalendarEventEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `calendar_event` (`id`,`title`,`location`,`notes`,`allDay`,`startDay`,`startMinute`,`endDay`,`endMinute`,`frequency`,`interval`,`weekdayMask`,`untilDay`,`count`,`color`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CalendarEventEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.location)
        statement.bindText(4, entity.notes)
        val _tmp: Int = if (entity.allDay) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        statement.bindLong(6, entity.startDay)
        statement.bindLong(7, entity.startMinute.toLong())
        statement.bindLong(8, entity.endDay)
        statement.bindLong(9, entity.endMinute.toLong())
        statement.bindText(10, entity.frequency)
        statement.bindLong(11, entity.interval.toLong())
        statement.bindLong(12, entity.weekdayMask.toLong())
        val _tmpUntilDay: Long? = entity.untilDay
        if (_tmpUntilDay == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpUntilDay)
        }
        val _tmpCount: Int? = entity.count
        if (_tmpCount == null) {
          statement.bindNull(14)
        } else {
          statement.bindLong(14, _tmpCount.toLong())
        }
        statement.bindLong(15, entity.color.toLong())
        statement.bindLong(16, entity.createdAt)
      }
    }
    this.__insertAdapterOfCalendarExdateEntity = object : EntityInsertAdapter<CalendarExdateEntity>() {
      protected override fun createQuery(): String = "INSERT OR IGNORE INTO `calendar_exdate` (`eventId`,`day`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CalendarExdateEntity) {
        statement.bindLong(1, entity.eventId)
        statement.bindLong(2, entity.day)
      }
    }
    this.__updateAdapterOfCalendarEventEntity = object : EntityDeleteOrUpdateAdapter<CalendarEventEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `calendar_event` SET `id` = ?,`title` = ?,`location` = ?,`notes` = ?,`allDay` = ?,`startDay` = ?,`startMinute` = ?,`endDay` = ?,`endMinute` = ?,`frequency` = ?,`interval` = ?,`weekdayMask` = ?,`untilDay` = ?,`count` = ?,`color` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: CalendarEventEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.location)
        statement.bindText(4, entity.notes)
        val _tmp: Int = if (entity.allDay) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        statement.bindLong(6, entity.startDay)
        statement.bindLong(7, entity.startMinute.toLong())
        statement.bindLong(8, entity.endDay)
        statement.bindLong(9, entity.endMinute.toLong())
        statement.bindText(10, entity.frequency)
        statement.bindLong(11, entity.interval.toLong())
        statement.bindLong(12, entity.weekdayMask.toLong())
        val _tmpUntilDay: Long? = entity.untilDay
        if (_tmpUntilDay == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmpUntilDay)
        }
        val _tmpCount: Int? = entity.count
        if (_tmpCount == null) {
          statement.bindNull(14)
        } else {
          statement.bindLong(14, _tmpCount.toLong())
        }
        statement.bindLong(15, entity.color.toLong())
        statement.bindLong(16, entity.createdAt)
        statement.bindLong(17, entity.id)
      }
    }
  }

  public override suspend fun insert(e: CalendarEventEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfCalendarEventEntity.insertAndReturnId(_connection, e)
    _result
  }

  public override suspend fun insertExdate(x: CalendarExdateEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCalendarExdateEntity.insert(_connection, x)
  }

  public override suspend fun update(e: CalendarEventEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfCalendarEventEntity.handle(_connection, e)
  }

  public override fun observeEvents(): Flow<List<CalendarEventEntity>> {
    val _sql: String = "SELECT * FROM calendar_event"
    return createFlow(__db, false, arrayOf("calendar_event")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfLocation: Int = getColumnIndexOrThrow(_stmt, "location")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfAllDay: Int = getColumnIndexOrThrow(_stmt, "allDay")
        val _columnIndexOfStartDay: Int = getColumnIndexOrThrow(_stmt, "startDay")
        val _columnIndexOfStartMinute: Int = getColumnIndexOrThrow(_stmt, "startMinute")
        val _columnIndexOfEndDay: Int = getColumnIndexOrThrow(_stmt, "endDay")
        val _columnIndexOfEndMinute: Int = getColumnIndexOrThrow(_stmt, "endMinute")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfInterval: Int = getColumnIndexOrThrow(_stmt, "interval")
        val _columnIndexOfWeekdayMask: Int = getColumnIndexOrThrow(_stmt, "weekdayMask")
        val _columnIndexOfUntilDay: Int = getColumnIndexOrThrow(_stmt, "untilDay")
        val _columnIndexOfCount: Int = getColumnIndexOrThrow(_stmt, "count")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<CalendarEventEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CalendarEventEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpLocation: String
          _tmpLocation = _stmt.getText(_columnIndexOfLocation)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpAllDay: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfAllDay).toInt()
          _tmpAllDay = _tmp != 0
          val _tmpStartDay: Long
          _tmpStartDay = _stmt.getLong(_columnIndexOfStartDay)
          val _tmpStartMinute: Int
          _tmpStartMinute = _stmt.getLong(_columnIndexOfStartMinute).toInt()
          val _tmpEndDay: Long
          _tmpEndDay = _stmt.getLong(_columnIndexOfEndDay)
          val _tmpEndMinute: Int
          _tmpEndMinute = _stmt.getLong(_columnIndexOfEndMinute).toInt()
          val _tmpFrequency: String
          _tmpFrequency = _stmt.getText(_columnIndexOfFrequency)
          val _tmpInterval: Int
          _tmpInterval = _stmt.getLong(_columnIndexOfInterval).toInt()
          val _tmpWeekdayMask: Int
          _tmpWeekdayMask = _stmt.getLong(_columnIndexOfWeekdayMask).toInt()
          val _tmpUntilDay: Long?
          if (_stmt.isNull(_columnIndexOfUntilDay)) {
            _tmpUntilDay = null
          } else {
            _tmpUntilDay = _stmt.getLong(_columnIndexOfUntilDay)
          }
          val _tmpCount: Int?
          if (_stmt.isNull(_columnIndexOfCount)) {
            _tmpCount = null
          } else {
            _tmpCount = _stmt.getLong(_columnIndexOfCount).toInt()
          }
          val _tmpColor: Int
          _tmpColor = _stmt.getLong(_columnIndexOfColor).toInt()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = CalendarEventEntity(_tmpId,_tmpTitle,_tmpLocation,_tmpNotes,_tmpAllDay,_tmpStartDay,_tmpStartMinute,_tmpEndDay,_tmpEndMinute,_tmpFrequency,_tmpInterval,_tmpWeekdayMask,_tmpUntilDay,_tmpCount,_tmpColor,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeExdates(): Flow<List<CalendarExdateEntity>> {
    val _sql: String = "SELECT * FROM calendar_exdate"
    return createFlow(__db, false, arrayOf("calendar_exdate")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfEventId: Int = getColumnIndexOrThrow(_stmt, "eventId")
        val _columnIndexOfDay: Int = getColumnIndexOrThrow(_stmt, "day")
        val _result: MutableList<CalendarExdateEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CalendarExdateEntity
          val _tmpEventId: Long
          _tmpEventId = _stmt.getLong(_columnIndexOfEventId)
          val _tmpDay: Long
          _tmpDay = _stmt.getLong(_columnIndexOfDay)
          _item = CalendarExdateEntity(_tmpEventId,_tmpDay)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun `get`(id: Long): CalendarEventEntity? {
    val _sql: String = "SELECT * FROM calendar_event WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfLocation: Int = getColumnIndexOrThrow(_stmt, "location")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfAllDay: Int = getColumnIndexOrThrow(_stmt, "allDay")
        val _columnIndexOfStartDay: Int = getColumnIndexOrThrow(_stmt, "startDay")
        val _columnIndexOfStartMinute: Int = getColumnIndexOrThrow(_stmt, "startMinute")
        val _columnIndexOfEndDay: Int = getColumnIndexOrThrow(_stmt, "endDay")
        val _columnIndexOfEndMinute: Int = getColumnIndexOrThrow(_stmt, "endMinute")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfInterval: Int = getColumnIndexOrThrow(_stmt, "interval")
        val _columnIndexOfWeekdayMask: Int = getColumnIndexOrThrow(_stmt, "weekdayMask")
        val _columnIndexOfUntilDay: Int = getColumnIndexOrThrow(_stmt, "untilDay")
        val _columnIndexOfCount: Int = getColumnIndexOrThrow(_stmt, "count")
        val _columnIndexOfColor: Int = getColumnIndexOrThrow(_stmt, "color")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: CalendarEventEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpLocation: String
          _tmpLocation = _stmt.getText(_columnIndexOfLocation)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          val _tmpAllDay: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfAllDay).toInt()
          _tmpAllDay = _tmp != 0
          val _tmpStartDay: Long
          _tmpStartDay = _stmt.getLong(_columnIndexOfStartDay)
          val _tmpStartMinute: Int
          _tmpStartMinute = _stmt.getLong(_columnIndexOfStartMinute).toInt()
          val _tmpEndDay: Long
          _tmpEndDay = _stmt.getLong(_columnIndexOfEndDay)
          val _tmpEndMinute: Int
          _tmpEndMinute = _stmt.getLong(_columnIndexOfEndMinute).toInt()
          val _tmpFrequency: String
          _tmpFrequency = _stmt.getText(_columnIndexOfFrequency)
          val _tmpInterval: Int
          _tmpInterval = _stmt.getLong(_columnIndexOfInterval).toInt()
          val _tmpWeekdayMask: Int
          _tmpWeekdayMask = _stmt.getLong(_columnIndexOfWeekdayMask).toInt()
          val _tmpUntilDay: Long?
          if (_stmt.isNull(_columnIndexOfUntilDay)) {
            _tmpUntilDay = null
          } else {
            _tmpUntilDay = _stmt.getLong(_columnIndexOfUntilDay)
          }
          val _tmpCount: Int?
          if (_stmt.isNull(_columnIndexOfCount)) {
            _tmpCount = null
          } else {
            _tmpCount = _stmt.getLong(_columnIndexOfCount).toInt()
          }
          val _tmpColor: Int
          _tmpColor = _stmt.getLong(_columnIndexOfColor).toInt()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result = CalendarEventEntity(_tmpId,_tmpTitle,_tmpLocation,_tmpNotes,_tmpAllDay,_tmpStartDay,_tmpStartMinute,_tmpEndDay,_tmpEndMinute,_tmpFrequency,_tmpInterval,_tmpWeekdayMask,_tmpUntilDay,_tmpCount,_tmpColor,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: Long) {
    val _sql: String = "DELETE FROM calendar_event WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
