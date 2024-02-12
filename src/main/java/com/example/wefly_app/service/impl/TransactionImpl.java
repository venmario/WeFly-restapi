package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.*;
import com.example.wefly_app.repository.*;
import com.example.wefly_app.request.transaction.InvoiceDTO;
import com.example.wefly_app.request.transaction.MidtransRequestModel;
import com.example.wefly_app.request.transaction.MidtransResponseModel;
import com.example.wefly_app.request.transaction.TransactionSaveModel;
import com.example.wefly_app.service.CheckinService;
import com.example.wefly_app.service.TransactionService;
import com.example.wefly_app.util.FileStorageProperties;
import com.example.wefly_app.util.SimpleStringUtils;
import com.example.wefly_app.util.TemplateResponse;
import com.example.wefly_app.util.exception.FileStorageException;
import com.example.wefly_app.util.exception.IncorrectUserCredentialException;
import com.example.wefly_app.util.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionImpl implements TransactionService {
    private final Path fileStorageLocation;
//    @Autowired
//    public TransactionImpl (FileStorageProperties fileStorageProperties) {
//        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
//                .toAbsolutePath().normalize();
//        try {
//            Files.createDirectories(this.fileStorageLocation);
//        } catch (Exception ex) {
//            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
//        }
//    }
    private final String serverKey;
    public final TransactionRepository transactionRepository;
    public final UserRepository userRepository;
    public final FlightClassRepository flightClassRepository;
    public final TemplateResponse templateResponse;
    public final SimpleStringUtils simpleStringUtils;
    public final BankRepository bankRepository;
    public final PaymentRepository paymentRepository;
    private final ETicketRepository eticketRepository;
    private final CheckinService checkinService;


    @Autowired
    public TransactionImpl (@Value("${midtrans.server-key}") String serverKey,
                            TransactionRepository transactionRepository, UserRepository userRepository,
                            FlightClassRepository flightClassRepository, TemplateResponse templateResponse,
                            SimpleStringUtils simpleStringUtils, BankRepository bankRepository, PaymentRepository paymentRepository,
                            FileStorageProperties fileStorageProperties, ETicketRepository eticketRepository,
                            CheckinService checkinService) {
        this.serverKey = serverKey;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.flightClassRepository = flightClassRepository;
        this.templateResponse = templateResponse;
        this.simpleStringUtils = simpleStringUtils;
        this.bankRepository = bankRepository;
        this.paymentRepository = paymentRepository;
        this.eticketRepository = eticketRepository;
        this.checkinService = checkinService;
        this.fileStorageLocation = Paths.get(fileStorageProperties.getInvoiceDir())
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Transactional
    @Override
    public Map<Object, Object> save(TransactionSaveModel request) throws IOException {
        try {
            log.info("save transaction");
            if (request.getInfantPassenger() > request.getAdultPassenger()) {
                throw new ValidationException("The number of infant passengers cannot exceed the total number of adult passengers");
            }
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attributes.getRequest().getAttribute("userId");
            Optional<User> checkDataDBUser = userRepository.findById(userId);
            if (!checkDataDBUser.isPresent()) {
                log.error("Save transaction error : unidentified user");
                throw new IncorrectUserCredentialException("unidentified token user");
            }

            ModelMapper modelMapper = new ModelMapper();
            Orderer orderer = modelMapper.map(request.getOrderer(), Orderer.class);

            Transaction transaction = new Transaction();
            List<TransactionDetail> transactionDetails = request.getTransactionDetails().stream()
                    .map(transactionDetail -> {
                                TransactionDetail transactionDetail1 = new TransactionDetail();
                                FlightClass checkDataDBFlightClass = flightClassRepository.findById(transactionDetail.getFlightClassId())
                                                .orElseThrow(() -> new EntityNotFoundException("Flight not found"));
                                transactionDetail1.setFlightClass(checkDataDBFlightClass);
                                transactionDetail1.setTotalPriceAdult(checkDataDBFlightClass.getBasePriceAdult().multiply(
                                        BigDecimal.valueOf(request.getAdultPassenger())
                                ));
                                transactionDetail1.setTotalPriceChild(checkDataDBFlightClass.getBasePriceChild().multiply(
                                        BigDecimal.valueOf(request.getChildPassenger())
                                ));
                                transactionDetail1.setTotalPriceInfant(checkDataDBFlightClass.getBasePriceInfant().multiply(
                                        BigDecimal.valueOf(request.getInfantPassenger())
                                ));
                                transactionDetail1.setTransaction(transaction);
                                checkDataDBFlightClass.setAvailableSeat(checkDataDBFlightClass.getAvailableSeat() - (request.getAdultPassenger() + request.getChildPassenger()));
                                flightClassRepository.save(checkDataDBFlightClass);
                                return transactionDetail1;
                            })
                    .collect(Collectors.toList());
            BigDecimal totalPrice = transactionDetails.stream()
                    .map(transactionDetail -> transactionDetail.getTotalPriceAdult()
                            .add(transactionDetail.getTotalPriceChild())
                            .add(transactionDetail.getTotalPriceInfant())
                    )
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            transaction.setSeatClass(transactionDetails.get(0).getFlightClass().getSeatClass());
            transaction.setTransactionDetails(transactionDetails);
            transaction.setTotalPrice(totalPrice);
            transaction.setOrderer(orderer);
            transaction.setUser(checkDataDBUser.get());
            transaction.setAdultPassenger(request.getAdultPassenger());
            transaction.setChildPassenger(request.getChildPassenger());
            transaction.setInfantPassenger(request.getInfantPassenger());
            List<Passenger> passengers = request.getPassengers().stream()
                            .map(passenger -> {
                                Passenger passenger1 = new Passenger();
                                passenger1.setFirstName(passenger.getFirstName());
                                passenger1.setLastName(passenger.getLastName());
                                passenger1.setDateOfBirth(passenger.getDateOfBirth());
                                passenger1.setNationality(passenger.getNationality());
                                passenger1.setPassengerType(passenger.getPassengerType());
                                passenger1.setTransaction(transaction);
                                return passenger1;
                            })
                    .collect(Collectors.toList());
            transaction.setPassengers(passengers);
            transaction.setPayment(new Payment());
            Transaction transactionSaved = transactionRepository.save(transaction);
            Map<String, Object> response = midtransRequest(transaction);
            Payment payment = transactionSaved.getPayment();
            payment.setToken((String) response.get("token"));
            payment.setExpiryTime(LocalDateTime.now().plusHours(1));
            paymentRepository.save(payment);
            response.put("transaction", transactionSaved);
            log.info("save transaction success, proceed to payment");
            return templateResponse.success(response);
        } catch (Exception e) {
            log.error("Save transaction error ", e);
            throw e;
        }
    }

    public Map<String, Object> midtransRequest (Transaction request) throws IOException {
        try {
            log.info("midtrans request");
            MidtransRequestModel midtransRequest = getMidtransRequestModel(request);

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(midtransRequest);

            String url = "https://app.sandbox.midtrans.com/snap/v1/transactions";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("Content-Type", "application/json");
            String encodedAuth = Base64.getEncoder().encodeToString(serverKey.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {});
            log.info("midtrans request success");
            return response.getBody();
        } catch (Exception e) {
            log.error("midtrans request error ", e);
            throw e;
        }
    }

    @NotNull
    private static MidtransRequestModel getMidtransRequestModel(Transaction request) {
        Map<String, Object> transactionDetails = new HashMap<>();
        transactionDetails.put("order_id", request.getId());
        transactionDetails.put("gross_amount", request.getTotalPrice());

        Map<String, Object> customerDetails = new HashMap<>();
        customerDetails.put("first_name", request.getOrderer().getFirstName());
        customerDetails.put("last_name", request.getOrderer().getLastName());
        customerDetails.put("email", request.getOrderer().getEmail());
        customerDetails.put("phone", request.getOrderer().getPhoneNumber());

        MidtransRequestModel midtransRequest = new MidtransRequestModel();
        midtransRequest.setTransactionDetails(transactionDetails);
        midtransRequest.setCustomerDetails(customerDetails);
        return midtransRequest;
    }

    @Transactional
    public Map<Object, Object> midtransGetResponse (MidtransResponseModel request) {
        log.info("midtrans response");
        try {
            if (!midtransValidator(request)) {
                log.error("midtrans response error : validation failed");
                throw new ValidationException("validation failed");
            }
            Optional<Transaction> checkDataDBTransaction = transactionRepository.findById(request.getOrderId());
            if (!checkDataDBTransaction.isPresent()) {
                log.error("midtrans response error : transaction not found");
                throw new EntityNotFoundException("transaction not found");
            }
            Payment payment = checkDataDBTransaction.get().getPayment();
            payment.setSettlementTime(request.getSettlementTime());
            payment.setExpiryTime(request.getExpiryTime());
            payment.setGrossAmount(new BigDecimal(request.getGrossAmount()));
            switch (request.getPaymentType()) {
                case "credit_card":
                    if (request.getCardType().equals("debit")) {
                        payment.setPaymentType("Debit Card");
                    }
                    payment.setIssuer(request.getBank());
                    payment.setPaymentType("Credit Card");
                    break;
                case "gopay" :
                case "shopeepay":
                    payment.setIssuer(request.getPaymentType());
                    payment.setPaymentType("E-Wallet");
                    break;
                case "qris":
                    payment.setIssuer(request.getAcquirer());
                    payment.setPaymentType("QRIS");
                    break;
                case "bank_transfer":
                    if (!request.getVaNumbers().isEmpty()) {
                        payment.setIssuer((String) request.getVaNumbers().get(0).get("bank"));
                    } else {
                        payment.setIssuer("Permata");
                    }
                    payment.setPaymentType("Virtual Account");
                    break;
                case "echannel":
                    payment.setIssuer("Mandiri");
                    payment.setPaymentType("Virtual Account");
                    break;
                case "cstore":
                    payment.setIssuer(request.getStore());
                    payment.setPaymentType("Convenience Store");
                    break;
                default:
                    payment.setIssuer("unknown");
            }
            if (request.getTransactionStatus().matches("settlement|capture")) {
                payment.setTransactionStatus("PAID");
                payment = generateInvoice(payment);
                checkinService.save(payment.getTransaction());
                checkinService.generateETicket(payment.getTransaction());
            } else {
                payment.setTransactionStatus(request.getTransactionStatus());
            }
            log.info("midtrans response update success");
            return templateResponse.success(paymentRepository.save(payment));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Resource getInvoice(Long transactionId) {
        try {
            log.info("get invoice");
            ServletRequestAttributes attribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attribute.getRequest().getAttribute("userId");
            log.info("Update User : " + userId);
            Optional<User> checkDataDBUser = userRepository.findById(userId);
            log.info("Update User : " + userId);
            if (!checkDataDBUser.isPresent()) {
                throw new IncorrectUserCredentialException("unidentified token user");
            }
            Optional<Transaction> checkDataDBTransaction = transactionRepository.findById(transactionId);
            if (checkDataDBTransaction.get().getUser().getId() != userId)
                throw new ValidationException("transaction not found");
            Path filePath = this.fileStorageLocation.resolve(checkDataDBTransaction.get().getPayment().getInvoice()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) throw new FileStorageException("File not found " + filePath);
            else {
                log.info("get invoice succeed");
                return resource;
            }
        } catch (MalformedURLException ex) {
            log.error("get payment proof error ", ex);
            throw new FileStorageException("File not found ", ex);
        }

    }

    public boolean midtransValidator (MidtransResponseModel request) throws NoSuchAlgorithmException {
        log.info("validating midtrans response");
        String signatureKey = request.getOrderId().toString() + request.getStatusCode().toString() +
                request.getGrossAmount() + serverKey;
        StringBuilder generatedKey = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(signatureKey.getBytes());
            for (byte b : hash) {
                generatedKey.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-512 algorithm not found");
            throw e;
        }
        log.info("midtrans response validation success");
        if (Objects.equals(generatedKey.toString(), request.getSignatureKey())) {
            return request.getFraudStatus().equals("accept");
        }
        return false;
    }

    @Override
    public Map<Object, Object> delete(Long request) {
        try {
            log.info("delete transaction");
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attributes.getRequest().getAttribute("userId");

            Optional<User> checkDataDBUser = userRepository.findById(userId);
            if (!checkDataDBUser.isPresent()) {
                log.error("Save transaction error : unidentified user");
                throw new IncorrectUserCredentialException("unidentified token user");
            }
            Optional<Transaction> checkDataDBTransaction = transactionRepository.findById(request);
            if (checkDataDBTransaction.get().getUser().getId() != userId) throw new ValidationException("transaction not found");
            checkDataDBTransaction.get().setDeletedDate(new Date());

            log.info("transaction deleted");
            return templateResponse.success(checkDataDBTransaction.get());
        } catch (Exception e) {
            log.error("Transaction deletion error ", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> getById(Long request) {
        try {
            log.info("get transaction");
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attributes.getRequest().getAttribute("userId");
            Optional<User> checkDataDBUser = userRepository.findById(userId);
            if (!checkDataDBUser.isPresent()) {
                log.error("Save transaction error : unidentified user");
                throw new IncorrectUserCredentialException("unidentified token user");
            }
            Optional<Transaction> checkDataDBTransaction = transactionRepository.findById(request);
            if (checkDataDBTransaction.get().getUser().getId() != userId) throw new ValidationException("transaction not found");

            log.info("get transaction succeed");
            return templateResponse.success(checkDataDBTransaction.get());
        } catch (Exception e) {
            log.error("get transaction error ", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> getAll(int page, int size, String orderBy, String orderType,
                                      String startDate, String endDate, String paymentStatus,
                                      String exceptionStatus) {
        try {
            log.info("get all transaction");
            ServletRequestAttributes attribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attribute.getRequest().getAttribute("userId");
            Optional<User> checkDataDBUser = userRepository.findById(userId);
            log.info("Update User : " + userId);
            if (!checkDataDBUser.isPresent()) {
                throw new IncorrectUserCredentialException("unidentified token user");
            }

            Pageable pageable = simpleStringUtils.getShort(orderBy, orderType, page, size);
            Specification<Transaction> specification = ((root, criteriaQuery, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
                if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    LocalDate start = LocalDate.parse(startDate, formatter);
                    LocalDate end = LocalDate.parse(endDate, formatter);
                    predicates.add(criteriaBuilder.between(root.get("createdDate").as(LocalDate.class), start, end));
                    log.info("filtering by date range");
                }
                if (paymentStatus != null && !paymentStatus.isEmpty()) {
                    predicates.add(criteriaBuilder.equal(root.get("payment").get("transactionStatus"), paymentStatus.toUpperCase()));
                    log.info("filtering by status");
                }
                if (exceptionStatus != null && !exceptionStatus.isEmpty()) {
                    predicates.add(criteriaBuilder.notEqual(root.get("payment").get("transactionStatus"), exceptionStatus.toUpperCase()));
                    log.info("filtering by exception status");
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            });

            Page<Transaction> list = transactionRepository.findAll(specification, pageable);
            log.info("get all transaction succeed");
            return templateResponse.success(list);
        } catch (Exception e) {
            log.error("get all transaction error ", e);
            throw e;
        }
    }


    @Transactional
    public Payment generateInvoice(Payment request) throws IOException {
        log.info("generate invoice");
        Transaction transaction = request.getTransaction();
        String fileName = "invoice-" + transaction.getId() + ".pdf";
        String path = "invoice/" + fileName;
        PdfWriter writer = new PdfWriter(path);
        PdfDocument pdf = new PdfDocument(writer);
        try (Document document = new Document(pdf, PageSize.A4)) {
            InvoiceDTO invoiceDTO = getInvoiceDTO(transaction);
            ImageData imageData = ImageDataFactory.create("invoice/properties/logo.png");
            Image image = new Image(imageData).setHeight(80).setWidth(80)
                    .setHorizontalAlignment(HorizontalAlignment.RIGHT);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy - HH:mm");
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            Table table = new Table(new float[]{1, 1});
            table.setWidth(UnitValue.createPercentValue(100)); // Set table width to 100% of the page width

// First cell with content aligned to the left
            Cell leftCell = new Cell().add(new Paragraph("Invoice" +
                            "\n" + "Order Id: " + transaction.getId())
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
        } catch (Exception e) {
            log.error("Error while generating invoice", e);
        }
        log.info("generate invoice success");
        request.setInvoice(fileName);
        return request;
    }

    @Transactional
    public InvoiceDTO getInvoiceDTO(Transaction request) {
        Optional<Transaction> checkDataDBTransaction = transactionRepository.findById(request.getId());
        if (!checkDataDBTransaction.isPresent()) {
            throw new EntityNotFoundException("transaction not found");
        }
        Transaction transaction = checkDataDBTransaction.get();
//        Hibernate.initialize(transaction.getTransactionDetails());
        InvoiceDTO invoiceDTO = new InvoiceDTO();
        invoiceDTO.setOrderer(transaction.getOrderer());
        invoiceDTO.setPayment(transaction.getPayment());
        invoiceDTO.setTransactionDetails(transaction.getTransactionDetails());
        Map<String, Integer> transactionMap = new LinkedHashMap<>();
        transactionMap.put("Adult", transaction.getAdultPassenger());
        transactionMap.put("Child", transaction.getChildPassenger());
        transactionMap.put("Infant", transaction.getInfantPassenger());
        invoiceDTO.setTransaction(transactionMap);
        return invoiceDTO;
    }


//    @Override
//    public Map<Object, Object> getAllBank(int page, int size, String orderBy, String orderType) {
//        try {
//            log.info("get all bank");
//            Pageable pageable = simpleStringUtils.getShort(orderBy, orderType, page, size);
//            Specification<Bank> specification = ((root, criteriaQuery, criteriaBuilder) -> {
//                List<Predicate> predicates = new ArrayList<>();
//                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//            });
//            Page<Bank> list = bankRepository.findAll(specification, pageable);
//            Map<Object, Object> map = new HashMap<>();
//            map.put("data", list);
//            log.info("get all bank succeed");
//            return map;
//
//        } catch (Exception e) {
//            log.error("get all bank error ", e);
//            throw e;
//        }
//    }

//    @Override
//    public Map<Object, Object> savePayment(PaymentRegisterModel request) {
//        try {
//            log.info("save payment");
//            Optional<Transaction> checkDataDBTransaction = transactionRepository.findById(request.getTransactionId());
//            if (!checkDataDBTransaction.isPresent()) throw new EntityNotFoundException("transaction not found");
//            Optional<Bank> checkDataDBBank = bankRepository.findById(request.getBankId());
//            if (!checkDataDBBank.isPresent()) throw new EntityNotFoundException("bank not found");
//            Payment payment = new Payment();
//            payment.setBank(checkDataDBBank.get());
//            payment.setTransaction(checkDataDBTransaction.get());
//            log.info("save payment succeed");
//            return templateResponse.success(paymentRepository.save(payment));
//        } catch (Exception e) {
//            log.error("save payment error ", e);
//            throw e;
//        }
//    }

//    @Override
//    public Map<Object, Object> savePaymentProof(MultipartFile file, Long paymentId) throws IOException {
//        ServletRequestAttributes attribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
//        Long userId = (Long) attribute.getRequest().getAttribute("userId");
//        Optional<User> checkDataDBUser = userRepository.findById(userId);
//        log.info("Update User : " + userId);
//        if (!checkDataDBUser.isPresent()) {
//            throw new IncorrectUserCredentialException("unidentified token user");
//        }
//        Optional<Payment> checkDataDBPayment = paymentRepository.findById(paymentId);
//        if (!checkDataDBPayment.isPresent()) throw new EntityNotFoundException("payment not found");
//        Date date = new Date();
//        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmss");
//        String strDate = formatter.format(date);
//
//        String nameFormat= file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") );
//        if(nameFormat.isEmpty()){
//            nameFormat = ".png";
//        }
//        String userFolder = BASE_UPLOAD_FOLDER + "user_" + checkDataDBUser.get().getId() + "/";
//        Path userFolderPath = Paths.get(userFolder);
//        if (Files.notExists(userFolderPath)) {
//            Files.createDirectories(userFolderPath);
//        }
//        String fileName = strDate + nameFormat;
//        String filePath = userFolder + fileName;
//        Path to = Paths.get(filePath);
//        Map<Object, Object> map = new HashMap<>();
//
//        try {
//            Files.copy(file.getInputStream(), to);
//        } catch (Exception e) {
//            log.error("Error while saving file", e);
//            map.put("file name", fileName);
//            map.put("file download uri", null);
//            map.put("file type", file.getContentType());
//            map.put("file size", file.getSize());
//            map.put("status", e.getMessage());
//            return map;
//        }
//        checkDataDBPayment.get().setPaymentProof(fileName);
//        checkDataDBPayment.get().setStatus(PaymentStatus.PROCESSING);
//        Object obj = paymentRepository.save(checkDataDBPayment.get());
//
//        map.put("file name", fileName);
//        map.put("file download uri", null);
//        map.put("file type", file.getContentType());
//        map.put("file size", file.getSize());
//        map.put("payment", obj);
//        map.put("status", "success");
//        return map;
//    }
//
//    @Override
//    public Resource getPaymentProof(Long paymentId) {
//        try {
//            log.info("get payment proof");
//            ServletRequestAttributes attribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
//            Long userId = (Long) attribute.getRequest().getAttribute("userId");
//            Optional<User> checkDataDBUser = userRepository.findById(userId);
//            log.info("Update User : " + userId);
//            if (!checkDataDBUser.isPresent()) {
//                throw new IncorrectUserCredentialException("unidentified token user");
//            }
//            Optional<Payment> checkDataDBPayment = paymentRepository.findById(paymentId);
//            if (!checkDataDBPayment.isPresent()) throw new EntityNotFoundException("payment not found");
//            String userFolder = "user_" + checkDataDBPayment.get().getTransaction().getUser().getId() + "/";
//            Path filePath = this.fileStorageLocation.resolve(userFolder + checkDataDBPayment.get().getPaymentProof()).normalize();
//            Resource resource = new UrlResource(filePath.toUri());
//            if (!resource.exists()) throw new FileStorageException("File not found " + filePath);
//            else {
//                log.info("get payment proof succeed");
//                return resource;
//            }
//        } catch (MalformedURLException ex) {
//            log.error("get payment proof error ", ex);
//            throw new FileStorageException("File not found " ,ex);
//        }
//    }


}
