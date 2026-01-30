package sk.tany.rest.api.service;

public interface OneDriveService {
    void uploadFile(String folderPath, String fileName, byte[] content);
}
