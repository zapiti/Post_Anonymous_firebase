package com.dev.nathan.kotlinanonymousmapsposts

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

import java.util.Date

import de.hdodenhof.circleimageview.CircleImageView

class NewsAdapter(var news_list: List<NewsPost>) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {
    lateinit var context: Context

    private var firebaseFirestore: FirebaseFirestore? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_list_item, parent, false)
        context = parent.context
        firebaseFirestore = FirebaseFirestore.getInstance()
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val desc_data = news_list[position].desc
        holder.setDescText(desc_data)

        val image_url = news_list[position].image_url
        holder.setBlogImage(image_url)

        val user_id = news_list[position].user_id


        //User Data will be retrieved here...
        firebaseFirestore!!.collection("Users").document(user_id).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {

                val userName = task.result.getString("name")
                val userImage = task.result.getString("image")

                holder.setUserData(userName, userImage)


            } else {

                //Firebase Exception

            }
        }

        val millisecond = news_list[position].timestamp.getTime()
        val dateString = DateFormat.format("MM/dd/yyyy", Date(millisecond)).toString()
        holder.setTime(dateString)
        val address = news_list[position].address
        holder.setAdressText(address)

    }

    override fun getItemCount(): Int {
        return news_list.size
    }

    inner class ViewHolder(private val mView: View) : RecyclerView.ViewHolder(mView) {

        private var descView: TextView? = null
        private var blogImageView: ImageView? = null
        private var blogDate: TextView? = null
        private var addressTextView: TextView? = null

        private var blogUserName: TextView? = null
        private val blogUserImage: CircleImageView? = null

        fun setDescText(descText: String) {

            descView = mView.findViewById(R.id.news_content_desc)
            descView!!.text = "Descrição : $descText"

        }

        fun setAdressText(adressText: String) {

            addressTextView = mView.findViewById(R.id.news_content_title_post)
            addressTextView!!.text = "Local : $adressText"

        }

        fun setBlogImage(downloadUri: String) {
            blogImageView = mView.findViewById(R.id.news_post_image)
            if (!TextUtils.isEmpty(downloadUri)) {
                blogImageView!!.visibility = View.VISIBLE
                val requestOptions = RequestOptions()
                requestOptions.placeholder(R.drawable.image_placeholder)
                Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).into(blogImageView!!)
            }

        }

        fun setTime(date: String) {

            blogDate = mView.findViewById(R.id.blog_date)
            //      blogDate.setText(date);

        }

        fun setUserData(name: String, image: String) {


            blogUserName = mView.findViewById(R.id.blog_user_name)

            //            blogUserName.setText(name);

            val placeholderOption = RequestOptions()
            placeholderOption.placeholder(R.drawable.profile_placeholder)


        }

    }

}
