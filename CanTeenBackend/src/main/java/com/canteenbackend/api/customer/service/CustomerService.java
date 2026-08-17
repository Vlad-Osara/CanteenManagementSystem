package com.canteenbackend.api.customer.service;

import com.canteenbackend.api.transaction.model.Transaction;
import com.canteenbackend.api.transaction.model.TransactionType;
import com.canteenbackend.api.transaction.repository.TransactionRepository;
import com.canteenbackend.api.user.model.User;
import com.canteenbackend.utils.vnpay.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomerService {

    @Value("${vnpay.url}")
    private String vnp_PayUrl;
    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;
    @Value("${vnpay.hash-secret}")
    private String vnp_HashSecret;
    @Value("${vnpay.return-url}")
    private String vnp_ReturnUrl;

    private final TransactionRepository transactionRepository;

    @Transactional
    public String createVNPayPaymentUrl(User customer, BigDecimal amount, HttpServletRequest request) throws Exception {

        // 1. Tạo và Lưu giao dịch ở trạng thái chờ trước để sinh UUID tự động từ BaseModel
        Transaction transaction = Transaction.builder()
                .customer(customer)
                .amount(amount)
                .type(TransactionType.DEPOSIT)
                .description("Nạp tiền qua cổng VNPay (Đang xử lý)")
                .build();

        transaction = transactionRepository.save(transaction);

        // 2. Cấu hình các tham số gửi sang VNPay
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = transaction.getId().toString(); // Sử dụng trực tiếp UUID làm mã đơn hàng
        String vnp_OrderInfo = "Nap tien cho user " + customer.getId();
        String vnp_OrderType = "other";

        // VNPay tính số tiền nhân 100 (Ví dụ: 100,000đ thì gửi sang là 10000000)
        long vnp_Amount = amount.multiply(new BigDecimal(100)).longValue();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15); // Link thanh toán hết hạn sau 15 phút
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // 3. Sắp xếp tham số theo thứ tự alpha-bêta và build chuỗi hash
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                // Build Hash Data
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // Build Query String
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // 4. Ký mã bảo mật mã hóa HMAC-SHA512
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayUtil.hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        // Trả về link thanh toán hiển thị QR cho client
        return vnp_PayUrl + "?" + queryUrl;
    }
}
