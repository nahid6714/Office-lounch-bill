package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BillItem
import com.example.data.BillRepository
import com.example.data.FoodBillUiModel
import com.example.util.BengaliUtils
import com.example.util.QuickPreset
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FoodBillViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BillRepository

    init {
        val dao = AppDatabase.getDatabase(application).foodBillDao()
        repository = BillRepository(dao)
    }

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    private val _currentBillState = MutableStateFlow(CurrentBillState())
    val currentBillState: StateFlow<CurrentBillState> = _currentBillState.asStateFlow()

    private val _historyBills = MutableStateFlow<List<FoodBillUiModel>>(emptyList())
    val historyBills: StateFlow<List<FoodBillUiModel>> = _historyBills.asStateFlow()

    private val _quickPresets = MutableStateFlow<List<QuickPreset>>(emptyList())
    val quickPresets: StateFlow<List<QuickPreset>> = _quickPresets.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

    private val prefs = application.getSharedPreferences("food_bill_prefs", android.content.Context.MODE_PRIVATE)

    init {
        // Load Quick Presets from SharedPreferences
        loadQuickPresets()

        // Default initial items corresponding to the user's template
        resetToInitialTemplate()

        viewModelScope.launch {
            repository.allBills.collectLatest { bills ->
                _historyBills.value = bills
            }
        }
    }

    private fun loadQuickPresets() {
        val jsonStr = prefs.getString("quick_presets_json", null)
        if (jsonStr.isNullOrEmpty()) {
            _quickPresets.value = emptyList()
        } else {
            try {
                val array = org.json.JSONArray(jsonStr)
                val list = mutableListOf<QuickPreset>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val name = obj.optString("name", "")
                    val qty = obj.optString("defaultQty", "")
                    val rate = obj.optString("defaultRate", "")
                    val amount = obj.optString("defaultAmount", "")
                    if (name.isNotBlank()) {
                        list.add(QuickPreset(name, qty, rate, amount))
                    }
                }
                _quickPresets.value = list
            } catch (e: Exception) {
                _quickPresets.value = emptyList()
            }
        }
    }

    private fun saveQuickPresetsToPrefs(presets: List<QuickPreset>) {
        try {
            val array = org.json.JSONArray()
            presets.forEach { p ->
                val obj = org.json.JSONObject()
                obj.put("name", p.name)
                obj.put("defaultQty", p.defaultQty)
                obj.put("defaultRate", p.defaultRate)
                obj.put("defaultAmount", p.defaultAmount)
                array.put(obj)
            }
            prefs.edit().putString("quick_presets_json", array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addCustomQuickPreset(name: String, qty: String, rate: String = "", amount: String = "") {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        val currentList = _quickPresets.value.toMutableList()
        // Replace if already exists or add to beginning
        val existingIndex = currentList.indexOfFirst { it.name.equals(trimmedName, ignoreCase = true) }
        val newPreset = QuickPreset(trimmedName, qty.trim(), rate.trim(), amount.trim())
        if (existingIndex != -1) {
            currentList[existingIndex] = newPreset
        } else {
            currentList.add(0, newPreset)
        }

        _quickPresets.value = currentList
        saveQuickPresetsToPrefs(currentList)
        viewModelScope.launch { _uiEvent.emit("'$trimmedName' দ্রুত তালিকায় যোগ করা হয়েছে") }
    }

    fun removeQuickPreset(preset: QuickPreset) {
        val currentList = _quickPresets.value.filterNot { it.name == preset.name }
        _quickPresets.value = currentList
        saveQuickPresetsToPrefs(currentList)
        viewModelScope.launch { _uiEvent.emit("'${preset.name}' দ্রুত তালিকা থেকে সরানো হয়েছে") }
    }

    fun resetQuickPresetsToDefault() {
        _quickPresets.value = emptyList()
        saveQuickPresetsToPrefs(emptyList())
        viewModelScope.launch { _uiEvent.emit("প্রিসেট তালিকা রিসেট করা হয়েছে") }
    }

    fun saveSettings(centerName: String, subtitle: String, purchaserLabel: String) {
        prefs.edit()
            .putString("saved_center_name", centerName.trim())
            .putString("saved_subtitle", subtitle.trim())
            .putString("saved_purchaser_label", purchaserLabel.trim())
            .apply()
        _currentBillState.update {
            it.copy(
                centerName = centerName.trim(),
                subtitle = subtitle.trim(),
                purchaserLabel = purchaserLabel.trim()
            )
        }
        viewModelScope.launch { _uiEvent.emit("সেটিংস সফলভাবে সংরক্ষণ করা হয়েছে!") }
    }

    fun resetAllUserData() {
        prefs.edit().clear().apply()
        _quickPresets.value = emptyList()
        resetToInitialTemplate()
        viewModelScope.launch { _uiEvent.emit("সমস্ত সংরক্ষিত তথ্য রিসেট করা হয়েছে") }
    }

    fun resetToInitialTemplate() {
        val todayStr = dateFormat.format(Date())
        val savedCenterName = prefs.getString("saved_center_name", "") ?: ""
        val savedSubtitle = prefs.getString("saved_subtitle", "") ?: ""
        val savedPurchaserLabel = prefs.getString("saved_purchaser_label", "") ?: ""

        _currentBillState.value = CurrentBillState(
            editingBillId = 0L,
            dateString = todayStr,
            centerName = savedCenterName,
            subtitle = savedSubtitle,
            purchaserName = "",
            purchaserLabel = savedPurchaserLabel,
            items = listOf(
                BillItem(name = "", quantity = "", rate = "0", amount = 0.0)
            )
        )
    }

    fun updateDate(newDate: String) {
        _currentBillState.update { it.copy(dateString = newDate) }
    }

    fun updateCenterName(newName: String) {
        _currentBillState.update { it.copy(centerName = newName) }
    }

    fun updateSubtitle(newSubtitle: String) {
        _currentBillState.update { it.copy(subtitle = newSubtitle) }
    }

    fun updatePurchaserName(name: String) {
        _currentBillState.update { it.copy(purchaserName = name) }
    }

    fun updatePurchaserLabel(label: String) {
        _currentBillState.update { it.copy(purchaserLabel = label) }
    }

    fun addItemRow() {
        _currentBillState.update { state ->
            if (state.items.size >= 18) {
                viewModelScope.launch { _uiEvent.emit("সর্বোচ্চ ১৮ টি আইটেম যোগ করা সম্ভব") }
                return@update state
            }
            val updatedItems = state.items.toMutableList()
            updatedItems.add(BillItem(name = "", quantity = "", rate = "0", amount = 0.0))
            state.copy(items = updatedItems)
        }
    }

    fun addQuickPresetItem(name: String, qty: String, rate: String = "", amount: String = "") {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return

        val rateVal = BengaliUtils.parseBengaliNumber(rate)
        val amountVal = BengaliUtils.parseBengaliNumber(amount)
        val qtyVal = extractNumber(qty)

        val calcAmount = when {
            amountVal > 0 -> amountVal
            rateVal > 0 -> if (qtyVal > 0) rateVal * qtyVal else rateVal
            else -> 0.0
        }

        val calcRate = when {
            rate.isNotBlank() && rate != "0" -> rate
            amountVal > 0 && qtyVal > 0 -> {
                val r = amountVal / qtyVal
                val str = if (r % 1.0 == 0.0) r.toLong().toString() else String.format(Locale.US, "%.1f", r)
                BengaliUtils.toBengaliDigits(str)
            }
            else -> "0"
        }

        _currentBillState.update { state ->
            val updatedItems = state.items.toMutableList()
            val existingIndex = updatedItems.indexOfFirst { it.name.trim() == trimmedName && it.name.isNotBlank() }
            if (existingIndex != -1) {
                // Item already in memo -> Unselect / Remove
                if (updatedItems.size > 1) {
                    updatedItems.removeAt(existingIndex)
                } else {
                    updatedItems[0] = BillItem(id = updatedItems[0].id, name = "", quantity = "", rate = "0", amount = 0.0)
                }
                return@update state.copy(items = updatedItems)
            }

            // Item not in memo -> Add to memo
            val emptyIndex = updatedItems.indexOfFirst { it.name.isBlank() && it.quantity.isBlank() }
            val newItem = BillItem(name = trimmedName, quantity = qty, rate = calcRate, amount = calcAmount)
            if (emptyIndex != -1) {
                updatedItems[emptyIndex] = newItem
            } else {
                if (updatedItems.size >= 18) {
                    viewModelScope.launch { _uiEvent.emit("সর্বোচ্চ ১৮ টি আইটেম যোগ করা সম্ভব") }
                    return@update state
                }
                updatedItems.add(newItem)
            }
            state.copy(items = updatedItems)
        }
    }

    fun updateItemName(id: String, name: String) {
        _currentBillState.update { state ->
            val updated = state.items.map { item ->
                if (item.id == id) item.copy(name = name) else item
            }
            state.copy(items = updated)
        }
    }

    fun updateItemQuantity(id: String, qty: String) {
        _currentBillState.update { state ->
            val updated = state.items.map { item ->
                if (item.id == id) item.copy(quantity = qty) else item
            }
            state.copy(items = updated)
        }
    }

    fun updateItemRate(id: String, rate: String) {
        _currentBillState.update { state ->
            val updated = state.items.map { item ->
                if (item.id == id) {
                    val rateVal = BengaliUtils.parseBengaliNumber(rate)
                    val qtyVal = extractNumber(item.quantity)
                    val calcAmount = if (qtyVal > 0) rateVal * qtyVal else rateVal
                    item.copy(rate = rate, amount = calcAmount)
                } else item
            }
            state.copy(items = updated)
        }
    }

    fun updateItemAmount(id: String, amountStr: String) {
        _currentBillState.update { state ->
            val amountVal = BengaliUtils.parseBengaliNumber(amountStr)
            val updated = state.items.map { item ->
                if (item.id == id) item.copy(amount = amountVal) else item
            }
            state.copy(items = updated)
        }
    }

    fun removeItemRow(id: String) {
        _currentBillState.update { state ->
            if (state.items.size <= 1) {
                // If only 1 row left, reset it instead of deleting
                state.copy(items = listOf(BillItem(name = "", quantity = "", rate = "0", amount = 0.0)))
            } else {
                state.copy(items = state.items.filterNot { it.id == id })
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveCurrentBill() {
        val currentState = _currentBillState.value
        val validItems = currentState.items.filter { it.name.isNotBlank() || it.amount > 0 }
        
        if (validItems.isEmpty()) {
            viewModelScope.launch { _uiEvent.emit("কমপক্ষে একটি আইটেমের বিবরণ বা টাকা লিখুন") }
            return
        }

        viewModelScope.launch {
            val total = validItems.sumOf { it.amount }
            val dateObj = try { dateFormat.parse(currentState.dateString) } catch (e: Exception) { null }
            val timestamp = dateObj?.time ?: System.currentTimeMillis()

            val id = repository.saveBill(
                id = currentState.editingBillId,
                dateString = currentState.dateString,
                timestamp = timestamp,
                purchaserName = currentState.purchaserName,
                note = "",
                items = validItems,
                totalAmount = total
            )

            _currentBillState.update { it.copy(editingBillId = id) }
            _uiEvent.emit("বিল সফলভাবে সংরক্ষণ করা হয়েছে!")
        }
    }

    fun loadBillForEditing(bill: FoodBillUiModel) {
        val savedCenterName = prefs.getString("saved_center_name", "") ?: ""
        val savedSubtitle = prefs.getString("saved_subtitle", "") ?: ""
        val savedPurchaserLabel = prefs.getString("saved_purchaser_label", "") ?: ""

        _currentBillState.value = CurrentBillState(
            editingBillId = bill.id,
            dateString = bill.dateString,
            centerName = savedCenterName,
            subtitle = savedSubtitle,
            purchaserName = bill.purchaserName,
            purchaserLabel = savedPurchaserLabel,
            items = bill.items.ifEmpty { listOf(BillItem(name = "", quantity = "", rate = "0", amount = 0.0)) }
        )
        viewModelScope.launch {
            _uiEvent.emit("${bill.dateString} তারিখের বিল লোড করা হয়েছে")
        }
    }

    fun deleteBill(id: Long) {
        viewModelScope.launch {
            repository.deleteBill(id)
            if (_currentBillState.value.editingBillId == id) {
                resetToInitialTemplate()
            }
            _uiEvent.emit("বিলটি মুছে ফেলা হয়েছে")
        }
    }

    private fun extractNumber(str: String): Double {
        val englishStr = BengaliUtils.toEnglishDigits(str)
        val regex = Regex("""\d+(\.\d+)?""")
        val match = regex.find(englishStr)
        return match?.value?.toDoubleOrNull() ?: 0.0
    }
}

data class CurrentBillState(
    val editingBillId: Long = 0L,
    val dateString: String = "",
    val centerName: String = "",
    val subtitle: String = "",
    val purchaserName: String = "",
    val purchaserLabel: String = "",
    val approverLabel: String = "",
    val items: List<BillItem> = emptyList()
) {
    val totalAmount: Double
        get() = items.sumOf { it.amount }
}
