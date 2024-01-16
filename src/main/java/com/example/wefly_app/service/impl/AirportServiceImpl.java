package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.Airport;
import com.example.wefly_app.repository.AirportRepository;
import com.example.wefly_app.request.AirportDeleteModel;
import com.example.wefly_app.request.AirportRegisterModel;
import com.example.wefly_app.request.AirportUpdateModel;
import com.example.wefly_app.service.AirportService;
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

@Service
@Slf4j
public class AirportServiceImpl implements AirportService {
    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private TemplateResponse templateResponse;
    @Autowired
    private SimpleStringUtils simpleStringUtils;
    @Override
    public Map<Object, Object> save(AirportRegisterModel request) {
        try {
            log.info("Save New Airport");
            if (airportRepository.getSimilarName(request.getName()) > 0){
                throw new EntityExistsException("Airport with name " + request.getName() + " already exists");
            }
            Airport airport = new Airport();
            airport.setName(request.getName());
            airport.setCity(request.getCity());
            airport.setCountry(request.getCountry());
            airport.setAirportCode(request.getAirportCode());
            airport.setStatus(request.getStatus());

            log.info("Airport Saved");
            return templateResponse.success(airportRepository.save(airport));
        } catch (Exception e) {
            log.error("Error Saving Airport", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> update(AirportUpdateModel request, Long id) {;
        try {
            log.info("Update Airport");
            Optional<Airport> checkDataDBAirport = airportRepository.findById(id);
            if (!checkDataDBAirport.isPresent()) throw new EntityNotFoundException("Airport with id " + id + " not found");
            int count = 0;
            if (request.getName() != null){
                checkDataDBAirport.get().setName(request.getName());
                count++;
            }
            if (request.getCity() != null) {
                checkDataDBAirport.get().setCity(request.getCity());
                count++;
            }
            if (request.getCountry() != null) {
                checkDataDBAirport.get().setCountry(request.getCountry());
                count++;
            }
            if (request.getAirportCode() != null) {
                checkDataDBAirport.get().setAirportCode(request.getAirportCode());
                count++;
            }
            if (request.getStatus() != checkDataDBAirport.get().getStatus()) {
                checkDataDBAirport.get().setStatus(request.getStatus());
                count++;
            }
            if (count > 0) {
                checkDataDBAirport.get().setUpdatedDate(new Date());
                log.info("Airport Updated");
                return templateResponse.success(airportRepository.save(checkDataDBAirport.get()));
            }

            log.info("Airport Not Updated");
            return templateResponse.success("No changes made");
        } catch (Exception e) {
            log.error("Error Updating Airport", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> delete(AirportDeleteModel request, Long id) {
        try {
            log.info("Delete Airport");
            Optional<Airport> checkDataDBAirport = airportRepository.findById(id);
            if (!checkDataDBAirport.isPresent()) throw new EntityNotFoundException("Airport with id " + id + " not found");

            checkDataDBAirport.get().setDeletedDate(new Date());
            airportRepository.delete(checkDataDBAirport.get());
            log.info("Airport Deleted");
            return templateResponse.success("Airport Deleted Successfully");
        } catch (Exception e) {
            log.error("Error Deleting Airport", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> getById(Long id) {
        try {
            log.info("Get Airport");
            Optional<Airport> checkDataDBAirport = airportRepository.findById(id);
            if (!checkDataDBAirport.isPresent()) throw new EntityNotFoundException("Airport with id " + id + " not found");

            log.info("Airport Found");
            return templateResponse.success(checkDataDBAirport.get());
        } catch (Exception e) {
            log.error("Error Getting Airport", e);
            throw e;
        }
    }

    @Override
    public Map<Object, Object> getAll(int page, int size, String orderBy, String orderType,
                                      String name, String city, String country,
                                      String airportCode) {
        try {
            log.info("Get List Airports");
            Pageable pageable = simpleStringUtils.getShort(orderBy, orderType, page, size);
            Specification<Airport> specification = ((root, criteriaQuery, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (name != null && !name.isEmpty()) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
                    log.info("list airport name like");
                }
                if (city != null && !city.isEmpty()) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), "%" + city.toLowerCase() + "%"));
                    log.info("list airport city like");
                }
                if (country != null && !country.isEmpty()) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("country")), "%" + country.toLowerCase() + "%"));
                    log.info("list airport country like");
                }
                if (airportCode != null && !airportCode.isEmpty()) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("airportCode")), "%" + airportCode.toLowerCase() + "%"));
                    log.info("list airport airportCode like");
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            });

            Page<Airport> list = airportRepository.findAll(specification, pageable);
            Map<Object, Object> map = new HashMap<>();
            map.put("data", list);
            log.info("List Airports Found");
            return map;
        } catch (Exception e) {
            log.error("Error Getting Airports", e);
            throw e;
        }
    }
}
