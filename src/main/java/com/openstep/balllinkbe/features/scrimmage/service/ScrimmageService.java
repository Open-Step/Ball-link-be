package com.openstep.balllinkbe.features.scrimmage.service;

import com.openstep.balllinkbe.domain.file.FileMeta;
import com.openstep.balllinkbe.domain.file.FileStorageService;
import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GameEvent;
import com.openstep.balllinkbe.domain.game.GameLineupPlayer;
import com.openstep.balllinkbe.domain.team.Player;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.team.TeamMember;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.domain.venue.Venue;
import com.openstep.balllinkbe.features.scrimmage.dto.request.AddGuestRequest;
import com.openstep.balllinkbe.features.scrimmage.dto.request.CreateScrimmageRequest;
import com.openstep.balllinkbe.features.scrimmage.dto.request.EndScrimmageRequest;
import com.openstep.balllinkbe.features.scrimmage.dto.request.SaveLineupDto;
import com.openstep.balllinkbe.features.scrimmage.dto.response.*;
import com.openstep.balllinkbe.features.scrimmage.repository.FileMetaRepository;
import com.openstep.balllinkbe.features.scrimmage.repository.GameEventRepository;
import com.openstep.balllinkbe.features.scrimmage.repository.GameLineupPlayerRepository;
import com.openstep.balllinkbe.features.scrimmage.repository.GameRepository;
import com.openstep.balllinkbe.features.team_manage.repository.PlayerRepository;
import com.openstep.balllinkbe.features.team_manage.repository.TeamMemberRepository;
import com.openstep.balllinkbe.features.team_manage.repository.TeamRepository;
import com.openstep.balllinkbe.features.venue.repository.VenueRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScrimmageService {

    private final GameRepository gameRepository;
    private final VenueRepository venueRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final GameLineupPlayerRepository gameLineupPlayerRepository;
    private final PlayerRepository playerRepository;
    private final FileStorageService fileStorageService;
    private final FileMetaRepository fileMetaRepository;
    private final ScoreExportService scoreExportService;
    private final GameEventRepository gameEventRepository;

    /**
     * 자체전 생성
     * @param teamId 홉 팀 식별자
     * @param currentUser 팀장/매니저
     * @param createScrimmageDto scheduledAt(예정시간), venueId(장소), opponentName(상대팀), note(진행 기록)
     *
     * [Question]
     * Away 팀의 경우는 opponentName만 적용하는지?
     * -> 그렇다면, 자체전 조회시에 반환 데이터에 있는 away 리스트는 뭘로 채워지는지 궁금합니다.
     */
    @Transactional
    public CreateScrimmageResponse createScrimmage(Long teamId, User currentUser, CreateScrimmageRequest createScrimmageDto) {

        // 팀장/매니저 권한 검증
        TeamMember teamMember = teamMemberRepository.findByTeamIdAndUserId(teamId, currentUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if (teamMember.getRole().equals(TeamMember.Role.PLAYER)){
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        // 홈 팀 조회
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        // 장소(venue) 조회
        Venue venue = venueRepository.findById(createScrimmageDto.venueId())
                .orElseThrow(() -> new CustomException(ErrorCode.VENUE_NOT_FOUND));

        Game game = Game.builder()
                .isScrimmage(true)
                .homeTeam(team)
                .opponentName(createScrimmageDto.opponentName())
                .venue(venue)
                .scheduledAt(createScrimmageDto.scheduledAt())
                .createdAt(LocalDateTime.now())
                .build();

        Game save = gameRepository.save(game);

        return new CreateScrimmageResponse(save.getId());
    }

    /**
     * 자체전 목록 조회
     * @param teamId 홈 팀 식별자
     * @param state 자체전 상태
     * @param startDate 자체전 조회 시작일
     * @param endDate 자체전 조회 종료일
     * @param page 현재 페이지
     * @param size 받아올 크기
     */
    public ScrimmageListResponse getScrimmageList(Long teamId, String state,
                                    LocalDate startDate, LocalDate endDate, int page, int size) {

        Game.State gameState;
        try{
            // 잘못된 입력값 검증
            gameState = Game.State.valueOf(state);
        } catch (IllegalArgumentException e){
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        // 페이지 객체
        Pageable pageable = PageRequest.of(page, size);

        // Home Team
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        // 시작일
        LocalDateTime start = startDate.atStartOfDay();
        // 종료일 (종료일은 23시 59분 59초까지 설정)
        LocalDateTime end = endDate.atTime(23, 59, 59);

        // 페이징 반환
        Page<Game> scrimmages = gameRepository.findScrimmagesByTeamAndDateRange(team.getId(), gameState, start, end, pageable);

        // 자체전 리스트
        List<GameDto> gameList = scrimmages.map(GameDto::from)
                .stream().toList();

        // 반환 DTO 생성
        ScrimmageListResponse responseData = ScrimmageListResponse.builder()
                .page(page)
                .size(scrimmages.getSize())
                .items(gameList)
                .total(scrimmages.getTotalPages())
                .build();

        return responseData;
    }

    /**
     * 자체전 상세 조회
     * @param gameId 게임(자체전) 식별자
     */
    public GameDto getScrimmageDetail(Long gameId){

        // 해당 자체전 조회
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        GameDto from = GameDto.from(game);

        return from;
    }

    /**
     * 자체전 라인업 저장
     */
    public SuccessResponse saveLineUp(Long gameId, SaveLineupDto saveLineUpDto) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        Team homeTeam = game.getHomeTeam();
        // 자체전인 경우에는 AwayTeam 객체가 없을것 같아서 주석처리
//        Team awayTeam = game.getAwayTeam();


        // HomePlayer
        List<Player> homePlayer = saveLineUpDto.home().stream()
                .map(home -> Player.builder().team(homeTeam).name(home).build())
                .toList();

        // AwayPlayer
        List<Player> awayPlayer = saveLineUpDto.away().stream()
                .map(away -> Player.builder().name(away).build())
                .toList();

        // HomeGameLineup
        List<GameLineupPlayer> homeGameLineup = homePlayer.stream()
                .map(player ->
                        GameLineupPlayer.builder()
                                .game(game)
                                .player(player)
                                .team(homeTeam)
                                .teamSide(GameLineupPlayer.Side.HOME)
                                .build()
                ).toList();

        // AwayGameLineup
        List<GameLineupPlayer> awayGameLineup = awayPlayer.stream()
                .map(player ->
                        GameLineupPlayer.builder()
                                .game(game)
                                .player(player)
                                .teamSide(GameLineupPlayer.Side.AWAY)
                                .build()
                ).toList();

        playerRepository.saveAll(homePlayer);
        playerRepository.saveAll(awayPlayer);
        gameLineupPlayerRepository.saveAll(homeGameLineup);
        gameLineupPlayerRepository.saveAll(awayGameLineup);

        return new SuccessResponse(true);
    }

    /**
     * 자체전 게스트 추가
     * @param gameId 게임(자체전) 식별자
     * @param addGuestRequest 게스트 이름
     */
    @Transactional
    public AddGuestResponse addGuest(Long gameId, AddGuestRequest addGuestRequest, User currentUser) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        GameLineupPlayer guestPlayer = GameLineupPlayer.builder()
                .game(game)
                .guestName(addGuestRequest.name())
                .build();

        GameLineupPlayer save = gameLineupPlayerRepository.save(guestPlayer);

        return new AddGuestResponse(save.getId(), save.getGuestName());
    }

    /**
     * 자체전 종료 및 기록지 export 처리 메서드
     */
    @Transactional
    public EndScrimmageResponse endScrimmageAndExport(Long gameId, User currentUser, EndScrimmageRequest request) {

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        // 팀장/매니저 권한 검증
        TeamMember teamMember = teamMemberRepository.findByTeamIdAndUserId(game.getHomeTeam().getId(), currentUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if (teamMember.getRole().equals(TeamMember.Role.PLAYER)){
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        // 경기 이벤트 조회
        List<GameEvent> events = gameEventRepository.findByGameIdOrderByTsAsc(gameId);

        // PDF 기록지 생성
        byte[] pdfContent = scoreExportService.generatePdfScoreSheet(game, events, request.exportFormat());

        // 파일 이름 및 경로 생성 (FileStorageService에서 처리)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String originalFileName = String.format("%d_%s.pdf", gameId, timestamp);

        // 파일 저장 및 상대경로 반환
        String relativePath = fileStorageService.storeFile(
                gameId,
                FileMeta.OwnerType.SCRIMMAGE,
                FileMeta.FileCategory.RESULT,
                originalFileName,
                pdfContent
        );

        // FileMeta 저장
        FileMeta fileMeta = FileMeta.builder()
                .fileId(relativePath) // fileId를 relativePath로 사용하거나 별도 생성
                .originalName(originalFileName)
                .contentType("application/pdf")
                .sizeBytes((long) pdfContent.length)
                .relativePath(relativePath)
                .ownerType(FileMeta.OwnerType.SCRIMMAGE)
                .ownerId(gameId)
                .category(FileMeta.FileCategory.RESULT)
                .createdAt(LocalDateTime.now())
                .build();
        // 명시적 저장
        fileMetaRepository.save(fileMeta);

        // 경기 상태 업데이트 (종료 및 소프트 삭제)
        game.setState(Game.State.FINISHED);
        game.setFinishedAt(LocalDateTime.now());
        // 명시적 저장
        gameRepository.save(game);

        // 응답 생성
        String cdnUrl = fileStorageService.toCdnUrl(relativePath);
        String fileName = relativePath.substring(relativePath.lastIndexOf('/') + 1);

        return new EndScrimmageResponse(cdnUrl, fileName);
    }

    /**
     * 자체전 삭제 (관리자용)
     * @param gameId 게임(자체전) 식별자
     * @param currentUser 팀장/매니저
     */
    @Transactional
    public void deleteScrimmage(Long gameId, User currentUser) {

        // 자체전 조회
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        // 홈 teamId
        Long teamId = game.getHomeTeam().getId();

        // 팀장/매니저 권한 검증
        TeamMember teamMember = teamMemberRepository.findByTeamIdAndUserId(teamId, currentUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if (teamMember.getRole().equals(TeamMember.Role.PLAYER)){
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        // 자체전 삭제
        game.setDeletedAt(LocalDateTime.now());
        // 명시적 저장
        gameRepository.save(game);
    }

    /**
     * 자체전 복원
     * @param gameId 게임(자체전) 식별자
     * @param currentUser 팀장/매니저
     */
    @Transactional
    public SuccessResponse restoreScrimmage(Long gameId, User currentUser) {

        // 자체전 조회
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        // 홈 teamId
        Long teamId = game.getHomeTeam().getId();

        // 팀장/매니저 권한 검증
        TeamMember teamMember = teamMemberRepository.findByTeamIdAndUserId(teamId, currentUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if (teamMember.getRole().equals(TeamMember.Role.PLAYER)){
            throw new CustomException(ErrorCode.UNAUTHORIZED_MEMBER);
        }

        // 자체전 복원
        game.setDeletedAt(null);
        // 명시적 저장
        gameRepository.save(game);

        return new SuccessResponse(true);
    }
}
