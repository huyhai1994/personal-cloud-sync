package org.mini_lab.personal_cloud_sync.aspect;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class MdcLoggingFilterTest {
    private MdcLoggingFilter mdcFilter;

    private static final String HEADER_NAME = "X-Request-ID";
    private static final String MDC_KEY = "correlationId";

    @BeforeEach
    void setUp() {
        mdcFilter = new MdcLoggingFilter();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void shouldPutRequestIdIntoMdc_whenHeaderExists() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HEADER_NAME, "req-123");

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            assertThat(MDC.get(MDC_KEY)).isEqualTo("req-123");
        };

        mdcFilter.doFilter(request, response, filterChain);

        assertThat(response.getHeader(HEADER_NAME)).isEqualTo("req-123");
        assertThat(MDC.get(MDC_KEY)).isNull();
    }

    @Test
    void shouldGenerateRequestId_whenHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            assertThat(MDC.get(MDC_KEY)).isNotBlank();
        };

        mdcFilter.doFilter(request, response, filterChain);

        assertThat(response.getHeader(HEADER_NAME)).isNotBlank();
        assertThat(MDC.get(MDC_KEY)).isNull();
    }

    @Test
    void shouldClearMdcEven_whenExceptionOccurs() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HEADER_NAME, "req-error");

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain filterChain = (req, res) -> {
            assertThat(MDC.get(MDC_KEY)).isEqualTo("req-error");
            throw new RuntimeException("boom");
        };

        assertThatThrownBy(() -> mdcFilter.doFilter(request, response, filterChain)).isInstanceOf(RuntimeException.class).hasMessage("boom");

        assertThat(MDC.get(MDC_KEY)).isNull();
    }

}