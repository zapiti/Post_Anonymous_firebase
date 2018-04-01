package com.dev.nathan.kotlinanonymousmapsposts

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.jetbrains.anko.doAsync


class AttestantFragment : Fragment() {

    private lateinit var blog_list_view: RecyclerView
    private var blog_list: MutableList<AttestantPost>? = null

    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var attestantRecyclerAdapter: AttestantRecyclerAdapter

    private lateinit var lastVisible: DocumentSnapshot


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_attestant, container, false)

        blog_list = ArrayList<AttestantPost>()
        blog_list_view = view.findViewById(R.id.attestant_list_view)

        firebaseAuth = FirebaseAuth.getInstance()

        attestantRecyclerAdapter = AttestantRecyclerAdapter(blog_list!!)
        blog_list_view.layoutManager = LinearLayoutManager(container!!.context)
        blog_list_view.adapter = attestantRecyclerAdapter

        if (firebaseAuth.currentUser != null) {

            firebaseFirestore = FirebaseFirestore.getInstance()

            blog_list_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val reachedBottom = !recyclerView!!.canScrollVertically(1)

                    if (reachedBottom) {

                        val desc = lastVisible?.getString("desc")
                        Toast.makeText(container?.context, "Post : $desc", Toast.LENGTH_SHORT).show()

                        loadMorePost()

                    }

                }
            })
            doAsync {
            val firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3)
            firstQuery.addSnapshotListener { documentSnapshots, e ->
                lastVisible = documentSnapshots.documents[documentSnapshots.size() - 1]

                for (doc in documentSnapshots.documentChanges) {
                    try {
                    if (doc.type == DocumentChange.Type.ADDED) {


                            val attestantPost:AttestantPost? = doc.document.toObject(AttestantPost::class.java)
                            blog_list!!.add(attestantPost!!)
                            attestantRecyclerAdapter.notifyDataSetChanged()


                        }
                } catch (e: Exception) {
                        Log.e("erro", e.toString())
                    }
                    }

            }


            }

        }

        // Inflate the layout for this fragment
        return view
    }

    fun loadMorePost() {

        val nextQuery = firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3)

        nextQuery.addSnapshotListener { documentSnapshots, e ->
            if (!documentSnapshots.isEmpty) {

                lastVisible = documentSnapshots.documents[documentSnapshots.size() - 1]
                for (doc in documentSnapshots.documentChanges) {
                    if (doc.type == DocumentChange.Type.ADDED) {

                        val attestantPost = doc.document.toObject<AttestantPost>(AttestantPost::class.java)
                        blog_list?.add(attestantPost)

                        attestantRecyclerAdapter.notifyDataSetChanged()

                    }
                }

            }
        }

    }

}// Required empty public constructor

