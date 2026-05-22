# SmartSpend — Personal Finance Manager

**SmartSpend** là ứng dụng desktop hỗ trợ quản lý tài chính cá nhân: ghi lại thu nhập và chi tiêu, lập ngân sách theo danh mục, theo dõi dòng tiền và nhận gợi ý tài chính từ Smart Coach.

Ứng dụng được xây dựng bằng **JavaFX** với giao diện dashboard trực quan, phù hợp cho việc theo dõi thói quen chi tiêu hằng ngày và lập kế hoạch sử dụng tiền hợp lý hơn.

**Host / Developer:** Trần Trung Hiếu  
**Email:** trantrunghieu30032006@gmail.com

## Những gì SmartSpend làm được

### Quản lý tài khoản

- Đăng ký tài khoản bằng họ tên, email và mật khẩu.
- Đăng nhập bằng email và mật khẩu.
- Ghi nhớ email đăng nhập trên máy cá nhân.
- Quên mật khẩu bằng mã xác nhận 6 số.
- Gửi email chào mừng, mã đặt lại mật khẩu và cảnh báo đăng nhập khi người dùng đã cấu hình email notifications.
- **Gmail quick access (local):** tạo/mở nhanh tài khoản theo email để trình diễn ứng dụng; đây không phải Google OAuth chính thức.

### Dashboard

- Xem nhanh số dư ròng, thu nhập, chi tiêu và số tiền có thể dành cho mục tiêu tiếp theo.
- Biểu đồ dòng tiền của 6 tháng gần nhất với dữ liệu thật đã nhập.
- Theo dõi giao dịch gần đây.
- Điểm sức khỏe tài chính và gợi ý ưu tiên theo tình hình dòng tiền.

### Transactions

- Thêm khoản thu hoặc khoản chi.
- Ghi ngày giao dịch, danh mục và nội dung ghi chú.
- Sửa giao dịch bằng cách double-click vào một dòng trong bảng.
- Xóa giao dịch đã chọn.
- Tìm kiếm và lọc theo loại giao dịch.
- Xuất danh sách giao dịch hoặc báo cáo tổng hợp dưới dạng CSV.

### Monthly Budgets

- Thiết lập ngân sách cho từng danh mục chi tiêu trong tháng hiện tại.
- Xem tổng ngân sách, số tiền đã dùng và số tiền còn lại.
- Theo dõi mức sử dụng của từng danh mục bằng progress bar.
- Nhận cảnh báo trực quan khi chi tiêu gần hoặc vượt giới hạn.

### Insights

- So sánh thu nhập và chi tiêu theo thời gian.
- Xem tỷ lệ tiết kiệm và dòng tiền khả dụng.
- Phân tích các nhóm chi tiêu cao nhất dựa trên giao dịch thật.
- Mở Smart Coach từ phần recommendation để nhận phân tích sâu hơn.

### Smart Coach AI Center

Smart Coach là khu vực tư vấn tài chính của SmartSpend. Người dùng có thể chọn nhiều chế độ phân tích:

- **Tư vấn tổng quan:** tóm tắt tình hình tài chính và hành động nên ưu tiên.
- **Phân tích dòng tiền:** xem thu, chi, số dư và nhóm chi tiêu nổi bật.
- **Lập ngân sách:** đề xuất giới hạn chi tiêu cho tháng kế tiếp.
- **Cảnh báo rủi ro:** tìm dấu hiệu chi tiêu vượt khả năng tài chính.
- **Kế hoạch tiết kiệm:** đề xuất mức tiết kiệm phù hợp theo dữ liệu hiện có.

Smart Coach luôn có chế độ phân tích offline nên vẫn sử dụng được ngay cả khi chưa cài AI local. Khi máy có **Ollama** và model đã tải, ứng dụng sẽ tự phát hiện và dùng mô hình AI local để trả lời tự nhiên hơn. Dữ liệu giao dịch khi dùng Ollama local chỉ được gửi tới dịch vụ chạy trên chính máy người dùng.

## Yêu cầu để chạy ứng dụng

- Windows 10 hoặc Windows 11.
- JDK 21 trở lên.
- Internet ở lần chạy đầu tiên để Maven tải các thư viện cần thiết.

SmartSpend dùng cơ sở dữ liệu local và tự tạo dữ liệu cần thiết trong lần chạy đầu tiên. Người dùng không cần tạo database thủ công.

## Chạy SmartSpend trên Windows

### Cách nhanh nhất

1. Giải nén project.
2. Mở terminal tại thư mục có file `pom.xml`.
3. Chạy:

```powershell
.\run-windows.cmd
```

Script sẽ tìm JDK phù hợp trên máy và khởi động ứng dụng.

### Chạy trực tiếp bằng Maven Wrapper

Khi máy đã cấu hình Java sẵn, có thể chạy:

```powershell
.\mvnw.cmd clean javafx:run
```

### Mở project bằng IntelliJ IDEA

1. Mở IntelliJ IDEA → **File → Open**.
2. Chọn thư mục chứa `pom.xml`.
3. Khi IntelliJ hỏi, chọn **Trust Project**.
4. Vào **File → Project Structure → Project**, chọn SDK là JDK 21 trở lên.
5. Mở tab **Maven** và chọn **Reload All Maven Projects**.
6. Mở Terminal trong IntelliJ và chạy:

```powershell
.\run-windows.cmd
```

## Tài khoản dùng thử

```text
Email: demo@smartspend.test
Password: demo123
```

Bạn cũng có thể đăng ký tài khoản mới trực tiếp tại màn hình đăng ký.

## Bật tính năng gửi email

Email notifications giúp SmartSpend gửi mã quên mật khẩu, email chào mừng, cảnh báo đăng nhập và báo cáo từ Smart Coach.

Trong ứng dụng, mở mục **Email notifications** rồi điền thông tin SMTP. Với Gmail:

```text
SMTP Host: smtp.gmail.com
SMTP Port: 465
From Email: địa chỉ Gmail dùng để gửi
Username: địa chỉ Gmail dùng để gửi
Password: Gmail App Password
Use SSL/TLS: bật
```

Sau khi lưu, bấm **Send Test Email** để kiểm tra.

Lưu ý: Gmail yêu cầu dùng **App Password**, không dùng mật khẩu đăng nhập Gmail thông thường. Nếu chưa cấu hình email, các chức năng còn lại vẫn hoạt động; phần quên mật khẩu có thể hiển thị mã kiểm thử ngay trong ứng dụng.

## Sử dụng AI local với Ollama

Ollama là phần mở rộng tùy chọn dành cho Smart Coach. Ứng dụng vẫn dùng được khi không cài Ollama.

Để cài nhanh trên Windows và tải model mặc định:

```powershell
.\setup-ollama-windows.ps1
```

Hoặc nếu đã cài Ollama, chạy:

```powershell
ollama pull llama3.2
```

Sau đó mở SmartSpend → **Smart Coach**. Ứng dụng tự kiểm tra Ollama local, hiển thị các model đã cài và cho phép chọn model trực tiếp trong giao diện.

## Dữ liệu cá nhân

Dữ liệu của ứng dụng được lưu trên máy trong thư mục:

```text
./data/
```

Muốn tạo lại dữ liệu từ đầu:

1. Đóng ứng dụng.
2. Xóa thư mục `data/`.
3. Chạy lại SmartSpend.

## Lưu ý khi đưa source code lên GitHub

Không đưa các nội dung riêng tư hoặc cấu hình bí mật lên repository, đặc biệt là:

```text
Gmail App Password
API key
Thư mục data/
File database local
```
