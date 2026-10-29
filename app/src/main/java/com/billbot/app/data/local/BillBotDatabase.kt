package com.billbot.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.billbot.app.data.local.dao.BusinessSettingsDao
import com.billbot.app.data.local.dao.CustomerDao
import com.billbot.app.data.local.dao.InvoiceDao
import com.billbot.app.data.local.dao.ProductDao
import com.billbot.app.data.local.entity.BusinessSettingsEntity
import com.billbot.app.data.local.entity.CustomerEntity
import com.billbot.app.data.local.entity.InvoiceEntity
import com.billbot.app.data.local.entity.InvoiceItemEntity
import com.billbot.app.data.local.entity.ProductEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        BusinessSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BillBotDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun businessSettingsDao(): BusinessSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: BillBotDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BillBotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BillBotDatabase::class.java,
                    "billbot_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: BillBotDatabase) {
                // Pre-populate business profile
                database.businessSettingsDao().insertOrUpdate(
                    BusinessSettingsEntity(
                        id = 1,
                        shopName = "BillBot Retail Mart",
                        address = "Shop 12, Main Commercial Hub, MG Road, Bengaluru - 560001",
                        phone = "+91 98765 43210",
                        email = "support@billbotmart.in",
                        gstin = "29AAAAA0000A1Z5",
                        invoicePrefix = "INV-",
                        nextInvoiceSequence = 1001,
                        defaultTaxRate = 18.0,
                        isTaxEnabled = true,
                        currencySymbol = "₹",
                        termsAndConditions = "Goods once sold will not be taken back. Subject to local jurisdiction. Thank you for your business!"
                    )
                )

                // Pre-populate some starter inventory
                val starterProducts = listOf(
                    ProductEntity(productCode = "PRD-101", name = "Basmati Rice (1kg)", sellingPrice = 120.0, stockQuantity = 50, unit = "kg"),
                    ProductEntity(productCode = "PRD-102", name = "Organic Wheat Flour (5kg)", sellingPrice = 275.0, stockQuantity = 40, unit = "pack"),
                    ProductEntity(productCode = "PRD-103", name = "Pure Dairy Milk (1L)", sellingPrice = 64.0, stockQuantity = 30, unit = "litre"),
                    ProductEntity(productCode = "PRD-104", name = "Refined Sunflower Oil (1L)", sellingPrice = 145.0, stockQuantity = 25, unit = "bottle"),
                    ProductEntity(productCode = "PRD-105", name = "Premium Green Tea (250g)", sellingPrice = 190.0, stockQuantity = 20, unit = "box"),
                    ProductEntity(productCode = "PRD-106", name = "Refined Sugar (1kg)", sellingPrice = 48.0, stockQuantity = 60, unit = "kg"),
                    ProductEntity(productCode = "PRD-107", name = "Roasted Coffee Beans (500g)", sellingPrice = 350.0, stockQuantity = 15, unit = "pack")
                )
                for (p in starterProducts) {
                    database.productDao().insertProduct(p)
                }

                // Pre-populate starter customers
                val starterCustomers = listOf(
                    CustomerEntity(name = "Rajesh Sharma", phone = "+91 98111 22334", email = "rajesh.sharma@example.com", address = "Indiranagar, Bengaluru"),
                    CustomerEntity(name = "Priya Sundaram", phone = "+91 98222 33445", email = "priya.s@example.com", address = "Koramangala, Bengaluru"),
                    CustomerEntity(name = "Amit Patel", phone = "+91 98333 44556", email = "amit.patel@example.com", address = "Whitefield, Bengaluru")
                )
                for (c in starterCustomers) {
                    database.customerDao().insertCustomer(c)
                }
            }
        }
    }
}
