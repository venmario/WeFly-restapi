package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.*;
import com.example.wefly_app.repository.BoardingPassRepository;
import com.example.wefly_app.repository.ETicketRepository;
import com.example.wefly_app.repository.TransactionRepository;
import com.example.wefly_app.repository.UserRepository;
import com.example.wefly_app.request.checkin.BoardingPassDTO;
import com.example.wefly_app.request.checkin.CheckinRequestModel;
import com.example.wefly_app.service.CheckinService;
import com.example.wefly_app.util.*;
import com.example.wefly_app.util.exception.FileHandlingException;
import com.example.wefly_app.util.exception.ValidationException;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.properties.AreaBreakType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class CheckInServiceImpl implements CheckinService {
    private final ETicketRepository eticketRepository;
    private final TemplateResponse templateResponse;
    private final UserRepository userRepository;
    private final Map<String, Path> fileStorageLocation = new HashMap<>();
    private final BoardingPassRepository boardingPassRepository;
    private final EmailTemplate emailTemplate;
    private final EmailSender emailSender;
    private final String homePageUrl;
    private final FileCreation fileCreation;

    @Autowired
    public CheckInServiceImpl(ETicketRepository eticketRepository, TemplateResponse templateResponse,
                              FileStorageProperties fileStorageProperties, UserRepository userRepository,
                              BoardingPassRepository boardingPassRepository, EmailSender emailSender,
                              EmailTemplate emailTemplate, @Value("${frontend.homepage.url}")String homePageUrl,
                              FileCreation fileCreation) {
        this.eticketRepository = eticketRepository;
        this.templateResponse = templateResponse;
        this.userRepository = userRepository;
        this.boardingPassRepository = boardingPassRepository;
        this.emailSender = emailSender;
        this.emailTemplate = emailTemplate;
        this.homePageUrl = homePageUrl;
        this.fileCreation = fileCreation;
        Path eticket = Paths.get(fileStorageProperties.getETicketDir()).toAbsolutePath().normalize();
        Path boardingPass = Paths.get(fileStorageProperties.getBoardingPassDir()).toAbsolutePath().normalize();
        this.fileStorageLocation.put("eticket", eticket);
        this.fileStorageLocation.put("boardingPass", boardingPass);
        try {
            Files.createDirectories(eticket);
            Files.createDirectories(boardingPass);
        } catch (IOException e) {
            throw new FileHandlingException("Could not create the directory where the uploaded files will be stored.", e);
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
            log.error("get ETicket Error: " + e.getMessage());
            throw new FileHandlingException("Boarding Pass File Path Not Found ", e);
        } catch (FileNotFoundException e) {
            throw new FileHandlingException("Boarding Pass File Not Found ", e);
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
            if (!request.getOrdererLastName().equalsIgnoreCase(eticket.getTransaction().getOrderer().getLastName())){
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
                fileCreation.generateBoardingPass(document, boardingPassDTO, boardingPass);
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

}
