package com.dev.nathan.kotlinanonymousmapsposts

import android.location.Geocoder
import android.os.Bundle
import android.util.Log


import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import com.google.firebase.firestore.FirebaseFirestore

import com.google.gson.Gson
import org.jetbrains.anko.doAsync

import java.io.IOException


class MapsFragment : SupportMapFragment(), OnMapReadyCallback {


    private var googleMap: GoogleMap? = null
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        firebaseFirestore = FirebaseFirestore.getInstance()

        getMapAsync(this@MapsFragment)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        //converter latitude longitude do local

        val address = "Rua Rio Solimoes 1661,Jardim Europa,Uberlandia"
        val centralPosition = getCordToAddress(address)
        //posiçao
        if (centralPosition != null) {
            centralizaEm(centralPosition)
        }


         doAsync {
             firebaseFirestore!!.collection("Posts").get().addOnCompleteListener { task ->
                 val g: Gson = Gson()

                 if (task.isSuccessful) {
                     for (document in task.result) {
                         // Log.d("veioo", document.getId() + " => " + document.getData());

                         if (document.getString("address") == null) {
                             Log.d("veio", g.toJson(document.getString("address")))
                         } else {
                             val cordenada = getCordToAddress(g.toJson(document.getString("address")))
                             activity?.runOnUiThread {
                                 val marcador = MarkerOptions()
                                 if (cordenada != null) {

                                     marcador.title(g.toJson(document.getString("desc")))
                                     Log.d("veioooooo", g.toJson(cordenada))
                                     marcador.position(cordenada)
                                     googleMap.addMarker(marcador)
                                 }

                             }


                             //  marcador.snippet(String.valueOf(aluno.getNota()));

                         }


                     }

                 } else {

                     //Firebase Exception

                 }
             }
         }


        Localizador(context!!, googleMap)
    }

    fun centralizaEm(coordenada: LatLng) {
        if (googleMap != null) {
            val update = CameraUpdateFactory.newLatLngZoom(coordenada, 12f)

            googleMap?.moveCamera(update)
            //            googleMap.getUiSettings().setCompassEnabled(false);
            //            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            //            CameraPosition cameraPosition = new CameraPosition.Builder()
            //                    .target(coordenada)
            //                    .zoom(18)
            //                    .tilt(67.5f)
            //                    .bearing(314)
            //                    .build();
            //            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        }
    }


    private fun getCordToAddress(address: String): LatLng? {
        try {
            val geocoder = Geocoder(context)
            val resultados = geocoder.getFromLocationName(address, 1)
            if (!resultados.isEmpty()) {
                return LatLng(resultados[0].latitude, resultados[0].longitude)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }
}

