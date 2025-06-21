1. Cài đặt posgresql (trên docker)
  
3. Tạo table logs giả sử là: 

CREATE TABLE storage_event (
    id              VARCHAR(64) PRIMARY KEY,
    target_type     VARCHAR(64) NOT NULL,
    target_id       VARCHAR(64) NOT NULL,
    subject_type    VARCHAR(64) NOT NULL,
    subject_id      VARCHAR(64) NOT NULL,
    action          VARCHAR(64) NOT NULL,
    data            JSONB,
    correlation_id  VARCHAR(64),
    created_at      BIGINT  NOT NULL
);

2. Viết chương trình java đọc logs từ database và gửi vào open search.
   
4. Xây dựng chương trình java để tìm kiếm nội dung log. Kết quả trả về json.
   
6. Xây dựng dashboard để show kết quả logs. Kết quả trả về dạng json,

Gợi ý: 
- Tự tạo sample log, gợi ý sử dụng AI để có dữ liệu giống thực thế nhất. GIả sử UserLogedIn UserLogedOut …
- Ý tưởng xây dựng dashboard thảo AI quan trọng là giúp cho quản trị viên theo dõi báo cáo tổng quát dễ dàng
- Các trường dữ liệu trong DB nếu chưa hiểu vai trò mục đích hãy hỏi AI
- Lưu ý không cần phải dùng Spring Boot. Dùng java client tương tác trực tiếp với open search thì sẽ nhanh hơn. 
- Xử lý logic trả về json, không cần xây dựng api. (Tách biệt business & framework)
- Khái niệm nào chưa hiểu không hiểu hỏi AI
- Điểm nào nghi vấn hỏi lại Leader
