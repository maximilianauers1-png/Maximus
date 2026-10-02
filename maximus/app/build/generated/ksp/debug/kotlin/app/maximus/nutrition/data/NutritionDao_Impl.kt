package app.maximus.nutrition.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Double
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
public class NutritionDao_Impl(
  __db: RoomDatabase,
) : NutritionDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfNutritionLogEntity: EntityUpsertAdapter<NutritionLogEntity>
  init {
    this.__db = __db
    this.__upsertAdapterOfNutritionLogEntity = EntityUpsertAdapter<NutritionLogEntity>(object : EntityInsertAdapter<NutritionLogEntity>() {
      protected override fun createQuery(): String = "INSERT INTO `nutrition_log` (`epochDay`,`weightKg`,`kcal`,`proteinG`,`note`) VALUES (?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: NutritionLogEntity) {
        statement.bindLong(1, entity.epochDay)
        val _tmpWeightKg: Double? = entity.weightKg
        if (_tmpWeightKg == null) {
          statement.bindNull(2)
        } else {
          statement.bindDouble(2, _tmpWeightKg)
        }
        val _tmpKcal: Double? = entity.kcal
        if (_tmpKcal == null) {
          statement.bindNull(3)
        } else {
          statement.bindDouble(3, _tmpKcal)
        }
        val _tmpProteinG: Double? = entity.proteinG
        if (_tmpProteinG == null) {
          statement.bindNull(4)
        } else {
          statement.bindDouble(4, _tmpProteinG)
        }
        statement.bindText(5, entity.note)
      }
    }, object : EntityDeleteOrUpdateAdapter<NutritionLogEntity>() {
      protected override fun createQuery(): String = "UPDATE `nutrition_log` SET `epochDay` = ?,`weightKg` = ?,`kcal` = ?,`proteinG` = ?,`note` = ? WHERE `epochDay` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: NutritionLogEntity) {
        statement.bindLong(1, entity.epochDay)
        val _tmpWeightKg: Double? = entity.weightKg
        if (_tmpWeightKg == null) {
          statement.bindNull(2)
        } else {
          statement.bindDouble(2, _tmpWeightKg)
        }
        val _tmpKcal: Double? = entity.kcal
        if (_tmpKcal == null) {
          statement.bindNull(3)
        } else {
          statement.bindDouble(3, _tmpKcal)
        }
        val _tmpProteinG: Double? = entity.proteinG
        if (_tmpProteinG == null) {
          statement.bindNull(4)
        } else {
          statement.bindDouble(4, _tmpProteinG)
        }
        statement.bindText(5, entity.note)
        statement.bindLong(6, entity.epochDay)
      }
    })
  }

  public override suspend fun upsert(e: NutritionLogEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfNutritionLogEntity.upsert(_connection, e)
  }

  public override fun observeLog(): Flow<List<NutritionLogEntity>> {
    val _sql: String = "SELECT * FROM nutrition_log ORDER BY epochDay"
    return createFlow(__db, false, arrayOf("nutrition_log")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfEpochDay: Int = getColumnIndexOrThrow(_stmt, "epochDay")
        val _columnIndexOfWeightKg: Int = getColumnIndexOrThrow(_stmt, "weightKg")
        val _columnIndexOfKcal: Int = getColumnIndexOrThrow(_stmt, "kcal")
        val _columnIndexOfProteinG: Int = getColumnIndexOrThrow(_stmt, "proteinG")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _result: MutableList<NutritionLogEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: NutritionLogEntity
          val _tmpEpochDay: Long
          _tmpEpochDay = _stmt.getLong(_columnIndexOfEpochDay)
          val _tmpWeightKg: Double?
          if (_stmt.isNull(_columnIndexOfWeightKg)) {
            _tmpWeightKg = null
          } else {
            _tmpWeightKg = _stmt.getDouble(_columnIndexOfWeightKg)
          }
          val _tmpKcal: Double?
          if (_stmt.isNull(_columnIndexOfKcal)) {
            _tmpKcal = null
          } else {
            _tmpKcal = _stmt.getDouble(_columnIndexOfKcal)
          }
          val _tmpProteinG: Double?
          if (_stmt.isNull(_columnIndexOfProteinG)) {
            _tmpProteinG = null
          } else {
            _tmpProteinG = _stmt.getDouble(_columnIndexOfProteinG)
          }
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          _item = NutritionLogEntity(_tmpEpochDay,_tmpWeightKg,_tmpKcal,_tmpProteinG,_tmpNote)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(day: Long) {
    val _sql: String = "DELETE FROM nutrition_log WHERE epochDay = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, day)
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
