package se.danielmartensson.threads;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import se.danielmartensson.Main;
import se.danielmartensson.controller.MainController;
import se.danielmartensson.tools.FileHandeling;
import se.danielmartensson.tools.observablelists.Resolutions;
import se.danielmartensson.tools.observablelists.Webcams;

public class CameraThread extends Thread {

	private AtomicBoolean runCamera;
	private AtomicBoolean runRecording;
	private boolean hasOpen = false;
	private boolean boundedBoxIsApplied = false;

	// From the GUI
	private Webcam webcam;
	private String sampleTime = "1.0"; // Initial sample time
	private Double resolutionHeight;
	private Double resolutionWidht;
	private int classNumber;
	private File selectedSaveToFolder;
	private AtomicInteger boundedBoxWidth;
	private AtomicInteger boundedBoxHeight;
	private ImageView cameraImageView;
	private BufferedImage cutNoBound;
	private Button startRecordingButton;
	private Button stopRecordingButton;
	private Button closeCameraButton;
	private Button openCameraButton;
	private Button scanButton;
	private ChoiceBox<Webcams> usbCameraDropdownButton;
	private ChoiceBox<Resolutions> cameraResolutionDropdownButton;
	private ChoiceBox<String> sampleIntervallDropdownButton;
	private ChoiceBox<Resolutions> pictureResolutionDropdownButton;
	private ChoiceBox<Integer> classNumberDropdownButton;
	private Button saveToFolderButton;
	private TextField boundingBoxHeightTextField;
	private TextField boundingBoxWidthTextField;

	public CameraThread(AtomicBoolean runCamera, AtomicBoolean runRecording) {
		this.runCamera = runCamera;
		this.runRecording = runRecording;
	}

	@Override
	public void run() {
		while (Main.RUNTHREAD.get()) {
			while (runCamera.get()) {
				Platform.runLater(() -> {
					openCamera();
					if(hasOpen) {
						BufferedImage image = getImage();
						//BufferedImage mirror = mirrorImage(image);
						BufferedImage cut = cutImageWithBoundedBox(image);
						File savedImage = saveImage(cut);
						showSavedImage(savedImage);
						if (runRecording.get() && boundedBoxIsApplied) {
							disableTheseComponentsWhenStartRecording();
							File[] classes = checkClassFolderAndClassPathsFileStatus();
							copySavedImageToClassFolder(classes);
						} else {
							enableTheseComponentsWhenStopRecording();
						}
					}else {
						closeCamera();
					}
	
				});
				threadSleep();
			}
			closeCamera();
			threadSleep();
		}
		closeCamera();
	}

	private void copySavedImageToClassFolder(File[] classes) {
		// Before we proceed, we need to find at what index we are at
		int classFileName = classes.length + 1;

		// Write these class files now
		File classPathFile = null;
		File classImage = null;
		File classImageLabel = null;
		if (FileHandeling.operativeSystem.contains("Windows")) {
			classPathFile = new File(selectedSaveToFolder.getAbsolutePath() + "\\ClassPaths.txt");
			classImage = new File(selectedSaveToFolder.getAbsolutePath() + "\\" + classNumber + "\\" + classFileName + ".png");
			classImageLabel = new File(selectedSaveToFolder.getAbsolutePath() + "\\" + classNumber + "\\" + classFileName + ".txt");
		} else {
			classPathFile = new File(selectedSaveToFolder.getAbsolutePath() + "/ClassPaths.txt");
			classImage = new File(selectedSaveToFolder.getAbsolutePath() + "/" + classNumber + "/" + classFileName + ".png");
			classImageLabel = new File(selectedSaveToFolder.getAbsolutePath() + "/" + classNumber + "/" + classFileName + ".txt");
		}

		// Create files that don't exist
		try {
			classImage.createNewFile();
			classImageLabel.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Moving the saved image to the class folder and rename it
		try {
			ImageIO.write(cutNoBound, "png", classImage);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Get the relative size for the class label. Notice that x, y is center of the
		// rectangle
		float cw = cutNoBound.getWidth();
		float ch = cutNoBound.getHeight();
		float bw = boundedBoxWidth.floatValue();
		float bh = boundedBoxHeight.floatValue();
		float x = (cw / 2) / cw; // Always 0.5
		float y = (ch / 2) / ch; // Same here
		float width = bw / cw;
		float height = bh / ch;

		// Write the label and the path
		try {
			BufferedWriter labelRow = new BufferedWriter(new FileWriter(classImageLabel));
			labelRow.write(classNumber + " " + x + " " + y + " " + width + " " + height); // <object-class> <x> <y> <width> <height>
			labelRow.close();
			BufferedWriter pathRow = new BufferedWriter(new FileWriter(classPathFile, true)); // Append
			pathRow.write(classImage.getAbsolutePath() + "\n");
			pathRow.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File[] checkClassFolderAndClassPathsFileStatus() {
		// Check if we have created folder and file
		File classFolder = null;
		File classPathFile = null;
		if (FileHandeling.operativeSystem.contains("Windows")) {
			classFolder = new File(selectedSaveToFolder.getAbsolutePath() + "\\" + classNumber);
			classPathFile = new File(selectedSaveToFolder.getAbsolutePath() + "\\ClassPaths.txt");
		} else {
			classFolder = new File(selectedSaveToFolder.getAbsolutePath() + "/" + classNumber);
			classPathFile = new File(selectedSaveToFolder.getAbsolutePath() + "/ClassPaths.txt");
		}
		if (!classFolder.exists())
			classFolder.mkdirs();
		if (!classPathFile.exists()) {
			try {
				classPathFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return classFolder.listFiles((dir, name) -> name.endsWith(".png")); // Get all the files and its names
	}

	private void closeCamera() {
		if (hasOpen) {
			webcam.close();
			hasOpen = false;
			enableTheseComponentsWhenStopCamera();
		}
	}

	private void openCamera() {
		if (!hasOpen) {
			try {
				webcam.open();
				hasOpen = true;
			}catch(Exception e) {
				hasOpen = false;
			}
			disableTheseComponentsWhenStartCamera();
		}
	}

	private void threadSleep() {
		try {
			long time = (long) (Double.parseDouble(sampleTime) * 1000.0);
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void showSavedImage(File savedImage) {
		Image show = new Image(savedImage.toURI().toString());
		cameraImageView.setImage(show);
	}

	private File saveImage(BufferedImage cut) {
		try {
			String cameraImagePath = "";
			if (FileHandeling.operativeSystem.contains("Windows")) {
				cameraImagePath = selectedSaveToFolder.getAbsolutePath() + "\\camera.png";
			} else {
				cameraImagePath = selectedSaveToFolder.getAbsolutePath() + "/camera.png";
			}
			File savedImage = new File(cameraImagePath);
			ImageIO.write(cut, "png", savedImage);
			return savedImage;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private BufferedImage cutImageWithBoundedBox(BufferedImage image) {
		int widthCamera = image.getWidth();
		int heightCamera = image.getHeight();
		int widthResolution = resolutionWidht.intValue();
		int heightResolution = resolutionHeight.intValue();

		// We need to have a cutted picture in the center
		int x = widthCamera / 2 - widthResolution / 2;
		int y = heightCamera / 2 - heightResolution / 2;
		if (x <= 0 || y <= 0) {
			boundedBoxIsApplied = false;
			return image; // No bounded box - None square
		} else {
			BufferedImage cut = image.getSubimage(x, y, widthResolution, heightResolution); // All sub images are square
			cutNoBound = copyImage(cut);
			createBoundedBox(cut);
			return cut;
		}
	}

	public BufferedImage copyImage(BufferedImage source) {
		BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = b.getGraphics();
		g.drawImage(source, 0, 0, null);
		g.dispose();
		return b;
	}

	private void createBoundedBox(BufferedImage cut) {
		int cw = cut.getWidth();
		int ch = cut.getHeight();
		int bw = boundedBoxWidth.intValue();
		int bh = boundedBoxHeight.intValue();
		int rgb = 0xFFFF0000; // Full red with opacity 1

		// Need to be sure that we are not over the limits
		if (bw > cw || bh > ch) {
			boundedBoxIsApplied = false;
			return;
		}

		// Create the box now
		for (int x = 0; x < cw; x++) {
			for (int y = 0; y < ch; y++) {
				// Horizontal lines
				if (x >= cw / 2 - bw / 2 && x < cw / 2 + bw / 2 && y == ch / 2 - bh / 2) {
					cut.setRGB(x, y, rgb);
					cut.setRGB(x, y + bh - 1, rgb); // Mirror
				}
				// Vertical lines
				if (y >= ch / 2 - bh / 2 && y < ch / 2 + bh / 2 && x == cw / 2 - bw / 2) {
					cut.setRGB(x, y, rgb);
					cut.setRGB(x + bw - 1, y, rgb); // Mirror
				}
			}
		}
		boundedBoxIsApplied = true;
	}

	@SuppressWarnings("unused")
	private BufferedImage mirrorImage(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage mirror = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < height; y++) {
			for (int lx = 0, rx = width - 1; lx < width; lx++, rx--) {
				int p = image.getRGB(lx, y);
				mirror.setRGB(rx, y, p);
			}
		}
		return mirror;
	}

	private BufferedImage getImage() {
		return copyImage(webcam.getImage()); // We need to specify the Image Type. webcam.getImage() gives type 0
	}

	public void disableTheseComponentsWhenStartRecording() {
		stopRecordingButton.setDisable(false);
		startRecordingButton.setDisable(true);
		closeCameraButton.setDisable(true);
	}

	public void disableTheseComponentsWhenStartCamera() {
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

	public void enableTheseComponentsWhenStopRecording() {
		stopRecordingButton.setDisable(true);
		startRecordingButton.setDisable(false);
		closeCameraButton.setDisable(false);
	}

	public void enableTheseComponentsWhenStopCamera() {
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

	public void setComponents(MainController mainController) {
		webcam = mainController.getUsbCameraDropdownButton().getSelectionModel().getSelectedItem().getWebcam();
		sampleTime = mainController.getSampleIntervallDropdownButton().getSelectionModel().getSelectedItem();
		resolutionHeight = mainController.getPictureResolutionDropdownButton().getSelectionModel().getSelectedItem().getHeight();
		resolutionWidht = mainController.getPictureResolutionDropdownButton().getSelectionModel().getSelectedItem().getWidth();
		classNumber = mainController.getClassNumberDropdownButton().getSelectionModel().getSelectedItem().intValue();
		selectedSaveToFolder = mainController.getSelectedSaveToFolder();
		boundedBoxWidth = mainController.getBoundedBoxWidth();
		boundedBoxHeight = mainController.getBoundedBoxHeight();
		cameraImageView = mainController.getCameraImageView();
		startRecordingButton = mainController.getStartRecordingButton();
		stopRecordingButton = mainController.getStopRecordingButton();
		closeCameraButton = mainController.getCloseCameraButton();
		openCameraButton = mainController.getOpenCameraButton();
		scanButton = mainController.getScanButton();
		usbCameraDropdownButton = mainController.getUsbCameraDropdownButton();
		cameraResolutionDropdownButton = mainController.getCameraResolutionDropdownButton();
		sampleIntervallDropdownButton = mainController.getSampleIntervallDropdownButton();
		pictureResolutionDropdownButton = mainController.getPictureResolutionDropdownButton();
		classNumberDropdownButton = mainController.getClassNumberDropdownButton();
		saveToFolderButton = mainController.getSaveToFolderButton();
		boundingBoxHeightTextField = mainController.getBoundingBoxHeightTextField();
		boundingBoxWidthTextField = mainController.getBoundingBoxWidthTextField();
	}
}
