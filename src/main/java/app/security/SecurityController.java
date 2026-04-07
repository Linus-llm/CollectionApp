package app.security;

import app.Main;
import app.config.HibernateConfig;
import app.entities.User;
import app.exceptions.ApiException;
import app.exceptions.ValidationException;
import app.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dk.bugelhartmann.UserDTO;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.UnauthorizedResponse;
import jakarta.persistence.EntityManagerFactory;
import dk.bugelhartmann.ITokenSecurity;
import dk.bugelhartmann.TokenSecurity;
import dk.bugelhartmann.TokenVerificationException;
import java.text.ParseException;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityController implements ISecurityController {
    private EntityManagerFactory emf = app.Main.emf;
    private SecurityDao securityDAO = new SecurityDao(emf);
    private ObjectMapper mapper = new ObjectMapper();
    private ITokenSecurity tokenSecurity = new TokenSecurity();


    @Override
    public void login(Context ctx) {
        UserDTO user = ctx.bodyAsClass(UserDTO.class);
        User foundUser;
        try {
            foundUser = securityDAO.getVerifiedUser(user.getUsername(), user.getPassword());
            //username and roles get put into the token
            String token = createToken(new UserDTO(foundUser.getUsername(), foundUser.getRolesAsStrings()));
            ObjectNode node = mapper.createObjectNode();

            ctx.status(200).json(node
                    .put("token", token)
                    .put("username", foundUser.getUsername()));
        } catch (ValidationException e) {
            throw new ApiException(401, e.getMessage());
        }



    }

    @Override
    public void register(Context ctx) {
        try {
            User user = ctx.bodyAsClass(User.class);
            if (user == null || user.getUsername() == null || user.getPassword() == null || user.getEmail() == null) {
                ctx.status(400).result("Username, password and email are required");
                return;
            }
            User createdUser = securityDAO.createUser(user.getUsername(), user.getPassword(), user.getEmail());
            ObjectNode response = mapper.createObjectNode();
            response.put("msg", "User registered");
            response.put("id", createdUser.getId());
            response.put("username", createdUser.getUsername());
            ctx.json(response).status(201);
        } catch (ValidationException e) {

            ObjectNode error = mapper.createObjectNode();
            error.put("error", e.getMessage());

            ctx.status(400).json(error);
        }
    }

    @Override
    public void authenticate(Context ctx) {
        // This is a preflight request => no need for authentication
        if (ctx.method().toString().equals("OPTIONS")) {
            ctx.status(200);
            return;
        }
        // If the endpoint is not protected with roles or is open to ANYONE role, then skip
        Set<String> allowedRoles = ctx.routeRoles().stream().map(role -> role.toString().toUpperCase()).collect(Collectors.toSet());
        if (isOpenEndpoint(allowedRoles))
            return;

        // If there is no token we do not allow entry
        UserDTO verifiedTokenUser = validateAndGetUserFromToken(ctx);
        ctx.attribute("user", verifiedTokenUser); // -> ctx.attribute("user") in ApplicationConfig beforeMatched filter

    }

    @Override
    public void authorize(Context ctx) {
        Set<String> allowedRoles = ctx.routeRoles()
                .stream()
                .map(role -> role.toString().toUpperCase())
                .collect(Collectors.toSet());

        // 1. Check if the endpoint is open to all (either by not having any roles or having the ANYONE role set
        if (isOpenEndpoint(allowedRoles))
            return;
        // 2. Get user and ensure it is not null
        UserDTO user = ctx.attribute("user");
        if (user == null) {
            throw new ApiException(403, "No user was added from the token");
        }
        // 3. See if any role matches
        if (!userHasAllowedRole(user, allowedRoles))
            throw new ApiException(403, "User was not authorized with roles: " + user.getRoles() + ". Needed roles are: " + allowedRoles);
    }

    private String createToken(UserDTO user) {
        try {
            String ISSUER;
            String TOKEN_EXPIRE_TIME;
            String SECRET_KEY;

            if (System.getenv("DEPLOYED") != null) {
                ISSUER = System.getenv("ISSUER");
                TOKEN_EXPIRE_TIME = System.getenv("TOKEN_EXPIRE_TIME");
                SECRET_KEY = System.getenv("SECRET_KEY");
            } else {
                ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
                TOKEN_EXPIRE_TIME = Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties");
                SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");
            }
            return tokenSecurity.createToken(user, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException(500, "Could not create token: " + e.getMessage());
        }
    }

    private static String getToken(Context ctx) {
        String header = ctx.header("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new UnauthorizedResponse("Authorization header is missing");
        }

        String[] parts = header.split(" ");

        if (parts.length < 2 || parts[1].isEmpty()) {
            throw new UnauthorizedResponse("Authorization header is malformed");
        }

        String token = parts[1];

        return token;
    }
    private boolean isOpenEndpoint(Set<String> allowedRoles) {
        // If the endpoint is not protected with any roles:
        if (allowedRoles.isEmpty())
            return true;

        // 1. Get permitted roles and Check if the endpoint is open to all with the ANYONE role
        if (allowedRoles.contains("ANYONE")) {
            return true;
        }
        return false;
    }

    private UserDTO validateAndGetUserFromToken(Context ctx) {
        String token = getToken(ctx);
        UserDTO verifiedTokenUser = verifyToken(token);
        if (verifiedTokenUser == null) {
            throw new UnauthorizedResponse("Invalid user or token"); // UnauthorizedResponse is javalin 6 specific but response is not json!
        }
        return verifiedTokenUser;
    }

    private UserDTO verifyToken(String token) {
        boolean IS_DEPLOYED = (System.getenv("DEPLOYED") != null);
        String SECRET = IS_DEPLOYED ? System.getenv("SECRET_KEY") : Utils.getPropertyValue("SECRET_KEY", "config.properties");

        try {
            if (tokenSecurity.tokenIsValid(token, SECRET) && tokenSecurity.tokenNotExpired(token)) {
                return tokenSecurity.getUserWithRolesFromToken(token);
            } else {
                throw new ApiException(403, "Token is not valid");
            }
        } catch (ParseException | ApiException | TokenVerificationException e) {
            //logger.error("Could not create token", e);
            throw new ApiException(HttpStatus.UNAUTHORIZED.getCode(), "Unauthorized. Could not verify token");
        }
    }

    private static boolean userHasAllowedRole(UserDTO user, Set<String> allowedRoles) {
        return user.getRoles().stream()
                .anyMatch(role -> allowedRoles.contains(role.toUpperCase()));
    }
}
