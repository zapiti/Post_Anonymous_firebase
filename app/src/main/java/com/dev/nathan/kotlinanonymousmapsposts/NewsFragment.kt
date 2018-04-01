package com.dev.nathan.kotlinanonymousmapsposts


import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v4.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.jetbrains.anko.support.v4.toast

import java.util.ArrayList



class NewsFragment : Fragment() {

    private lateinit var news_list_view: RecyclerView
    private lateinit var news_list: MutableList<NewsPost>
    private var mainToolbar: Toolbar? = null
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var newsPostToolbar: Toolbar
    private lateinit var lastVisible: DocumentSnapshot


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.fragment_news, container, false)
  


        news_list = ArrayList<NewsPost>()
        news_list_view = view!!.findViewById(R.id.news_list_view)

        firebaseAuth = FirebaseAuth.getInstance()

        newsAdapter = NewsAdapter(news_list)
        news_list_view.layoutManager = LinearLayoutManager(container?.context)
        news_list_view.adapter = newsAdapter

        if (firebaseAuth.currentUser != null) {

            firebaseFirestore = FirebaseFirestore.getInstance()

            news_list_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val reachedBottom = !recyclerView!!.canScrollVertically(1)

                    if (reachedBottom) {

                        val desc = lastVisible.getString("desc")
                        toast( "Post : $desc")

                        loadMorePost()

                    }

                }
            })

            val firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3)
            firstQuery.addSnapshotListener { documentSnapshots, e ->
                lastVisible = documentSnapshots.documents[documentSnapshots.size() - 1]

                for (doc in documentSnapshots.documentChanges) {
                    if (doc.type == DocumentChange.Type.ADDED) {

                        val newsPost = doc.document.toObject<NewsPost>(NewsPost::class.java)
                        news_list.add(newsPost)

                        newsAdapter.notifyDataSetChanged()

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

                        val newsPost = doc.document.toObject<NewsPost>(NewsPost::class.java)
                        news_list.add(newsPost)

                        newsAdapter.notifyDataSetChanged()

                    }
                }

            }
        }

    }

}// Required empty public constructor
