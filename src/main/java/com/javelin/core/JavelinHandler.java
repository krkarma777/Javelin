package com.javelin.core;

import javax.naming.Context;

public interface JavelinHandler {
    void handle(Context ctx) throws Exception;
}
