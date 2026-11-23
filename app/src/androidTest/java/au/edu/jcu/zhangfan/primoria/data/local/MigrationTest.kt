package au.edu.jcu.zhangfan.primoria.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Verifies that learners upgrading from v1 keep their data when content_reports is added. */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PrimoriaDatabase::class.java,
    )

    @Test
    fun migrate1To2_keepsExistingAnswersAndAddsReportsTable() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL("INSERT INTO topics (id, title, title_key, created_at) VALUES (1, 'Algebra', 'algebra', 0)")
            db.execSQL(
                "INSERT INTO questions (id, topic_id, prompt, options, correct_index, explanation, difficulty, source, created_at) " +
                    "VALUES (1, 1, 'Q', '[\"a\",\"b\",\"c\",\"d\"]', 0, 'e', 'STANDARD', 'AI_GENERATED', 0)",
            )
            db.execSQL(
                "INSERT INTO attempts (question_id, topic_id, session_id, chosen_index, is_correct, xp_awarded, answered_at, epoch_day) " +
                    "VALUES (1, 1, 's', 0, 1, 10, 0, 0)",
            )
        }

        helper.runMigrationsAndValidate(TEST_DB, 2, true).use { db ->
            db.query("SELECT COUNT(*) FROM attempts").use { cursor ->
                cursor.moveToFirst()
                assertEquals(1, cursor.getInt(0))
            }
            db.execSQL("INSERT INTO content_reports (question_id, reason, created_at) VALUES (1, 'wrong', 0)")
            db.query("SELECT COUNT(*) FROM content_reports").use { cursor ->
                cursor.moveToFirst()
                assertEquals(1, cursor.getInt(0))
            }
        }
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
