package com.example.wefly_app.service.oauth;

import com.example.wefly_app.entity.*;
import com.example.wefly_app.repository.*;
import com.example.wefly_app.request.airplane.AirplaneRegisterModel;
import com.example.wefly_app.request.flight.FlightRegisterModel;
import com.example.wefly_app.service.AirlineService;
import com.example.wefly_app.service.AirplaneService;
import com.example.wefly_app.service.FlightService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
@Service
public class DatabaseSeeder implements ApplicationRunner {

    private static final String TAG = "DatabaseSeeder {}";

    private Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private AirlineRepository airlineRepository;

    @Autowired
    private AirplaneService airplaneService;

    @Autowired
    private AirplaneRepository airplaneRepository;

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private RolePathRepository rolePathRepository;

    private String defaultPassword = "password";

    private String[] users = new String[]{
            "admin@mail.com:ROLE_SUPERUSER ROLE_ADMIN",
            "user@mail.com:ROLE_USER"
    };

    private String[] clients = new String[]{
            "my-client-apps:ROLE_READ ROLE_WRITE", // mobile
            "my-client-web:ROLE_READ ROLE_WRITE" // web
    };

    private String[] roles = new String[] {
            //default
            "ROLE_SUPERUSER:user_role:^/.*:GET|PUT|POST|PATCH|DELETE|OPTIONS",
            "ROLE_ADMIN:user_role:^/.*:GET|PUT|POST|PATCH|DELETE|OPTIONS",
            "ROLE_READ:oauth_role:^/.*:GET|PUT|POST|PATCH|DELETE|OPTIONS",
            "ROLE_WRITE:oauth_role:^/.*:GET|PUT|POST|PATCH|DELETE|OPTIONS",

            //User
            "ROLE_USER:user_role:^/v1/user/(update|delete):PUT|DELETE",
    };


    @Override
    @Transactional
    public void run(ApplicationArguments applicationArguments) throws IOException {
        String password = encoder.encode(defaultPassword);

        this.insertRoles();
        this.insertClients(password);
        this.insertUser(password);
        this.insertAirports();
        this.insertAirlines();
        this.insertAirplanes();
        this.insertFlights();
    }

    public void insertAirports() throws IOException {
        if (airportRepository.count() == 0) {
            ObjectMapper mapper = new ObjectMapper();
            List<Airport> airports = mapper.readValue(Paths.get("./database_seeder/indo-airports.json").toFile(),
                    mapper.getTypeFactory().constructCollectionType(List.class, Airport.class));
            airportRepository.saveAll(airports);
        }
    }

    public void insertAirlines() throws IOException {
        if (airlineRepository.count() == 0) {
            ObjectMapper mapper = new ObjectMapper();
            List<Airline> airlines = mapper.readValue(Paths.get("./database_seeder/test-airlines.json").toFile(),
                    mapper.getTypeFactory().constructCollectionType(List.class, Airline.class));
            airlineRepository.saveAll(airlines);
        }
    }

    public void insertAirplanes() throws IOException {
        if (airplaneRepository.count() == 0) {
            ObjectMapper mapper = new ObjectMapper();
            List<AirplaneRegisterModel> airplanes = mapper.readValue(Paths.get("./database_seeder/test-airplanes.json").toFile(),
                    mapper.getTypeFactory().constructCollectionType(List.class, AirplaneRegisterModel.class));
            airplanes.forEach(airplaneService::save);
        }
    }

    public void insertFlights() throws IOException {
        if (flightRepository.count() == 0) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModules(new JavaTimeModule());
            List<FlightRegisterModel> flights = mapper.readValue(Paths.get("./database_seeder/test-flights.json").toFile(),
                    mapper.getTypeFactory().constructCollectionType(List.class, FlightRegisterModel.class));
            flights.forEach(flightService::save);
        }
    }

    @Transactional
    public void insertRoles() {
        for (String role: roles) {
            String[] str = role.split(":");
            String name = str[0];
            String type = str[1];
            String pattern = str[2];
            String[] methods = str[3].split("\\|");
            Role oldRole = roleRepository.findOneByName(name);
            if (null == oldRole) {
                oldRole = new Role();
                oldRole.setName(name);
                oldRole.setType(type);
                oldRole.setRolePaths(new ArrayList<>());
                for (String m: methods) {
                    String rolePathName = name.toLowerCase()+"_"+m.toLowerCase();
                    RolePath rolePath = rolePathRepository.findOneByName(rolePathName);
                    if (null == rolePath) {
                        rolePath = new RolePath();
                        rolePath.setName(rolePathName);
                        rolePath.setMethod(m.toUpperCase());
                        rolePath.setPattern(pattern);
                        rolePath.setRole(oldRole);
                        rolePathRepository.save(rolePath);
                        oldRole.getRolePaths().add(rolePath);
                    }
                }
            }

            roleRepository.save(oldRole);
        }
    }

    @Transactional
    public void insertClients(String password) {
        for (String c: clients) {
            String[] s = c.split(":");
            String clientName = s[0];
            String[] clientRoles = s[1].split("\\s");
            Client oldClient = clientRepository.findOneByClientId(clientName);
            if (null == oldClient) {
                oldClient = new Client();
                oldClient.setClientId(clientName);
                oldClient.setAccessTokenValiditySeconds(28800);//1 jam 3600 :token valid : seharian kerja : normal 1 jam
                oldClient.setRefreshTokenValiditySeconds(7257600);// refresh
                oldClient.setGrantTypes("password refresh_token authorization_code");
                oldClient.setClientSecret(password);
                oldClient.setApproved(true);
                oldClient.setRedirectUris("");
                oldClient.setScopes("read write");
                List<Role> rls = roleRepository.findByNameIn(clientRoles);

                if (rls.size() > 0) {
                    oldClient.getAuthorities().addAll(rls);
                }
            }
            clientRepository.save(oldClient);
        }
    }

    @Transactional
    public void insertUser(String password) {
        for (String user: users) {
            String[] str = user.split(":");
            String email = str[0];
            String[] roleNames = str[1].split("\\s");

            User oldUser = userRepository.findOneByUsername(email);
            if (null == oldUser) {
                oldUser = new User();
                oldUser.setUsername(email);
                oldUser.setPassword(password);
                oldUser.setEnabled(true);
                List<Role> r = roleRepository.findByNameIn(roleNames);
                oldUser.setRoles(r);
            }

            userRepository.save(oldUser);
        }
    }
}

