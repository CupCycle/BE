package com.example.cupcycle.controller;


import com.example.cupcycle.entity.Cup;
import com.example.cupcycle.service.ApiResponse;
import com.example.cupcycle.service.CupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/cup")
@RequiredArgsConstructor
public class CupController {
    private final CupService cupService;

    @PostMapping("/borrow")
    public ResponseEntity<ApiResponse<String>> borrowCup(@RequestParam int cafeId, @RequestParam int studentId,
                                                         @RequestParam int cupId) {

        ApiResponse<String> response = cupService.borrowCup(cafeId, studentId, cupId);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/return")
    public ResponseEntity<ApiResponse<String>> returnCup(
            @RequestParam int cupId,
            @RequestParam int returnStationId) {

        ApiResponse<String> response = cupService.returnCup(cupId, returnStationId);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/updateCupStatus")
    public ResponseEntity<ApiResponse<String>> updateCupStatusAndReward(@RequestParam int cupId) {
        ApiResponse<String> response = cupService.updateCupStatusAndReward(cupId);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(response);
    }

    @GetMapping("/qrcode")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getCupInfoByQRCode(@RequestParam String qrcode) {
        Optional<Cup> cupOptional = cupService.findByQrCode(qrcode);

        if(cupOptional.isPresent()) {
            Cup cup = cupOptional.get();
            Map<String, Integer> data = new HashMap<>();
            data.put("cafeId", cup.getCafe().getCafeId());
            data.put("cupId", cup.getCupId());

            ApiResponse<Map<String, Integer>> response = new ApiResponse<>(true, 1000, "QR 코드로 컵 정보 조회 성공", data);
            return ResponseEntity.ok(response);
        }
        else {
            ApiResponse<Map<String, Integer>> response = new ApiResponse<>(false, 4004, "해당 QR 코드에 대한 컵 정보를 찾을 수 없습니다.");
            return ResponseEntity.status(404).body(response);
        }
    }
}
