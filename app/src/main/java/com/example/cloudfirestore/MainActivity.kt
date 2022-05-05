package com.example.cloudfirestore

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.cloudfirestore.adapters.UserAdapter
import com.example.cloudfirestore.databinding.ActivityMainBinding
import com.example.cloudfirestore.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.lang.RuntimeException


class MainActivity : AppCompatActivity() {

    lateinit var firebaseFireStore: FirebaseFirestore
    lateinit var binding:ActivityMainBinding
    lateinit var userAdapter: UserAdapter
    lateinit var list:ArrayList<User>
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var reference:StorageReference
    var imgUrl:String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        dexter()


        firebaseFireStore = FirebaseFirestore.getInstance()
        list = ArrayList()
        initViews()

        firebaseStorage = FirebaseStorage.getInstance()
        reference = firebaseStorage.getReference("images")


    }

    private fun dexter() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) { /* ... */
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) { /* ... */
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) { /* ... */
                }
            }).check()
    }

    private fun initViews() {
        binding.btnSave.setOnClickListener {
           val name =  binding.edit1.text.toString()
           val age =  binding.edit2.text.toString().toInt()

            val user = User(name, age, imgUrl!!)

            firebaseFireStore.collection("users")
                .add(user)
                .addOnSuccessListener {
                    Toast.makeText(this, it.id, Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
        }

        binding.btnError.setOnClickListener {
            throw RuntimeException("Hello Crash")
        }


        firebaseFireStore.collection("users")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful){
                    var result = it.result

                    result.forEach {queryDocumentSnapshot ->  
                        val user = queryDocumentSnapshot.toObject(User::class.java)
                        list.add(user)
                    }
                    userAdapter = UserAdapter(list)
                    binding.mainRv.adapter = userAdapter
                }
            }


        binding.ivMain.setOnClickListener {
            getImageContent.launch("image/*")
        }


    }

    private var getImageContent =
        registerForActivityResult(ActivityResultContracts.GetContent()){uri ->
            binding.ivMain.setImageURI(uri)
            val m = System.currentTimeMillis()
        var uploadTask = reference.child(m.toString()).putFile(uri)
            uploadTask.addOnSuccessListener {
                if (it.task.isSuccessful){
                 var downloadUrl = it.metadata?.reference?.downloadUrl
                    downloadUrl?.addOnSuccessListener {imgUri ->
                        imgUrl = imgUri.toString()
                    }

                }
            }.addOnFailureListener{
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }
}