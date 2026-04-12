package com.nttung.oufood.Helper;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.nttung.oufood.Class.User;
import com.nttung.oufood.common.Common;

import java.util.ArrayList;

import io.paperdb.Paper;

public final class SessionManager {

    private static final String KEY_CURRENT_USER = "CURRENTUSER";

    public interface UserCallback {
        void onResult(@Nullable User user);
    }

    private SessionManager() {
    }

    public static void init(@NonNull Context context) {
        Paper.init(context);
    }

    @Nullable
    public static User getCachedUser() {
        User user = Paper.book().read(KEY_CURRENT_USER, null);
        if (user != null) {
            Common.CURRENTUSER = user;
        }
        return user;
    }

    public static void saveUser(@Nullable User user) {
        Common.CURRENTUSER = user;
        if (user != null) {
            Paper.book().write(KEY_CURRENT_USER, user);
        } else {
            Paper.book().delete(KEY_CURRENT_USER);
        }
    }

    public static void clearSession() {
        Common.CURRENTUSER = null;
        Paper.book().delete(KEY_CURRENT_USER);
        Paper.book().delete(Common.USERNAME_KEY);
        FirebaseAuth.getInstance().signOut();
    }

    public static boolean hasFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static void fetchUserFromFirebase(@NonNull UserCallback callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            callback.onResult(null);
            return;
        }

        String uid = firebaseUser.getUid();
        Common.FIREBASE_DATABASE.getReference(Common.REF_USERS).child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            saveUser(user);
                            callback.onResult(user);
                            return;
                        }

                        User fallbackUser = createFallbackUser(firebaseUser);
                        Common.FIREBASE_DATABASE.getReference(Common.REF_USERS).child(uid).setValue(fallbackUser)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        saveUser(fallbackUser);
                                        callback.onResult(fallbackUser);
                                    } else {
                                        callback.onResult(null);
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onResult(null);
                    }
                });
    }

    @NonNull
    private static User createFallbackUser(@NonNull FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        String email = firebaseUser.getEmail() == null ? "" : firebaseUser.getEmail();
        String name = firebaseUser.getDisplayName();

        if (name == null || name.trim().isEmpty()) {
            int atPos = email.indexOf("@");
            if (atPos > 0) {
                name = email.substring(0, atPos);
            } else {
                name = "Khach hang";
            }
        }

        String defaultAvatar = "https://res.cloudinary.com/dqe19i7og/image/upload/v1731659931/pngtree-account-avatar-user-abstract-circle-background-flat-color-icon-png-image_1650938_xqovwm.jpg";
        return new User(uid, name, "", email, defaultAvatar, new ArrayList<>());
    }
}