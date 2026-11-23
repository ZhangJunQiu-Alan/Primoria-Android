package au.edu.jcu.zhangfan.primoria.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ReportDao {
    @Insert
    suspend fun insert(report: ContentReportEntity): Long

    @Query("SELECT COUNT(*) FROM content_reports")
    suspend fun count(): Int
}
