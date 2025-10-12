package com.openstep.balllinkbe.features.scrimmage.service;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GameEvent;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ScoreExportServiceImpl implements ScoreExportService {

    // PDF에 사용할 한글 폰트 파일 경로 넣기 (이건 자율)
    private static final String FONT_PATH = "src/main/resources/fonts/NanumGothic.ttf";

    /**
     * 경기 기록 데이터를 PDF 파일로 생성합니다.
     * @param game          경기 정보 객체
     * @param events        경기 이벤트 목록
     * @param exportFormat  파일 형식 (일단, PDF만 지원하게끔 해놓기)
     * @return              생성된 PDF 파일의 바이트 배열
     */
    @Override
    public byte[] generatePdfScoreSheet(Game game, List<GameEvent> events, String exportFormat) {
        // exportFormat이 PDF가 아닐 경우 예외 발생
        if (!"PDF".equalsIgnoreCase(exportFormat)) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }

        try (ByteArrayOutputStream byteArr = new ByteArrayOutputStream()) {
            // PDF 문서를 작성하기 위한 기본 설정
            PdfWriter writer = new PdfWriter(byteArr);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 한글 폰트 설정
            PdfFont font = PdfFontFactory.createFont(FONT_PATH, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            document.setFont(font);

            // 문서 제목 추가
            document.add(new Paragraph(String.format("자체전 공식 기록지 (Game ID: %d)", game.getId()))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(20));

            document.add(new Paragraph(" ")); // 공백 단락 추가

            // 경기 정보 테이블 추가
            addGameInfo(document, game);

            document.add(new Paragraph(" ")); // 공백 단락 추가

            // 경기 이벤트 테이블 추가
            addGameEventsTable(document, events);

            document.close();
            return byteArr.toByteArray();

        } catch (IOException e) {
            // PDF 생성 중 입출력 오류 발생 시 예외 처리
            throw new CustomException(ErrorCode.PDF_EXPORT_FAILED);
        }
    }

    /**
     * 경기 정보를 담는 테이블을 문서에 추가합니다.
     * @param document  PDF 문서 객체
     * @param game      경기 정보 객체
     */
    private void addGameInfo(Document document, Game game) {
        // 4개의 컬럼을 가진 테이블 생성
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 3, 1, 3}));
        infoTable.setWidth(UnitValue.createPercentValue(100)); // 테이블 너비를 100%로 설정

        // 헤더 및 셀 추가
        infoTable.addCell(createHeaderCell("경기 일시"));
        infoTable.addCell(createTextCell(game.getScheduledAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        infoTable.addCell(createHeaderCell("경기장"));
        // 경기장 정보가 없을 경우 "정보 없음"으로 표시
        infoTable.addCell(createTextCell(game.getVenue() != null ? game.getVenue().getName() : "정보 없음"));

        infoTable.addCell(createHeaderCell("Home"));
        infoTable.addCell(createTextCell(game.getHomeTeam().getName()));
        infoTable.addCell(createHeaderCell("Away"));
        infoTable.addCell(createTextCell(game.getOpponentName()));

        document.add(infoTable);
    }

    /**
     * 경기 이벤트 목록을 테이블로 만들어 문서에 추가합니다.
     * @param document  PDF 문서 객체
     * @param events    경기 이벤트 목록
     */
    private void addGameEventsTable(Document document, List<GameEvent> events) {
        // 6개의 컬럼을 가진 테이블 생성
        Table eventTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 2, 2, 2, 3}));
        eventTable.setWidth(UnitValue.createPercentValue(100)); // 테이블 너비를 100%로 설정

        // 테이블 헤더 셀 추가
        eventTable.addHeaderCell(createHeaderCell("쿼터"));
        eventTable.addHeaderCell(createHeaderCell("시간"));
        eventTable.addHeaderCell(createHeaderCell("팀"));
        eventTable.addHeaderCell(createHeaderCell("선수"));
        eventTable.addHeaderCell(createHeaderCell("이벤트"));
        eventTable.addHeaderCell(createHeaderCell("비고"));

        // 이벤트 목록을 순회하며 테이블에 데이터 셀 추가
        for (GameEvent event : events) {
            eventTable.addCell(createTextCell(String.valueOf(event.getPeriod())));
            eventTable.addCell(createTextCell(event.getClockTime()));
            // 팀 정보가 없을 경우 "N/A"로 표시
            eventTable.addCell(createTextCell(event.getTeam() != null ? event.getTeam().getName() : "N/A"));
            // 선수 정보가 없을 경우 "N/A"로 표시
            eventTable.addCell(createTextCell(event.getPlayer() != null ? event.getPlayer().getName() : "N/A"));
            eventTable.addCell(createTextCell(event.getType().toString()));
            // 비고 정보가 없을 경우 빈 문자열로 표시
            eventTable.addCell(createTextCell(event.getMeta() != null ? event.getMeta() : ""));
        }

        document.add(eventTable);
    }

    /**
     * 텍스트를 포함하는 헤더 셀을 생성합니다.
     * @param text  셀에 들어갈 텍스트
     * @return      생성된 Cell 객체
     */
    private Cell createHeaderCell(String text) {
        return new Cell().add(new Paragraph(text).setBold()).setTextAlignment(TextAlignment.CENTER);
    }

    /**
     * 텍스트를 포함하는 일반 셀을 생성합니다.
     * @param text  셀에 들어갈 텍스트
     * @return      생성된 Cell 객체
     */
    private Cell createTextCell(String text) {
        return new Cell().add(new Paragraph(text));
    }
}