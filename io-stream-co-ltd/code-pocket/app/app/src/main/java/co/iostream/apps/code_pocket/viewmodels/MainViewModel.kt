package co.iostream.apps.code_pocket.viewmodels

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import co.iostream.apps.code_pocket.data.entities.CodeItemEntity
import co.iostream.apps.code_pocket.domain.models.CodeItem
import co.iostream.apps.code_pocket.data.repositories.ICodeItemRepository
import co.iostream.apps.code_pocket.domain.models.CodeItemLabel
import co.iostream.apps.code_pocket.domain.models.CodeItemZone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.*
import java.time.LocalDateTime
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.iostream.apps.code_pocket.ui.screens.calculateDelay
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule

data class CodeItemEntity(val id: Int, val name: String)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val codeItemRepository: ICodeItemRepository
) : ViewModel() {
    // region Scanning control properties

    private var _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private var _isCameraScanning = MutableStateFlow(false)
    val isCameraScanning = _isCameraScanning.asStateFlow()

    private var _scannedResult = MutableStateFlow(String())
    val scannedResult = _scannedResult.asStateFlow()

    private var _currentCode = MutableStateFlow("")
    val currentCode = _currentCode.asStateFlow()

    fun isItemEmpty():Boolean{
        return (_items.all { it.isDeleted } || _items.isEmpty())
    }
    fun isTrashEmpty():Boolean{
        return (_items.all { !it.isDeleted } || _items.isEmpty())
    }
    fun enableScanning(value: Boolean) {
        _isScanning.value = value
    }

    fun enableCameraScanning(value: Boolean) {
        _isCameraScanning.value = value
    }

    fun setScannedResult(value: String) {
        _scannedResult.value = value
    }

    fun setCurrentCode(value: String) {
        _currentCode.value = value
    }


    //endregion

    //region Data plane properties

    private var _currentZone = MutableStateFlow(CodeItemZone.ME)
    val currentZone = _currentZone.asStateFlow()

    private var _items = mutableStateListOf<CodeItemEntity>()
    val items = MutableStateFlow(_items)

    private var _searchKeyword = MutableStateFlow(String())
    val searchKeyword = _searchKeyword.asStateFlow()

    private var _selectedItem = MutableStateFlow<CodeItemEntity?>(null)
    val selectedItem = _selectedItem.asStateFlow()

    private var _currentEditingItem = MutableStateFlow<CodeItemEntity?>(null)
    val currentEditingItem = _currentEditingItem.asStateFlow()

    private var _deletedItems = mutableStateListOf<CodeItemEntity>()
    val deletedItems = MutableStateFlow(_deletedItems)

    private var deletionTimestamps = mutableMapOf<Int, LocalDateTime>()

    fun setSearchKeyword(value: String) {
        _searchKeyword.value = value
    }

    fun setSelectedItem(item: CodeItemEntity?) {
        _selectedItem.value = item
    }

    fun setCurrentEditingItem(item: CodeItemEntity?) {
        _currentEditingItem.value = item
    }

    fun setCurrentZone(item: CodeItemZone) {
        _currentZone.value = item
    }

    fun setZone(item: CodeItemEntity){
        item.zone = if(item.zone == CodeItemZone.ME) CodeItemZone.OTHERS else CodeItemZone.ME
    }

    suspend fun getByZone(zone: CodeItemZone) {
        viewModelScope.launch {
            codeItemRepository.getByZone(zone).collect {
                _items.clear()
                _items.addAll(it.toMutableStateList())
            }
        }
    }

    suspend fun getByLabelAndZone(zone: CodeItemZone, label: CodeItemLabel) {
        viewModelScope.launch {
            codeItemRepository.getByLabelAndZone(zone, label).collect {
                _items.clear()
                _items.addAll(it.toMutableStateList())
            }
        }
    }

    suspend fun getOne(id: Long, callback: ((item: CodeItemEntity) -> Unit)?) {
        codeItemRepository.getById(id).collect {
            if (callback != null) {
                callback(it)
            }
        }
    }

    suspend fun addOne(item: CodeItem) {
        codeItemRepository.insert(
            CodeItemEntity(
                item.code, item.description, item.label, item.createdAt, item.zone, "",item.isDeleted
            )
        )
    }
    private suspend fun UpdateDeleted(item: CodeItemEntity){
        item.isDeleted = !item.isDeleted
    }
    suspend fun updateOne(item: CodeItemEntity) {
        try {
            codeItemRepository.update(item)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateItem(item: CodeItemEntity) {
        try {
            codeItemRepository.update(item)
            codeItemRepository.getById(item.id).collect { result ->
                val itemIndex = _items.indexOfFirst { it.id == result.id }
                if (itemIndex != -1) {
                    _items[itemIndex] = result
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteOne(item: CodeItemEntity) {
        try {
            codeItemRepository.delete(item)
            _items.removeIf { it.id == item.id }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun temporaryDeleteOne(item: CodeItemEntity) {
        try {
            UpdateDeleted(item)
            codeItemRepository.update(item)
            scheduleDeletionAfter30Days(item)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteAll() {
        try {
            for(item in _items){
                if(item.isDeleted) {
                    codeItemRepository.delete(item)
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun temporaryDeleteAll() {
        try {
            for(item in _items){
                if(!item.isDeleted) {
                    UpdateDeleted(item)
                    codeItemRepository.update(item)
                    scheduleDeletionAfter30Days(item)
                }
            }

        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun restoreOne(item: CodeItemEntity) {
        try {
            UpdateDeleted(item)
            codeItemRepository.update(item)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun restoreAll() {
        try {
            for(item in _items){
                if(item.isDeleted) {
                    UpdateDeleted(item)
                    codeItemRepository.update(item)
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun scheduleDeletionAfter30Days(item: CodeItemEntity) {
        CoroutineScope(Dispatchers.IO).launch {
            var remainingDelay = TimeUnit.MINUTES.toMillis(1)
            while (remainingDelay > 0 && isActive) {
                delay(1000)
                remainingDelay -= 1000
                if (!item.isDeleted) {
                    yield()
                    return@launch
                }
            }
            if (isActive && item.isDeleted) {
                codeItemRepository.delete(item)
            }
        }
    }

    suspend fun moveQRCode(item: CodeItemEntity){
        try{
            setZone(item)
            codeItemRepository.update(item)
        } catch (e: Exception){
            throw e
        }
    }


}

