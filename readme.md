# Chương trình chat giữa 2 người hoặc nhóm với nhau

- Communication dựa trên Netty
- Database sử dụng RocksDB
- Truyền nhận message qua protocol buffer

# Yêu cầu

- Chạy command sau: ```protoc -I=./src/main/proto --java_out=./src/main/java ./src/main/proto/letchat.proto ```
- Cài đặt RocksDB
- Cài đặt protobuf (libprotoc 3.6.1)
