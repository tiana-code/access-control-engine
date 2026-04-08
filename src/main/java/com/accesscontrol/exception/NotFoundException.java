package com.accesscontrol.exception;

public class NotFoundException extends AccessControlException {
    public NotFoundException(String entityName, Object id) {

        super(entityName + " not found: " + id);
    }
}
