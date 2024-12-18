package com.example.cupcycle.service;

import com.example.cupcycle.entity.*;

import com.example.cupcycle.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CupService {
    private static final double CARBON_INCREASE = 0.029;
    private static final int REWARD = 100;
    private final CafeRepository cafeRepository;
    private final StudentRepository studentRepository;
    private final CupRepository cupRepository;
    private final ReturnStationRepository returnStationRepository;
    private final ReturnEventRepository returnEventRepository;

    @Transactional
    public ApiResponse<String> borrowCup(int cafeId, int studentId, int cupId)
    {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카페를 찾을 수 없습니다."));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(()->new IllegalArgumentException("해당 학생을 찾을 수 없습니다."));
        Cup cup = cupRepository.findById(cupId)
                .orElseThrow(()->new IllegalArgumentException("해당 컵을 찾을 수 없습니다."));

        if (!cup.getStatus().equals(Cup.CupStatus.AVAILABLE)) {
            return new ApiResponse<>(false, 6002, "해당 컵은 대여 가능한 상태가 아닙니다.");
        }

        // 1. Cafe의 availableCup 감소
        if(cafe.getAvailableCups() <=0) {
            return new ApiResponse<>(false, 6001, "대여 가능한 컵이 없습니다.");
        }
        cafe.decreaseAvailableCups();
        cafeRepository.save(cafe);

        //2. Student의 cupCount 증가 및 carbonReduction 증가
        student.increaseCupCount();
        student.increaseCarbonReduction(CARBON_INCREASE);
        studentRepository.save(student);

        //3.Cup의 상태 변경 및 borrowTime 갱신
        updateCupStatus(cup, Cup.CupStatus.BORROWED);

        // 4
        cup.setStudent(student);
        cupRepository.save(cup);

        return new ApiResponse<>(true, 1000, "대여가 완료되었습니다.");
    }

    @Transactional
    public ApiResponse<String> returnCup(int cupId, int returnStationId) {
        Cup cup = cupRepository.findById(cupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 컵을 찾을 수 없습니다."));

        ReturnStation returnStation = returnStationRepository.findById(returnStationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 반납기를 찾을 수 없습니다."));
        if (!cup.getStatus().equals(Cup.CupStatus.BORROWED)) {
            return new ApiResponse<>(false, 6002, "해당 컵은 반납 가능한 상태가 아닙니다.");
        }

        // 1. Cup의 상태 변경 및 returnTime 갱신
        updateCupStatus(cup, Cup.CupStatus.RETURNED);

        // 2. ReturnStation의 current_cup 증가
        returnStation.increaseCurrentCup();
        returnStationRepository.save(returnStation);

        //3. 반납 이벤트 기록
        ReturnEvent event = new ReturnEvent();
        event.setReturnStationId(returnStationId);
        returnEventRepository.save(event);


        return new ApiResponse<>(true, 1000, "반납이 완료되었습니다.");
    }

    @Transactional
    public ApiResponse<String> updateCupStatusAndReward(int cafeId, int cupId)
    {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카페를 찾을 수 없습니다."));
        Cup cup = cupRepository.findById(cupId)
                .orElseThrow(()->new IllegalArgumentException("해당 컵을 찾을 수 없습니다."));

        if(cup == null)
        {
            return new ApiResponse<>(false, 7001, "컵을 찾을 수 없습니다.");
        }
        if(cup.getStatus() != Cup.CupStatus.RETURNED) {
            return new ApiResponse<>(false, 7002, "컵의 상태가 returned가 아닙니다.");
        }

        //컵의 상태를 available로 변경
        updateCupStatus(cup, Cup.CupStatus.AVAILABLE);

        // 1. Cafe의 availableCup 증가
        if(cafe.getAvailableCups() >= 20) {
            return new ApiResponse<>(false, 6001, "카페의 컵 재고가 꽉 차 있습니다.");
        }

        //카페의 재고 증가
        cafe.increaseAvailableCups();
        cup.setCafe(cafe);
        cupRepository.save(cup);

        //학생의 보상 포인트 증가
        Student student = cup.getStudent();
        student.increaseReward(REWARD);
        studentRepository.save(student);

        // 추가 리워드 지급 조건 확인 및 지급
        int cupCount = student.getCupCount();
        if (cupCount == 3 || cupCount == 5 || cupCount == 10) {
            student.increaseReward(300); // 300포인트 추가
            studentRepository.save(student); // 추가 리워드 저장
        }

        return new ApiResponse<>(true, 1000, "컵 상태와 학생 보상이 성공적으로 업데이트되었습니다.");
    }

    
    public Optional<Cup> findByQrCode(String qrCode) {
        return cupRepository.findCupByQrcode(qrCode);
    }

    private void updateCupStatus(Cup cup, Cup.CupStatus newStatus) {
        cup.setStatus(newStatus);
        if (newStatus == Cup.CupStatus.BORROWED) {
            cup.setBorrowTime(Timestamp.valueOf(LocalDateTime.now()));
        } else if (newStatus == Cup.CupStatus.RETURNED) {
            cup.setReturnTime(Timestamp.valueOf(LocalDateTime.now()));
        }
        cupRepository.save(cup);
    }
}
