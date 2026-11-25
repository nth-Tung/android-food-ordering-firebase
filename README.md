# 🍔 OuFood - Android Food Ordering App (Production MVP)

Ứng dụng đặt món ăn trực tuyến hiện đại dành cho nền tảng Android, được xây dựng theo tiêu chuẩn **Production MVP** dựa trên các quy chuẩn mới nhất của Google Android: **Clean Architecture**, **Jetpack Compose (Material 3)**, **Kotlin 2.0+**, **Hilt DI**, **Room Database** và **Firebase**.

---

## 🛠 Tech Stack & Kiến Trúc

- **Language:** Kotlin 2.0+ (100% Kotlin, Null-Safety, Coroutines, StateFlow)
- **UI Framework:** Jetpack Compose + Material 3 (Edge-to-Edge, Dark & Light Mode)
- **Architecture:** Clean Architecture + MVVM (Data - Domain - Presentation Layers)
- **Dependency Injection:** Dagger Hilt
- **Local Persistence:** Room Database (Cart) & Jetpack DataStore Preferences (Session)
- **Remote Backend:** Firebase Authentication & Firebase Realtime Database
- **Image Loading:** Coil Compose
- **Logging & Monitoring:** Timber

---

## 📂 Cấu Trúc Dự Án (Clean Architecture)

```
com.nttung.oufood/
├── core/
│   ├── common/             # Resource wrapper, Formatters tiền tệ/ngày tháng
│   └── designsystem/       # Material 3 Theme, Typography, Colors, Components
├── data/
│   ├── local/              # Room DB (CartEntity, CartDao), DataStore Preferences
│   ├── remote/dto/         # Firebase Realtime DB Data Transfer Objects
│   ├── mapper/             # Data mappers giữa DTO/Entity và Domain Model
│   └── repository/         # Triển khai Repository (Auth, Food, Cart, Order)
├── domain/
│   ├── model/              # Domain Models sạch (Food, Category, CartItem, Order, User)
│   └── repository/         # Repository Interfaces
├── di/                     # Hilt Modules (AppModule, RepositoryModule)
└── presentation/
    ├── auth/               # Đăng nhập, Đăng ký, Quên mật khẩu
    ├── home/               # Khám phá món ăn, Danh mục, Tìm kiếm realtime
    ├── detail/             # Chi tiết món, Chọn số lượng, Thêm vào giỏ
    ├── cart/               # Giỏ hàng, Quản lý số lượng, Đặt hàng
    ├── orders/             # Theo dõi trạng thái đơn hàng realtime, Chi tiết đơn
    ├── profile/            # Thông tin tài khoản, Cập nhật hồ sơ, Đăng xuất
    ├── navigation/         # NavHost, App Destinations & Routes
    └── MainActivity.kt     # Single Activity duy nhất
```

---

## 🚀 Các Tính Năng MVP Đã Hoàn Thiện

1. **Authentication (Xác thực):**
   - Đăng nhập, Đăng ký, Quên mật khẩu với Firebase Auth.
   - Quản lý phiên làm việc tự động với Firebase Auth State Listener và DataStore.
   - Không lưu mật khẩu plain-text vào Database (chuẩn bảo mật).

2. **Khám phá món ăn (Home & Search):**
   - Hiển thị danh mục món ăn dạng filter chips cuộn ngang mượt mà.
   - Lưới món ăn 2 cột hiển thị hình ảnh, giá gốc, % giảm giá, điểm đánh giá.
   - Tìm kiếm món ăn realtime (Debounce 300ms) kết hợp bộ lọc danh mục.

3. **Chi tiết món (Food Detail):**
   - Header hình ảnh tràn viền.
   - Chọn số lượng món linh hoạt (+ / -), ghi chú riêng cho quán.
   - Thêm vào giỏ hàng cục bộ tức thời với Snackbar thông báo.

4. **Giỏ hàng & Đặt hàng (Cart & Checkout):**
   - Quản lý giỏ hàng offline-first lưu vào **Room Database** (không sợ mất giỏ khi tắt app).
   - Tự động tính toán tổng tiền, chiết khấu và phí giao hàng.
   - Form nhập địa chỉ nhận hàng, phương thức thanh toán tiền mặt (COD).
   - Tạo đơn hàng đẩy lên Firebase Realtime Database và tự động dọn sạch giỏ hàng khi thành công.

5. **Theo dõi đơn hàng (Order Tracking):**
   - Phân loại đơn: *Đang thực hiện* và *Lịch sử hoàn tất*.
   - Theo dõi trạng thái đơn hàng thời gian thực: *Chờ xác nhận*, *Đã xác nhận*, *Đang giao*, *Đã giao*, *Đã hủy*.
   - Hỗ trợ hủy đơn hàng khi đơn còn ở trạng thái Chờ xác nhận.

6. **Hồ sơ cá nhân (Profile):**
   - Xem thông tin tài khoản, chỉnh sửa Họ tên & Số điện thoại.
   - Đăng xuất an toàn với hộp thoại xác nhận.
