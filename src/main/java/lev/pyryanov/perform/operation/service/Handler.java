package lev.pyryanov.perform.operation.service;

import lev.pyryanov.perform.operation.model.ApplicationStatusResponse;

public interface Handler {
    ApplicationStatusResponse performOperation(String id);
}
