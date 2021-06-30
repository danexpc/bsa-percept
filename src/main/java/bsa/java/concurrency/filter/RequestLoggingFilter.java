package bsa.java.concurrency.filter;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;

@Log4j2
@Component
public class RequestLoggingFilter extends AbstractRequestLoggingFilter {

    public RequestLoggingFilter() {
        setIncludeClientInfo(true);
        setIncludeQueryString(true);
        setIncludePayload(true);
        setIncludeHeaders(true);
        setBeforeMessagePrefix("REQUEST DATA : ");
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        log.info(message);
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
    }
}
