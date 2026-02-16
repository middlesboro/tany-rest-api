package sk.tany.rest.api.service;

import sk.tany.rest.api.dto.OneDriveFileDto;

import java.util.List;

public interface OneDriveService {
    void uploadFile(String folderPath, String fileName, byte[] content);
    List<OneDriveFileDto> listFiles(String folderPath);
    void deleteFile(String fileId);
}
