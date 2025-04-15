package com.healthlink.Controllers;

import com.healthlink.Entities.ForumResponse;
import com.healthlink.Services.ForumResponseService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class EditCommentController {
    @FXML private TextArea commentArea;

    private ForumResponse response;
    private MainController mainController;
    private final ForumResponseService responseService = new ForumResponseService();

    public void setResponse(ForumResponse response) {
        this.response = response;
        commentArea.setText(response.getDescription());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleSave() {
        response.setDescription(commentArea.getText());
        responseService.update(response);
        mainController.showForumDetails(mainController.getCurrentForum());
    }

    @FXML
    private void handleCancel() {
        mainController.showForumDetails(mainController.getCurrentForum());
    }
}