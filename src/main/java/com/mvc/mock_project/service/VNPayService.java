package com.mvc.mock_project.service;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

public interface VNPayService {
    String createOrder(BigDecimal courtAmount, String orderInfo, String returnUrl, HttpServletRequest request);
    int orderReturn(HttpServletRequest request);
}
