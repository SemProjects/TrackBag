package com.example.ravi.bag_location;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;


import java.util.concurrent.TimeUnit;

public class signup extends AppCompatActivity  {

    private FirebaseAuth mAuth;
    PhoneAuthCredential phoneCredential;
    private FirebaseAuth.AuthStateListener mAuthListener;
    Button signup,start_verify,verify,resend;
    EditText name, email, pass, cpass,mobile, mVerificationField ;
    LinearLayout hideLayout;
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;


    void init() {
        mAuth = FirebaseAuth.getInstance();
        signup = (Button) findViewById(R.id.btnSignup);
        start_verify = (Button) findViewById(R.id.start_verify);
        verify = (Button) findViewById(R.id.verify);
        resend = (Button) findViewById(R.id.resend);
        name = (EditText) findViewById(R.id.editTextname);
        email = (EditText) findViewById(R.id.editTextemail);
        pass = (EditText) findViewById(R.id.editTextpass);
        cpass = (EditText) findViewById(R.id.editTextcpass);
        mobile = (EditText) findViewById(R.id.editTextMobile);
        mVerificationField = (EditText) findViewById(R.id.editTextCode);
        hideLayout = (LinearLayout) findViewById(R.id.hidden_part);
        hideLayout.setVisibility(View.GONE);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(checkFields())
                    createAccount(email.getText().toString(),pass.getText().toString());
            }
        });
        start_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validatePhoneNumber()) {
                    return;
                }
                hideLayout.setVisibility(View.VISIBLE);
                startPhoneNumberVerification();
            }
        });
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = mVerificationField.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    mVerificationField.setError("Cannot be empty.");
                    return;
                }
                phoneCredential = PhoneAuthProvider.getCredential(mVerificationId, code);
            }
        });
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode(mResendToken);
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d("Tag", "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("Tag", "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    mobile.setError("Invalid phone number.");
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    mobile.setError("SMS quota Expired");
                    // [END_EXCLUDE]
                }
            }
        @Override
        public void onCodeSent(String verificationId,
                PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d("Tag", "onCodeSent:" + verificationId);

            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;

        }
    };
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        init();
    }

    private void createAccount(String email, String password) {
        final ProgressDialog progressDialog = new ProgressDialog(signup.this);
        progressDialog.setMessage("Signing In....");
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("TAG", "createUserWithEmail:onComplete:" + task.isSuccessful());
                        progressDialog.dismiss();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(signup.this, "Failed Try again", Toast.LENGTH_LONG).show();
                        }
                        else {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            user.updatePhoneNumber(phoneCredential);
                            Intent i = new Intent(signup.this, MainActivity.class);
                            startActivity(i);
                        }

                        // ...
                    }
                });

    }

    private void startPhoneNumberVerification() {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile.getText().toString(),        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }
    // [START resend_verification]
    private void resendVerificationCode(PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile.getText().toString(),        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }
    private boolean validatePhoneNumber() {
        String phoneNumber = mobile.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            mobile.setError("Invalid phone number.");
            return false;
        }
        return true;
    }
    private boolean checkFields(){
        String spass, scpass, sname, semail;

        spass = pass.getText().toString().trim();
        scpass = cpass.getText().toString().trim();
        sname = name.getText().toString().trim();
        semail = email.getText().toString().trim();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (sname != null && semail != null && scpass != null && spass != null) {
            if(sname.length()<5) {
                name.setText(null);
               name.setError("Name should be minimum of length 5");
            }
            else if(spass.length() < 8 || !spass.matches(".*\\d+.*") || !spass.matches(".*[a-z].*")) {
                pass.setText(null);
                cpass.setText(null);
                if (spass.length() < 8)
                    pass.setError("Password should be minimum of length 8");
                else if(!spass.matches(".*[a-z].*"))
                    pass.setError("Password should contain Characters");
                else if (!spass.matches(".*\\d+.*"))
                    pass.setError("Password should contain Numbers");
            }
            else if (spass.compareTo(scpass) == 0 && semail.matches(emailPattern)) {
                return true;
            } else if (!semail.matches(emailPattern)) {
                email.setError("Invalid email address");
                email.setText(null);
            }else {
                pass.setText(null);
                cpass.setText(null);
                pass.setError("Confirm Password Not Matched");
            }
        }
        else
        {
            Toast.makeText(this, "Incomplete Information!", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}