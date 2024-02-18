package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.*;
import com.example.wefly_app.repository.*;
import com.example.wefly_app.request.transaction.ETicketDTO;
import com.example.wefly_app.request.transaction.InvoiceDTO;
import com.example.wefly_app.request.transaction.MidtransRequestModel;
import com.example.wefly_app.request.transaction.MidtransResponseModel;
import com.example.wefly_app.request.transaction.TransactionSaveModel;
import com.example.wefly_app.service.TransactionService;
import com.example.wefly_app.util.*;
import com.example.wefly_app.util.exception.FileHandlingException;
import com.example.wefly_app.util.exception.IncorrectUserCredentialException;
import com.example.wefly_app.util.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.properties.AreaBreakType;
import lombok.extern.slf4j.Slf4j;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionImpl implements TransactionService {
    private final Map<String, Path> fileStorageLocation = new HashMap<>();
    private final String serverKey;
    public final TransactionRepository transactionRepository;
    public final UserRepository userRepository;
    public final FlightClassRepository flightClassRepository;
    public final TemplateResponse templateResponse;
    public final SimpleStringUtils simpleStringUtils;
    public final PaymentRepository paymentRepository;
    private final EmailTemplate emailTemplate;
    private final EmailSender emailSender;
    private final String homePageUrl;
    private final ETicketRepository eTicketRepository;
    private final FileCreation fileCreation;

    @Autowired
    public TransactionImpl (@Value("${midtrans.server-key}") String serverKey,
                            TransactionRepository transactionRepository, UserRepository userRepository,
                            FlightClassRepository flightClassRepository, TemplateResponse templateResponse,
                            SimpleStringUtils simpleStringUtils, PaymentRepository paymentRepository,
                            FileStorageProperties fileStorageProperties, EmailTemplate emailTemplate,
                            EmailSender emailSender, ETicketRepository eTicketRepository,
                            @Value("${frontend.homepage.url}") String homePageUrl, FileCreation fileCreation) {
        this.serverKey = serverKey;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.flightClassRepository = flightClassRepository;
        this.templateResponse = templateResponse;
        this.simpleStringUtils = simpleStringUtils;
        this.paymentRepository = paymentRepository;
        this.emailTemplate = emailTemplate;
        this.emailSender = emailSender;
        this.homePageUrl = homePageUrl;
        this.eTicketRepository = eTicketRepository;
        this.fileCreation = fileCreation;
        Path eticket = Paths.get(fileStorageProperties.getETicketDir()).toAbsolutePath().normalize();
        Path paymenProof = Paths.get(fileStorageProperties.getPaymentProofDir()).toAbsolutePath().normalize();
        this.fileStorageLocation.put("eticket", eticket);
        this.fileStorageLocation.put("paymentProof", paymenProof);
        try {
            Files.createDirectories(eticket);
            Files.createDirectories(paymenProof);
        } catch (Exception ex) {
            throw new FileHandlingException("Could not create the directory where the uploaded files will be stored.", ex);
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
                payment = generatePaymentProof(payment);
                saveETicket(payment.getTransaction());
                sendEmailPaymentProofAndETicket(payment.getTransaction());
            } else if (request.getTransactionStatus().matches("expire")) {
                log.info("Reverting flight class seat available");
                int totalSeatBooked = checkDataDBTransaction.get().getAdultPassenger()
                        + checkDataDBTransaction.get().getChildPassenger();
                checkDataDBTransaction.get().getTransactionDetails()
                        .forEach(transactionDetail -> {
                            FlightClass flightClass = transactionDetail.getFlightClass();
                            flightClass.setAvailableSeat(flightClass.getAvailableSeat() + totalSeatBooked);
                            flightClassRepository.save(flightClass);
                        });
                log.info("Revert success");
                payment.setTransactionStatus(request.getTransactionStatus());
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
    public Map<Object, Object> getEticketResponse(Long transactionId){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        Long userId = (Long) attributes.getRequest().getAttribute("userId");
        Optional<User> checkDBUser = userRepository.findById(userId);
        if (!checkDBUser.isPresent()) {
            log.info("User Not Found");
            throw new EntityNotFoundException("User Not Found");
        }
        Optional<Transaction> checkDBTransaction = transactionRepository.findById(transactionId);
        if (!checkDBTransaction.isPresent() || checkDBTransaction.get().getUser().getId() != checkDBUser.get().getId()) {
            log.info("Unauthorized Access");
            throw new EntityNotFoundException("Transaction Not Found");
        }
        List<ETicketDTO> eTicketDTOList = checkDBTransaction.get().getEtickets().stream()
                .map(eTicket -> {
                    ETicketDTO eticketDTO = eTicketRepository.getETicketDTO(eTicket.getTransactionDetail().getId());
                    eticketDTO.setBookCode(eTicket.getBookCode());
                    return eticketDTO;
                }).collect(Collectors.toList());
        return templateResponse.success(eTicketDTOList);
    }

    @Override
    public Resource getPaymentProof(Long transactionId) {
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
            Path filePath = this.fileStorageLocation.get("paymentProof").resolve(checkDataDBTransaction.get().getPayment().getInvoice()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) throw new FileHandlingException("Payment Proof File not found " + filePath);
            else {
                log.info("get invoice succeed");
                return resource;
            }
        } catch (MalformedURLException ex) {
            log.error("get payment proof error ", ex);
            throw new FileHandlingException("Payment Proof File Path not found ", ex);
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

    public void sendEmailPaymentProofAndETicket(Transaction request) {
        log.info("Send Payment Proof and E-Ticket via Email");
        String template = emailTemplate.getPaymentProofTemplate();
        User user = request.getUser();
        String message = "We have received payment for your order. The Payment Proof and E-Ticket are attached below. " +
                "Thank your for using our services!";
        String thankMessage = "Thank You";
        template = template.replaceAll("\\{\\{USERNAME}}", user.getFullName());
        template = template.replaceAll("\\{\\{HOMEPAGE_URL}}", homePageUrl);
        template = template.replaceAll("\\{\\{MESSAGE}}", message);
        template = template.replaceAll("\\{\\{THANK_MESSAGE}}", thankMessage);
        String eTicket = String.valueOf(this.fileStorageLocation.get("eticket").resolve(request.getEticketFile()).normalize());
        String paymentProof = String.valueOf(this.fileStorageLocation.get("paymentProof").resolve(request.getPayment().getInvoice()));
        List<String> filePaths = new ArrayList<>();
        filePaths.add(eTicket);
        filePaths.add(paymentProof);
        emailSender.sendAsync(user.getUsername(), "Payment Proof and ETicket", template, filePaths);
        log.info("Email Sent");
    }


    @Transactional
    public Payment generatePaymentProof(Payment request) throws IOException {
        log.info("generate payment proof");
        Transaction transaction = request.getTransaction();
        String fileName = "Payment Proof-" + transaction.getId() + ".pdf";
        String path = "payment-proof/" + fileName;
        PdfWriter writer = new PdfWriter(path);
        PdfDocument pdf = new PdfDocument(writer);
        InvoiceDTO invoiceDTO = getInvoiceDTO(transaction);
        try (Document document = new Document(pdf, PageSize.A4)) {
            fileCreation.generatePaymentProof(document, invoiceDTO);
        } catch (Exception e) {
            log.error("Error while generating invoice", e);
        }
        log.info("generate payment proof success");
        request.setInvoice(fileName);
        return request;
    }

    @Transactional
    public InvoiceDTO getInvoiceDTO(Transaction request) {
        log.info("get invoice");
        Optional<Transaction> checkDataDBTransaction = transactionRepository.findById(request.getId());
        if (!checkDataDBTransaction.isPresent()) {
            log.error("get invoice error : transaction not found");
            throw new EntityNotFoundException("transaction not found");
        }
        Transaction transaction = checkDataDBTransaction.get();
        InvoiceDTO invoiceDTO = new InvoiceDTO();
        invoiceDTO.setId(transaction.getId());
        invoiceDTO.setTransactionDetails(transaction.getTransactionDetails());
        invoiceDTO.setPayment(transaction.getPayment());
        invoiceDTO.setOrderer(transaction.getOrderer());
        Map<String, Integer> transactionMap = new LinkedHashMap<>();
        transactionMap.put("Adult", transaction.getAdultPassenger());
        transactionMap.put("Child", transaction.getChildPassenger());
        transactionMap.put("Infant", transaction.getInfantPassenger());
        invoiceDTO.setTransaction(transactionMap);
        return invoiceDTO;
    }

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
            log.error("ETicket File Path Not Found");
            throw new FileHandlingException("ETicket File Path Not Found: " + e.getMessage());
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
                        ETicketDTO eticketDTO = eTicketRepository.getETicketDTO(transactionDetail.getId());
                        eticketDTO.setPassengers(passengers);
                        eticketDTO.setBookCode(eticket.getBookCode());
                        fileCreation.generateETicket(document, eticketDTO);
                        count.getAndIncrement();
                        return eticket;
                    }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("failed to save ETicket");
            throw new FileHandlingException("ETicket File Failed to save: " + e);
        }
        if (eTickets.isEmpty()){
            log.error("Failed to save E-Ticket");
            throw new EntityNotFoundException("Failed to save E-Ticket");
        }
        request.setEticketFile(fileName);
        request.setEtickets(eTickets);
        log.info("ETicket Save Success");
        transactionRepository.save(request);
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
            if (!checkDBTransaction.isPresent() || checkDBTransaction.get().getUser().getId() != checkDBUser.get().getId()) {
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
            throw new FileHandlingException("ETicket File Path Not Found ", e);
        } catch (FileNotFoundException e) {
            throw new FileHandlingException("ETicket File Not Found ", e);
        }
    }


}
