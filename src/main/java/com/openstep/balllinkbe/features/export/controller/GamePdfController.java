package com.openstep.balllinkbe.features.export.controller;

import com.openstep.balllinkbe.features.export.service.GamePdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
@Tag(name = "export-controller", description = "PDF/엑스포트 API")
public class GamePdfController {

    private final GamePdfService gamePdfService;

    // ※ produces를 지정하지 않음 → 예외 시 GlobalExceptionHandler가 JSON으로 응답(406 방지)
    @Operation(summary = "경기 박스스코어 PDF 다운로드", description = "경기 박스스코어를 온디맨드로 생성해 스트리밍합니다.")
    @GetMapping(value = "/games/{gameId}/boxscore/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadBoxscorePdf(@PathVariable Long gameId) {
        byte[] pdf = gamePdfService.renderBoxscorePdf(gameId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"game-" + gameId + "-boxscore.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}
