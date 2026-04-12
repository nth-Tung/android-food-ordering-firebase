package com.nttung.oufood.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import com.nttung.oufood.Activity.ChangePasswordActivity;
import com.nttung.oufood.Activity.EditAvatarActivity;
import com.nttung.oufood.Activity.EditNameUserActivity;
import com.nttung.oufood.Activity.EditPhoneUserActivity;
import com.nttung.oufood.Helper.SessionManager;
import com.nttung.oufood.Activity.SignInActivity;
import com.nttung.oufood.R;
import com.nttung.oufood.common.Common;


public class AccountFragment extends Fragment {

    private TextView name, phone, email, logout;

    private ImageView pic;

    private ImageView ic_next_name, ic_next_phone, ic_next_password;

    private ImageView buttonChangeImage;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);


        pic = view.findViewById(R.id.pic);
        name = view.findViewById(R.id.name);
        phone = view.findViewById(R.id.phone);
        email = view.findViewById(R.id.email);
        logout = view.findViewById(R.id.button_logout);
        buttonChangeImage=view.findViewById(R.id.button_changeImage);

        //logout
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SessionManager.init(requireContext());
                SessionManager.clearSession();
                startActivity(new Intent(requireActivity(), SignInActivity.class));
                requireActivity().finish();
            }
        });

        SessionManager.init(requireContext());
        if (Common.CURRENTUSER == null) {
            SessionManager.getCachedUser();
        }

        renderUserInfo();

        buttonChangeImage.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), EditAvatarActivity.class));
        });

        ic_next_name = view.findViewById(R.id.ic_next_name);
        ic_next_name.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), EditNameUserActivity.class));
        });

        ic_next_phone = view.findViewById(R.id.ic_next_phone);
        ic_next_phone.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), EditPhoneUserActivity.class));
        });

        ic_next_password = view.findViewById(R.id.ic_next_password);
        ic_next_password.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), ChangePasswordActivity.class));
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        renderUserInfo();
    }

    private void renderUserInfo() {
        if (Common.CURRENTUSER == null) {
            name.setText("");
            phone.setText("");
            email.setText("");
            return;
        }

        String url = Common.CURRENTUSER.getUrl();
        if (url != null && !url.trim().isEmpty()) {
            Picasso.get().load(url).into(pic);
        }

        name.setText(Common.CURRENTUSER.getName() == null ? "" : Common.CURRENTUSER.getName());

        String sPhone = Common.CURRENTUSER.getPhone();
        if (sPhone != null && sPhone.length() > 3) {
            phone.setText("*******" + sPhone.substring(sPhone.length() - 3));
        } else {
            phone.setText(sPhone == null ? "" : sPhone);
        }

        String s = Common.CURRENTUSER.getEmail();
        if (s != null && s.contains("@") && s.length() > 2) {
            int index = s.indexOf("@");
            email.setText(s.substring(0, 1) + "*****" + s.substring(Math.max(index - 1, 1)));
        } else {
            email.setText(s == null ? "" : s);
        }
    }
}