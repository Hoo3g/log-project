# 1. Cài đặt posgresql (trên docker)

# 2. Tạo table logs giả sử là: 
- Cấu trúc bảng như sau:

```
CREATE TABLE storage_event (
    id VARCHAR(64) PRIMARY KEY,
    target_type TEXT NOT NULL,
    target_id VARCHAR(64) NOT NULL,
    subject_type TEXT NOT NULL,
    subject_id VARCHAR(64) NOT NULL,
    action TEXT NOT NULL,
    data JSONB,
    correlation_id VARCHAR(64),
    created_at BIGINT NOT NULL DEFAULT 0
);
```

# ✅ 2. Viết chương trình Java đọc log và gửi vào OpenSearch

Viết chương trình Java thực hiện các bước sau:

- Kết nối tới PostgreSQL.
- Truy vấn bảng `storage_event` để lấy log mới.
- Gửi log tới OpenSearch sử dụng OpenSearch Java Client.

⚠️ Ghi chú: Không bắt buộc dùng Spring Boot. Java thuần + OpenSearch client sẽ nhẹ và nhanh hơn.
   
# ✅ 3. Xây dựng chương trình java để tìm kiếm nội dung log. Kết quả trả về json.
   
# ✅ 4. Xây dựng dashboard để show kết quả logs. Kết quả trả về dạng json,

```
Gợi ý: 
- Tự tạo sample log, gợi ý sử dụng AI để có dữ liệu giống thực thế nhất. GIả sử UserLogedIn UserLogedOut …
- Ý tưởng xây dựng dashboard thảo AI quan trọng là giúp cho quản trị viên theo dõi báo cáo tổng quát dễ dàng
- Các trường dữ liệu trong DB nếu chưa hiểu vai trò mục đích hãy hỏi AI
- Lưu ý không cần phải dùng Spring Boot. Dùng java client tương tác trực tiếp với open search thì sẽ nhanh hơn. 
- Xử lý logic trả về json, không cần xây dựng api. (Tách biệt business & framework)
- Khái niệm nào chưa hiểu không hiểu hỏi AI
- Điểm nào nghi vấn hỏi lại Leader
```
