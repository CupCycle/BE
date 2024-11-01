package com.example.cupcycle.dto;

import lombok.Data;

@Data
public class PurchaseHistoryDto {
    private int purchaseId;
    private int studentId;
    private String studentName;
    private int productId;
    private String productName;
}
