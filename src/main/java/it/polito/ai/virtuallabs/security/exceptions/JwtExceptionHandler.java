package it.polito.ai.virtuallabs.security.exceptions;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

@Component
public class JwtExceptionHandler implements AuthenticationEntryPoint, Serializable {
    private static final long serialVersionUID = -7858869558953243875L;
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        final String msg = (String) request.getAttribute("message");
        if(msg != null)
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, msg);
        else if (authException instanceof BadCredentialsException)
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        else
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid/Expired JWT: please create a valid one at /authenticate");
    }
}
