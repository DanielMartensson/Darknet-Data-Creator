package se.danielmartensson.tools;

import java.awt.Dimension;
import java.util.List;
import com.github.sarxos.webcam.Webcam;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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

	public boolean findResolutions(ChoiceBox<Webcams> usbCameraDropdownButton, ChoiceBox<Resolutions> cameraResolutionDropdownButton, Button openCameraButton, Button closeCameraButton) {
		// List our dimensions of selected camera
		if (usbCameraDropdownButton == null)
			return false;
		Dimension[] dimensions = usbCameraDropdownButton.getSelectionModel().getSelectedItem().getWebcam().getViewSizes();

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

	public void setResolution(ChoiceBox<Webcams> usbCameraDropdownButton, Resolutions resolutions) {
		int width = resolutions.getWidth().intValue();
		int height = resolutions.getHeight().intValue();
		usbCameraDropdownButton.getSelectionModel().getSelectedItem().getWebcam().setViewSize(new Dimension(width, height));
	}
}
