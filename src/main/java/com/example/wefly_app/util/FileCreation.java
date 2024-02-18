package com.example.wefly_app.util;

import com.example.wefly_app.entity.Airline;
import com.example.wefly_app.entity.BoardingPass;
import com.example.wefly_app.entity.Flight;
import com.example.wefly_app.entity.Passenger;
import com.example.wefly_app.request.checkin.BoardingPassDTO;
import com.example.wefly_app.request.transaction.ETicketDTO;
import com.example.wefly_app.request.transaction.InvoiceDTO;
import com.example.wefly_app.util.exception.FileHandlingException;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class FileCreation {
    public void generatePaymentProof(Document document, InvoiceDTO invoiceDTO) {
        log.info("Generating Payment Proof");
        try {
            ImageData imageData = ImageDataFactory.create("properties/logo.png");
            Image image = new Image(imageData).setHeight(80).setWidth(80)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy - HH:mm");
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            Table table = new Table(new float[]{1, 1});
            table.setWidth(UnitValue.createPercentValue(100)); // Set table width to 100% of the page width

// First cell with content aligned to the left
            Cell leftCell = new Cell().add(new Paragraph("Payment Proof" +
                            "\n" + "Order Id: " + invoiceDTO.getId())
                            .setFontSize(14))
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.LEFT);

// Second cell with content aligned to the right
            Cell rightCell = new Cell().add(image)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT);
            table.addCell(leftCell);
            table.addCell(rightCell);
            document.add(table);
            document.add(new LineSeparator(new SolidLine()));

            document.add(new Paragraph("Orderer Detail")
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(12));

            // Create a table with three columns
            Table table1 = new Table(new float[]{1, 2, 1}); // Adjust column ratios as needed
            table1.setWidth(UnitValue.createPercentValue(80)); // Set table width of the page width

// Define custom border for cells
            Border solidBorder = new SolidBorder(0.3f);
            Color greyColor = new DeviceRgb(165, 165, 165);
            table1.setBorder(solidBorder);

// First cell: "Nama Lengkap"
            Cell cell1 = new Cell().add(new Paragraph("Full Name").setFontColor(greyColor))
                    .setBorder(Border.NO_BORDER);
            cell1.add(new Paragraph(invoiceDTO.getOrderer().getFirstName() + " " + invoiceDTO.getOrderer().getLastName()));
            table1.addCell(cell1);

// Second cell: "Email"
            Cell cell2 = new Cell().add(new Paragraph("Email").setFontColor(greyColor))
                    .setBorder(Border.NO_BORDER);
            cell2.add(new Paragraph(invoiceDTO.getOrderer().getEmail()));
            table1.addCell(cell2);

// Third cell: "Nomor Ponsel"
            Cell cell3 = new Cell().add(new Paragraph("Phone Number").setFontColor(greyColor))
                    .setBorder(Border.NO_BORDER);
            cell3.add(new Paragraph(invoiceDTO.getOrderer().getPhoneNumber()));
            table1.addCell(cell3);

// Add the table to the document
            document.add(table1);

            document.add(new Paragraph("Transaction Detail")
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(12));

            // Payment and Method Table
            Table paymentMethodTable = new Table(new float[]{1, 1});
            paymentMethodTable.setWidth(UnitValue.createPercentValue(100));

// Payment Time
            Cell paymentTimeCell = new Cell().add(new Paragraph("Waktu Pembayaran: \n" + invoiceDTO
                            .getPayment().getSettlementTime().format(formatter)).setFont(regularFont))
                    .setBorder(Border.NO_BORDER);
            paymentMethodTable.addCell(paymentTimeCell);

// Payment Method
            Cell paymentMethodCell = new Cell().add(new Paragraph("Metode Pembayaran: \nVirtual Account BCA").setFont(regularFont))
                    .setBorder(Border.NO_BORDER);
            paymentMethodTable.addCell(paymentMethodCell);

            document.add(paymentMethodTable);
            document.add(new Paragraph(""));

// Line separator
            document.add(new LineSeparator(new SolidLine()));

// Product Table
            Table productTable = new Table(5);

// Headers
            productTable.addHeaderCell(new Cell().add(new Paragraph("No.").setFont(boldFont)).setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT).setWidth(30));
            productTable.addHeaderCell(new Cell().add(new Paragraph("Produk").setFont(boldFont)).setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT).setWidth(80));
            productTable.addHeaderCell(new Cell().add(new Paragraph("Deskripsi").setFont(boldFont)).setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT).setWidth(200));
            productTable.addHeaderCell(new Cell().add(new Paragraph("Jumlah").setFont(boldFont)).setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT).setWidth(50));
            productTable.addHeaderCell(new Cell().add(new Paragraph("Total").setFont(boldFont)).setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT).setWidth(140));

//Detail Order
            DecimalFormat decimalFormat = new DecimalFormat("#,###");
            decimalFormat.setGroupingSize(3);
            decimalFormat.setGroupingUsed(true);
            decimalFormat.setDecimalSeparatorAlwaysShown(false);
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
            symbols.setGroupingSeparator('.');
            decimalFormat.setDecimalFormatSymbols(symbols);

            Hibernate.initialize(invoiceDTO.getTransactionDetails());
            Flight flight = invoiceDTO.getTransactionDetails().get(0).getFlightClass().getFlightSchedule().getFlight();
            Airline airline = flight.getAirplane().getAirline();
            String departureAirport = flight.getDepartureAirport().getIata();
            String arrivalAirport = flight.getArrivalAirport().getIata();

            Map<String, Integer> transactionMap = invoiceDTO.getTransaction();
            AtomicInteger number = new AtomicInteger(1);
            List<BigDecimal> subTotal = new ArrayList<>();
            for (int i = 0; i < invoiceDTO.getTransactionDetails().size(); i++) {
                subTotal.add(invoiceDTO.getTransactionDetails().get(i).getTotalPriceAdult());
                subTotal.add(invoiceDTO.getTransactionDetails().get(i).getTotalPriceChild());
                subTotal.add(invoiceDTO.getTransactionDetails().get(i).getTotalPriceInfant());
                transactionMap.forEach((key, value) -> {
                    if (value > 0) {
                        productTable.addCell(new Cell().add(new Paragraph(String.valueOf(number.get()))).setBorder(Border.NO_BORDER)
                                .setTextAlignment(TextAlignment.RIGHT));
                        productTable.addCell(new Cell().add(new Paragraph(key + " Ticket")).setBorder(Border.NO_BORDER)
                                .setTextAlignment(TextAlignment.RIGHT));
                        productTable.addCell(new Cell().add(new Paragraph(airline.getName() +
                                " (" + departureAirport + " - " + arrivalAirport +
                                ")")).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
                        productTable.addCell(new Cell().add(new Paragraph(String.valueOf(value)))
                                .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
                        productTable.addCell(new Cell().add(new Paragraph("IDR " + decimalFormat.format(subTotal.get(number.get() - 1))))
                                .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
                        number.getAndIncrement();
                    }
                });
            }

// Add the product table to the document
            document.add(productTable);

            document.add(new LineSeparator(new SolidLine(0.5f)));

            document.add(new Paragraph(""));

            Table totalTable = new Table(UnitValue.createPointArray(new float[]{130, 130, 100, 140}));
            totalTable.setWidth(UnitValue.createPercentValue(100)); // Set table width to 100% of the page width
            Cell grandTotalLabelCell = new Cell().add(new Paragraph("Total Payment").setFont(boldFont))
                    .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
            Cell grandTotalValueCell = new Cell().add(new Paragraph("IDR " + decimalFormat.format(invoiceDTO.getPayment().getGrossAmount()))
                    .setFont(boldFont).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
            totalTable.addCell(new Cell().add(new Paragraph(" ")).setBorder(Border.NO_BORDER));
            totalTable.addCell(new Cell().add(new Paragraph(" ")).setBorder(Border.NO_BORDER));
            totalTable.addCell(grandTotalLabelCell);
            totalTable.addCell(grandTotalValueCell);
// Add the total table to the document
            document.add(totalTable);
            log.info("Payment Proof Generated");
        } catch (IOException e) {
            log.error("Error Generating Payment Proof", e);
            throw new FileHandlingException("Error Generating Payment Proof", e);
        }
    }

    public void generateETicket(Document document, ETicketDTO eticketDTO) {
        log.info("Generating ETicket");
        try {
            DateTimeFormatter formatterDay = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
            DateTimeFormatter formatterStandard = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm");

            ImageData imageData = ImageDataFactory.create("properties/logo.png");
            Image image = new Image(imageData);
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            Color blueColor = new DeviceRgb(30, 144, 255);
            Color greyColor = new DeviceRgb(165, 165, 165);
            Color yellowColor = new DeviceRgb(255, 255, 0);

            Table headerTable = new Table(new float[]{1, 1});
            headerTable.setWidth(UnitValue.createPercentValue(100));

            image.scaleToFit(80, 80);
            Paragraph logoParagraph = new Paragraph("Flight E-Ticket").setFont(boldFont).setFontSize(12).setFontColor(blueColor);
            Cell logoCell = new Cell().add(image).add(logoParagraph)
                    .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT).setVerticalAlignment(VerticalAlignment.TOP);
            headerTable.addCell(logoCell);

            Text bookingCodeText = new Text("Your Booking Code: \n").setFont(regularFont).setFontSize(10);
            Text bookingCode = new Text(eticketDTO.getBookCode()).setFont(boldFont).setFontSize(20);
            Paragraph bookingCodeParagraph = new Paragraph().add(bookingCodeText).add(bookingCode).setTextAlignment(TextAlignment.RIGHT);
            Cell bookingCodeCell = new Cell().add(bookingCodeParagraph).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE);
            headerTable.addCell(bookingCodeCell);

            document.add(headerTable);
            document.add(new LineSeparator(new SolidLine()));
            document.add(new Paragraph());

            document.add(new Paragraph("Flight | " + eticketDTO.getDepartureDate().format(formatterDay)).
                    setFont(boldFont).setFontSize(12).setBackgroundColor(blueColor)
                    .setBorder(new SolidBorder(blueColor, 1)));

            Table ticketTable = new Table(4);
            ticketTable.setWidth(UnitValue.createPercentValue(100));
            ticketTable.setBorder(new SolidBorder(1));

            ticketTable.addHeaderCell(new Cell().add(new Paragraph("Booking Code").setFont(regularFont).setFontSize(10))
                    .setWidth(80).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
            ticketTable.addHeaderCell(new Cell().add(new Paragraph(eticketDTO.getBookCode()).setFont(regularFont).setFontSize(10)
                            .setBackgroundColor(yellowColor))
                    .setWidth(60).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
            ticketTable.addHeaderCell(new Cell().add(new Paragraph(eticketDTO.getDepartureDate().format(formatterStandard))
                            .setFont(regularFont).setFontSize(10).setFontColor(greyColor))
                    .setWidth(130).setBorder(Border.NO_BORDER));
            ticketTable.addHeaderCell(new Cell().add(new Paragraph(eticketDTO.getArrivalDate().format(formatterStandard))
                            .setFont(regularFont).setFontSize(10).setFontColor(greyColor))
                    .setWidth(130).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            ticketTable.addCell(new Cell().add(new Paragraph(eticketDTO.getAirlineName())
                            .setFont(regularFont).setFontSize(10)).setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT));
            ticketTable.addCell(new Cell().add(new Paragraph(" ").setFont(regularFont).setFontSize(10))
                    .setBorder(Border.NO_BORDER));
            ticketTable.addCell(new Cell().add(new Paragraph(eticketDTO.getDepartureTime().format(formatterTime))
                    .setFont(regularFont).setFontSize(10)).setBorder(Border.NO_BORDER));
            ticketTable.addCell(new Cell().add(new Paragraph(eticketDTO.getArrivalTime().format(formatterTime))
                            .setFont(regularFont).setFontSize(10)).setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT));

            String departureAirportNameFormat = eticketDTO.getDepartProvince() + "-" + eticketDTO.getDepartCity()
                    + "(" + eticketDTO.getDepartIata() + ")";
            String arrivalAirportNameFormat = eticketDTO.getArrivalProvince() + "-" + eticketDTO.getArrivalCity()
                    + "(" + eticketDTO.getArrivalIata() + ")";

            ticketTable.addCell(new Cell().add(new Paragraph(eticketDTO.getFlightCode())
                            .setFont(regularFont).setFontSize(10))
                    .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
            ticketTable.addCell(new Cell().add(new Paragraph(" ").setFont(regularFont).setFontSize(10))
                    .setBorder(Border.NO_BORDER));

            ticketTable.addCell(new Cell().add(new Paragraph(departureAirportNameFormat).setFont(regularFont).setFontSize(10))
                    .setBorder(Border.NO_BORDER));
            ticketTable.addCell(new Cell().add(new Paragraph(arrivalAirportNameFormat).setFont(regularFont).setFontSize(10))
                    .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            ticketTable.addCell(new Cell().add(new Paragraph(eticketDTO.getSeatClass().name()).setFont(regularFont).setFontSize(10))
                    .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
            ticketTable.addCell(new Cell().add(new Paragraph(" ").setFont(regularFont).setFontSize(10))
                    .setBorder(Border.NO_BORDER));
            ticketTable.addCell(new Cell().add(new Paragraph(eticketDTO.getDepartAirportName() + ", Domestic Terminal")
                            .setFont(regularFont).setFontSize(10).setFontColor(greyColor))
                    .setBorder(Border.NO_BORDER));
            ticketTable.addCell(new Cell().add(new Paragraph(eticketDTO.getArrivalAirportName() + ", Domestic Terminal")
                            .setFont(regularFont).setFontSize(10).setFontColor(greyColor))
                    .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));


            document.add(ticketTable);
            document.add(new Paragraph());
            document.add(new Paragraph("Detail Passengers").setFont(boldFont).setFontSize(16));
            document.add(new Paragraph());

            Table passengerTable = new Table(5);
            passengerTable.setWidth(UnitValue.createPercentValue(100));
            passengerTable.setBorder(new SolidBorder(1));
            SolidBorder bottomBorder = new SolidBorder(0.5f);

            passengerTable.addHeaderCell(new Cell().add(new Paragraph("No").setFont(regularFont).setFontSize(10))
                    .setBorderBottom(bottomBorder).setTextAlignment(TextAlignment.CENTER).setWidth(60));
            passengerTable.addHeaderCell(new Cell().add(new Paragraph("Passenger").setFont(regularFont).setFontSize(10))
                    .setBorderBottom(bottomBorder).setTextAlignment(TextAlignment.LEFT).setWidth(140));
            passengerTable.addHeaderCell(new Cell().add(new Paragraph("Ticket Type").setFont(regularFont).setFontSize(10))
                    .setBorderBottom(bottomBorder).setTextAlignment(TextAlignment.LEFT).setWidth(60));
            passengerTable.addHeaderCell(new Cell().add(new Paragraph("Ticket Number").setFont(regularFont).setFontSize(10))
                    .setBorderBottom(bottomBorder).setTextAlignment(TextAlignment.LEFT).setWidth(140));
            passengerTable.addHeaderCell(new Cell().add(new Paragraph("Flight Number").setFont(regularFont).setFontSize(10))
                    .setBorderBottom(bottomBorder).setTextAlignment(TextAlignment.LEFT).setWidth(100));

            eticketDTO.getPassengers().forEach(passenger -> {
                String fullName = passenger.getFirstName() + " " + passenger.getLastName();
                passengerTable.addCell(new Cell().add(new Paragraph(String.valueOf(eticketDTO.getPassengers().indexOf(passenger) + 1))
                                .setFont(regularFont).setFontSize(10).setFontColor(greyColor))
                        .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
                passengerTable.addCell(new Cell().add(new Paragraph(fullName).setFont(regularFont)
                                .setFontSize(10))
                        .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
                passengerTable.addCell(new Cell().add(new Paragraph(passenger.getPassengerType())
                                .setFont(regularFont).setFontSize(10))
                        .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
                passengerTable.addCell(new Cell().add(new Paragraph(String.valueOf(passenger.getId())).setFont(regularFont)
                                .setFontSize(10))
                        .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
                passengerTable.addCell(new Cell().add(new Paragraph(eticketDTO.getFlightCode()).setFont(regularFont)
                                .setFontSize(10))
                        .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
            });
            document.add(passengerTable);
            log.info("ETicket Generated");
        } catch (IOException e) {
            log.error("Error Generating ETicket", e);
            throw new FileHandlingException("Error Generating ETicket", e);
        }
    }

    public void generateBoardingPass(Document document, BoardingPassDTO boardingPassDTO, BoardingPass boardingPass) {
        log.info("Generating Boarding Pass");
        try {
            DateTimeFormatter formatterDay = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            String fontPath = "properties/LiberationSBold.ttf";
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H);
            PdfFont italicFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
            Color greyColor = new DeviceRgb(165, 165, 165);

            Table headerTable = new Table(new float[]{1, 1});
            headerTable.setWidth(UnitValue.createPercentValue(100));

            Text destination = new Text(boardingPassDTO.getDepartIata() + " - " + boardingPassDTO.getArrivalIata()).setFont(boldFont).setFontSize(16);
            Text flightCode = new Text(boardingPassDTO.getFlightCode()).setFont(boldFont).setFontSize(10);
            Cell headerL = new Cell().add(new Paragraph().add(destination).add("\n").add(flightCode)).setWidth(100)
                    .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT).setVerticalAlignment(VerticalAlignment.MIDDLE);
            Cell headerR = new Cell().add(new Paragraph(boardingPassDTO.getDepartDate().format(formatterDay)).setFont(boldFont).setFontSize(18)).setWidth(120)
                    .setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE);
            headerTable.addCell(headerL);
            headerTable.addCell(headerR);

            document.add(headerTable);
            document.add(new LineSeparator(new SolidLine()));
            document.add(new Paragraph());

            Passenger passenger = boardingPass.getPassenger();
            String fullName = passenger.getFirstName() + " " + passenger.getLastName();
            LocalTime boardingTime = boardingPassDTO.getDepartureTime().minusMinutes(30);
            String seatAssignment = boardingPass.getSeatAvailability().getSeatColumn()
                    + boardingPass.getSeatAvailability().getSeatRow();

            Text passengerName = new Text(fullName).setFont(boldFont).setFontSize(16);
            Text passengerType = new Text(" " + passenger.getPassengerType()).setFont(italicFont).setFontSize(10);
            Paragraph passengers = new Paragraph().add(passengerName).add(passengerType).setTextAlignment(TextAlignment.LEFT);
            document.add(passengers);

            Table detailTicket = new Table(3);
            detailTicket.setWidth(UnitValue.createPercentValue(100));

            detailTicket.addCell(new Cell().add(new Paragraph()
                            .add(new Text("CLASS").setFont(boldFont).setFontSize(8))
                            .add(new Text("\n" + boardingPassDTO.getSeatClass().name()).setFont(regularFont).setFontSize(12).setFontColor(greyColor)))
                    .setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            detailTicket.addCell(new Cell().add(new Paragraph(" ")).setBorder(Border.NO_BORDER));
            detailTicket.addCell(new Cell().add(new Paragraph()
                            .add(new Text("BOARDING").setFont(boldFont).setFontSize(8))
                            .add(new Text("\n" + boardingTime).setFont(regularFont).setFontSize(12).setFontColor(greyColor)))
                    .setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));

            detailTicket.addCell(new Cell().add(new Paragraph()
                            .add(new Text("DEPARTURE").setFont(boldFont).setFontSize(8))
                            .add(new Text("\n" + boardingPassDTO.getDepartureTime()).setFont(regularFont).setFontSize(12).setFontColor(greyColor)))
                    .setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            detailTicket.addCell(new Cell().add(new Paragraph(" ")).setBorder(Border.NO_BORDER));
            detailTicket.addCell(new Cell().add(new Paragraph()
                            .add(new Text("ARRIVAL").setFont(boldFont).setFontSize(8))
                            .add(new Text("\n" + boardingPassDTO.getArrivalTime()).setFont(regularFont).setFontSize(12).setFontColor(greyColor)))
                    .setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));

            detailTicket.addCell(new Cell().add(new Paragraph()
                            .add(new Text("SEAT ASSIGNMENT").setFont(boldFont).setFontSize(8))
                            .add(new Text("\n" + seatAssignment).setFont(regularFont).setFontSize(12).setFontColor(greyColor)))
                    .setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            detailTicket.addCell(new Cell().add(new Paragraph(" ")).setBorder(Border.NO_BORDER));
            detailTicket.addCell(new Cell().add(new Paragraph()
                            .add(new Text("TERMINAL/GATE").setFont(boldFont).setFontSize(8))
                            .add(new Text("\n1/4").setFont(regularFont).setFontSize(12).setFontColor(greyColor)))
                    .setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));

            document.add(detailTicket);
            log.info("Boarding Pass Generated");
        } catch (IOException e) {
            log.error("Error Generating Boarding Pass", e);
            throw new FileHandlingException("Error Generating Boarding Pass", e);
        }
    }
}
