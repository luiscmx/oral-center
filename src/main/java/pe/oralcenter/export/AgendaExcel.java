package pe.oralcenter.export;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pe.oralcenter.model.*;

/** Escribe las citas autorizadas en un archivo Excel nuevo. */
public class AgendaExcel {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    public void guardar(Path archivo, List<CitaAgenda> citas, String fecha, String profesional, String estado) throws IOException {
        Path temporal = Files.createTempFile(archivo.toAbsolutePath().getParent(), ".agenda-", ".xlsx");
        try {
            try (XSSFWorkbook libro = new XSSFWorkbook()) {
                Sheet hoja = libro.createSheet("Agenda");
                String[][] informacion = {{"Oral Center - Agenda"}, {"Fecha", fecha}, {"Odontólogo", profesional},
                        {"Estado", estado}, {"Generado", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm"))},
                        {"Total de citas", String.valueOf(citas.size())}};
                for (int i = 0; i < informacion.length; i++) { escribirFila(hoja, i, informacion[i]); }
                Row encabezado = escribirFila(hoja, 7, new String[]{"Código", "Fecha", "Inicio", "Fin", "Duración (min)",
                        "Paciente / DNI", "Odontólogo", "Atención", "Estado", "Motivo"});
                Font fuente = libro.createFont();
                fuente.setBold(true);
                CellStyle titulos = libro.createCellStyle();
                titulos.setFont(fuente);
                titulos.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
                titulos.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                for (Cell celda : encabezado) { celda.setCellStyle(titulos); }
                CellStyle textoLargo = libro.createCellStyle();
                textoLargo.setWrapText(true);
                int numeroFila = 8;
                for (CitaAgenda dato : citas) {
                    Cita cita = dato.getCita();
                    Row fila = escribirFila(hoja, numeroFila, new String[]{String.valueOf(cita.getId()), cita.getFecha().format(FECHA),
                            cita.getHora().toString(), cita.getFin().toLocalTime().toString(), String.valueOf(cita.getDuracion()),
                            dato.getPaciente(), dato.getOdontologo(), dato.getAtencion(), cita.getEstado(), cita.getMotivo()});
                    fila.getCell(9).setCellStyle(textoLargo);
                    fila.setHeightInPoints(48);
                    numeroFila++;
                }
                int[] anchos = {14, 16, 12, 12, 18, 40, 34, 34, 20, 70};
                for (int i = 0; i < anchos.length; i++) { hoja.setColumnWidth(i, anchos[i] * 256); }
                hoja.createFreezePane(0, 8);
                hoja.setAutoFilter(new CellRangeAddress(7, numeroFila - 1, 0, 9));
                try (OutputStream salida = Files.newOutputStream(temporal)) { libro.write(salida); }
            }
            // Solo se publica el archivo completo; no se reemplaza uno existente.
            Files.move(temporal, archivo);
        } finally { Files.deleteIfExists(temporal); }
    }

    private Row escribirFila(Sheet hoja, int numero, String[] valores) {
        Row fila = hoja.createRow(numero);
        for (int i = 0; i < valores.length; i++) {
            // setCellValue(String) conserva ceros, códigos largos y textos como =SUM(...).
            fila.createCell(i).setCellValue(valores[i]);
        }
        return fila;
    }
}
