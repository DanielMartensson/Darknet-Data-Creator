package se.danielmartensson.tools;

import java.awt.Dimension;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.sarxos.webcam.Webcam;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import se.danielmartensson.tools.observablelists.Resolutions;
import se.danielmartensson.tools.observablelists.Webcams;

public class SelectedWebCam {

	public boolean scanWebCams(ChoiceBox<Webcams> usbCameraDropdownButton) {
		// List our cameras
		List<Webcam> webcamsList = Webcam.getWebcams();
		if (webcamsList == null)
			return false;

		// Check if we found any cameras
		boolean camerasFound;
		if (webcamsList.size() > 0) {
			usbCameraDropdownButton.setDisable(false);
			camerasFound = true;
		} else {
			usbCameraDropdownButton.setDisable(true);
			camerasFound = false;
		}

		// Fill the drop down button
		ObservableList<Webcams> listOfCameraNames = FXCollections.observableArrayList();
		for (Webcam webcam : webcamsList)
			listOfCameraNames.add(new Webcams(webcam.getName(), webcam));
		usbCameraDropdownButton.setItems(listOfCameraNames);

		return camerasFound;
	}

	public boolean findResolutions(Webcam webcam, ChoiceBox<Resolutions> cameraResolutionDropdownButton, Button openCameraButton, Button closeCameraButton) {
		// List our dimensions of selected camera
		if (webcam == null)
			return false;
		Dimension[] dimensions = webcam.getViewSizes();

		// Check if we found any dimensions
		boolean dimensionsFound;
		if (dimensions.length > 0) {
			cameraResolutionDropdownButton.setDisable(false);
			openCameraButton.setDisable(false);
			dimensionsFound = true;
		} else {
			cameraResolutionDropdownButton.setDisable(true);
			openCameraButton.setDisable(true);
			closeCameraButton.setDisable(true);
			dimensionsFound = false;
		}

		// Fill the drop down button
		ObservableList<Resolutions> listOfResolutions = FXCollections.observableArrayList();
		for (Dimension dimension : dimensions)
			listOfResolutions.add(new Resolutions(dimension.getWidth(), dimension.getHeight(), dimension));
		cameraResolutionDropdownButton.setItems(listOfResolutions);
		return dimensionsFound;

	}

	public void disableComponents(Button scanButton, ChoiceBox<Webcams> usbCameraDropdownButton, ChoiceBox<Resolutions> cameraResolutionDropdownButton, ChoiceBox<Integer> sampleIntervallDropdownButton, ChoiceBox<Resolutions> pictureResolutionDropdownButton, ChoiceBox<Integer> classNumberDropdownButton, Button saveToFolderButton, Button openCameraButton, Button closeCameraButton, TextField boundingBoxHeightTextField, TextField boundingBoxWidthTextField, Button startRecordingButton, Button stopRecordingButton, AtomicBoolean runCamera, File selectedSaveToFolderDirectory) {
		if (selectedSaveToFolderDirectory == null)
			return;

		runCamera.set(true);

		// We want to disable this
		scanButton.setDisable(true);
		usbCameraDropdownButton.setDisable(true);
		cameraResolutionDropdownButton.setDisable(true);
		sampleIntervallDropdownButton.setDisable(true);
		pictureResolutionDropdownButton.setDisable(true);
		classNumberDropdownButton.setDisable(true);
		saveToFolderButton.setDisable(true);
		openCameraButton.setDisable(true);
		stopRecordingButton.setDisable(true);

		// We want to enable this
		closeCameraButton.setDisable(false);
		startRecordingButton.setDisable(false);
		boundingBoxHeightTextField.setDisable(false);
		boundingBoxWidthTextField.setDisable(false);

	}

	public void setResolution(Webcam webcam, Resolutions resolutions) {
		int width = resolutions.getWidth().intValue();
		int height = resolutions.getHeight().intValue();
		webcam.setViewSize(new Dimension(width, height));
	}

	public void enableComponents(Button scanButton, ChoiceBox<Webcams> usbCameraDropdownButton, ChoiceBox<Resolutions> cameraResolutionDropdownButton, ChoiceBox<Integer> sampleIntervallDropdownButton, ChoiceBox<Resolutions> pictureResolutionDropdownButton, ChoiceBox<Integer> classNumberDropdownButton, Button saveToFolderButton, Button openCameraButton, Button closeCameraButton, TextField boundingBoxHeightTextField, TextField boundingBoxWidthTextField, Button startRecordingButton, Button stopRecordingButton, AtomicBoolean runCamera) {
		runCamera.set(false);

		// We want to enable this
		scanButton.setDisable(false);
		usbCameraDropdownButton.setDisable(false);
		cameraResolutionDropdownButton.setDisable(false);
		sampleIntervallDropdownButton.setDisable(false);
		pictureResolutionDropdownButton.setDisable(false);
		classNumberDropdownButton.setDisable(false);
		saveToFolderButton.setDisable(false);
		openCameraButton.setDisable(false);

		// We want to disable this
		closeCameraButton.setDisable(true);
		startRecordingButton.setDisable(true);
		stopRecordingButton.setDisable(true);
		boundingBoxHeightTextField.setDisable(true);
		boundingBoxWidthTextField.setDisable(true);

	}
}
