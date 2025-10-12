package com.openstep.balllinkbe.features.scrimmage.controller;

import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.scrimmage.dto.request.AddGuestRequest;
import com.openstep.balllinkbe.features.scrimmage.dto.request.CreateScrimmageRequest;
import com.openstep.balllinkbe.features.scrimmage.dto.request.EndScrimmageRequest;
import com.openstep.balllinkbe.features.scrimmage.dto.request.SaveLineupDto;
import com.openstep.balllinkbe.features.scrimmage.dto.response.*;
import com.openstep.balllinkbe.features.scrimmage.service.ScrimmageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "scrimmage-controller", description = "스크림(자체전) 관련 API")
public class ScrimmageController {

    private final ScrimmageService scrimmageService;

    @PostMapping("/teams/{teamId}/scrimmages")
    @Operation(summary = "자체전 생성", description = "자체전을 생성합니다. 팀장/매니저만 생성 가능합니다.")
    public ResponseEntity<CreateScrimmageResponse> createScrimmage(@PathVariable long teamId,
                                                                   @AuthenticationPrincipal User currentUser,
                                                                   @Valid @RequestBody CreateScrimmageRequest scrimmageRequest) {

        CreateScrimmageResponse scrimmage = scrimmageService.createScrimmage(teamId, currentUser, scrimmageRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(scrimmage);
    }

    @GetMapping("/teams/{teamId}/scrimmages")
    @Operation(summary = "자체전 목록 조회", description = "자체전 목록을 조회합니다.")
    public ResponseEntity<ScrimmageListResponse> getScrimmageList(@PathVariable long teamId,
                                              @AuthenticationPrincipal User currentUser,
                                              @RequestParam String state,
                                              @RequestParam LocalDate from,
                                              @RequestParam LocalDate to,
                                              @RequestParam(required = false, defaultValue = "0") int page,
                                              @RequestParam(required = false, defaultValue = "10") int size) {

        ScrimmageListResponse scrimmageList = scrimmageService.getScrimmageList(teamId, state, from, to, page, size);
        return ResponseEntity.ok(scrimmageList);
    }

    @GetMapping("/scrimmages/{id}")
    @Operation(summary = "자체전 상세 조회", description = "자체전을 상세 조회합니다.")
    public ResponseEntity<GameDto> getScrimmage(@PathVariable long id,
                                                @AuthenticationPrincipal User currentUser) {

        GameDto scrimmageDetail = scrimmageService.getScrimmageDetail(id);
        return ResponseEntity.ok(scrimmageDetail);
    }

    @PutMapping("/scrimmages/{id}/guests")
    @Operation(summary = "자체전 라인업 저장", description = "자체전 라인업을 저장합니다.")
    public ResponseEntity<SuccessResponse> saveScrimmageLineup(@PathVariable long id,
                                                 @AuthenticationPrincipal User currentUser,
                                                 @RequestBody SaveLineupDto saveLineupDto) {

        SuccessResponse successResponse = scrimmageService.saveLineUp(id, saveLineupDto);
        return ResponseEntity.ok(successResponse);
    }

    @PostMapping("/scrimmages/{id}/guests")
    @Operation(summary = "자체전 게스트 추가", description = "자체전 게스트를 추가합니다.")
    public ResponseEntity<AddGuestResponse> addScrimmageGuest(@PathVariable long id,
                                               @RequestBody @Valid AddGuestRequest addGuestRequest,
                                               @AuthenticationPrincipal User currentUser){

        AddGuestResponse addGuestResponse = scrimmageService.addGuest(id, addGuestRequest, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(addGuestResponse);
    }

    // 진행중
    @PostMapping("/scrimmages/{id}:end")
    @Operation(summary = "자체전 종료/내보내기", description = "자체전을 종료/내보내기 합니다.")
    public ResponseEntity<EndScrimmageResponse> endScrimmage(@PathVariable long id,
                                          @AuthenticationPrincipal User currentUser,
                                          @RequestBody EndScrimmageRequest endScrimmageRequest) {

        EndScrimmageResponse endScrimmageResponse = scrimmageService.endScrimmageAndExport(id, currentUser, endScrimmageRequest);
        return ResponseEntity.ok(endScrimmageResponse);
    }

    @DeleteMapping("/scrimmages/{id}")
    @Operation(summary = "자체전 삭제(관리자용)", description = "자체전을 삭제처리합니다. deletedAt으로 삭제 처리 (관리자용)")
    public ResponseEntity<Void> deleteScrimmage(@PathVariable long id,
                                             @AuthenticationPrincipal User currentUser) {

        scrimmageService.deleteScrimmage(id, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/scrimmages/{id}:restore")
    @Operation(summary = "자체전 복원", description = "자체전을 복원합니다. deletedAt을 NULL 처리")
    public ResponseEntity<SuccessResponse> restoreScrimmage(@PathVariable long id,
                                                            @AuthenticationPrincipal User currentUser) {

        SuccessResponse successResponse = scrimmageService.restoreScrimmage(id, currentUser);
        return ResponseEntity.ok(successResponse);
    }
}
