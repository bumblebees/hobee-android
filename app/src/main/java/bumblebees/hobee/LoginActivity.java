package bumblebees.hobee;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import bumblebees.hobee.utilities.*;

import com.facebook.*;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private SignInButton googleLoginButton;
    private SessionManager session;
    private GoogleSignInOptions googleSignIn;
    private static GoogleApiClient googleApiClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SocketIO.getInstance().start(this);

        session = new SessionManager(getApplicationContext());

        //initialize the application settings_img
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);

        //If user has already logged in, retrieve data from the preferences and go to homepage
        if(session.isLoggedIn()){
            //SocketIO.getInstance().getUserAndLogin(session.getUser().getLoginId(), getApplicationContext());

            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        }



        /*
            FACEBOOK SIGN IN
         */
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.fb_login_button);
        loginButton.setReadPermissions(Arrays.asList("user_birthday", "email"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                SocketIO.getInstance().checkIfExists(AccessToken.getCurrentAccessToken(), LoginActivity.this);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });






        /*
            GOOGLE SIGN IN
        */


        googleSignIn = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignIn)
                .build();

        googleLoginButton = (SignInButton) findViewById(R.id.google_login_button);
        googleLoginButton.setSize(SignInButton.SIZE_WIDE);
        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, 9001);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 9001) {
            GoogleSignInResult res = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (res.isSuccess()) {
                GoogleSignInAccount acc = res.getSignInAccount();
                SocketIO.getInstance().checkIfExists(acc, getApplicationContext());
            }
            else {
                Log.d("acc", "sign-in failed");
            }
        }
        else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("acc", connectionResult.toString());
    }

}
