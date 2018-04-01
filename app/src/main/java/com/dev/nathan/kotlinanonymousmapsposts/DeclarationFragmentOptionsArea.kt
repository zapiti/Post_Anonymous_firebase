package com.dev.nathan.kotlinanonymousmapsposts

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.toast


@SuppressLint("ValidFragment")
class DeclarationFragmentOptionsArea(val type : String) : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater?.inflate(R.layout.fragment_declaration_itens_area, container, false)
        var title = view.find<TextView>(R.id.declaration_title_to_material)

        var area1 = view.find<CardView>(R.id.declaration_content_cadview_area1)
        var text1 = view.find<TextView>(R.id.declaration_text_content_area1)

        var area2 = view.find<CardView>(R.id.declaration_content_cadview_area2)
        var text2 = view.find<TextView>(R.id.declaration_text_content_area2)





        when (type){

            getString(R.string.about_us_title)-> {
                aboutFunction(title,area1)
            }
            getString(R.string.inspiration_title)-> {
                inspirationFunction(title,area2)
            }

        }


        return view
    }



    private fun inspirationFunction(title:TextView,area: CardView) {
        toast(getString(R.string.support_title))
        title.text = getString(R.string.support_title)
        area.visibility = View.VISIBLE
        area.backgroundColor = ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
    }

    private fun aboutFunction(title:TextView,area: CardView) {
        toast(getString(R.string.info_title))
        title.text = getString(R.string.info_title)
        area.visibility = View.VISIBLE
        area.backgroundColor = ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
    }
}