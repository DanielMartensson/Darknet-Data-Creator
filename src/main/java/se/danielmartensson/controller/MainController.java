package se.danielmartensson.controller;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.sarxos.webcam.Webcam;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import lombok.Getter;
import se.danielmartensson.threads.CameraThread;
import se.danielmartensson.threads.TrainingThread;
import se.danielmartensson.tools.FileHandeling;
import se.danielmartensson.tools.IntRangeStringConverter;
import se.danielmartensson.tools.SelectedWebCam;
import se.danielmartensson.tools.observablelists.Resolutions;
import se.danielmartensson.tools.observablelists.Webcams;

@Getter
public class MainController {

    @FXML
    private Button scanButton;

    @FXML
    private ChoiceBox<Webcams> usbCameraDropdownButton;

    @FXML
    private ChoiceBox<Resolutions> cameraResolutionDropdownButton;

    @FXML
    private ChoiceBox<Integer> sampleIntervallDropdownButton;

    @FXML
    private ChoiceBox<Resolutions> pictureResolutionDropdownButton;

    @FXML
    private TextField boundingBoxWidthTextField;

    @FXML
    private TextField boundingBoxHeightTextField;

    @FXML
    private ChoiceBox<Integer> classNumberDropdownButton;

    @FXML
    private Button saveToFolderButton;

    @FXML
    private Text saveToFolderLabel;

    @FXML
    private Button openCameraButton;

    @FXML
    private Button closeCameraButton;

    @FXML
    private Button startRecordingButton;

    @FXML
    private Button stopRecordingButton;

    @FXML
    private ImageView cameraImageView;

    @FXML
    private Text selectDataFileLabel;

    @FXML
    private Button selectDataFileButton;

    @FXML
    private Text selectConfigFileLabel;

    @FXML
    private Button selectConfigFileButton;

    @FXML
    private Text selectWeightFileLabel;

    @FXML
    private Button selectWeightFileButton;

    @FXML
    private Text selectDarknetFileLabel;

    @FXML
    private Button selectDarknetFileButton;

    @FXML
    private TextArea terminalTextArea;

    @FXML
    private Button startTrainingButton;

    @FXML
    private Button stopTrainingButton;
    
    @FXML
    private CheckBox meanAveragePrecisionsCheckBox;

	private SelectedWebCam selectedWebCam;

	private FileHandeling fileHandeling;

	private CameraThread cameraThread;
	
	private TrainingThread trainingThread;

	private AtomicBoolean runCamera;
	
	private AtomicBoolean runTraining;

	private AtomicBoolean runRecording;
	
	private AtomicInteger boundedBoxWidth;
	
	private AtomicInteger boundedBoxHeight;
	
	private Webcam webcam;
	
	private File selectedSaveToFolder;

	private File selectedWeightFile;

	private File selectedDataFile;

	private File selectedDarknetFile;

	private File selectedConfigFile;

    @FXML
    void boundingBoxHeightTextFieldValueChanged(ActionEvent event) {
    	boundedBoxHeight.set(Integer.parseInt(boundingBoxHeightTextField.getText()));
    }

    @FXML
    void boundingBoxWidthTextFieldValueChanged(ActionEvent event) {
    	boundedBoxWidth.set(Integer.parseInt(boundingBoxWidthTextField.getText()));
    }

    @FXML
    void closeCameraButtonPressed(ActionEvent event) {
    	runCamera.set(false);
    }

    @FXML
    void openCameraButtonPressed(ActionEvent event) {
		if (selectedSaveToFolder == null)
			return;
    	cameraThread.setComponents(this);
    	runCamera.set(true);
    }

    @FXML
    void saveToFolderButtonPressed(ActionEvent event) {
    	selectedSaveToFolder = fileHandeling.selectFolder(saveToFolderLabel);
    }

    @FXML
    void scanButtonPressed(ActionEvent event) {
    	selectedWebCam.scanWebCams(usbCameraDropdownButton);
    }

    @FXML
    void selectConfigFileButtonPressed(ActionEvent event) {
    	selectedConfigFile = fileHandeling.selectFile(selectConfigFileLabel, "Configuration file", new String[]{"*.cfg"});
    }

    @FXML
    void selectDarknetFileButtonPressed(ActionEvent event) {
    	selectedDarknetFile = fileHandeling.selectFile(selectDarknetFileLabel, "Darknet file", new String[]{"*", "*.exe"});
    }

    @FXML
    void selectDataFileButtonPressed(ActionEvent event) {
    	selectedDataFile = fileHandeling.selectFile(selectDataFileLabel, "Data file", new String[]{"*.data"});
    }

    @FXML
    void selectWeightFileButtonPressed(ActionEvent event) {
    	selectedWeightFile = fileHandeling.selectFile(selectWeightFileLabel, "Weight file", new String[]{"*.weights"});
    }

    @FXML
    void startRecordingButtonPressed(ActionEvent event) {
    	runRecording.set(true);
    }

    @FXML
    void startTrainingButtonPressed(ActionEvent event) {
    	if (selectedDarknetFile == null || selectedDataFile == null || selectedWeightFile == null || selectedConfigFile == null)
			return;
    	trainingThread.setComponents(this);
    	runTraining.set(true);
    }

    @FXML
    void stopRecordingButtonPressed(ActionEvent event) {
    	runRecording.set(false);
    }

    @FXML
    void stopTrainingButtonPressed(ActionEvent event) {
    	runTraining.set(false);
    }
    
	private void usbCameraDropdownButtonValueChanged(ActionEvent e) {
		if(usbCameraDropdownButton.getSelectionModel().getSelectedItem() == null)
			return;
		webcam = usbCameraDropdownButton.getSelectionModel().getSelectedItem().getWebcam();
		selectedWebCam.findResolutions(webcam, cameraResolutionDropdownButton, openCameraButton, closeCameraButton);
	}
	private void cameraResolutionDropdownButtonValueCanged(ActionEvent e) {
		selectedWebCam.setResolution(webcam, cameraResolutionDropdownButton.getSelectionModel().getSelectedItem());
	}
    
    @FXML
    void initialize() {
    	// Declare objects first
    	selectedWebCam = new SelectedWebCam();
    	fileHandeling = new FileHandeling();
    	
    	// Threads
    	runCamera = new AtomicBoolean();
    	runRecording = new AtomicBoolean();
    	cameraThread = new CameraThread(runCamera, runRecording);
    	cameraThread.start();
    	
    	runTraining = new AtomicBoolean();
    	trainingThread = new TrainingThread(runTraining);
    	trainingThread.start();
    	
    	// Fill with values and select the first one
    	ObservableList<Integer> listOfSampleIntervals = FXCollections.observableArrayList();
    	ObservableList<Resolutions> listOfResolutions = FXCollections.observableArrayList();
    	ObservableList<Integer> listOfClassNumbers = FXCollections.observableArrayList();
    	for(int i = 1; i <= 10; i++) 
    		listOfSampleIntervals.add(i);
    	for(int i = 0; i < 100; i++) 
    		listOfClassNumbers.add(i);
    	listOfResolutions.add(new Resolutions(608.0, 608.0, null));
    	listOfResolutions.add(new Resolutions(512.0, 512.0, null));
    	listOfResolutions.add(new Resolutions(416.0, 416.0, null));
    	listOfResolutions.add(new Resolutions(320.0, 320.0, null));
    	sampleIntervallDropdownButton.setItems(listOfSampleIntervals);
    	pictureResolutionDropdownButton.setItems(listOfResolutions);
    	classNumberDropdownButton.setItems(listOfClassNumbers);
    	
    	// Select the first one
    	sampleIntervallDropdownButton.getSelectionModel().select(0);
    	pictureResolutionDropdownButton.getSelectionModel().select(0);
    	classNumberDropdownButton.getSelectionModel().select(0);
    	
    	// Listeners
    	usbCameraDropdownButton.setOnAction(e -> usbCameraDropdownButtonValueChanged(e));
    	cameraResolutionDropdownButton.setOnAction(e -> cameraResolutionDropdownButtonValueCanged(e));
    	
    	// Textformaters
    	StringConverter<Integer> maxBoundedWidth = new IntRangeStringConverter(1, 800);
    	StringConverter<Integer> maxBoundedHeight = new IntRangeStringConverter(1, 600);
    	boundingBoxWidthTextField.setTextFormatter(new TextFormatter<>(maxBoundedWidth, 0));
    	boundingBoxHeightTextField.setTextFormatter(new TextFormatter<>(maxBoundedHeight, 0));
    	boundingBoxWidthTextField.setText("1");
    	boundingBoxHeightTextField.setText("1");
    	boundedBoxWidth = new AtomicInteger(1);
    	boundedBoxHeight = new AtomicInteger(1);
    }
}
