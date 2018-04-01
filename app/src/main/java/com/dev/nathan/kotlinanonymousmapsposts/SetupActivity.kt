package com.dev.nathan.kotlinanonymousmapsposts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

import java.util.HashMap

import de.hdodenhof.circleimageview.CircleImageView


class SetupActivity : AppCompatActivity() {

    private lateinit var setupImage: CircleImageView
    private lateinit var mainImageURI: Uri

    private lateinit var user_id: String

    private var isChanged = false

    private lateinit var setupName: EditText
    private lateinit var setupBtn: Button
    private lateinit var setupProgress: ProgressBar

    private lateinit var storageReference: StorageReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var setupToolbar: Toolbar
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        setupToolbar = findViewById(R.id.setupToolbar)
        
        setSupportActionBar(setupToolbar)
        supportActionBar?.title = "Account Setup"

        firebaseAuth = FirebaseAuth.getInstance()
        user_id = firebaseAuth.currentUser!!.uid

        firebaseFirestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference


        setupImage = findViewById(R.id.setup_image)
        setupName = findViewById(R.id.setup_name)
        setupBtn = findViewById(R.id.setup_btn)
        setupProgress = findViewById(R.id.setup_progress)

        setupProgress.visibility = View.VISIBLE
        setupBtn.isEnabled = false

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {

                if (task.result.exists()) {

                    val name = task.result.getString("name")
                    val image = task.result.getString("image")

                    mainImageURI = Uri.parse(image)

                    setupName.setText(name)

                    val placeholderRequest = RequestOptions()
                    placeholderRequest.placeholder(R.drawable.default_image)

                    Glide.with(this@SetupActivity).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage)


                }

            } else {

                val error = task.exception?.message
                Toast.makeText(this@SetupActivity, "(FIRESTORE Retrieve Error) : $error", Toast.LENGTH_LONG).show()

            }

            setupProgress.visibility = View.INVISIBLE
            setupBtn.isEnabled = true
        }


        setupBtn.setOnClickListener {
            val user_name = setupName.text.toString()

            if (!TextUtils.isEmpty(user_name) && mainImageURI != null) {

                setupProgress.visibility = View.VISIBLE

                if (isChanged) {

                    user_id = firebaseAuth.currentUser!!.uid

                    val image_path = storageReference.child("profile_images").child(user_id + ".jpg")
                    image_path.putFile(mainImageURI).addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            storeFirestore(task, user_name)

                        } else {

                            val error = task.exception?.message
                            Toast.makeText(this@SetupActivity, "(IMAGE Error) : $error", Toast.LENGTH_LONG).show()

                            setupProgress.visibility = View.INVISIBLE

                        }
                    }

                } else {

                    storeFirestore(null, user_name)

                }

            }
        }

        setupImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (ContextCompat.checkSelfPermission(this@SetupActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this@SetupActivity, "Permission Denied", Toast.LENGTH_LONG).show()
                    ActivityCompat.requestPermissions(this@SetupActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

                } else {

                    BringImagePicker()

                }

            } else {

                BringImagePicker()

            }
        }


    }

    private fun storeFirestore(task: Task<UploadTask.TaskSnapshot>?, user_name: String) {

        val download_uri: Uri?

        if (task != null) {

            download_uri = task.result.downloadUrl

        } else {

            download_uri = mainImageURI

        }

        val userMap = HashMap<String, String>()
        userMap["name"] = user_name
        userMap["image"] = download_uri.toString()

        firebaseFirestore.collection("Users").document(user_id).set(userMap as Map<String, Any>).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                Toast.makeText(this@SetupActivity, "The user Settings are updated.", Toast.LENGTH_LONG).show()
                val mainIntent = Intent(this@SetupActivity, MainActivity::class.java)
                startActivity(mainIntent)
                finish()

            } else {

                val error = task.exception?.message
                Toast.makeText(this@SetupActivity, "(FIRESTORE Error) : $error", Toast.LENGTH_LONG).show()

            }

            setupProgress.visibility = View.INVISIBLE
        }


    }

    private fun BringImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this@SetupActivity)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {

                mainImageURI = result.uri
                setupImage.setImageURI(mainImageURI)

                isChanged = true

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                val error = result.error

            }
        }

    }
}
