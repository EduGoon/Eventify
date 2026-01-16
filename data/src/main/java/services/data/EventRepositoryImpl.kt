package services.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : EventRepository {
    override fun getEvents(): Flow<List<Event>> = callbackFlow {
        val subscription = firestore.collection("events")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val events = snapshot.toObjects<Event>()
                    trySend(events)
                }
            }
        awaitClose { subscription.remove() }
    }
}
