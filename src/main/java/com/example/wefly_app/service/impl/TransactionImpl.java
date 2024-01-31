package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.*;
import com.example.wefly_app.entity.enums.PaymentStatus;
import com.example.wefly_app.entity.enums.Status;
import com.example.wefly_app.repository.*;
import com.example.wefly_app.request.transaction.MidtransRequestModel;
import com.example.wefly_app.request.transaction.PaymentRegisterModel;
import com.example.wefly_app.request.transaction.TransactionSaveModel;
import com.example.wefly_app.service.TransactionService;
import com.example.wefly_app.util.FileStorageProperties;
import com.example.wefly_app.util.SimpleStringUtils;
import com.example.wefly_app.util.TemplateResponse;
import com.example.wefly_app.util.exception.FileStorageException;
import com.example.wefly_app.util.exception.IncorrectUserCredentialException;
import com.example.wefly_app.util.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionImpl implements TransactionService {
    @Value("${app.upload.payment.proof}")//FILE_SHOW_RUL
    private String BASE_UPLOAD_FOLDER;
    @Value("${midtrans.server-key}")
    private String SERVER_KEY;
    private final Path fileStorageLocation;
    @Autowired
    public TransactionRepository transactionRepository;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public FlightClassRepository flightClassRepository;
    @Autowired
    public TemplateResponse templateResponse;
    @Autowired
    public SimpleStringUtils simpleStringUtils;
    @Autowired
    public BankRepository bankRepository;
    @Autowired
    public PaymentRepository paymentRepository;
    @Autowired
    public TransactionImpl (FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
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
                                passenger1.setTransaction(transaction);
                                return passenger1;
                            })
                    .collect(Collectors.toList());
            transaction.setPassengers(passengers);
            Transaction transactionSaved = transactionRepository.save(transaction);
            log.info("save transaction success, proceed to payment");
            return templateResponse.success(midtransRequest(transactionSaved));
        } catch (Exception e) {
            log.error("Save transaction error ", e);
            throw e;
        }
    }

    public Map midtransRequest (Transaction request) throws IOException {
        try {
            log.info("midtrans request");
            Map<String, Object> transactionDetails = new HashMap<>();
            transactionDetails.put("order_id", request.getId());
            transactionDetails.put("gross_amount", request.getTotalPrice());
            System.out.println("request.getTotalPrice() = " + request.getTotalPrice());
            System.out.println("request.getId() = " + request.getId());

            Map<String, Object> customerDetails = new HashMap<>();
            customerDetails.put("first_name", request.getOrderer().getFirstName());
            customerDetails.put("last_name", request.getOrderer().getLastName());
            customerDetails.put("email", request.getOrderer().getEmail());
            customerDetails.put("phone", request.getOrderer().getPhoneNumber());

            MidtransRequestModel midtransRequest = new MidtransRequestModel();
            midtransRequest.setTransactionDetails(transactionDetails);
            midtransRequest.setCustomerDetails(customerDetails);

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(midtransRequest);

            String url = "https://app.sandbox.midtrans.com/snap/v1/transactions";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("Content-Type", "application/json");
            String encodedAuth = Base64.getEncoder().encodeToString(SERVER_KEY.getBytes());
            headers.set("Authorization", "Basic " + encodedAuth);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            Map result = mapper.readValue(response.getBody(), Map.class);
            log.info("midtrans request success");
            return result;
        } catch (Exception e) {
            log.error("midtrans request error ", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> delete(Long request) {
        try {
            log.info("delete transaction");
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attributes.getRequest().getAttribute("userId");
            System.out.println("userId = " + userId);
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
    public Map<Object, Object> getAll(int page, int size, String orderBy, String orderType, String startDate, String endDate, String status) {
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
                if (status != null && !status.isEmpty()) {
                    Status statusEnum = Status.valueOf(status);
                    predicates.add(criteriaBuilder.equal(root.get("status"), statusEnum));
                    log.info("filtering by status");
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

    @Override
    public Map<Object, Object> getAllBank(int page, int size, String orderBy, String orderType) {
        try {
            log.info("get all bank");
            Pageable pageable = simpleStringUtils.getShort(orderBy, orderType, page, size);
            Specification<Bank> specification = ((root, criteriaQuery, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            });
            Page<Bank> list = bankRepository.findAll(specification, pageable);
            Map<Object, Object> map = new HashMap<>();
            map.put("data", list);
            log.info("get all bank succeed");
            return map;

        } catch (Exception e) {
            log.error("get all bank error ", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> savePayment(PaymentRegisterModel request) {
        try {
            log.info("save payment");
            Optional<Transaction> checkDataDBTransaction = transactionRepository.findById(request.getTransactionId());
            if (!checkDataDBTransaction.isPresent()) throw new EntityNotFoundException("transaction not found");
            Optional<Bank> checkDataDBBank = bankRepository.findById(request.getBankId());
            if (!checkDataDBBank.isPresent()) throw new EntityNotFoundException("bank not found");
            Payment payment = new Payment();
            payment.setBank(checkDataDBBank.get());
            payment.setTransaction(checkDataDBTransaction.get());
            log.info("save payment succeed");
            return templateResponse.success(paymentRepository.save(payment));
        } catch (Exception e) {
            log.error("save payment error ", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> savePaymentProof(MultipartFile file, Long paymentId) throws IOException {
        ServletRequestAttributes attribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        Long userId = (Long) attribute.getRequest().getAttribute("userId");
        Optional<User> checkDataDBUser = userRepository.findById(userId);
        log.info("Update User : " + userId);
        if (!checkDataDBUser.isPresent()) {
            throw new IncorrectUserCredentialException("unidentified token user");
        }
        Optional<Payment> checkDataDBPayment = paymentRepository.findById(paymentId);
        if (!checkDataDBPayment.isPresent()) throw new EntityNotFoundException("payment not found");
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyhhmmss");
        String strDate = formatter.format(date);

        String nameFormat= file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") );
        if(nameFormat.isEmpty()){
            nameFormat = ".png";
        }
        String userFolder = BASE_UPLOAD_FOLDER + "user_" + checkDataDBUser.get().getId() + "/";
        Path userFolderPath = Paths.get(userFolder);
        if (Files.notExists(userFolderPath)) {
            Files.createDirectories(userFolderPath);
        }
        String fileName = strDate + nameFormat;
        String filePath = userFolder + fileName;
        Path to = Paths.get(filePath);
        Map<Object, Object> map = new HashMap<>();

        try {
            Files.copy(file.getInputStream(), to);
        } catch (Exception e) {
            log.error("Error while saving file", e);
            map.put("file name", fileName);
            map.put("file download uri", null);
            map.put("file type", file.getContentType());
            map.put("file size", file.getSize());
            map.put("status", e.getMessage());
            return map;
        }
        checkDataDBPayment.get().setPaymentProof(fileName);
        checkDataDBPayment.get().setStatus(PaymentStatus.PROCESSING);
        Object obj = paymentRepository.save(checkDataDBPayment.get());

        map.put("file name", fileName);
        map.put("file download uri", null);
        map.put("file type", file.getContentType());
        map.put("file size", file.getSize());
        map.put("payment", obj);
        map.put("status", "success");
        return map;
    }

    @Override
    public Resource getPaymentProof(Long paymentId) {
        try {
            log.info("get payment proof");
            ServletRequestAttributes attribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            Long userId = (Long) attribute.getRequest().getAttribute("userId");
            Optional<User> checkDataDBUser = userRepository.findById(userId);
            log.info("Update User : " + userId);
            if (!checkDataDBUser.isPresent()) {
                throw new IncorrectUserCredentialException("unidentified token user");
            }
            Optional<Payment> checkDataDBPayment = paymentRepository.findById(paymentId);
            if (!checkDataDBPayment.isPresent()) throw new EntityNotFoundException("payment not found");
            String userFolder = "user_" + checkDataDBPayment.get().getTransaction().getUser().getId() + "/";
            Path filePath = this.fileStorageLocation.resolve(userFolder + checkDataDBPayment.get().getPaymentProof()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) throw new FileStorageException("File not found " + filePath);
            else {
                log.info("get payment proof succeed");
                return resource;
            }
        } catch (MalformedURLException ex) {
            log.error("get payment proof error ", ex);
            throw new FileStorageException("File not found " ,ex);
        }
    }


}
