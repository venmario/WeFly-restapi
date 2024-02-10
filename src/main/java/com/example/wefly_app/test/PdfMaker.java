package com.example.wefly_app.test;

import com.example.wefly_app.WeFlyApplication;
import com.example.wefly_app.entity.Airline;
import com.example.wefly_app.entity.Flight;
import com.example.wefly_app.entity.Transaction;
import com.example.wefly_app.repository.TransactionRepository;
import com.example.wefly_app.request.transaction.InvoiceDTO;
import com.example.wefly_app.service.impl.TransactionImpl;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class PdfMaker {

    public static void main(String[] args) throws IOException {
//         Start the Spring application context
//        ConfigurableApplicationContext context = SpringApplication.run(WeFlyApplication.class, args);
//
//        TransactionImpl transactionService = context.getBean(TransactionImpl.class);
//        TransactionServiceTest transactionServiceTest = context.getBean(TransactionServiceTest.class);
//        Transaction transaction = transactionServiceTest.getInvoiceDTO(29L);
//        transactionService.generateInvoice(transaction);
//
//
//        InvoiceDTO invoiceDTO = transactionService.getInvoiceDTO(29L);



        String path = "e-ticket/e-ticket.pdf";
        PdfWriter writer = new PdfWriter(path);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        ImageData imageData = ImageDataFactory.create("e-ticket/properties/logo.png");
        Image image = new Image(imageData).setHeight(80).setWidth(80)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT);

        Table headerTable = new Table(2);

// You might need to scale the logo to fit your header's design
//        logo.scaleToFit(50, 50);
        Cell logoCell = new Cell().add(image);
// Remove borders if needed and set background color or other styles
        logoCell.setBorder(Border.NO_BORDER);
        headerTable.addCell(logoCell);

// Add the booking code text
        Paragraph bookingCode = new Paragraph("ZKSGDC")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(20);
// Set the paragraph styles as per your design
        Cell bookingCodeCell = new Cell().add(bookingCode);
        bookingCodeCell.setTextAlignment(TextAlignment.RIGHT);
        bookingCodeCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
// Remove borders if needed and set background color or other styles
        bookingCodeCell.setBorder(Border.NO_BORDER);
        headerTable.addCell(bookingCodeCell);

// Add the table to the document as a header
        document.add(headerTable);


        document.close();
        System.out.println("Invoice generated");

    }


}

