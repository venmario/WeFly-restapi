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
        // Start the Spring application context
        ConfigurableApplicationContext context = SpringApplication.run(WeFlyApplication.class, args);

        TransactionImpl transactionService = context.getBean(TransactionImpl.class);
        TransactionServiceTest transactionServiceTest = context.getBean(TransactionServiceTest.class);
        Transaction transaction = transactionServiceTest.getInvoiceDTO(29L);
        transactionService.generateInvoice(transaction);


//        InvoiceDTO invoiceDTO = transactionService.getInvoiceDTO(29L);
//
//        String path = "invoice/invoice.pdf";
//        PdfWriter writer = new PdfWriter(path);
//        PdfDocument pdf = new PdfDocument(writer);
//        Document document = new Document(pdf, PageSize.A4);
//        ImageData imageData = ImageDataFactory.create("invoice/properties/logo.png");
//        Image image = new Image(imageData).setHeight(80).setWidth(80)
//                .setHorizontalAlignment(HorizontalAlignment.RIGHT);
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy - HH:mm");
//        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
//        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
//
//        Table table = new Table(new float[]{1, 1});
//        table.setWidth(UnitValue.createPercentValue(100)); // Set table width to 100% of the page width
//
//// First cell with content aligned to the left
//        Cell leftCell = new Cell().add(new Paragraph("Invoice" +
//                        "\n" + "Order Id: " + "156")
//                        .setFontSize(14))
//                .setVerticalAlignment(VerticalAlignment.MIDDLE)
//                .setBorder(Border.NO_BORDER)
//                .setTextAlignment(TextAlignment.LEFT);
//
//// Second cell with content aligned to the right
//        Cell rightCell = new Cell().add(image)
//                .setVerticalAlignment(VerticalAlignment.MIDDLE)
//                .setBorder(Border.NO_BORDER)
//                .setTextAlignment(TextAlignment.RIGHT);
//        table.addCell(leftCell);
//        table.addCell(rightCell);
//        document.add(table);
//        document.add(new LineSeparator(new SolidLine()));
//
//        document.add(new Paragraph("Orderer Detail")
//                .setTextAlignment(TextAlignment.LEFT)
//                .setFontSize(12));
//
//        // Create a table with three columns
//        Table table1 = new Table(new float[]{1, 2, 1}); // Adjust column ratios as needed
//        table1.setWidth(UnitValue.createPercentValue(80)); // Set table width to 100% of the page width
//
//// Define custom border for cells
//        Border solidBorder = new SolidBorder(0.3f);
//        Color greyColor = new DeviceRgb(165, 165, 165);
//        table1.setBorder(solidBorder);
//
//// First cell: "Nama Lengkap"
//        Cell cell1 = new Cell().add(new Paragraph("Full Name").setFontColor(greyColor))
//                .setBorder(Border.NO_BORDER);
//        cell1.add(new Paragraph(invoiceDTO.getOrderer().getFirstName() + " " + invoiceDTO.getOrderer().getLastName()));
//        table1.addCell(cell1);
//
//// Second cell: "Email"
//        Cell cell2 = new Cell().add(new Paragraph("Email").setFontColor(greyColor))
//                .setBorder(Border.NO_BORDER);
//        cell2.add(new Paragraph(invoiceDTO.getOrderer().getEmail()));
//        table1.addCell(cell2);
//
//// Third cell: "Nomor Ponsel"
//        Cell cell3 = new Cell().add(new Paragraph("Phone Number").setFontColor(greyColor))
//                .setBorder(Border.NO_BORDER);
//        cell3.add(new Paragraph(invoiceDTO.getOrderer().getPhoneNumber()));
//        table1.addCell(cell3);
//
//// Add the table to the document
//        document.add(table1);
//
//        document.add(new Paragraph("Transaction Detail")
//                .setTextAlignment(TextAlignment.LEFT)
//                .setFontSize(12));
//
//        // Payment and Method Table
//        Table paymentMethodTable = new Table(new float[]{1, 1});
//        paymentMethodTable.setWidth(UnitValue.createPercentValue(100));
//
//// Payment Time
//        Cell paymentTimeCell = new Cell().add(new Paragraph("Waktu Pembayaran: \n" + invoiceDTO
//                        .getPayment().getSettlementTime().format(formatter)).setFont(regularFont))
//                .setBorder(Border.NO_BORDER);
//        paymentMethodTable.addCell(paymentTimeCell);
//
//// Payment Method
//        Cell paymentMethodCell = new Cell().add(new Paragraph("Metode Pembayaran: \nVirtual Account BCA").setFont(regularFont))
//                .setBorder(Border.NO_BORDER);
//        paymentMethodTable.addCell(paymentMethodCell);
//
//        document.add(paymentMethodTable);
//        document.add(new Paragraph(""));
//
//// Line separator
//        document.add(new LineSeparator(new SolidLine()));
//
//// Product Table
//        Table productTable = new Table(5);
//
//// Headers
//        productTable.addHeaderCell(new Cell().add(new Paragraph("No.").setFont(boldFont)).setBorder(Border.NO_BORDER)
//                .setTextAlignment(TextAlignment.RIGHT).setWidth(30));
//        productTable.addHeaderCell(new Cell().add(new Paragraph("Produk").setFont(boldFont)).setBorder(Border.NO_BORDER)
//                .setTextAlignment(TextAlignment.RIGHT).setWidth(80));
//        productTable.addHeaderCell(new Cell().add(new Paragraph("Deskripsi").setFont(boldFont)).setBorder(Border.NO_BORDER)
//                .setTextAlignment(TextAlignment.RIGHT).setWidth(200));
//        productTable.addHeaderCell(new Cell().add(new Paragraph("Jumlah").setFont(boldFont)).setBorder(Border.NO_BORDER)
//                .setTextAlignment(TextAlignment.RIGHT).setWidth(50));
//        productTable.addHeaderCell(new Cell().add(new Paragraph("Total").setFont(boldFont)).setBorder(Border.NO_BORDER)
//                .setTextAlignment(TextAlignment.RIGHT).setWidth(140));
//
////Detail Order
//        DecimalFormat decimalFormat = new DecimalFormat("#,###");
//        decimalFormat.setGroupingSize(3);
//        decimalFormat.setGroupingUsed(true);
//        decimalFormat.setDecimalSeparatorAlwaysShown(false);
//        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
//        symbols.setGroupingSeparator('.');
//        decimalFormat.setDecimalFormatSymbols(symbols);
//
//        Hibernate.initialize(invoiceDTO.getTransactionDetails());
//        Flight flight = invoiceDTO.getTransactionDetails().get(0).getFlightClass().getFlight();
//        Airline airline = flight.getAirplane().getAirline();
//        String departureAirport = flight.getDepartureAirport().getIata();
//        String arrivalAirport = flight.getArrivalAirport().getIata();
//
//        Map<String, Integer> transactionMap = invoiceDTO.getTransaction();
//        AtomicInteger number = new AtomicInteger(1);
//        List<BigDecimal> subTotal = new ArrayList<>();
//        for (int i = 0; i < invoiceDTO.getTransactionDetails().size(); i++) {
//            subTotal.add(invoiceDTO.getTransactionDetails().get(i).getTotalPriceAdult());
//            subTotal.add(invoiceDTO.getTransactionDetails().get(i).getTotalPriceChild());
//            subTotal.add(invoiceDTO.getTransactionDetails().get(i).getTotalPriceInfant());
//            transactionMap.forEach((key, value) -> {
//                if (value > 0) {
//                    productTable.addCell(new Cell().add(new Paragraph(String.valueOf(number.get()))).setBorder(Border.NO_BORDER)
//                            .setTextAlignment(TextAlignment.RIGHT));
//                    productTable.addCell(new Cell().add(new Paragraph(key + " Ticket")).setBorder(Border.NO_BORDER)
//                            .setTextAlignment(TextAlignment.RIGHT));
//                    productTable.addCell(new Cell().add(new Paragraph(airline.getName() +
//                            " (" + departureAirport + " - " + arrivalAirport +
//                            ")")).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
//                    productTable.addCell(new Cell().add(new Paragraph(String.valueOf(value)))
//                            .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
//                    productTable.addCell(new Cell().add(new Paragraph("IDR " + decimalFormat.format(subTotal.get(number.get() - 1))))
//                            .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
//                    number.getAndIncrement();
//                }
//            });
//        }
//
//// Add the product table to the document
//        document.add(productTable);
//
//        document.add(new LineSeparator(new SolidLine(0.5f)));
//
//        document.add(new Paragraph(""));
//
//        Table totalTable = new Table(UnitValue.createPointArray(new float[]{130, 130, 100, 140}));
//        totalTable.setWidth(UnitValue.createPercentValue(100)); // Set table width to 100% of the page width
//        Cell grandTotalLabelCell = new Cell().add(new Paragraph("Total Payment").setFont(boldFont))
//                .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
//        Cell grandTotalValueCell = new Cell().add(new Paragraph("IDR " + decimalFormat.format(invoiceDTO.getPayment().getGrossAmount()))
//                .setFont(boldFont).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
//        totalTable.addCell(new Cell().add(new Paragraph(" ")).setBorder(Border.NO_BORDER));
//        totalTable.addCell(new Cell().add(new Paragraph(" ")).setBorder(Border.NO_BORDER));
//        totalTable.addCell(grandTotalLabelCell);
//        totalTable.addCell(grandTotalValueCell);
//
//// Add the total table to the document
//        document.add(totalTable);
//
////// Note at the bottom
////        Paragraph note = new Paragraph("*Imbal jasa penjualan sudah termasuk PPN sebesar 11%")
////                .setFont(regularFont)
////                .setFontSize(8)
////                .setTextAlignment(TextAlignment.RIGHT);
////        document.add(note);
//
//        document.close();
        System.out.println("Invoice generated");

    }


}

