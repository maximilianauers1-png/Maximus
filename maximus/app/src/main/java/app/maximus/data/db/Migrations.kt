package app.maximus.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 (P0) -> v2 (P2 Strongman). Only adds tables; SQL mirrors Room's generated schema
 * (column affinities, NOT NULL, primary keys, foreign keys and index names) so that
 * Room's post-migration TableInfo validation passes.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `exercise` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`seedKey` TEXT, `nameDe` TEXT NOT NULL, `nameEn` TEXT NOT NULL, `category` TEXT NOT NULL, " +
                "`isEvent` INTEGER NOT NULL, `defaultMode` TEXT NOT NULL, `notes` TEXT NOT NULL)"
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_exercise_seedKey` ON `exercise` (`seedKey`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `program` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, `isTemplate` INTEGER NOT NULL, `deloadEvery` INTEGER NOT NULL, " +
                "`weekCount` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `program_day` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`programId` INTEGER NOT NULL, `week` INTEGER NOT NULL, `dayIndex` INTEGER NOT NULL, `name` TEXT NOT NULL, " +
                "FOREIGN KEY(`programId`) REFERENCES `program`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_program_day_programId` ON `program_day` (`programId`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `program_exercise` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`dayId` INTEGER NOT NULL, `exerciseId` INTEGER NOT NULL, `position` INTEGER NOT NULL, `rule` TEXT NOT NULL, " +
                "`ruleA` REAL NOT NULL, `ruleB` REAL NOT NULL, `ruleC` INTEGER NOT NULL, " +
                "FOREIGN KEY(`dayId`) REFERENCES `program_day`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_program_exercise_dayId` ON `program_exercise` (`dayId`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `set_prescription` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`programExerciseId` INTEGER NOT NULL, `position` INTEGER NOT NULL, `reps` INTEGER NOT NULL, " +
                "`loadType` TEXT NOT NULL, `loadValue` REAL NOT NULL, `restSeconds` INTEGER NOT NULL, `note` TEXT NOT NULL, " +
                "`distanceM` REAL, `heightCm` REAL, `implementKg` REAL, `timeCapS` INTEGER, `eventMode` TEXT, " +
                "FOREIGN KEY(`programExerciseId`) REFERENCES `program_exercise`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_set_prescription_programExerciseId` ON `set_prescription` (`programExerciseId`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `workout_session` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`epochDay` INTEGER NOT NULL, `note` TEXT NOT NULL)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `set_log` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`sessionId` INTEGER NOT NULL, `exerciseId` INTEGER NOT NULL, `epochDay` INTEGER NOT NULL, " +
                "`position` INTEGER NOT NULL, `weightKg` REAL NOT NULL, `reps` INTEGER NOT NULL, `rpe` REAL, " +
                "`distanceM` REAL, `heightCm` REAL, `timeS` REAL, `e1rm` REAL, `isE1rmPr` INTEGER NOT NULL, " +
                "`isRepPr` INTEGER NOT NULL, " +
                "FOREIGN KEY(`sessionId`) REFERENCES `workout_session`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_set_log_sessionId` ON `set_log` (`sessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_set_log_exerciseId` ON `set_log` (`exerciseId`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `f_table_override` (`reps` INTEGER NOT NULL, `rpeTenths` INTEGER NOT NULL, " +
                "`fraction` REAL NOT NULL, PRIMARY KEY(`reps`, `rpeTenths`))"
        )
    }
}

/**
 * v2 (P2) -> v3 (P1 core: vault, calendar, notebook). Only adds tables; SQL mirrors Room's
 * generated schema so the post-migration TableInfo validation passes.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `vault_entry` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`uid` TEXT NOT NULL, `title` TEXT NOT NULL, `username` TEXT NOT NULL, `url` TEXT NOT NULL, " +
                "`favorite` INTEGER NOT NULL, `payload` BLOB NOT NULL, `createdAt` INTEGER NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL, `passwordChangedAt` INTEGER NOT NULL)"
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_vault_entry_uid` ON `vault_entry` (`uid`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `calendar_event` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`title` TEXT NOT NULL, `location` TEXT NOT NULL, `notes` TEXT NOT NULL, `allDay` INTEGER NOT NULL, " +
                "`startDay` INTEGER NOT NULL, `startMinute` INTEGER NOT NULL, `endDay` INTEGER NOT NULL, " +
                "`endMinute` INTEGER NOT NULL, `frequency` TEXT NOT NULL, `interval` INTEGER NOT NULL, " +
                "`weekdayMask` INTEGER NOT NULL, `untilDay` INTEGER, `count` INTEGER, `color` INTEGER NOT NULL, " +
                "`createdAt` INTEGER NOT NULL)"
        )

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `calendar_exdate` (`eventId` INTEGER NOT NULL, `day` INTEGER NOT NULL, " +
                "PRIMARY KEY(`eventId`, `day`), FOREIGN KEY(`eventId`) REFERENCES `calendar_event`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_calendar_exdate_eventId` ON `calendar_exdate` (`eventId`)")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `note` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`title` TEXT NOT NULL, `body` TEXT NOT NULL, `tags` TEXT NOT NULL, `pinned` INTEGER NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)"
        )
    }
}

/** v3 (P1) -> v4 (P3 nutrition): daily log table. */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `nutrition_log` (`epochDay` INTEGER NOT NULL, `weightKg` REAL, `kcal` REAL, " +
                "`proteinG` REAL, `note` TEXT NOT NULL, PRIMARY KEY(`epochDay`))"
        )
    }
}

/** v4 (P3) -> v5 (P4 D&D): characters, monsters and the roll history. */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `dnd_character` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, `summary` TEXT NOT NULL, `payload` TEXT NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `dnd_monster` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, `challengeRating` REAL NOT NULL, `payload` TEXT NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `dnd_roll` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`expression` TEXT NOT NULL, `label` TEXT NOT NULL, `total` INTEGER NOT NULL, `detail` TEXT NOT NULL, " +
                "`epochMillis` INTEGER NOT NULL, `favorite` INTEGER NOT NULL)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_dnd_roll_epochMillis` ON `dnd_roll` (`epochMillis`)")
    }
}
