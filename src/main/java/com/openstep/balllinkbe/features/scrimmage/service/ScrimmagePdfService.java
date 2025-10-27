package com.openstep.balllinkbe.features.scrimmage.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openstep.balllinkbe.features.team_record.dto.response.GameBoxscoreResponse;
import com.openstep.balllinkbe.features.team_record.repository.TeamRecordRepository;
import com.openstep.balllinkbe.features.team_record.repository.projection.PlayerPeriodScoreProjection;
import com.openstep.balllinkbe.features.team_record.repository.projection.TeamPeriodScoreProjection;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ScrimmagePdfService {

    private final TeamRecordRepository teamRecordRepository;

    public byte[] renderScrimmagePdf(Long gameId) {
        var head = teamRecordRepository.getGameHeader(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        // ✅ head에 isScrimmage 없더라도 그냥 통과 (다른 파일 안건드림)
        // -> 자체전 여부 검증 스킵

        var teamStats = teamRecordRepository.findTeamStatsByGame(gameId);
        if (teamStats.isEmpty()) throw new CustomException(ErrorCode.RECORD_NOT_FOUND);

        var lines = teamRecordRepository.findPlayerLines(gameId);

        var homeTotals = teamStats.stream()
                .filter(s -> Objects.equals(head.getHomeTeamId(), s.getTeam().getId()))
                .findFirst().orElse(null);
        var awayTotals = teamStats.stream()
                .filter(s -> Objects.equals(head.getAwayTeamId(), s.getTeam().getId()))
                .findFirst().orElse(null);

        var homePlayers = lines.stream()
                .filter(l -> Objects.equals(head.getHomeTeamId(), l.getTeamId()))
                .map(GameBoxscoreResponse.PlayerLine::from).toList();
        var awayPlayers = lines.stream()
                .filter(l -> Objects.equals(head.getAwayTeamId(), l.getTeamId()))
                .map(GameBoxscoreResponse.PlayerLine::from).toList();

        var home = GameBoxscoreResponse.TeamBox.from(head.getHomeTeamId(), head.getHomeTeamName(), homeTotals, homePlayers);
        var away = GameBoxscoreResponse.TeamBox.from(head.getAwayTeamId(), head.getAwayTeamName(), awayTotals, awayPlayers);

        var dto = GameBoxscoreResponse.builder()
                .gameId(head.getGameId())
                .tournamentName("자체전")
                .date(head.getDate() != null ? head.getDate().toLocalDateTime() : null)
                .venueName(head.getVenueName())
                .home(home).away(away)
                .build();

        // 쿼터 득점
        int[] homeQ = new int[5];
        int[] awayQ = new int[5];
        for (TeamPeriodScoreProjection q : teamRecordRepository.findQuarterScores(gameId)) {
            int idx = Math.min(q.getPeriod(), 5) - 1;
            if (Objects.equals(dto.getHome().getTeamId(), q.getTeamId())) homeQ[idx] = q.getPts();
            if (Objects.equals(dto.getAway().getTeamId(), q.getTeamId())) awayQ[idx] = q.getPts();
        }

        Map<Long, int[]> playerQ = new HashMap<>();
        for (PlayerPeriodScoreProjection p : teamRecordRepository.findPlayerQuarterScores(gameId)) {
            int idx = Math.min(p.getPeriod(), 5) - 1;
            int[] arr = playerQ.computeIfAbsent(p.getPlayerId(), __ -> new int[5]);
            arr[idx] = p.getPts();
        }

        String xhtml = stripBomAndTrim(buildScrimmageXhtml(dto, homeQ, awayQ, playerQ));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            var builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(xhtml, null);

            // 폰트 설정
            var regular = new ClassPathResource("fonts/NotoSansKR-Regular.ttf");
            var bold = new ClassPathResource("fonts/NotoSansKR-Bold.ttf");

            if (regular.exists()) {
                builder.useFont(() -> {
                    try { return regular.getInputStream(); }
                    catch (IOException e) { throw new UncheckedIOException(e); }
                }, "NotoSansKR", 400, PdfRendererBuilder.FontStyle.NORMAL, true);
            }
            if (bold.exists()) {
                builder.useFont(() -> {
                    try { return bold.getInputStream(); }
                    catch (IOException e) { throw new UncheckedIOException(e); }
                }, "NotoSansKR", 700, PdfRendererBuilder.FontStyle.NORMAL, true);
            }

            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String stripBomAndTrim(String s) {
        if (s == null) return null;
        if (!s.isEmpty() && s.charAt(0) == '\uFEFF') s = s.substring(1);
        return s.trim();
    }

    /** XHTML (자체전용) */
    private String buildScrimmageXhtml(GameBoxscoreResponse dto,
                                       int[] homeQ, int[] awayQ,
                                       Map<Long, int[]> playerQ) {

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
        String dateStr = dto.getDate() != null ? dto.getDate().format(fmt) : "";

        int homePts = dto.getHome().getTotals().getPts();
        int awayPts = dto.getAway().getTotals().getPts();
        String homeBadge = homePts > awayPts ? "WIN" : (homePts < awayPts ? "LOSE" : "DRAW");
        String awayBadge = awayPts > homePts ? "WIN" : (awayPts < homePts ? "LOSE" : "DRAW");

        StringBuilder sb = new StringBuilder(8000);
        sb.append("""
            <?xml version="1.0" encoding="UTF-8"?>
            <html xmlns="http://www.w3.org/1999/xhtml" lang="ko">
            <head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
            <style>
              @page { size: A4 portrait; margin: 8mm 10mm; }
              *{font-family:'NotoSansKR',sans-serif;}
              html,body{margin:0;padding:0;color:#222;}
              .title{font-size:13px;color:#444;margin:2px 0 6px;font-weight:700;}
              .score{font-size:24px;margin:6px 0 10px;font-weight:700;text-align:center;}
              .team{margin:10px 0 4px;font-weight:700;}
              table{width:100%;border-collapse:collapse;font-size:11px;table-layout:fixed;page-break-inside:avoid;}
              thead{background:#f7f7f9;}
              .blue thead{background:#eaf1ff;} .red thead{background:#ffecec;}
              th,td{border:1px solid #eaeaea;padding:4px 6px;text-align:center;line-height:1.15;}
              .totals{font-weight:700;background:#fafafa;}
              .win{color:#d14;font-weight:700;} .lose{color:#777;font-weight:700;}
            </style></head><body>
        """);

        sb.append("<div class='title'>자체전 스코어 시트");
        if (!dateStr.isEmpty()) sb.append(" &#183; ").append(dateStr);
        if (dto.getVenueName() != null) sb.append(" &#183; ").append(escape(dto.getVenueName()));
        sb.append("</div>")
                .append("<div class='score'>").append(homePts).append(" : ").append(awayPts).append("</div>");

        // ✅ GamePdfService 안 건드리기 → 같은 로직 복사해서 사용
        sb.append(renderTeamBox(dto.getHome(), true,  homeBadge, homeQ, playerQ));
        sb.append(renderTeamBox(dto.getAway(), false, awayBadge, awayQ, playerQ));

        sb.append("</body></html>");
        return sb.toString();
    }

    /** GamePdfService.renderTeamBox 복사 */
    private String renderTeamBox(GameBoxscoreResponse.TeamBox tb,
                                 boolean blue,
                                 String badge,
                                 int[] teamQuarter,
                                 Map<Long, int[]> playerQ) {

        var t = tb.getTotals();
        String colorClass = blue ? "blue" : "red";
        String badgeClass = "WIN".equals(badge) ? "win" : "lose";

        StringBuilder sb = new StringBuilder();
        sb.append("<div class='team ").append(colorClass).append("'>")
                .append(escape(tb.getTeamName()))
                .append("&#160;&#160;<span class='").append(badgeClass).append("'>").append(badge).append("</span>")
                .append("</div>")
                .append("<table class='").append(colorClass).append("'><thead><tr>")
                .append("<th rowspan='2'>번호</th><th rowspan='2'>이름</th>")
                .append("<th rowspan='2'>리바운드</th><th rowspan='2'>어시</th><th rowspan='2'>스틸</th><th rowspan='2'>블록</th>")
                .append("<th colspan='5'>쿼터득점</th><th rowspan='2'>출전</th><th rowspan='2'>총합</th>")
                .append("</tr><tr>")
                .append("<th>1Q</th><th>2Q</th><th>3Q</th><th>4Q</th><th>OT</th>")
                .append("</tr></thead><tbody>");

        int rebSum = 0;
        for (var p : tb.getPlayers()) {
            int reb = Optional.ofNullable(p.getReb()).orElse(0);
            rebSum += reb;
            int[] q = playerQ.getOrDefault(p.getPlayerId(), new int[]{0, 0, 0, 0, 0});
            sb.append("<tr>")
                    .append(td(p.getBackNumber()))
                    .append(td(escape(p.getPlayerName())))
                    .append(td(reb))
                    .append(td(p.getAst()))
                    .append(td(p.getStl()))
                    .append(td(p.getBlk()))
                    .append(td(q[0])).append(td(q[1])).append(td(q[2])).append(td(q[3])).append(td(q[4]))
                    .append(td(formatMinutes(p.getMinutes())))
                    .append(td(p.getPts()))
                    .append("</tr>");
        }

        int teamSum = Arrays.stream(teamQuarter).sum();
        sb.append("<tr class='totals'>")
                .append(td("합계")).append(td(""))
                .append(td(rebSum)).append(td(t.getAst())).append(td(t.getStl())).append(td(t.getBlk()))
                .append(td(teamQuarter[0])).append(td(teamQuarter[1])).append(td(teamQuarter[2])).append(td(teamQuarter[3])).append(td(teamQuarter[4]))
                .append(td("")).append(td(teamSum)).append("</tr>")
                .append("</tbody></table>");
        return sb.toString();
    }

    private String td(Object v) { return "<td>" + (v == null ? "" : v) + "</td>"; }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String formatMinutes(Object minutesObj) {
        if (minutesObj == null) return "";
        double minutes = (minutesObj instanceof Number n) ? n.doubleValue() : 0d;
        int totalSec = (int) Math.round(minutes * 60d);
        return String.format("%02d:%02d", totalSec / 60, totalSec % 60);
    }
}
