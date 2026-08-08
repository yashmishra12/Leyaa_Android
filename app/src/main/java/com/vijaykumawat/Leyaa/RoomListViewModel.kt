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
            val currentMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US).format(java.util.Date())
            
            snapshot?.documents?.forEach { doc ->
                val roomId = doc.id
                
                // Process recurring bills safely using transactions
                db.collection("rooms").document(roomId).collection("recurring_bills").get()
                    .addOnSuccessListener { recBills ->
                        if (!recBills.isEmpty) {
                            val activeBillsRef = db.collection("${roomId}_BILLS")
                            for (recDoc in recBills.documents) {
                                val lastProcessed = recDoc.getString("lastProcessedMonth")
                                if (lastProcessed != currentMonth) {
                                    db.runTransaction { transaction ->
                                        val freshDoc = transaction.get(recDoc.reference)
                                        val freshLastProcessed = freshDoc.getString("lastProcessedMonth")
                                        if (freshLastProcessed != currentMonth) {
                                            val newBillRef = activeBillsRef.document()
                                            val newBillData = freshDoc.data?.toMutableMap() ?: return@runTransaction
                                            newBillData.remove("lastProcessedMonth")
                                            newBillData.remove("frequency")
                                            newBillData["timestamp"] = java.util.Date()
                                            transaction.set(newBillRef, newBillData)
                                            transaction.update(recDoc.reference, "lastProcessedMonth", currentMonth)
                                        }
                                    }
                                }
                            }
                        }
                    }
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
