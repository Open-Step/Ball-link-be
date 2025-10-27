package com.openstep.balllinkbe.features.scrimmage.service;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.scrimmage.dto.request.CreateScrimmageRequest;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import com.openstep.balllinkbe.features.game.repository.GameRepository;
import com.openstep.balllinkbe.features.team_manage.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ScrimmageService {

    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;

    /** 자체전 생성 */
    @Transactional
    public Long createScrimmage(CreateScrimmageRequest req, User currentUser) {
        Team home = teamRepository.findById(req.getHomeTeamId())
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
        Team away = teamRepository.findById(req.getAwayTeamId())
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        Game game = Game.builder()
                .homeTeam(home)
                .awayTeam(away)
                .isScrimmage(true)
                .state(Game.State.SCHEDULED)  // ✅ 여기!
                .createdAt(LocalDateTime.now())
                .build();

        gameRepository.save(game);
        return game.getId();
    }

    @Transactional
    public void startScrimmage(Long gameId, User currentUser) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        if (!game.isScrimmage()) throw new CustomException(ErrorCode.INVALID_GAME_TYPE);

        game.setState(Game.State.ONGOING);  // ✅ 수정
        game.setStartedAt(LocalDateTime.now());
    }

    @Transactional
    public void endScrimmage(Long gameId, User currentUser) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        if (!game.isScrimmage()) throw new CustomException(ErrorCode.INVALID_GAME_TYPE);

        game.setState(Game.State.FINISHED);  // ✅ 수정
        game.setFinishedAt(LocalDateTime.now());
    }
}
