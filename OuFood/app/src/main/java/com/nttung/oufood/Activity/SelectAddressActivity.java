package com.nttung.oufood.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.nttung.oufood.Adapter.AddressSuggetionAdapter;
import com.nttung.oufood.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelectAddressActivity extends AppCompatActivity {
    private final int FINE_PERMISSION_CODE = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;

    private Toolbar toolbar;
    private RecyclerView recyclerViewAddressSuggestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_address);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewAddressSuggestion= findViewById(R.id.recyclerView_addressSuggestion);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
    }

    private void getLastLocation(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SelectAddressActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location == null) {
                    Toast.makeText(SelectAddressActivity.this, "Không lấy được vị trí hiện tại", Toast.LENGTH_SHORT).show();
                    return;
                }

                Geocoder geocoder = new Geocoder(SelectAddressActivity.this);
                List<Address> addresses = new ArrayList<>();
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 15);
                } catch (IOException e) {
                    Toast.makeText(SelectAddressActivity.this, "Không lấy được gợi ý địa chỉ", Toast.LENGTH_SHORT).show();
                }

                if (addresses == null || addresses.isEmpty()) {
                    Toast.makeText(SelectAddressActivity.this, "Không có địa chỉ gợi ý", Toast.LENGTH_SHORT).show();
                    return;
                }

                AddressSuggetionAdapter adapter = new AddressSuggetionAdapter(addresses, address -> {
                    Intent data = new Intent();
                    data.putExtra("selected_address", address.getAddressLine(0));
                    data.putExtra("selected_ward_path", address.getSubAdminArea());
                    setResult(RESULT_OK, data);
                    finish();
                });
                recyclerViewAddressSuggestion.setLayoutManager(new LinearLayoutManager(SelectAddressActivity.this,LinearLayoutManager.VERTICAL,false));
                recyclerViewAddressSuggestion.setAdapter(adapter);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền vị trí để gợi ý địa chỉ", Toast.LENGTH_SHORT).show();
            }
        }
    }
}