package fr.umontpellier.service;

public interface UserNotificationService {
    void showSuccessPopup(String title, String content);
    void showErrorPopup(String title, String content);
}