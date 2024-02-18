package com.example.wefly_app.test;

import com.example.wefly_app.WeFlyApplication;
import com.example.wefly_app.entity.BoardingPass;
import com.example.wefly_app.entity.ETicket;
import com.example.wefly_app.entity.Passenger;
import com.example.wefly_app.entity.SeatAvailability;
import com.example.wefly_app.repository.BoardingPassRepository;
import com.example.wefly_app.repository.ETicketRepository;
import com.example.wefly_app.request.checkin.BoardingPassDTO;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
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
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class PdfMaker {

    public static void main(String[] args) throws IOException {
//         Start the Spring application context
        ConfigurableApplicationContext context = SpringApplication.run(WeFlyApplication.class, args);

//        TransactionImpl transactionService = context.getBean(TransactionImpl.class);
        TransactionServiceTest transactionServiceTest = context.getBean(TransactionServiceTest.class);
//        Transaction transaction = transactionServiceTest.getInvoiceDTO(29L);
//        transactionService.generateInvoice(transaction);
//
//
//        InvoiceDTO invoiceDTO = transactionService.getInvoiceDTO(29L);
//
//
//        List<Passenger> passengers = transactionServiceTest.getPassengerList(4L);
//
        ETicketRepository eticketRepository = context.getBean(ETicketRepository.class);
        BoardingPassRepository boardingPassRepository = context.getBean(BoardingPassRepository.class);

        ETicket eTicket = eticketRepository.findById(1L).get();
        List<Passenger> passengers = transactionServiceTest.getPassengerList(eTicket.getTransaction().getId());
        List<SeatAvailability> listAvailableSeat = boardingPassRepository
                .findAvailableSeats(eTicket.getTransactionDetail().getFlightClass().getId());
        BoardingPassDTO boardingPassDTO = boardingPassRepository.findFlightDetailsByTransactionDetailId(eTicket.getTransactionDetail().getId());
//        DateTimeFormatter formatterDay = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
//        DateTimeFormatter formatterStandard = DateTimeFormatter.ofPattern("dd MMMM yyyy");
//        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm");


        String path = "boarding-pass/boardingPass.pdf";
        PdfWriter writer = new PdfWriter(path);
        PdfDocument pdf = new PdfDocument(writer);
        try (Document document = new Document(pdf, PageSize.A6)){
            AtomicInteger count = new AtomicInteger(0);
            passengers.forEach(passenger -> {
                if (count.get() > 0) document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                BoardingPass boardingPass = new BoardingPass();
                SeatAvailability seatAvailability = listAvailableSeat.get(count.get());
                seatAvailability.setAvailable(false);
                boardingPass.setPassenger(passenger);
                boardingPass.setETicket(eTicket);
                boardingPass.setSeatAvailability(seatAvailability);
                testLoop(document, boardingPassDTO, boardingPass);
                count.getAndIncrement();
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("Invoice generated");

    }

    public static void testLoop(Document document, BoardingPassDTO boardingPassDTO, BoardingPass boardingPass) {
        try {
            DateTimeFormatter formatterDay = DateTimeFormatter.ofPattern("dd MMMM yyyy");
            String fontPath = "boarding-pass/properties/LiberationSBold.ttf";
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
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}

