package com.dev.nathan.kotlinanonymousmapsposts

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng


class Localizador(context: Context, private val mapa: GoogleMap) : GoogleApiClient.ConnectionCallbacks, LocationListener {

    private val client: GoogleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .build()

    init {
        client.connect()
    }

    @SuppressLint("MissingPermission")
    override fun onConnected(bundle: Bundle?) {
        val request = LocationRequest()
        //apenas quando desloca
        request.smallestDisplacement = 50f//metros
        request.interval = 1000//milisegundos
        //prioridades bateria etc
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY


        //        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);//permisao

    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onLocationChanged(location: Location) {
        val cordenada = LatLng(location.latitude, location.longitude)

        val cameraUpdate = CameraUpdateFactory.newLatLng(cordenada)
        mapa.moveCamera(cameraUpdate)
    }
}
