package com.nttung.oufood.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.nttung.oufood.Helper.SessionManager;
import com.nttung.oufood.R;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        SessionManager.init(this);
        SessionManager.getCachedUser();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadCurrentUser();
        } else {
            startActivity(new Intent(IntroActivity.this, SignInActivity.class));
            finish();
        }
    }

    private void loadCurrentUser() {
        SessionManager.fetchUserFromFirebase(user -> {
            if (user != null) {
                startActivity(new Intent(IntroActivity.this, HomeActivity.class));
            } else {
                SessionManager.clearSession();
                startActivity(new Intent(IntroActivity.this, SignInActivity.class));
            }
            finish();
        });
    }
}