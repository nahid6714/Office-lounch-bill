package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BillRepository(private val dao: FoodBillDao) {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val listType = Types.newParameterizedType(List::class.java, BillItem::class.java)
    private val jsonAdapter = moshi.adapter<List<BillItem>>(listType)

    val allBills: Flow<List<FoodBillUiModel>> = dao.getAllBills().map { list ->
        list.map { entity -> entity.toUiModel(jsonAdapter) }
    }

    val totalSpentAllTime: Flow<Double> = dao.getTotalSpentAllTime().map { it ?: 0.0 }

    suspend fun saveBill(
        id: Long = 0,
        dateString: String,
        timestamp: Long,
        purchaserName: String,
        note: String,
        items: List<BillItem>,
        totalAmount: Double
    ): Long {
        val itemsJson = jsonAdapter.toJson(items)
        val entity = FoodBillEntity(
            id = id,
            dateString = dateString,
            timestamp = timestamp,
            purchaserName = purchaserName,
            note = note,
            totalAmount = totalAmount,
            itemsJson = itemsJson
        )
        return if (id == 0L) {
            dao.insertBill(entity)
        } else {
            dao.updateBill(entity)
            id
        }
    }

    suspend fun deleteBill(id: Long) {
        dao.deleteBillById(id)
    }

    suspend fun getBillById(id: Long): FoodBillUiModel? {
        val entity = dao.getBillById(id) ?: return null
        return entity.toUiModel(jsonAdapter)
    }

    private fun FoodBillEntity.toUiModel(adapter: com.squareup.moshi.JsonAdapter<List<BillItem>>): FoodBillUiModel {
        val parsedItems = try {
            adapter.fromJson(itemsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        return FoodBillUiModel(
            id = id,
            dateString = dateString,
            timestamp = timestamp,
            purchaserName = purchaserName,
            note = note,
            totalAmount = totalAmount,
            items = parsedItems
        )
    }
}

data class FoodBillUiModel(
    val id: Long,
    val dateString: String,
    val timestamp: Long,
    val purchaserName: String,
    val note: String,
    val totalAmount: Double,
    val items: List<BillItem>
)
