import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class MainPanelController implements Initializable {
    private int localStorageFolderLevelCounter = 0;
    private int cloudStorageFolderLevelCounter = 0;
    private String clientStorage = "Client/ClientStorage";


    @FXML
    ListView<StorageItem> listOfLocalElements;
    @FXML
    Button localStorageUpdate;
    @FXML
    VBox firstBlockMainPanel;
    @FXML
    ListView<StorageItem> listOfCloudStorageElements;
    @FXML
    Button cloudStorageUpdate;
    @FXML
    Button localStorageDelete;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ServerConnectionController.startConnection();
        initializeListOfLocalStorageItems();
        mainPanelServerListener.setDaemon(true);
        mainPanelServerListener.start();
    }

    Thread mainPanelServerListener = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while (true) {
                    Object object = null;
                    object = ServerConnectionController.readIncomingObject();
                }
            } catch (ClassNotFoundException | IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    });

    public void initializeListOfLocalStorageItems() {
        ObservableList<StorageItem> listOfLocalItems = FXCollections.observableArrayList();
        File pathToLocalStorage = new File(clientStorage);
        File[] listOfLocalStorageFiles = pathToLocalStorage.listFiles();
        if (listOfLocalStorageFiles.length == 0 && localStorageFolderLevelCounter == 0) {
            Image image = new Image("/icons/dropfilesicon.png");
            BackgroundImage bi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
            listOfLocalElements.setBackground(new Background(bi));
            listOfLocalElements.setOpacity(0.9);
            listOfLocalElements.setItems(listOfLocalItems);
            listOfLocalElements.setCellFactory(param -> new StorageListViewItem());
        } else if (listOfLocalStorageFiles.length > 0) {
            listOfLocalElements.setBackground(null);
            for (int i = 0; i < listOfLocalStorageFiles.length; i++) {
                long initialSizeOfLocalFileOrDirectory = 0;
                String nameOfLocalFileOrDirectory = listOfLocalStorageFiles[i].getName();
                if (listOfLocalStorageFiles[i].isDirectory()) {
                    try {
                        initialSizeOfLocalFileOrDirectory = getActualSizeOfFolder(listOfLocalStorageFiles[i]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    initialSizeOfLocalFileOrDirectory = listOfLocalStorageFiles[i].length();
                }
                String dateOfLastModification = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(listOfLocalStorageFiles[i].lastModified()));
                File pathToFileInLocalStorage = new File(listOfLocalStorageFiles[i].getAbsolutePath());
                listOfLocalItems.addAll(new StorageItem(nameOfLocalFileOrDirectory, initialSizeOfLocalFileOrDirectory, false, dateOfLastModification, pathToFileInLocalStorage));
            }
            listOfLocalElements.setItems(listOfLocalItems);
            listOfLocalElements.setCellFactory(param -> new StorageListViewItem());
        } else {
            listOfLocalElements.setItems(listOfLocalItems);
            listOfLocalElements.setCellFactory(param -> new StorageListViewItem());
        }
    }

    private long getActualSizeOfFolder(File listOfLocalStorageFile) {
        return 100L;
    }


    public void selectAllFilesFromLocalStorage(ActionEvent actionEvent) {
    }

    public void uploadFilesToCloudStorage(ActionEvent actionEvent) {
    }

    public void downloadFilesIntoLocalStorage(ActionEvent actionEvent) {
    }

    public void selectAllFilesFromCloudStorage(ActionEvent actionEvent) {
    }

    public void updateCloudStoragePanel(ActionEvent actionEvent) {
    }
}
