package com.openstep.balllinkbe.features.tournament.service;

import com.openstep.balllinkbe.domain.tournament.Tournament;
import com.openstep.balllinkbe.features.tournament.dto.request.CreateTournamentRequest;
import com.openstep.balllinkbe.features.tournament.dto.request.UpdateTournamentRequest;
import com.openstep.balllinkbe.features.tournament.dto.response.TournamentResponse;
import com.openstep.balllinkbe.features.tournament.repository.TournamentRepository;
import com.openstep.balllinkbe.domain.user.User;
import com.openstep.balllinkbe.features.tournament.repository.TournamentTeamRepository;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentTeamRepository tournamentTeamRepository;

    /** 대회 생성 */
    public TournamentResponse createTournament(CreateTournamentRequest req, User admin) {
        // 이름 중복 검증
        if (req.getName() != null &&
                tournamentRepository.findAll().stream().anyMatch(t -> t.getName().equals(req.getName()))) {
            throw new CustomException(ErrorCode.DUPLICATE_TOURNAMENT_NAME);
        }

        // 날짜 검증
        LocalDate startDate = req.getStartDate() != null ? LocalDate.parse(req.getStartDate()) : null;
        LocalDate endDate = req.getEndDate() != null ? LocalDate.parse(req.getEndDate()) : null;
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.INVALID_TOURNAMENT_DATE);
        }

        Tournament t = Tournament.builder()
                .name(req.getName())
                .location(req.getLocation())
                .season(req.getSeason())
                .startDate(startDate)
                .endDate(endDate)
                .status(Tournament.Status.SCHEDULED)
                .build();

        tournamentRepository.save(t);
        return new TournamentResponse(t);
    }

    /** 대회 수정 */
    public TournamentResponse updateTournament(Long id, UpdateTournamentRequest req, User admin) {
        Tournament t = tournamentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.TOURNAMENT_NOT_FOUND));

        // 진행 중/종료된 대회 수정 불가
        if (t.getStatus() == Tournament.Status.ONGOING || t.getStatus() == Tournament.Status.FINISHED) {
            throw new CustomException(ErrorCode.TOURNAMENT_IN_PROGRESS);
        }

        // 날짜 검증
        if (req.getStartDate() != null && req.getEndDate() != null) {
            LocalDate start = LocalDate.parse(req.getStartDate());
            LocalDate end = LocalDate.parse(req.getEndDate());
            if (start.isAfter(end)) {
                throw new CustomException(ErrorCode.INVALID_TOURNAMENT_DATE);
            }
            t.setStartDate(start);
            t.setEndDate(end);
        } else {
            if (req.getStartDate() != null) t.setStartDate(LocalDate.parse(req.getStartDate()));
            if (req.getEndDate() != null) t.setEndDate(LocalDate.parse(req.getEndDate()));
        }

        if (req.getName() != null) t.setName(req.getName());
        if (req.getLocation() != null) t.setLocation(req.getLocation());
        if (req.getSeason() != null) t.setSeason(req.getSeason());
        if (req.getStatus() != null) t.setStatus(Tournament.Status.valueOf(req.getStatus()));

        return new TournamentResponse(t);
    }

    /** 대회 삭제 */
    public void deleteTournament(Long id) {
        Tournament t = tournamentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.TOURNAMENT_NOT_FOUND));

        if (t.getStatus() == Tournament.Status.ONGOING) {
            throw new CustomException(ErrorCode.TOURNAMENT_IN_PROGRESS);
        }
        if (t.getStatus() == Tournament.Status.FINISHED) {
            throw new CustomException(ErrorCode.TOURNAMENT_ALREADY_FINISHED);
        }

        tournamentRepository.delete(t);
    }

    /** 전체 or 상태별 조회 */
    @Transactional
    public List<TournamentResponse> getAllTournaments() {
        List<Tournament> tournaments = tournamentRepository.findAll();

        return tournaments.stream()
                .map(t -> {
                    int count = tournamentTeamRepository.countByTournamentId(t.getId());
                    return new TournamentResponse(t, count);
                })
                .toList();
    }



    /** 상세 조회 */
    @Transactional
    public TournamentResponse getTournament(Long id) {
        Tournament t = tournamentRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.TOURNAMENT_NOT_FOUND));
        return new TournamentResponse(t);
    }
}
