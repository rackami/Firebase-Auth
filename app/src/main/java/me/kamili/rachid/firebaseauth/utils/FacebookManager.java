package me.kamili.rachid.firebaseauth.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import static com.facebook.internal.FacebookDialogFragment.TAG;

public class FacebookManager {

    private static FacebookManager instance;
    private CallbackManager mCallbackManager;
    private LoginManager mLoginManager;
    private ILoginInteraction loginListener;
    private ISignOutInteraction signOutListener;
    private Activity mActivity;
    private FirebaseAuth mAuth;

    public FacebookManager() {

    }

    private FacebookManager(Activity activity) {
        this.mActivity = activity;
        this.mCallbackManager = CallbackManager.Factory.create();
        this.mLoginManager = LoginManager.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    public static FacebookManager getInstance(Activity activity) {
        if (instance == null)
            instance = new FacebookManager(activity);
        instance.attach(activity);
        return instance;
    }

    private void attach(Object object) {

        if (object instanceof ILoginInteraction) {
            this.loginListener = (ILoginInteraction) object;
        }
        if (object instanceof ISignOutInteraction) {
            this.signOutListener = (ISignOutInteraction) object;
        }
        if (object instanceof Activity) {
            this.mActivity = (Activity) object;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void signIn() {
        mLoginManager.logInWithReadPermissions(mActivity, Arrays.asList("email", "public_profile"));
        mLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(mActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            loginListener.onLoginSuccess(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(mActivity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            loginListener.onLoginError(task.getException().getMessage());
                        }
                    }
                });
    }

    public FirebaseUser getUser() {
        return mAuth.getCurrentUser();
    }

    public void signOut() {
        mAuth.signOut();
        mLoginManager.logOut();
        signOutListener.onSignOut(getUser() == null);
    }

    public interface ILoginInteraction {
        void onLoginSuccess(FirebaseUser user);
        void onLoginError(String error);
    }

    public interface ISignOutInteraction {
        void onSignOut(boolean isSignedOut);
    }
}
