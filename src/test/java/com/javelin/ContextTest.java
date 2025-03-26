package com.javelin;

import com.javelin.core.Context;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ContextTest {

    @Test
    void testQueryParamParsing() throws IOException {
        // HttpExchange를 mock으로 대체
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRequestURI()).thenReturn(URI.create("/test?id=hello&lang=kr"));

        Context ctx = new HttpExchangeContext(exchange);

        assertEquals("hello", ctx.queryParam("id"));
        assertEquals("kr", ctx.queryParam("lang"));
    }
}
