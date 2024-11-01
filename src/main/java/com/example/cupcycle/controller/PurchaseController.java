package com.example.cupcycle.controller;

import com.example.cupcycle.dto.PurchaseHistoryDto;
import com.example.cupcycle.entity.PurchaseHistory;
import com.example.cupcycle.service.ApiResponse;
import com.example.cupcycle.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/purchase")
public class PurchaseController {
    private final PurchaseService purchaseService;

    /*
    * 상품 구매 신청
     */
    @PostMapping("/requestPurchase")
    public ResponseEntity<ApiResponse<PurchaseHistory>> purchaseProduct(
            @RequestParam int studentId,
            @RequestParam int productId) {
        try {
            PurchaseHistory purchaseHistory = purchaseService.purchaseProduct(studentId, productId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>(true,200, "물품 교환 신청이 완료되었습니다.", purchaseHistory));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false,6001, e.getMessage()));
        }
    }

    /*
     * 상품 구매 수락
     */
    @PostMapping("/acceptPurchase")
    public ResponseEntity<ApiResponse<Void>> acceptPurchase(
            @RequestParam int purchaseHistoryId) {
        try {
            purchaseService.acceptPurchase(purchaseHistoryId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>(true,200, "구매가 성공적으로 수락되었습니다.", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false,6001, e.getMessage()));
        }
    }

    /*
     * 상품 구매 신청 목록 조회
     */
    @GetMapping("/getPurchaseHistory")
    public ResponseEntity<ApiResponse<List<PurchaseHistoryDto>>> getPurchaseHistory() {
        List<PurchaseHistoryDto> purchaseHistoryList = purchaseService.getPurchaseHistory();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(true,200, "구매 신청 목록 조회에 성공하였습니다.", purchaseHistoryList));
    }
}
