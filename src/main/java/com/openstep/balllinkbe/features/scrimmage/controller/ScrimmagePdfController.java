package com.openstep.balllinkbe.features.scrimmage.controller;

import com.openstep.balllinkbe.features.scrimmage.service.ScrimmagePdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scrimmages")
@RequiredArgsConstructor
@Tag(name = "scrimmage-export-controller", description = "자체전(스크림) PDF/엑스포트 API")
public class ScrimmagePdfController {

    private final ScrimmagePdfService scrimmagePdfService;

    @Operation(summary = "자체전 박스스코어 PDF 다운로드", description = "is_scrimmage=true 경기의 박스스코어 PDF를 생성해 스트리밍합니다.")
    @GetMapping(value = "/{gameId}/boxscore/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadScrimmagePdf(@PathVariable Long gameId) {
        byte[] pdf = scrimmagePdfService.renderScrimmagePdf(gameId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"scrimmage-" + gameId + "-boxscore.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
