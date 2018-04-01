package com.dev.nathan.kotlinanonymousmapsposts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.widget.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import id.zelory.compressor.Compressor
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*


class AttestantPostActivity : AppCompatActivity() {

    private lateinit var attestantPostToolbar: Toolbar
    private lateinit var attestantPostImage: ImageView
    private lateinit var attestantPostDesc: EditText
    private lateinit var attestantPostBtn: Button
    private lateinit var attestantPostProgress: ProgressBar
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGetCurrentUserLocations: Button
    private lateinit var mAddress: EditText

    private lateinit var current_user_id: String

    private var attestantPostImageUri: Uri? = null

    private lateinit var storageReference: StorageReference
    private lateinit var firrebaseFirestore: FirebaseFirestore

    private lateinit var compressedImageFile: Bitmap
    internal lateinit var button: Button
    internal lateinit var textView: TextView
    internal lateinit var locationManager: LocationManager
    internal lateinit var lattitude: String
    internal lateinit var longitude: String
    internal lateinit var geocoder: Geocoder
    internal  var addresses: List<Address>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attestant_post)

        geocoder = Geocoder(this, Locale.getDefault())

        mAuth = FirebaseAuth.getInstance()
        current_user_id = mAuth.currentUser!!.uid
        storageReference = FirebaseStorage.getInstance().reference
        firrebaseFirestore = FirebaseFirestore.getInstance()

        attestantPostToolbar = findViewById(R.id.attestant_post_toolbar)
        setSupportActionBar(attestantPostToolbar)
        supportActionBar?.setTitle(R.string.attestant_page_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        attestantPostProgress = findViewById(R.id.attestant_post_progress)
        attestantPostImage = findViewById(R.id.attestant_post_image)
        attestantPostDesc = findViewById(R.id.attestant_post_desc)
        attestantPostBtn = findViewById(R.id.attestant_post_btn)
        attestantPostBtn.isClickable = true

        mGetCurrentUserLocations = findViewById(R.id.attestant_post_getcurrent_location_btn)
        mAddress = findViewById(R.id.attestant_post_address_edittext)

        mGetCurrentUserLocations.setOnClickListener {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps()

            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                getLocation()
            }
        }





        attestantPostImage.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (ContextCompat.checkSelfPermission(this@AttestantPostActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this@AttestantPostActivity, "Permission Denied", Toast.LENGTH_LONG).show()
                    ActivityCompat.requestPermissions(this@AttestantPostActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

                } else {

                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1)
                            .setMinCropResultSize(512, 512)
                            .start(this@AttestantPostActivity)
                }

            } else {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .setMinCropResultSize(512, 512)
                        .start(this@AttestantPostActivity)

            }

        }



        attestantPostBtn.setOnClickListener {
            val desc = attestantPostDesc.text.toString()
            val address = mAddress.text.toString()
            mAddress.setText("ENDEREÇO INDEFINIDO")


            if (!TextUtils.isEmpty(desc) && !TextUtils.isEmpty(address)) {
                attestantPostBtn.isClickable = false
                attestantPostProgress.visibility = View.VISIBLE
                if (attestantPostImageUri != null) {
                    val randomName = UUID.randomUUID().toString()

                    val filePath = storageReference.child("post_images").child("$randomName.jpg")

                    filePath.putFile(attestantPostImageUri!!).addOnCompleteListener { task ->
                        val downloadUri = task.result.downloadUrl.toString()
                        if (task.isSuccessful) {

                            val newImagefile = File(attestantPostImageUri?.path)

                            try {
                                compressedImageFile = Compressor(this@AttestantPostActivity)
                                        .setMaxHeight(100)
                                        .setMaxWidth(100)
                                        .setQuality(2)
                                        .compressToBitmap(newImagefile)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                            val baos = ByteArrayOutputStream()
                            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                            val thumbdData = baos.toByteArray()

                            val uploadTask = storageReference.child("post_images/thumbs")
                                    .child("$randomName.jpg").putBytes(thumbdData)

                            uploadTask.addOnSuccessListener { taskSnapshot ->
                                val downloadthumbUri = taskSnapshot.downloadUrl.toString()

                                addToFirebasePost(downloadUri, downloadthumbUri, desc, address)
                            }.addOnFailureListener {
                                //Error handling
                            }

                        } else {

                            attestantPostProgress.visibility = View.INVISIBLE
                        }
                    }

                } else {

                    addToFirebasePost("", "", desc, address)

                }
            }else{
                toast("Por favor, insira uma descrição")
            }
        }
    }

    private fun addToFirebasePost(downloadUri: String, downloadthumbUri: String, desc: String, address: String) {
        val postMap = HashMap<String, Any>()
        postMap["image_url"] = downloadUri
        postMap["thumb"] = downloadthumbUri
        postMap["desc"] = desc
        postMap["address"] = address
        postMap["user_id"] = current_user_id
        postMap["timestamp"] = FieldValue.serverTimestamp()
        doAsync {
            firrebaseFirestore?.collection("Posts")?.add(postMap)?.addOnCompleteListener { task ->

                runOnUiThread {
                if (task.isSuccessful) {
                    Toast.makeText(this@AttestantPostActivity, getString(R.string.post_was_added), Toast.LENGTH_LONG).show()
                    val mainIntent = Intent(this@AttestantPostActivity, MainActivity::class.java)
                    startActivity(mainIntent)
                    finish()

                } else {

                }

                    attestantPostProgress.visibility = View.INVISIBLE
                }

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {

                attestantPostImageUri = result.uri

                attestantPostImage.setImageURI(attestantPostImageUri)


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(this@AttestantPostActivity, getString(R.string.image_error) + error, Toast.LENGTH_LONG).show()

            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this@AttestantPostActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@AttestantPostActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this@AttestantPostActivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)

        } else {
            val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            val location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            val location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            if (location != null) {
                val latti = location.latitude
                val longi = location.longitude


                try {
                    addresses = geocoder.getFromLocation(latti, longi, 1)
                    if (addresses != null && addresses!!.size > 0) {
                        val address = addresses!![0].getAddressLine(0)
                        val city = addresses!![0].locality
                        val state = addresses!![0].adminArea
                        val country = addresses!![0].countryName
                        val postalCode = addresses!![0].postalCode
                        val knownName = addresses!![0].featureName

                        mAddress.setText(address)

                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }


                lattitude = latti.toString()
                longitude = longi.toString()


            } else if (location1 != null) {
                val latti = location1.latitude
                val longi = location1.longitude
                lattitude = latti.toString()
                longitude = longi.toString()

                mAddress.setText("")

            } else if (location2 != null) {
                val latti = location2.latitude
                val longi = location2.longitude
                lattitude = latti.toString()
                longitude = longi.toString()

                mAddress.setText("")

            } else {

                Toast.makeText(this, "Unble to Trace your location", Toast.LENGTH_SHORT).show()

            }
        }
    }

    protected fun buildAlertMessageNoGps() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                .setNegativeButton("No") { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    companion object {

        private val REQUEST_LOCATION = 1
    }
}

