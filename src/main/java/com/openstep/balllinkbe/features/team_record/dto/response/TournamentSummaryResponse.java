package com.openstep.balllinkbe.features.team_record.dto.response;

import com.openstep.balllinkbe.features.team_record.repository.projection.TournamentAggProjection;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class TournamentSummaryResponse {
    private List<Item> items;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
    public static class Item {
        private Long tournamentId;
        private String tournamentName;
        private String season;
        private String status;
        private LocalDate startDate;
        private LocalDate endDate;
        private int games, wins, losses, pts;
    }

    public static TournamentSummaryResponse from(List<TournamentAggProjection> rows) {
        List<Item> items = rows.stream().map(r -> Item.builder()
                .tournamentId(r.getTournamentId())
                .tournamentName(r.getTournamentName())
                .season(r.getSeason())
                .status(r.getStatus())
                .startDate(r.getStartDate() != null ? r.getStartDate().toLocalDate() : null)
                .endDate(r.getEndDate() != null ? r.getEndDate().toLocalDate() : null)
                .games(nz(r.getGames())).wins(nz(r.getWins())).losses(nz(r.getLosses())).pts(nz(r.getPts()))
                .build()).toList();
        return new TournamentSummaryResponse(items);
    }
    private static int nz(Integer v) { return v == null ? 0 : v; }
}
