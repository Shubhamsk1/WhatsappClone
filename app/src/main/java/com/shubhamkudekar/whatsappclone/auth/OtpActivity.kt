package com.shubhamkudekar.whatsappclone.auth

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import com.shubhamkudekar.whatsappclone.R
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.shubhamkudekar.whatsappclone.auth.LoginActivity
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit

const val PHONE_NUMBER = "phoneNumber"
class OtpActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mCountDownTimer: CountDownTimer
    private val TAG: String?="Check: "
    lateinit var callbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var phoneNumber:String?= null
    var mVerificationId:String?=null
    var mResendToken: PhoneAuthProvider.ForceResendingToken?=null
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        initViews()
        startVerify()

    }

    private fun startVerify() {

        val options = PhoneAuthOptions.newBuilder()
            .setPhoneNumber(phoneNumber.toString())       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        showTimer(60000)
        progressDialog = createProgressDialog("Sending a verification code",false)
        progressDialog.show()
    }

    private fun showTimer(milliSecInFuture: Long) {
        resendBtn.isVisible=false
        mCountDownTimer=object :CountDownTimer(milliSecInFuture,1000){
            override fun onTick(p0: Long) {
                counterTv.isVisible=true
                counterTv.text=getString(R.string.second_remaining,p0/1000)
            }

            override fun onFinish() {
                counterTv.isVisible = false
                resendBtn.isVisible= true
            }

        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(mCountDownTimer!=null){
            mCountDownTimer!!.cancel()
        }
    }

    private fun initViews() {
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        verifytv.text=getString(R.string.verify_number,phoneNumber)
        setSpannableString()

        verifyBtn.setOnClickListener(this)
        resendBtn.setOnClickListener(this)

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                if(::progressDialog.isInitialized){
                    progressDialog.dismiss()
                }
                val smsCode=credential.smsCode
                Log.d("SMS CODE:",smsCode.toString())
                if(!smsCode.isNullOrBlank()){
                    sentcodeEt.setText(smsCode)
                }
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")
                if(::progressDialog.isInitialized){
                    progressDialog.dismiss()
                }
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val mAuth=FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener{
                if(it.isSuccessful){
                    startActivity(Intent(this, SignUpActivity::class.java))
                    finish()
                }else{
                    notifyUserAndRetry("Your Phone Number Verification is failed.Try Again !!")
                }
            }

    }

    private fun notifyUserAndRetry(message: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setPositiveButton("Ok"){_,_->
                showLoginActivity()
            }
            setNegativeButton("Cancel"){dialog,_ ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }


    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_text,phoneNumber))
        val clickableSpan=object :ClickableSpan(){
            override fun onClick(p0: View) {
                //send back
                showLoginActivity()

            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                //set colors and remove underline
                ds.isUnderlineText=false
                ds.color=ds.linkColor
            }


        }
        span.setSpan(clickableSpan,span.length-13,span.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        waitingTv.movementMethod=LinkMovementMethod.getInstance()
        waitingTv.text=span
    }

    private fun showLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    override fun onBackPressed() {

    }

    override fun onClick(v: View?) {
         when(v){
             verifyBtn->{
                 val code=sentcodeEt.text.toString()
                 if(code.isNotEmpty() && !mVerificationId.isNullOrBlank()) {
//                     progressDialog=createProgressDialog("Please wait....",false)
//                     progressDialog.show()
                     val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code)
                     signInWithPhoneAuthCredential(credential)
                 }
             }
             resendBtn->{
                 val code=sentcodeEt.text.toString()
                 if (mResendToken != null){
                     showTimer(60000)
                     progressDialog=createProgressDialog("Sending a verification code ",false)
                     progressDialog.show()
                     val options = PhoneAuthOptions.newBuilder()
                         .setPhoneNumber(phoneNumber.toString())       // Phone number to verify
                         .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                         .setActivity(this)                 // Activity (for callback binding)
                         .setCallbacks(callbacks)
                         .setForceResendingToken(mResendToken!!)// OnVerificationStateChangedCallbacks
                         .build()
                     PhoneAuthProvider.verifyPhoneNumber(options)

                 }


             }
         }
    }
}

fun Context.createProgressDialog(message: String, isCancellable:Boolean):ProgressDialog{
    return ProgressDialog(this).apply {
        setCancelable(false)
        setMessage(message)
        setCanceledOnTouchOutside(false)
    }
}