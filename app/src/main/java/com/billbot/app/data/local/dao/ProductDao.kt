package com.billbot.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.billbot.app.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE productCode = :code LIMIT 1")
    suspend fun getProductByCode(code: String): ProductEntity?

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR productCode LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProductsFlow(query: String): Flow<List<ProductEntity>>

    @Query("UPDATE products SET stockQuantity = stockQuantity - :quantity WHERE id = :productId AND stockQuantity >= :quantity")
    suspend fun decrementStock(productId: Long, quantity: Int): Int

    @Query("UPDATE products SET stockQuantity = :newStock WHERE id = :productId")
    suspend fun updateStock(productId: Long, newStock: Int)

    @Query("SELECT COUNT(*) FROM products")
    fun getProductCountFlow(): Flow<Int>
}
