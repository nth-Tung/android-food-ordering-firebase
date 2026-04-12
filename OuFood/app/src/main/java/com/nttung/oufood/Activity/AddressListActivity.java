package com.nttung.oufood.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nttung.oufood.Adapter.AddressAdapter;
import com.nttung.oufood.Class.AnAddress;
import com.nttung.oufood.Helper.SessionManager;
import com.nttung.oufood.R;
import com.nttung.oufood.common.Common;

import java.util.ArrayList;
import java.util.List;

public class AddressListActivity extends AppCompatActivity {

    public static final String EXTRA_SELECT_MODE = "select_mode";
    public static final String EXTRA_EDIT_INDEX = "edit_index";
    public static final String RESULT_ADDRESS = "selected_address";
    public static final String RESULT_WARD_PATH = "selected_ward_path";
    public static final String RESULT_NAME = "selected_name";
    public static final String RESULT_PHONE = "selected_phone";

    private Toolbar toolbar;
    private TextView textViewUpdate;
    private TextView textViewTypeAddress;
    private TextView textViewAddress;
    private TextView textViewName;
    private AppCompatButton buttonAddAddress;
    private RecyclerView recyclerViewAddress;

    private AddressAdapter adapter;
    private final List<AnAddress> addresses = new ArrayList<>();
    private boolean selectMode;

    private final ActivityResultLauncher<Intent> editAddressLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> loadAddressData());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        selectMode = getIntent().getBooleanExtra(EXTRA_SELECT_MODE, false);

        textViewUpdate = findViewById(R.id.textView_update);
        textViewTypeAddress = findViewById(R.id.textView_typeAddress);
        textViewAddress = findViewById(R.id.textView_address);
        textViewName = findViewById(R.id.textView_name);
        buttonAddAddress = findViewById(R.id.button_addAddress);
        recyclerViewAddress = findViewById(R.id.recyclerView_address);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SessionManager.init(this);
        if (Common.CURRENTUSER == null) {
            SessionManager.getCachedUser();
        }

        adapter = new AddressAdapter(addresses, selectMode, new AddressAdapter.OnAddressActionListener() {
            @Override
            public void onEdit(int position, AnAddress address) {
                openAddressEditor(position);
            }

            @Override
            public void onSelect(int position, AnAddress address) {
                returnSelectedAddress(address);
            }
        });
        recyclerViewAddress.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerViewAddress.setAdapter(adapter);

        textViewUpdate.setOnClickListener(v -> {
            int index = findDefaultAddressIndex();
            if (index < 0 && !addresses.isEmpty()) {
                index = 0;
            }

            if (selectMode && index >= 0 && index < addresses.size()) {
                returnSelectedAddress(addresses.get(index));
                return;
            }

            openAddressEditor(index);
        });

        buttonAddAddress.setOnClickListener(v -> openAddressEditor(-1));

        loadAddressData();
    }

    private void loadAddressData() {
        SessionManager.fetchUserFromFirebase(user -> runOnUiThread(() -> {
            addresses.clear();
            if (user != null && user.getAddresses() != null) {
                addresses.addAll(user.getAddresses());
            }
            adapter.notifyDataSetChanged();
            bindPrimaryAddress();
        }));
    }

    private void bindPrimaryAddress() {
        if (addresses.isEmpty()) {
            textViewTypeAddress.setText("Chưa có địa chỉ");
            textViewAddress.setText("Vui lòng thêm địa chỉ mới");
            textViewName.setText("");
            textViewUpdate.setText(selectMode ? "Chọn" : "Sửa");
            return;
        }

        int defaultIndex = findDefaultAddressIndex();
        if (defaultIndex < 0) {
            defaultIndex = 0;
        }

        AnAddress primary = addresses.get(defaultIndex);
        textViewTypeAddress.setText(primary.getTypeAddress() == null ? "OTHER" : primary.getTypeAddress().toString());
        textViewAddress.setText(primary.getAddress() == null ? "" : primary.getAddress());
        textViewName.setText(primary.getName() == null ? "" : primary.getName());
        textViewUpdate.setText(selectMode ? "Chọn" : "Sửa");
    }

    private int findDefaultAddressIndex() {
        for (int i = 0; i < addresses.size(); i++) {
            if (addresses.get(i) != null && addresses.get(i).getIsDefault()) {
                return i;
            }
        }
        return -1;
    }

    private void openAddressEditor(int editIndex) {
        Intent intent = new Intent(AddressListActivity.this, AddressActivity.class);
        intent.putExtra(EXTRA_EDIT_INDEX, editIndex);
        editAddressLauncher.launch(intent);
    }

    private void returnSelectedAddress(AnAddress address) {
        if (address == null) {
            Toast.makeText(this, "Địa chỉ không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent data = new Intent();
        data.putExtra(RESULT_ADDRESS, address.getAddress());
        data.putExtra(RESULT_WARD_PATH, address.getWard() == null ? "" : address.getWard().getPath());
        data.putExtra(RESULT_NAME, address.getName());
        data.putExtra(RESULT_PHONE, address.getPhone());
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}