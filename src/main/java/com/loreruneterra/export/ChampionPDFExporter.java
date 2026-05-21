package com.loreruneterra.export;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.loreruneterra.model.Campeon;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ChampionPDFExporter {

    private static final DeviceRgb COLOR_GOLD  = new DeviceRgb(200, 170, 110);
    private static final DeviceRgb COLOR_DARK  = new DeviceRgb(13,  22,  36);
    private static final DeviceRgb COLOR_GRAY  = new DeviceRgb(160, 155, 140);

    public static String exportar(Campeon campeon, String rutaDestino) throws IOException {
        String nombreArchivo = rutaDestino + "/" +
                campeon.getNombre().replaceAll("[^a-zA-Z0-9]", "_") + "_LoreRuneTerra.pdf";

        PdfWriter writer = new PdfWriter(nombreArchivo);
        PdfDocument pdf  = new PdfDocument(writer);
        Document doc     = new Document(pdf);
        doc.setMargins(40, 50, 40, 50);

        PdfFont fontBold    = PdfFontFactory.createFont("Helvetica-Bold");
        PdfFont fontNormal  = PdfFontFactory.createFont("Helvetica");
        PdfFont fontItalic  = PdfFontFactory.createFont("Helvetica-Oblique");

        // ── Cabecera ──────────────────────────────────────────
        doc.add(new Paragraph("LoreRuneTerra")
                .setFont(fontBold)
                .setFontSize(10)
                .setFontColor(COLOR_GOLD)
                .setTextAlignment(TextAlignment.RIGHT));

        // ── Imagen del campeón ────────────────────────────────
        try {
            String imgUrl = campeon.getImagen();
            if (imgUrl != null && !imgUrl.isEmpty()) {
                Image img = new Image(ImageDataFactory.create(new URL(imgUrl)));
                img.setWidth(UnitValue.createPercentValue(40));
                img.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
                doc.add(img);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen: " + e.getMessage());
        }

        // ── Nombre del campeón ────────────────────────────────
        doc.add(new Paragraph(campeon.getNombre())
                .setFont(fontBold)
                .setFontSize(28)
                .setFontColor(COLOR_GOLD)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10));

        // ── Título ────────────────────────────────────────────
        if (campeon.getTitulo() != null && !campeon.getTitulo().isEmpty()) {
            doc.add(new Paragraph(campeon.getTitulo())
                    .setFont(fontItalic)
                    .setFontSize(14)
                    .setFontColor(COLOR_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(8));
        }

        // ── Separador dorado ──────────────────────────────────
        SolidLine line = new SolidLine(1f);
        line.setColor(COLOR_GOLD);
        doc.add(new LineSeparator(line).setMarginBottom(12));

        // ── Clase y región ────────────────────────────────────
        String clase  = campeon.getClase()  != null ? campeon.getClase()  : "–";
        doc.add(new Paragraph("Clase: " + clase)
                .setFont(fontNormal)
                .setFontSize(12)
                .setFontColor(ColorConstants.WHITE)
                .setMarginBottom(16));

        // ── Biografía corta ───────────────────────────────────
        if (campeon.getBioCorta() != null && !campeon.getBioCorta().isEmpty()) {
            doc.add(new Paragraph("Biografía")
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setFontColor(COLOR_GOLD)
                    .setMarginBottom(6));

            doc.add(new Paragraph(campeon.getBioCorta())
                    .setFont(fontNormal)
                    .setFontSize(11)
                    .setFontColor(COLOR_GRAY)
                    .setMarginBottom(16));
        }

        // ── Biografía completa ────────────────────────────────
        if (campeon.getBioCompleta() != null && !campeon.getBioCompleta().isEmpty()) {
            doc.add(new Paragraph("Historia completa")
                    .setFont(fontBold)
                    .setFontSize(14)
                    .setFontColor(COLOR_GOLD)
                    .setMarginBottom(6));

            doc.add(new Paragraph(campeon.getBioCompleta())
                    .setFont(fontNormal)
                    .setFontSize(11)
                    .setFontColor(COLOR_GRAY)
                    .setMarginBottom(16));
        }

        // ── Pie de página ─────────────────────────────────────
        doc.add(new LineSeparator(line).setMarginTop(20));
        doc.add(new Paragraph("Exportado desde LoreRuneTerra · " +
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .setFont(fontItalic)
                .setFontSize(9)
                .setFontColor(COLOR_GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        doc.close();
        return nombreArchivo;
    }
}