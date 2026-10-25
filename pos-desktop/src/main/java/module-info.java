module com.tripos.pos {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.tripos.pos to javafx.fxml;
    opens com.tripos.pos.controller to javafx.fxml;
    opens com.tripos.pos.model to javafx.base;

    exports com.tripos.pos;
    exports com.tripos.pos.controller;
    exports com.tripos.pos.model;
    exports com.tripos.pos.service;
    exports com.tripos.pos.util;
}
