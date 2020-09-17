import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class MainPanelController implements Initializable {
    private String clientStorage = "Client/ClientStorage/";
    private LinkedList<File> folderCloudStorageListViews;
    private int bufferSize = 256;

    @FXML
    ListView<StorageItem> listOfLocalElements;
    @FXML
    Button localStorageSelect;
    @FXML
    Button cloudStorageSelect;
    @FXML
    VBox firstBlockMainPanel;
    @FXML
    VBox secondBlockMainPanel;
    @FXML
    ListView<StorageItem> listOfCloudStorageElements;
    @FXML
    Button cloudStorageDelete;
    @FXML
    Button localStorageDelete;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ServerConnectionController.startConnection();
        initializeListOfLocalStorageItems();
        mainPanelServerListener.setDaemon(true);
        mainPanelServerListener.start();
        ServerConnectionController.sendUpdateMessageToServer(CurrentLogin.getCurrentLogin());
    }

    Thread mainPanelServerListener = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while (true) {
                    Object object = ServerConnectionController.readIncomingObject();
                    if (object instanceof UpdateMessage) {
                        UpdateMessage message = (UpdateMessage) object;
                        folderCloudStorageListViews = new LinkedList<>();
                        folderCloudStorageListViews.addAll(message.getCloudStorageContents());
                        Platform.runLater(() -> initializeListOfCloudStorageItems(folderCloudStorageListViews));
                    } else if (object.toString().equals("DeletionSuccess")) {
                        Platform.runLater(() -> {
                            initializeListOfCloudStorageItems(folderCloudStorageListViews);
                        });
                    } else if (object instanceof FileMessage ) {
                        FileMessage fileMessage = (FileMessage) object;
                        try {
                            Files.write(Paths.get(clientStorage + "" + fileMessage.getFileName()), fileMessage.getData(), StandardOpenOption.CREATE);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        Platform.runLater(() -> initializeListOfLocalStorageItems());
                    } else if (object.toString().equals("success")) {
                        System.out.println("success");
                    }
                }
            } catch (ClassNotFoundException | IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    });


    public void initializeListOfLocalStorageItems() {
        ObservableList<StorageItem> listOfLocalItems = FXCollections.observableArrayList();
        listOfLocalElements.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        File pathToLocalStorage = new File(clientStorage);
        File[] listOfLocalStorageFiles = pathToLocalStorage.listFiles();
            listOfLocalElements.setBackground(null);
            for (int i = 0; i < listOfLocalStorageFiles.length; i++) {
                String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(listOfLocalStorageFiles[i].lastModified()));
                File pathToFileInLocalStorage = new File(listOfLocalStorageFiles[i].getAbsolutePath());
                listOfLocalItems.addAll(new StorageItem(listOfLocalStorageFiles[i].getName(), listOfLocalStorageFiles[i].length(), false, dateOfLastModification, pathToFileInLocalStorage));
            }
        listOfLocalElements.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listOfLocalElements.setItems(listOfLocalItems);
        listOfLocalElements.setCellFactory(param -> new StorageListViewItem());
    }

    public void initializeListOfCloudStorageItems(LinkedList<File> listOfCloudStorageFiles) {
        try {
            ObservableList<StorageItem> listOfCloudItems = FXCollections.observableArrayList();
                for (int i = 0; i < listOfCloudStorageFiles.size(); i++) {
                    String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(listOfCloudStorageFiles
                            .get(i).lastModified()));
                    File pathOfFileInCloudStorage = new File(listOfCloudStorageFiles.get(i).getAbsolutePath());
                    listOfCloudItems.addAll(new StorageItem(listOfCloudStorageFiles.get(i).getName(), listOfCloudStorageFiles.get(i).length(), false, dateOfLastModification, pathOfFileInCloudStorage));
                }
            listOfCloudStorageElements.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            listOfCloudStorageElements.setItems(listOfCloudItems);
            listOfCloudStorageElements.setCellFactory(param -> new StorageListViewItem());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    public void selectAllFilesFromLocalStorage(ActionEvent actionEvent) {
        if (listOfLocalElements.getItems().size() == listOfLocalElements.getSelectionModel().getSelectedItems().size()) {
            listOfLocalElements.getSelectionModel().clearSelection();
        } else {
            listOfLocalElements.getSelectionModel().selectAll();
        }
    }

    public void uploadFilesToCloudStorage(ActionEvent actionEvent) {
        ServerConnectionController.transferFilesToCloudStorage(CurrentLogin.getCurrentLogin(), getPathsOfSelectedFiles(listOfLocalElements));
    }

    public void downloadFilesIntoLocalStorage(ActionEvent actionEvent) {
        ServerConnectionController.sendFileRequest(getPathsOfSelectedFiles(listOfCloudStorageElements));
    }

    public void selectAllFilesFromCloudStorage(ActionEvent actionEvent) {
        if (listOfCloudStorageElements.getItems().size() == listOfCloudStorageElements.getSelectionModel().getSelectedItems().size()) {
            listOfCloudStorageElements.getSelectionModel().clearSelection();
        } else {
            listOfCloudStorageElements.getSelectionModel().selectAll();
        }
    }


    public LinkedList<File> getPathsOfSelectedFiles(ListView<StorageItem> selectedFileList) {
        LinkedList<File> listOfSelectedElements = new LinkedList<>();
        if (selectedFileList.getSelectionModel().getSelectedItems().size() != 0) {
            for (int i = 0; i < selectedFileList.getSelectionModel().getSelectedItems().size(); i++) {
                listOfSelectedElements.add(selectedFileList.getSelectionModel().getSelectedItems().get(i).getPathToFile());
            }
        }
        return listOfSelectedElements;
    }

    public void deleteChosenFilesFromLocalStorage(ActionEvent actionEvent) {
        for (int i = 0; i < getPathsOfSelectedFiles(listOfLocalElements).size(); i++) {
            try {
                Files.delete(Paths.get(getPathsOfSelectedFiles(listOfLocalElements).get(i).getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        initializeListOfLocalStorageItems();
    }

    public void deleteChosenFilesFromCloudStorage(ActionEvent actionEvent) {
        ServerConnectionController.sendDeletionMessage(CurrentLogin.getCurrentLogin(), getPathsOfSelectedFiles(listOfCloudStorageElements));
    }
}
