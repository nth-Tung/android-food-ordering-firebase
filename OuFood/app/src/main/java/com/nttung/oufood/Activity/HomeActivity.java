package com.nttung.oufood.Activity;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.nttung.oufood.Helper.SessionManager;
import com.nttung.oufood.Fragment.AccountFragment;
import com.nttung.oufood.Fragment.MenuFragment;
import com.nttung.oufood.Fragment.OrderFragment;
import com.nttung.oufood.R;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String TAG_MENU = "tab_menu";
    private static final String TAG_ORDER = "tab_order";
    private static final String TAG_ACCOUNT = "tab_account";

    private BottomNavigationView bottomNavigationView;
    private String currentTabTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SessionManager.init(this);
        SessionManager.getCachedUser();

        if (!SessionManager.hasFirebaseUser()) {
            startActivity(new android.content.Intent(HomeActivity.this, SignInActivity.class));
            finish();
            return;
        }

        SessionManager.fetchUserFromFirebase(user -> {
            if (user == null) {
                Toast.makeText(HomeActivity.this, "Không đồng bộ được tài khoản", Toast.LENGTH_SHORT).show();
            }
        });

        requestPermissions();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_menu) {
                switchTab(TAG_MENU);
                return true;
            }
            if (item.getItemId() == R.id.nav_order) {
                switchTab(TAG_ORDER);
                return true;
            }
            if (item.getItemId() == R.id.nav_account) {
                switchTab(TAG_ACCOUNT);
                return true;
            }
            return false;
        });

        String navigateTo = getIntent().getStringExtra("flag");
        if ("notification".equals(navigateTo)) {
            bottomNavigationView.setSelectedItemId(R.id.nav_order);
        } else {
            bottomNavigationView.setSelectedItemId(R.id.nav_menu);
        }
    }

    private void switchTab(@NonNull String targetTag) {
        if (targetTag.equals(currentTabTag)) {
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.isStateSaved()) {
            return;
        }

        Fragment target = fragmentManager.findFragmentByTag(targetTag);
        if (target == null) {
            if (TAG_MENU.equals(targetTag)) {
                target = new MenuFragment();
            } else if (TAG_ORDER.equals(targetTag)) {
                target = new OrderFragment();
            } else {
                target = new AccountFragment();
            }
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment menu = fragmentManager.findFragmentByTag(TAG_MENU);
        Fragment order = fragmentManager.findFragmentByTag(TAG_ORDER);
        Fragment account = fragmentManager.findFragmentByTag(TAG_ACCOUNT);

        if (menu != null) transaction.hide(menu);
        if (order != null) transaction.hide(order);
        if (account != null) transaction.hide(account);

        if (target.isAdded()) {
            transaction.show(target);
        } else {
            transaction.add(R.id.fragment_container, target, targetTag);
        }

        currentTabTag = targetTag;
        transaction.commit();
    }

    private void requestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        // Kiểm tra quyền đọc bộ nhớ
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_IMAGES);
        }

        // Kiểm tra quyền truy cập vị trí
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        //Kiểm tra quyền thông báo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS);
        }

        // Nếu còn quyền chưa được cấp, yêu cầu tất cả trong một lần
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }
}