package com.example.wefly_app.service.impl;

import com.example.wefly_app.entity.Airport;
import com.example.wefly_app.repository.AirportRepository;
import com.example.wefly_app.request.airport.AirportDeleteModel;
import com.example.wefly_app.request.airport.AirportRegisterModel;
import com.example.wefly_app.request.airport.AirportUpdateModel;
import com.example.wefly_app.service.AirportService;
import com.example.wefly_app.util.SimpleStringUtils;
import com.example.wefly_app.util.TemplateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.Predicate;
import java.util.*;

@Service
@Slf4j
public class AirportServiceImpl implements AirportService {
    private final AirportRepository airportRepository;
    private final TemplateResponse templateResponse;
    private final SimpleStringUtils simpleStringUtils;

    @Autowired
    public AirportServiceImpl(AirportRepository airportRepository,
                              TemplateResponse templateResponse,
                              SimpleStringUtils simpleStringUtils) {
        this.airportRepository = airportRepository;
        this.templateResponse = templateResponse;
        this.simpleStringUtils = simpleStringUtils;
    }

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
            airport.setIcao(request.getIcao());
            airport.setIata(request.getIata());
            airport.setProvince(request.getProvince());
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
            if (request.getIata() != null) {
                checkDataDBAirport.get().setIata(request.getIata());
                count++;
            }
            if (request.getIcao() != null) {
                checkDataDBAirport.get().setIcao(request.getIcao());
                count++;
            }
            if (request.getProvince() != null) {
                checkDataDBAirport.get().setProvince(request.getProvince());
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
                                      String iata, String icao, String province) {
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
                if (iata != null && !iata.isEmpty()) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("iata")), "%" + iata.toLowerCase() + "%"));
                    log.info("list airport airportCode like");
                }
                if (icao != null && !icao.isEmpty()) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("icao")), "%" + icao.toLowerCase() + "%"));
                    log.info("list airport airportCode like");
                }
                if (province != null && !province.isEmpty()) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("province")), "%" + province.toLowerCase() + "%"));
                    log.info("list airport province like");
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

    @Override
    public Map<Object, Object> getAllDropDown(String orderBy, String orderType,
                                      String name, String city, String country,
                                      String iata, String icao, String province) {
        try {
            log.info("Get List Airports Drop Down");
            Sort sort = Sort.by(Sort.Direction.fromString(orderType), orderBy);
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
                if (iata != null && !iata.isEmpty()) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("iata")), "%" + iata.toLowerCase() + "%"));
                    log.info("list airport airportCode like");
                }
                if (icao != null && !icao.isEmpty()) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("icao")), "%" + icao.toLowerCase() + "%"));
                    log.info("list airport airportCode like");
                }
                if (province != null && !province.isEmpty()) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("province")), "%" + province.toLowerCase() + "%"));
                    log.info("list airport province like");
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            });

            List<Airport> list = airportRepository.findAll(specification, sort);
            Map<Object, Object> map = new HashMap<>();
            map.put("data", list);
            log.info("List Airports Drop Down Found");
            return map;
        } catch (Exception e) {
            log.error("Error Getting Airports", e);
            throw e;
        }
    }
}
