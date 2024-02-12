package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.*;
import com.example.wefly_app.entity.enums.SeatClass;
import com.example.wefly_app.repository.AirlineRepository;
import com.example.wefly_app.repository.AirplaneRepository;
import com.example.wefly_app.request.airplane.AirplaneDeleteModel;
import com.example.wefly_app.request.airplane.AirplaneRegisterModel;
import com.example.wefly_app.request.airplane.AirplaneUpdateModel;
import com.example.wefly_app.service.AirplaneService;
import com.example.wefly_app.util.SimpleStringUtils;
import com.example.wefly_app.util.TemplateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AirplaneServiceImpl implements AirplaneService {
    @Autowired
    private AirplaneRepository airplaneRepository;
    @Autowired
    private TemplateResponse templateResponse;
    @Autowired
    private SimpleStringUtils simpleStringUtils;
    @Autowired
    private AirlineRepository airlineRepository;

    @Override
    public Map<Object, Object> save(AirplaneRegisterModel request) {
        try {
            log.info("Save New Airplane");
            if (airplaneRepository.getSimilarCode(request.getCode()) > 0) {
                throw new EntityExistsException("Airplane with code " + request.getCode() + " already exist");
            }
            Airline checkDataDBAirline = airlineRepository.findById(request.getAirlineId())
                    .orElseThrow(() -> new EntityNotFoundException("Airline with id " + request.getAirlineId() + " not found"));
            Airplane airplane = new Airplane();
            airplane.setType(request.getType());
            airplane.setAirline(checkDataDBAirline);
            airplane.setCode(request.getCode());
            List<SeatConfig> seatConfigs = request.getSeats().stream()
                            .map(seat -> {
                                SeatConfig airplaneSeat = new SeatConfig();
                                airplaneSeat.setSeatClass(SeatClass.valueOf(seat.getSeatClass()));
                                airplaneSeat.setSeatRow(seat.getNumberOfRow());
                                airplaneSeat.setSeatColumn(seat.getNumberOfColumn());
                                airplaneSeat.setAirplane(airplane);
                                return airplaneSeat;
                            }).collect(Collectors.toList());
            airplane.setSeatConfigs(seatConfigs);
            log.info("Airplane Saved");
            return templateResponse.success(airplaneRepository.save(airplane));
        } catch (Exception e) {
            log.error("Error Saving Airplane", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> update(AirplaneUpdateModel request, Long id) {
        try {
            log.info("Update Airplane");
            Optional<Airplane> checkDataDBAirplane = airplaneRepository.findById(id);
            if (!checkDataDBAirplane.isPresent()) throw new EntityExistsException("Airplane with id " + id + " not found");
            int count = 0;
            Airplane airplane = checkDataDBAirplane.get();
            if (request.getCode() != null && !request.getCode().isEmpty()) {
                airplane.setCode(request.getCode());
                count++;
            }
            if (request.getType() != null && !request.getType().isEmpty()) {
                airplane.setType(request.getType());
                count++;
            }
            if (count == 0) throw new IllegalArgumentException("No data to update");
            log.info("Airplane Updated");
            return templateResponse.success(airplaneRepository.save(airplane));
        } catch (Exception e) {
            log.error("Error Updating Airplane", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> delete(AirplaneDeleteModel request, Long id) {
        try {
            log.info("Delete Airplane");
            Optional<Airplane> checkDataDBAirplane = airplaneRepository.findById(id);
            if (!checkDataDBAirplane.isPresent()) throw new EntityExistsException("Airplane with id " + id + " not found");
            Airplane airplane = checkDataDBAirplane.get();
            log.info("Airplane Deleted");
            return templateResponse.success("Airplane with id " + id + " deleted");
        } catch (Exception e) {
            log.error("Error Deleting Airplane", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> getById(Long id) {
        try {
            log.info("Get Airplane By Id");
            Optional<Airplane> checkDataDBAirplane = airplaneRepository.findById(id);
            if (!checkDataDBAirplane.isPresent()) throw new EntityExistsException("Airplane with id " + id + " not found");
            log.info("Airplane Found");
            return templateResponse.success(checkDataDBAirplane.get());
        } catch (Exception e) {
            log.error("Error Getting Airplane", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> getAll(int page, int size, String orderBy, String orderType, String name, String type) {
        try {
            log.info("Get List Airplane");
            Pageable pageable = simpleStringUtils.getShort(orderBy, orderType, page, size);
            Specification<Airplane> specification = ((root, criteriaQuery, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (name != null && !name.isEmpty()) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
                    log.info("list airport name like");
                }
                if (type != null && !type.isEmpty()) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), "%" + type.toLowerCase() + "%"));
                    log.info("list airport city like");
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            });

            Page<Airplane> list = airplaneRepository.findAll(specification, pageable);
            Map<Object, Object> map = new HashMap<>();
            map.put("data", list);
            log.info("List Airports Found");
            return map;
        } catch (Exception e) {
            log.error("Error Getting Airplanes", e);
            throw e;
        }
    }
}
