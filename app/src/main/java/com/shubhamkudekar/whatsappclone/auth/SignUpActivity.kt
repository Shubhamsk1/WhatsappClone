package com.shubhamkudekar.whatsappclone.auth

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.shubhamkudekar.whatsappclone.MainActivity
import com.shubhamkudekar.whatsappclone.R
import com.shubhamkudekar.whatsappclone.modal.User
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    val storage by lazy {
        FirebaseStorage.getInstance()
    }
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    val database by lazy{
        FirebaseFirestore.getInstance()
    }
    lateinit var downloadUrl:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        userImgView.setOnClickListener{
            checkPermissionForImage()
            //FireBase Extensions - Image ThumbNail
        }
        nextBtn.setOnClickListener{
            nextBtn.isEnabled=false
            val name=nameEt.text.toString()
            when {
                name.isEmpty() -> {
                    Toast.makeText(this,"Name Cannot Be Empty",Toast.LENGTH_SHORT).show()
                }
                downloadUrl.isEmpty() -> {
                    Toast.makeText(this,"Image Cannot Be Empty",Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val user: User = User(name,downloadUrl,downloadUrl,auth.uid!!)
                    database.collection("users").document(auth.uid!!).set(user).addOnSuccessListener {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }.addOnFailureListener{
                        nextBtn.isEnabled=true
                    }
                }
            }

        }
    }

    private fun checkPermissionForImage() {
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED
            && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED
        ){
            val permission= arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            val permissionWrite= arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestPermissions(
                permission,
                1001
            )
            requestPermissions(
                permissionWrite,
                1002
            )

        }
        else{
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent=Intent(Intent.ACTION_PICK)
        intent.type="image/*"
        startActivityForResult(
            intent,
            1000
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("ImageView:","true")
        if(resultCode == Activity.RESULT_OK && requestCode==1000){
            data?.data.let {
                userImgView.setImageURI(it)
                uploadImage(it)
            }
        }
    }

    private fun uploadImage(it: Uri?) {
        nextBtn.isEnabled=false
        val ref = storage.reference.child("uploads/"+ auth.uid.toString())
        val uploadTask=ref.putFile(it!!)
        //if you forget about this read the documentation
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>>{ task ->
            if(!task.isSuccessful){
                task.exception?.let {
                    throw  it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener{
            nextBtn.isEnabled=true
            if(it.isSuccessful){
                downloadUrl=it.result.toString()
                Log.d("Download Url :" ,downloadUrl)
            }
        }.addOnFailureListener {
            Toast.makeText(this,"Upload Failed!!",Toast.LENGTH_SHORT).show()
        }
    }
}