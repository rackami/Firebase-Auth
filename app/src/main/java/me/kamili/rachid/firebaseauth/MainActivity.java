package me.kamili.rachid.firebaseauth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

import me.kamili.rachid.firebaseauth.utils.FacebookManager;

public class MainActivity extends AppCompatActivity implements FacebookManager.ILoginInteraction {

    private FacebookManager mFacebookManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFacebookManager = FacebookManager.getInstance(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mFacebookManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();

        mFacebookManager = FacebookManager.getInstance(this);
        if (mFacebookManager.getUser() != null) {
            startSecondActivity();
        }
    }

    private void startSecondActivity() {
        Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
        startActivity(intent);
    }

    public void loginFacebook(View view) {
        mFacebookManager.signIn();
    }

    @Override
    public void onLoginSuccess(FirebaseUser user) {
        startSecondActivity();
    }

    @Override
    public void onLoginError(String error) {
        Toast.makeText(this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
    }
}
