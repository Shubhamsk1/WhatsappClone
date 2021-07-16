package com.shubhamkudekar.whatsappclone.modal

import com.google.firebase.firestore.FieldValue

data class User(
    val name: String,
    val ImageUrl:String,
    val thumbImage:String,
    val uid:String,
    val deviceToken:String,
    val status:String,
    val onlineStatus:String,

){
    //Empty constructor for firebase(Always make this)
    constructor():this("","","","","","","")
    constructor(name: String,imageUrl:String,thumbImage: String,uid:String):this(
        name,
        imageUrl,
        thumbImage,
        uid,
        "",
        "Hey there I am using Whatsapp",
        FieldValue.serverTimestamp().toString()
    )
}
