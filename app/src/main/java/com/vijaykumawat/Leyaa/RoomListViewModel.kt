package com.vijaykumawat.Leyaa

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// A simple data class to represent a Room with its Document ID
data class RoomModel(
    val id: String,
    val title: String,
    val avatar: String,
    val itemCount: Int = 0,
    val topItems: List<String> = emptyList()
)

@HiltViewModel
class RoomListViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    
    private val _rooms = MutableLiveData<List<RoomModel>>()
    val rooms: LiveData<List<RoomModel>> = _rooms
    
    private var listenerRegistration: ListenerRegistration? = null
    
    init {
        startListening()
    }
    
    fun startListening() {
        if (listenerRegistration != null) return
        
        val uid = auth.currentUser?.uid ?: return
        val query = db.collection("rooms").whereArrayContains("members", uid)
        
        listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            
            val roomList = mutableListOf<RoomModel>()
            snapshot?.documents?.forEach { doc ->
                val title = doc.getString("title") ?: "Untitled Room"
                val avatar = doc.getString("avatar") ?: ""
                val newItems = doc.get("newItems") as? List<Map<String, Any>>
                val itemCount = newItems?.size ?: 0
                val topItems = newItems?.mapNotNull { it["name"] as? String }?.take(4) ?: emptyList()
                roomList.add(RoomModel(doc.id, title, avatar, itemCount, topItems))
            }
            _rooms.value = roomList
        }
    }
    
    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
