package com.openstep.balllinkbe.features.user.controller;

import com.openstep.balllinkbe.features.user.dto.response.MyCareerResponse;
import com.openstep.balllinkbe.features.user.service.MyCareerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.openstep.balllinkbe.domain.user.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@Tag(name = "my-career-controller", description = "로그인한 사용자의 개인 경기기록/통산기록 API")
public class MyCareerController {

    private final MyCareerService myCareerService;

    /** 마이커리어 전체 조회 (최근 경기 + 연도별 통산) */
    @GetMapping("/career")
    @Operation(
            summary = "마이커리어 조회",
            description = "로그인한 사용자의 최근 경기 5개와 연도별 통산 성적을 함께 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MyCareerResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "recentGames": [
                                                {
                                                  "gameId": 17,
                                                  "opponent": "BMW",
                                                  "date": "2024-10-07T00:00:00",
                                                  "pts": 24,
                                                  "reb": 8,
                                                  "ast": 5,
                                                  "stl": 3,
                                                  "blk": 2
                                                },
                                                {
                                                  "gameId": 16,
                                                  "opponent": "KT",
                                                  "date": "2024-10-01T00:00:00",
                                                  "pts": 27,
                                                  "reb": 10,
                                                  "ast": 6,
                                                  "stl": 1,
                                                  "blk": 2
                                                }
                                              ],
                                              "seasonStats": [
                                                {
                                                  "season": "2025",
                                                  "games": 15,
                                                  "pts": 420,
                                                  "reb": 180,
                                                  "ast": 85,
                                                  "stl": 45,
                                                  "blk": 32,
                                                  "fg2": 140,
                                                  "fg3": 35,
                                                  "ft": 105
                                                },
                                                {
                                                  "season": "2024",
                                                  "games": 28,
                                                  "pts": 756,
                                                  "reb": 322,
                                                  "ast": 162,
                                                  "stl": 78,
                                                  "blk": 58,
                                                  "fg2": 248,
                                                  "fg3": 68,
                                                  "ft": 192
                                                }
                                              ]
                                            }
                                            """)
                            )
                    )
            }
    )
    public ResponseEntity<MyCareerResponse> getMyCareer(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(myCareerService.getMyCareer(currentUser.getId()));
    }
}
