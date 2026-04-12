package com.nttung.oufood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.nttung.oufood.Class.AnAddress;
import com.nttung.oufood.Class.Ward;
import com.nttung.oufood.Helper.AddressType;
import com.nttung.oufood.Helper.SessionManager;
import com.nttung.oufood.R;
import com.nttung.oufood.common.Common;

import java.util.ArrayList;
import java.util.List;

public class AddressActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView imageView_updateAddress;
    private EditText editName;
    private EditText editPhone;
    private EditText editAddress;
    private AppCompatButton buttonSave;
    private AppCompatButton buttonDelete;
    private MaterialButtonToggleGroup toggleGroup;

    private final List<AnAddress> addresses = new ArrayList<>();
    private int editIndex = -1;
    private String selectedWardPath = "";

    private final ActivityResultLauncher<Intent> selectAddressLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }

                String address = result.getData().getStringExtra("selected_address");
                String wardPath = result.getData().getStringExtra("selected_ward_path");
                if (address != null) {
                    editAddress.setText(address);
                }
                selectedWardPath = wardPath == null ? "" : wardPath;
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        SessionManager.init(this);
        if (Common.CURRENTUSER == null) {
            SessionManager.getCachedUser();
        }

        if (Common.CURRENTUSER == null) {
            Toast.makeText(this, "Bạn cần đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (Common.CURRENTUSER.getAddresses() != null) {
            addresses.addAll(Common.CURRENTUSER.getAddresses());
        }

        editIndex = getIntent().getIntExtra(AddressListActivity.EXTRA_EDIT_INDEX, -1);

        editName = findViewById(R.id.textView_name);
        editPhone = findViewById(R.id.textView_phoneNumber);
        editAddress = findViewById(R.id.textView_address);
        imageView_updateAddress = findViewById(R.id.imageView_updateAddress);
        buttonSave = findViewById(R.id.button_saveAddress);
        buttonDelete = findViewById(R.id.button_deleteAddress);
        toggleGroup = findViewById(R.id.toggle_group);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (editIndex >= 0 && editIndex < addresses.size()) {
            bindAddressForEdit(addresses.get(editIndex));
        } else {
            buttonDelete.setEnabled(false);
            buttonDelete.setAlpha(0.5f);
        }

        imageView_updateAddress.setOnClickListener(v -> {
            selectAddressLauncher.launch(new Intent(AddressActivity.this, SelectAddressActivity.class));
        });

        buttonSave.setOnClickListener(v -> saveAddress());
        buttonDelete.setOnClickListener(v -> deleteAddress());
    }

    private void bindAddressForEdit(AnAddress address) {
        if (address == null) {
            return;
        }

        editName.setText(address.getName() == null ? "" : address.getName());
        editPhone.setText(address.getPhone() == null ? "" : address.getPhone());
        editAddress.setText(address.getAddress() == null ? "" : address.getAddress());
        selectedWardPath = address.getWard() == null ? "" : address.getWard().getPath();

        if (address.getTypeAddress() == AddressType.WORK) {
            toggleGroup.check(R.id.btn_work);
        } else if (address.getTypeAddress() == AddressType.OTHER) {
            toggleGroup.check(R.id.btn_other);
        } else {
            toggleGroup.check(R.id.btn_home);
        }
    }

    private void saveAddress() {
        String name = editName.getText() == null ? "" : editName.getText().toString().trim();
        String phone = editPhone.getText() == null ? "" : editPhone.getText().toString().trim();
        String addressText = editAddress.getText() == null ? "" : editAddress.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || addressText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        AddressType addressType = AddressType.HOME;
        int checkedId = toggleGroup.getCheckedButtonId();
        if (checkedId == R.id.btn_work) {
            addressType = AddressType.WORK;
        } else if (checkedId == R.id.btn_other) {
            addressType = AddressType.OTHER;
        }

        boolean isDefault = addresses.isEmpty();
        if (editIndex >= 0 && editIndex < addresses.size()) {
            isDefault = addresses.get(editIndex).getIsDefault();
        }

        AnAddress address = new AnAddress(
                addressText,
                isDefault,
                name,
                phone,
                addressType,
                new Ward("", "", "", selectedWardPath)
        );

        if (editIndex >= 0 && editIndex < addresses.size()) {
            addresses.set(editIndex, address);
        } else {
            addresses.add(address);
        }

        ensureDefaultAddress();
        persistAddressesAndFinish("Lưu địa chỉ thành công");
    }

    private void deleteAddress() {
        if (editIndex < 0 || editIndex >= addresses.size()) {
            Toast.makeText(this, "Không tìm thấy địa chỉ để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        addresses.remove(editIndex);
        ensureDefaultAddress();
        persistAddressesAndFinish("Đã xóa địa chỉ");
    }

    private void ensureDefaultAddress() {
        if (addresses.isEmpty()) {
            return;
        }

        boolean hasDefault = false;
        for (AnAddress item : addresses) {
            if (item != null && item.getIsDefault()) {
                hasDefault = true;
                break;
            }
        }

        if (!hasDefault && addresses.get(0) != null) {
            addresses.get(0).setIsDefault(true);
        }
    }

    private void persistAddressesAndFinish(String message) {
        Common.CURRENTUSER.setAddresses(new ArrayList<>(addresses));
        SessionManager.saveUser(Common.CURRENTUSER);

        Common.FIREBASE_DATABASE
                .getReference(Common.REF_USERS)
                .child(Common.CURRENTUSER.getIdUser())
                .child("addresses")
                .setValue(Common.CURRENTUSER.getAddresses())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AddressActivity.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddressActivity.this, "Không thể đồng bộ địa chỉ", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}