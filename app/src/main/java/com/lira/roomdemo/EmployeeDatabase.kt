package com.lira.roomdemo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EmployeeEntity::class], version = 1)
abstract class EmployeeDatabase: RoomDatabase() {

    abstract fun employeeDao():EmployeeDao

    // Precisa definir um companion object, que vai permitir que adicionemos funções nessa employee database
    // Isso vai manter uma referência pra qualquer database retornada por get instance
    // O que vai evitar inicializar a database repetidas vezes, o que não é eficiente em termos de performance.
    companion object{
        @Volatile
        private var INSTANCE: EmployeeDatabase? = null

        fun getInstance(context: Context): EmployeeDatabase{

            synchronized(this){
                var instance = INSTANCE

                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext, EmployeeDatabase::class.java, "employee_database").fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }

    }
}