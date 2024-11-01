package com.example.cupcycle.service;

import com.example.cupcycle.dto.PurchaseHistoryDto;
import com.example.cupcycle.entity.Product;
import com.example.cupcycle.entity.PurchaseHistory;
import com.example.cupcycle.entity.Student;
import com.example.cupcycle.repository.ProductRepository;
import com.example.cupcycle.repository.PurchaseHistoryRepository;
import com.example.cupcycle.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final StudentRepository studentRepository;
    private final ProductRepository productRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;

    /*
     * 상품 구매 신청
     */
    @Transactional
    public PurchaseHistory purchaseProduct(int studentId, int productId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("학생을 찾을 수 없습니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        // 상품 가격
        int productPrice = product.getPrice();

        if (student.getReward() < productPrice) {
            throw new RuntimeException("리워드 포인트가 부족합니다.");
        }

        // 학생 정보 업데이트
        student.setReward(student.getReward() - productPrice);
        studentRepository.save(student);

        // 구매 이력 생성
        PurchaseHistory purchaseHistory = PurchaseHistory.builder()
                .student(student)
                .product(product)
                .purchaseDate(new Timestamp(System.currentTimeMillis()))
                .build();

        return purchaseHistoryRepository.save(purchaseHistory);
    }


    /*
    * 상품 구매 수락
    */
    @Transactional
    public void acceptPurchase(int purchaseId) {
        PurchaseHistory purchaseHistory = purchaseHistoryRepository.findById(purchaseId)
                .orElseThrow(() -> new RuntimeException("구매 이력을 찾을 수 없습니다."));

        // 구매 수락 처리
        if (purchaseHistory.isAccepted()) {
            throw new RuntimeException("이미 수락된 구매입니다.");
        }

        purchaseHistory.setAccepted(true);
        purchaseHistoryRepository.save(purchaseHistory);
    }

    /*
     * 상품 신청 목록 조회
     */
//    public List<PurchaseHistory> getPurchaseHistory() {
//        return purchaseHistoryRepository.findAll();
//    }
    public List<PurchaseHistoryDto> getPurchaseHistory() {
        List<PurchaseHistory> purchaseHistoryList = purchaseHistoryRepository.findAll();
        return purchaseHistoryList.stream().map(purchaseHistory -> {
            PurchaseHistoryDto dto = new PurchaseHistoryDto();
            dto.setPurchaseId(purchaseHistory.getPurchaseId());
            dto.setStudentId(purchaseHistory.getStudent().getStudentId());
            dto.setStudentName(purchaseHistory.getStudent().getName());
            dto.setProductId(purchaseHistory.getProduct().getProductId());
            dto.setProductName(purchaseHistory.getProduct().getName());
            return dto;
        }).collect(Collectors.toList());
    }

}
