package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.*;
import com.example.wefly_app.repository.BoardingPassRepository;
import com.example.wefly_app.repository.ETicketRepository;
import com.example.wefly_app.repository.TransactionRepository;
import com.example.wefly_app.repository.UserRepository;
import com.example.wefly_app.request.checkin.BoardingPassDTO;
import com.example.wefly_app.request.checkin.CheckinRequestModel;
import com.example.wefly_app.request.checkin.ETicketDTO;
import com.example.wefly_app.service.CheckinService;
import com.example.wefly_app.util.*;
import com.example.wefly_app.util.exception.FileStorageException;
import com.example.wefly_app.util.exception.ValidationException;
import com.itextpdf.io.font.PdfEncodings;
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
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CheckInServiceImpl implements CheckinService {
    private final SimpleStringUtils simpleStringUtils;
    private final ETicketRepository eticketRepository;
    private final TemplateResponse templateResponse;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final Map<String, Path> fileStorageLocation = new HashMap<>();
    private final BoardingPassRepository boardingPassRepository;
    private final EmailTemplate emailTemplate;
    private final EmailSender emailSender;
    private final String homePageUrl;

    public CheckInServiceImpl(SimpleStringUtils simpleStringUtils, ETicketRepository eticketRepository,
                              TemplateResponse templateResponse, FileStorageProperties fileStorageProperties,
                              UserRepository userRepository, TransactionRepository transactionRepository,
                              BoardingPassRepository boardingPassRepository, EmailSender emailSender,
                              EmailTemplate emailTemplate, @Value("${frontend.homepage.url}")String homePageUrl) {
        this.simpleStringUtils = simpleStringUtils;
        this.eticketRepository = eticketRepository;
        this.templateResponse = templateResponse;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.boardingPassRepository = boardingPassRepository;
        this.emailSender = emailSender;
        this.emailTemplate = emailTemplate;
        this.homePageUrl = homePageUrl;
        Path eticket = Paths.get(fileStorageProperties.getETicketDir()).toAbsolutePath().normalize();
        Path boardingPass = Paths.get(fileStorageProperties.getBoardingPassDir()).toAbsolutePath().normalize();
        this.fileStorageLocation.put("eticket", eticket);
        this.fileStorageLocation.put("boardingPass", boardingPass);
        try {
            Files.createDirectories(eticket);
            Files.createDirectories(boardingPass);
        } catch (Exception e) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", e);
        }
    }


    @Override
    public Resource getETicket(Long transactionId) {
        log.info("Get ETicket");
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attributes.getRequest().getAttribute("userId");
            Optional<User> checkDBUser = userRepository.findById(userId);
            if (!checkDBUser.isPresent()) {
                log.info("User Not Found");
                throw new EntityNotFoundException("User Not Found");
            }
            Optional<Transaction> checkDBTransaction = transactionRepository.findById(transactionId);
            if (!checkDBTransaction.isPresent()) {
                log.info("Unauthorized Access");
                throw new EntityNotFoundException("Transaction Not Found");
            }
            Path filePath = this.fileStorageLocation.get("eticket").resolve(checkDBTransaction.get().getEticketFile()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                log.info("File Not Found");
                throw new FileNotFoundException("File Not Found " + filePath);
            } else {
                log.info("ETicket Found");
                return resource;
            }
        } catch (MalformedURLException e) {
            log.error("get ETicket Error: " + e.getMessage());
            throw new FileStorageException("File Not Found ", e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Resource getBoardingPass(Long eticketId) {
        log.info("Get Boarding Pass");
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attributes.getRequest().getAttribute("userId");
            Optional<User> checkDBUser = userRepository.findById(userId);
            if (!checkDBUser.isPresent()) {
                log.info("User Not Found");
                throw new EntityNotFoundException("User Not Found");
            }
            Optional<ETicket> checkDBETicket = eticketRepository.findById(eticketId);
            if (!checkDBETicket.isPresent()) {
                log.info("Boarding Pass not found");
                throw new EntityNotFoundException("Boarding Pass not found");
            }
            if (checkDBETicket.get().getTransaction().getUser().getId() != userId){
                log.info("Unauthorized");
                throw new ValidationException("Unauthorized");
            }
            Path filePath = this.fileStorageLocation.get("boardingPass").resolve(checkDBETicket.get().getBoardingPassFile())
                    .normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()){
                log.info("File not found");
                throw new FileNotFoundException("Boarding pass file not found " + filePath);
            } else {
                log.info("Boarding Pass Found");
                return resource;
            }
        } catch (MalformedURLException e) {
            log.error("get boarding pass error: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    @Transactional
    public void saveETicket(Transaction request) {
        log.info("Save New ETicket");
        List<TransactionDetail> transactionDetails = request.getTransactionDetails();
        List<ETicket> eTickets;
        List<Passenger> passengers = request.getPassengers();
        String fileName = "e-ticket-" + request.getId() + ".pdf";
        String path = "e-ticket/" + fileName;
        PdfWriter writer;
        try {
            writer = new PdfWriter(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File Not Found: " + e.getMessage());
        }
        PdfDocument pdf = new PdfDocument(writer);
        AtomicInteger count = new AtomicInteger(0);
        try (Document document = new Document(pdf, PageSize.A4)) {
            eTickets = transactionDetails.stream()
                    .map(transactionDetail -> {
                        if (count.get() > 0) document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                        ETicket eticket = new ETicket();
                        eticket.setTransaction(request);
                        eticket.setTransactionDetail(transactionDetail);
                        eticket.setBookCode(simpleStringUtils.randomStringChar(6));
                        generateETicket(document, eticket, passengers);
                        count.getAndIncrement();
                        return eticket;
                    }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("failed to save ETicket");
            throw new RuntimeException("File Not Found: " + e.getMessage());
        }
        if (eTickets.isEmpty()){
            log.error("Failed to generate E-Ticket");
            throw new EntityNotFoundException("Failed to generate E-Ticket");
        }
        request.setEticketFile(fileName);
        request.setEtickets(eTickets);
        log.info("ETicket Save Success");
        transactionRepository.save(request);
    }

    @Transactional
    public void generateETicket(Document document, ETicket eTicket, List<Passenger> passengers){
        log.info("Generate ETicket");
        try {
            ETicketDTO eticketDTO = eticketRepository.getETicketDTO(eTicket.getTransactionDetail().getId());
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
            Text bookingCode = new Text(eTicket.getBookCode()).setFont(boldFont).setFontSize(20);
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
            ticketTable.addHeaderCell(new Cell().add(new Paragraph(eTicket.getBookCode()).setFont(regularFont).setFontSize(10)
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

            passengers.forEach(passenger -> {
                String fullName = passenger.getFirstName() + " " + passenger.getLastName();
                passengerTable.addCell(new Cell().add(new Paragraph(String.valueOf(passengers.indexOf(passenger) + 1))
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
        } catch (IOException e) {
            log.info("Generate ETicket Error: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public Map<Object, Object> checkIn(CheckinRequestModel request) {
        try {
            ETicket eticket = eticketRepository.getETicketByBookCode(request.getBookingCode());
            log.info("Check ETicket Data");
            if (eticket == null) {
                throw new EntityNotFoundException("ETicket Not Found");
            }
            log.info("Match Orderer Data");
            if (!request.getOrdererLastName().equals(eticket.getTransaction().getOrderer().getLastName())){
                throw new EntityNotFoundException("Orderer Not Found");
            } else {
                log.info("Generate Boarding Pass");
                if (eticket.getBoardingPassFile() == null) {
                    eticket = saveBoardingPassEntity(eticket);
                    eticketRepository.save(eticket);
                }
                log.info("Boarding Pass Generated");
                String template = emailTemplate.getPaymentProofTemplate();
                String message = "Here we attached your boarding pass for all the passengers. " +
                        "Thank you for using our services, it is a pleasure to serve you. " +
                        "Enjoy your flight, hope you reach your destination safely.";
                String thankMessage = "Best Regards,";
                template = template.replaceAll("\\{\\{USERNAME}}", eticket.getTransaction().getUser().getFullName());
                template = template.replaceAll("\\{\\{HOMEPAGE_URL}}", homePageUrl);
                template = template.replaceAll("\\{\\{MESSAGE}}", message);
                template = template.replaceAll("\\{\\{THANK_MESSAGE}}", thankMessage);
                String boardingPass = String.valueOf(this.fileStorageLocation.get("boardingPass").resolve(eticket.getBoardingPassFile()));
                List<String> filePaths = new ArrayList<>();
                filePaths.add(boardingPass);
                emailSender.sendAsync(eticket.getTransaction().getUser().getUsername(), "Boarding Pass", template, filePaths);
                return templateResponse.success("Check In Success, check your email for boarding pass");
            }
        } catch (Exception e) {
            log.error("Check In Error: " + e.getMessage());
            throw new RuntimeException("Check In Error: " + e.getMessage());
        }
    }

    @Transactional
    public ETicket saveBoardingPassEntity(ETicket request) throws FileNotFoundException {
        log.info("Save Boarding Pass");
        List<Passenger> passengers = request.getTransaction().getPassengers();
        List<SeatAvailability> listAvailableSeat = boardingPassRepository
                .findAvailableSeats(request.getTransactionDetail().getFlightClass().getId());
        BoardingPassDTO boardingPassDTO = boardingPassRepository
                .findFlightDetailsByTransactionDetailId(request.getTransactionDetail().getId());
        String fileName = "boarding-pass-" + request.getId() + ".pdf";
        String path = "boarding-pass/" + fileName;
        PdfWriter writer = new PdfWriter(path);
        PdfDocument pdf = new PdfDocument(writer);
        try (Document document = new Document(pdf, PageSize.A6)) {
            AtomicInteger count = new AtomicInteger(0);
            passengers.forEach(passenger -> {
                if (count.get() > 0) document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                BoardingPass boardingPass = new BoardingPass();
                SeatAvailability seatAvailability = listAvailableSeat.get(count.get());
                seatAvailability.setAvailable(false);
                boardingPass.setPassenger(passenger);
                boardingPass.setETicket(request);
                boardingPass.setSeatAvailability(seatAvailability);
                generateBoardingPass(document, boardingPassDTO, boardingPass);
                boardingPassRepository.save(boardingPass);
                count.getAndIncrement();
            });
        } catch (Exception e) {
            log.error("Save Boarding Pass Error");
            throw e;
        }
        request.setBoardingPassFile(fileName);
        return request;
    }

    public void generateBoardingPass(Document document, BoardingPassDTO boardingPassDTO, BoardingPass boardingPass) {
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
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public Resource getBoardingPass() {
//
//    }

//    public Transaction generateETicket(Transaction request) {
//        log.info("Generate ETicket");
//        try {
//            List<E> transactionDetails = request.getTransactionDetails();
//            for (TransactionDetail transactionDetail : transactionDetails) {
//                ETicket eticket = new ETicket();
//                eticket.setTransaction(request);
//                eticket.setTransactionDetail(transactionDetail);
//                eticket.setBookCode(simpleStringUtils.randomStringChar(6));
//                eticketRepository.save(eticket);
//            }
//            log.info("ETicket Generate Success");
//            return request;
//        } catch (Exception e) {
//            log.error("Generate ETicket Error: " + e.getMessage());
//            throw new RuntimeException("Generate ETicket Error: " + e.getMessage());
//        }
//    }
}
