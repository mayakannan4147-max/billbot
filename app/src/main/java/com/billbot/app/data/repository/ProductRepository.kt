package com.billbot.app.data.repository

import com.billbot.app.data.local.dao.ProductDao
import com.billbot.app.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProductsFlow()

    fun searchProducts(query: String): Flow<List<ProductEntity>> {
        return if (query.isBlank()) {
            productDao.getAllProductsFlow()
        } else {
            productDao.searchProductsFlow(query.trim())
        }
    }

    suspend fun getProductById(id: Long): ProductEntity? = productDao.getProductById(id)

    suspend fun getProductByCode(code: String): ProductEntity? = productDao.getProductByCode(code)

    suspend fun insertProduct(product: ProductEntity): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)

    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)

    suspend fun updateStock(productId: Long, newStock: Int) = productDao.updateStock(productId, newStock)

    val productCount: Flow<Int> = productDao.getProductCountFlow()
}
