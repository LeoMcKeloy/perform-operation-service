package lev.pyryanov.perform.operation.client;

import lev.pyryanov.perform.operation.model.Response;

public interface Client {
    //блокирующий вызов сервиса 1 для получения статуса заявки
    Response getApplicationStatus1(String id);

    //блокирующий вызов сервиса 2 для получения статуса заявки
    Response getApplicationStatus2(String id);

}
