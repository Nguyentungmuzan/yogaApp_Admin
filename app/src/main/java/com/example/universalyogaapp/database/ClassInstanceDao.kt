package com.example.universalyogaapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.universalyogaapp.model.ClassInstance

@Dao
interface ClassInstanceDao {

    @Insert
    suspend fun insertClassInstance(classInstance: ClassInstance)

    @Update
    suspend fun updateClassInstance(classInstance: ClassInstance)

    @Delete
    suspend fun deleteClassInstance(classInstance: ClassInstance)

    @Query("SELECT * FROM class_instances")
    suspend fun getAllClassInstances(): List<ClassInstance>

    // Get all classes belonging to a course
    @Query("SELECT * FROM class_instances WHERE courseId = :courseId")
    suspend fun getClassInstancesByCourseId(courseId: Int): List<ClassInstance>

    // Get specific class by ID
    @Query("SELECT * FROM class_instances WHERE id = :classInstanceId")
    suspend fun getClassInstanceById(classInstanceId: Int): ClassInstance?

    @Query("DELETE FROM class_instances WHERE id = :classInstanceId")
    suspend fun deleteClassInstanceById(classInstanceId: Int)

    @Query("""
    SELECT * FROM class_instances 
    WHERE (:teacherName IS NULL OR teacher LIKE '%' || :teacherName || '%')
    AND (:dayOfWeekOrDate IS NULL OR date = :dayOfWeekOrDate)
""")
    suspend fun searchClasses(teacherName: String?, dayOfWeekOrDate: String?): List<ClassInstance>

    // Insert a list of class instances (for default data)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(classInstances: List<ClassInstance>)

    @Query("DELETE FROM class_instances ")
    suspend fun deleteAllClasses()

}
