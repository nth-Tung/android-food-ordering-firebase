package com.nttung.oufood.Activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nttung.oufood.Adapter.CartAdapter;
import com.nttung.oufood.Class.AnAddress;
import com.nttung.oufood.Class.Order;
import com.nttung.oufood.Class.Request;
import com.nttung.oufood.Class.Ward;
import com.nttung.oufood.Database.Database;
import com.nttung.oufood.Helper.Notification;
import com.nttung.oufood.R;
import com.nttung.oufood.Helper.AddressType;
import com.nttung.oufood.common.Common;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    Database database;
    Toolbar toolbar;
    TextView textViewBasketTotal;
    TextView textViewDelivery;
    TextView textViewDiscount;
    TextView textViewTotal_a;
    TextView textViewTotal_b;
    TextView textViewNoData;
    TextView textViewAddressName;
    TextView textViewAddressPhone;
    TextView textViewAddressDetail;
    TextView textViewWardPath;
    TextView textViewNullAddress;
    RelativeLayout layoutSelectAddress;
    AppCompatButton buttonOrder;

    List<Order> cart;

    CartAdapter adapter;
    private boolean isPlacingOrder = false;
    private AnAddress selectedAddress;

    private final ActivityResultLauncher<Intent> selectAddressLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }

                String address = result.getData().getStringExtra(AddressListActivity.RESULT_ADDRESS);
                String wardPath = result.getData().getStringExtra(AddressListActivity.RESULT_WARD_PATH);
                String selectedName = result.getData().getStringExtra(AddressListActivity.RESULT_NAME);
                String selectedPhone = result.getData().getStringExtra(AddressListActivity.RESULT_PHONE);

                if (address == null || address.trim().isEmpty()) {
                    return;
                }

                String name = selectedName != null ? selectedName : (Common.CURRENTUSER != null ? Common.CURRENTUSER.getName() : "");
                String phone = selectedPhone != null ? selectedPhone : (Common.CURRENTUSER != null ? Common.CURRENTUSER.getPhone() : "");
                selectedAddress = new AnAddress(
                        address,
                        true,
                        name,
                        phone,
                        AddressType.HOME,
                        new Ward("", "", "", wardPath == null ? "" : wardPath)
                );

                updateAddressUi();
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        database = new Database(CartActivity.this);

        textViewBasketTotal = findViewById(R.id.basketTotal);
        textViewDelivery = findViewById(R.id.delivery);
        textViewDiscount = findViewById(R.id.discount);
        textViewTotal_a = findViewById(R.id.total_a);
        textViewTotal_b = findViewById(R.id.total_b);
        textViewNoData = findViewById(R.id.no_data);
        textViewAddressName = findViewById(R.id.name);
        textViewAddressPhone = findViewById(R.id.phone);
        textViewAddressDetail = findViewById(R.id.address);
        textViewWardPath = findViewById(R.id.ward_getPath);
        textViewNullAddress = findViewById(R.id.null_address);
        layoutSelectAddress = findViewById(R.id.layout_select_address);


        toolbar = findViewById(R.id.toolbar_Cart);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        loadListCart();
        loadDefaultAddress();
        updateAddressUi();

        layoutSelectAddress.setOnClickListener(v ->
            selectAddressLauncher.launch(new Intent(CartActivity.this, AddressListActivity.class)
                    .putExtra(AddressListActivity.EXTRA_SELECT_MODE, true))
        );

        buttonOrder = findViewById(R.id.btn_order);
        buttonOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBtn_order();
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void loadDefaultAddress() {
        if (Common.CURRENTUSER == null || Common.CURRENTUSER.getAddresses() == null || Common.CURRENTUSER.getAddresses().isEmpty()) {
            return;
        }

        AnAddress fallback = Common.CURRENTUSER.getAddresses().get(0);
        for (AnAddress item : Common.CURRENTUSER.getAddresses()) {
            if (item != null && item.getIsDefault()) {
                fallback = item;
                break;
            }
        }
        selectedAddress = fallback;
    }

    public void loadListCart() {
        cart = database.getCarts();
        recyclerView.setLayoutManager(new LinearLayoutManager(CartActivity.this, RecyclerView.VERTICAL, false));
        adapter = new CartAdapter(cart);
        recyclerView.setAdapter(adapter);

        updateBill();

        if (adapter.getItemCount() > 0) {
            textViewNoData.setVisibility(View.GONE);
        } else {
            textViewNoData.setVisibility(View.VISIBLE);
        }

    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CartActivity.this);
            builder.setTitle("Delete a requests?");
            builder.setMessage("Are you sure?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                adapter.removeOrder(position);
                adapter.notifyItemRemoved(position);
                updateBill();

                if (adapter.getItems() == null || adapter.getItems().isEmpty()) {
                    textViewNoData.setVisibility(View.VISIBLE);
                } else {
                    textViewNoData.setVisibility(View.GONE);
                }

            });

            builder.setNegativeButton("No", (dialog, which) -> {
                int position = viewHolder.getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    adapter.notifyItemChanged(position);
                }
            });

            builder.show();
        }
    };

    public void updateBill() {
        try (Database database = new Database(getBaseContext())) {
            List<Order> orders = database.getCarts();
            double basketTotal = 0; // Tổng giá trị
            double delivery = 10000;
            double discount = 0; // Giảm giá
            if (!orders.isEmpty()) {
                for (Order order : orders) {
                        int price = parseIntSafe(order.getPrice());
                        int quantity = parseIntSafe(order.getQuantity());
                        int itemTotal = price * quantity;
                        basketTotal += itemTotal;

                        // Thêm logic giảm giá (nếu có discount)
                        discount += itemTotal * parseIntSafe(order.getDiscount()) / 100.0; // Ví dụ giảm giá theo %

                }
            }

            textViewBasketTotal.setText(String.valueOf(basketTotal));
            textViewDelivery.setText(String.valueOf(delivery));
            textViewDiscount.setText(String.valueOf(discount));
            double total = (basketTotal + delivery) - discount;
            textViewTotal_a.setText(String.valueOf(total));
            textViewTotal_b.setText(String.valueOf(total));
        }
    }

    private void clickBtn_order() {
//        if (diaChi == null) {
//            CuteToast.ct(getBaseContext(), "Hãy chọn địa chỉ", Toast.LENGTH_SHORT, CuteToast.WARN, true).show();
//        } else {
                if (Common.CURRENTUSER == null || Common.CURRENTUSER.getIdUser() == null) {
                    Toast.makeText(this, "Phiên đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (cart == null || cart.isEmpty()) {
                    Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedAddress == null) {
                    Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isPlacingOrder) {
                    return;
                }

                isPlacingOrder = true;
                buttonOrder.setEnabled(false);

                List<Order> orderSnapshot = new ArrayList<>(cart);

                String id = String.valueOf(System.currentTimeMillis());
                Request request = new Request(id, Common.CURRENTUSER.getIdUser(), textViewTotal_a.getText().toString(), orderSnapshot, selectedAddress, "0");

                Common.FIREBASE_DATABASE.getReference(Common.REF_REQUESTS).child(id).setValue(request)
                        .addOnCompleteListener(task -> {
                            isPlacingOrder = false;
                            buttonOrder.setEnabled(true);

                            if (task.isSuccessful()) {
                                try {
                                    scheduleNotification(Common.DELAY_TIME, id);
                                } catch (Exception ignored) {
                                }

                                database.cleanCart();
                                cart.clear();
                                if (adapter != null) {
                                    adapter.setItems(cart);
                                    adapter.notifyDataSetChanged();
                                }
                                updateBill();
                                textViewNoData.setVisibility(View.VISIBLE);

                                Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Đặt hàng thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                            }
                        });
//        }
    }

    private void updateAddressUi() {
        if (selectedAddress == null) {
            textViewNullAddress.setVisibility(View.VISIBLE);
            textViewAddressName.setText("");
            textViewAddressPhone.setText("");
            textViewAddressDetail.setText("");
            textViewWardPath.setText("");
            return;
        }

        textViewNullAddress.setVisibility(View.GONE);
        textViewAddressName.setText(selectedAddress.getName() == null ? "" : selectedAddress.getName());
        textViewAddressPhone.setText(selectedAddress.getPhone() == null ? "" : selectedAddress.getPhone());
        textViewAddressDetail.setText(selectedAddress.getAddress() == null ? "" : selectedAddress.getAddress());
        textViewWardPath.setText(selectedAddress.getWard() == null ? "" : selectedAddress.getWard().getPath());
    }

    private int parseIntSafe(String value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void scheduleNotification(int delayTime, String idRequest) {
        Intent notificationIntent = new Intent(CartActivity.this, Notification.class);

        notificationIntent.putExtra("idRequest", idRequest);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(CartActivity.this, Common.NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            long triggerTime = System.currentTimeMillis() + delayTime;
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}