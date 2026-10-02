package app.maximus

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.maximus.core.security.DatabaseKeyManager
import app.maximus.core.security.SqlCipherKeyFormat
import java.io.File
import java.security.SecureRandom
import java.util.UUID
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseEncryptionTest {

    private lateinit var context: Context
    private val dbName = "enc_test_" + UUID.randomUUID() + ".db"
    private val secret = "geheim-7f3a9c"

    @Before
    fun setUp() {
        System.loadLibrary("sqlcipher")
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    private fun helper(key: ByteArray): SupportSQLiteOpenHelper =
        SupportOpenHelperFactory(key).create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) = Unit
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )

    private fun randomKey(): ByteArray =
        SqlCipherKeyFormat.rawKeyLiteral(ByteArray(32).also(SecureRandom()::nextBytes))

    @Test
    fun fileIsEncryptedAndWrongKeyFails() {
        val keyA = randomKey()
        val keyB = randomKey()

        helper(keyA.copyOf()).also { h ->
            h.writableDatabase.apply {
                execSQL("CREATE TABLE t(v TEXT)")
                execSQL("INSERT INTO t VALUES('$secret')")
            }
            h.close()
        }

        val bytes = context.getDatabasePath(dbName).readBytes()
        val magic = "SQLite format 3\u0000".toByteArray(Charsets.US_ASCII)
        assertFalse(bytes.copyOf(magic.size).contentEquals(magic))
        assertFalse(String(bytes, Charsets.ISO_8859_1).contains(secret))

        val wrong = helper(keyB)
        assertThrows(Exception::class.java) { wrong.writableDatabase.query("SELECT count(*) FROM t").close() }
        wrong.close()

        helper(keyA.copyOf()).also { h ->
            h.readableDatabase.query("SELECT v FROM t").use { c ->
                c.moveToFirst()
                assertEquals(secret, c.getString(0))
            }
            h.close()
        }
    }

    @Test
    fun keyManagerIsStableAcrossInstances() {
        val dir = File(context.cacheDir, "keytest-" + UUID.randomUUID()).apply { mkdirs() }
        val alias = "test.kek." + UUID.randomUUID()
        try {
            val first = DatabaseKeyManager(dir, alias, "k.enc").obtainSqlCipherKey()
            val second = DatabaseKeyManager(dir, alias, "k.enc").obtainSqlCipherKey()
            assertEquals(SqlCipherKeyFormat.LITERAL_LENGTH, first.size)
            assertArrayEquals(first, second)
        } finally {
            dir.deleteRecursively()
            app.maximus.core.security.KeystoreAead(alias).deleteKey()
        }
    }
}
