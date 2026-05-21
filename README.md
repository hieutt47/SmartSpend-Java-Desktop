# SmartSpend Java Desktop

SmartSpend là ứng dụng quản lý tài chính cá nhân viết bằng JavaFX. Ứng dụng giúp người dùng ghi lại thu nhập, chi tiêu, theo dõi dòng tiền, xem thống kê theo tháng, quản lý ngân sách và nhận gợi ý tài chính thông qua Smart Coach.

Dự án phù hợp với nhu cầu học tập, thực hành Java desktop app và xây dựng một sản phẩm quản lý chi tiêu có đầy đủ các phần thường gặp: đăng nhập, đăng ký, database, dashboard, CRUD, xuất báo cáo, gửi email và trợ lý tư vấn.

## Chức năng chính

### Tài khoản

- Đăng ký tài khoản bằng họ tên, email và mật khẩu.
- Đăng nhập bằng email và mật khẩu.
- Google Sign-In ở chế độ demo/local để tạo nhanh tài khoản theo email Google.
- Quên mật khẩu bằng mã reset 6 số.
- Gửi email chào mừng, email mã reset và cảnh báo đăng nhập nếu đã cấu hình SMTP.

### Quản lý giao dịch

- Thêm giao dịch thu nhập hoặc chi tiêu.
- Chọn ngày, danh mục, số tiền và ghi chú.
- Sửa giao dịch bằng cách double-click trong bảng giao dịch.
- Xóa giao dịch đã chọn.
- Tìm kiếm và lọc giao dịch theo loại thu/chi.
- Xuất danh sách giao dịch ra file CSV.
- Xuất báo cáo tổng hợp ra file CSV.

### Dashboard

- Hiển thị số dư ròng.
- Hiển thị thu nhập, chi tiêu và khoản tiền nên giữ lại trong tháng.
- Biểu đồ dòng tiền 6 tháng gần nhất.
- Danh sách giao dịch gần đây.
- Điểm sức khỏe tài chính dựa trên dòng tiền và tỷ lệ tiết kiệm.
- Gợi ý ưu tiên tiếp theo dựa trên dữ liệu hiện tại.

### Budgets

- Xem ngân sách theo các nhóm chi tiêu.
- Theo dõi số tiền đã chi và phần còn lại.
- Cảnh báo trực quan khi một danh mục gần vượt ngân sách.

### Insights

- So sánh thu nhập và chi tiêu.
- Xem tỷ lệ tiết kiệm.
- Xem nhóm chi tiêu nổi bật.
- Đọc snapshot tài chính trong tháng.

### Smart Coach

Smart Coach là phần tư vấn tài chính trong app. Chức năng này đọc dữ liệu giao dịch của người dùng và đưa ra nhận xét như:

- Tháng này dòng tiền đang ổn hay đang âm.
- Nên tiết kiệm khoảng bao nhiêu.
- Danh mục nào đang chi nhiều nhất.
- Nên đặt ngân sách tháng sau như thế nào.
- Các hành động nên làm để cải thiện tình hình tài chính.

Smart Coach luôn có chế độ tư vấn offline tích hợp sẵn nên vẫn chạy được khi không có internet hoặc chưa cài AI local. Nếu máy có cài Ollama và đã tải model, app sẽ tự nhận Ollama để trả lời tự nhiên hơn.

## Yêu cầu để chạy

- Windows 10/11.
- JDK 21 hoặc mới hơn.
- Internet ở lần chạy đầu tiên để Maven tải thư viện.
- Không cần cài MySQL.

Database được tạo tự động bằng H2 embedded database và lưu trong thư mục `data/` của project.

## Cách chạy trên Windows

Mở terminal tại thư mục chứa file `pom.xml`, sau đó chạy:

```powershell
.\run-windows.cmd
```

File này sẽ tự tìm JDK 21+ trên máy và chạy project bằng Maven Wrapper.

Nếu muốn chạy thủ công:

```powershell
.\mvnw.cmd clean javafx:run
```

Nếu PowerShell/CMD báo chưa tìm thấy Java, hãy cài JDK 21 hoặc chọn JDK 21 trong IntelliJ IDEA.

## Cách mở bằng IntelliJ IDEA

1. Mở IntelliJ IDEA.
2. Chọn **File → Open**.
3. Chọn thư mục chứa `pom.xml`.
4. Bấm **Trust Project** nếu IntelliJ hỏi.
5. Vào **File → Project Structure → Project** và chọn SDK là JDK 21 hoặc mới hơn.
6. Mở tab Maven bên phải và bấm **Reload All Maven Projects**.
7. Mở Terminal trong IntelliJ và chạy:

```powershell
.\run-windows.cmd
```

## Tài khoản demo

```text
Email: demo@smartspend.test
Password: demo123
```

Bạn cũng có thể tự đăng ký tài khoản mới trong màn hình Register.

## Cấu hình gửi email

Trong app, mở mục **Email notifications** ở sidebar.

Nếu dùng Gmail, điền như sau:

```text
SMTP Host: smtp.gmail.com
SMTP Port: 465
From Email: email Gmail của bạn
Username: email Gmail của bạn
Password: Gmail App Password
Use SSL/TLS: bật
```

Sau đó bấm **Save** và **Send Test Email**.

Lưu ý: Gmail không cho dùng mật khẩu Gmail thường để gửi SMTP. Bạn cần bật xác minh 2 bước trong tài khoản Google và tạo **App Password**. App Password thường là mã 16 ký tự.

Khi SMTP hoạt động, SmartSpend có thể gửi:

- Mã quên mật khẩu.
- Email chào mừng khi đăng ký.
- Email cảnh báo đăng nhập.
- Báo cáo từ Smart Coach.

Nếu chưa cấu hình SMTP, app vẫn hoạt động bình thường. Riêng chức năng quên mật khẩu sẽ hiện mã reset demo trong popup để có thể kiểm thử offline.

## Smart Coach và Ollama

Ollama là tùy chọn, không bắt buộc.

Nếu muốn Smart Coach trả lời tự nhiên hơn bằng AI local, cài Ollama và tải model:

```powershell
.\setup-ollama-windows.ps1
```

Hoặc chạy thủ công:

```powershell
ollama pull llama3.2
```

Sau khi Ollama chạy được, mở SmartSpend và vào **Smart Coach**. App sẽ tự phát hiện Ollama ở máy local, không cần nhập API key và không cần cấu hình biến môi trường.

Nếu Ollama chưa có hoặc chưa sẵn sàng, Smart Coach tự dùng chế độ offline tích hợp sẵn.

## Dữ liệu ứng dụng

Dữ liệu local nằm trong:

```text
./data/smartspend.mv.db
```

Muốn reset dữ liệu demo:

1. Đóng app.
2. Xóa thư mục `data/`.
3. Chạy lại app.

App sẽ tự tạo lại database, danh mục mặc định và tài khoản demo.

## Thông tin host

```text
Trần Trung Hiếu
trantrunghieu30032006@gmail.com
```

Thông tin này được dùng trong phần Support, chữ ký email và báo cáo xuất từ app.

## Ghi chú bảo mật

Không đưa các thông tin sau lên GitHub:

```text
Gmail App Password
API keys
data/
*.mv.db
*.trace.db
```

Repo nên chỉ chứa source code, file cấu hình mẫu và hướng dẫn chạy.
