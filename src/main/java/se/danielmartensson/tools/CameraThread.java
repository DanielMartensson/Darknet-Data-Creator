package se.danielmartensson.tools;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

import javafx.application.Platform;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.AllArgsConstructor;
import se.danielmartensson.Main;
import se.danielmartensson.tools.observablelists.Resolutions;
import se.danielmartensson.tools.observablelists.Webcams;

public class CameraThread extends Thread {

	private AtomicBoolean runCamera;
	private AtomicBoolean runRecording;
	private boolean hasOpen = false;

	private Webcam webcam;
	private int sampleTime;
	private Double resolutionHeight;
	private Double resolutionWidht;
	private int classNumber;
	private File selectedSaveToFolderDirectory;
	private Integer boundedBoxWidth;
	private Integer boundedBoxHeight;
	private ImageView cameraImageView;

	public CameraThread(AtomicBoolean runCamera, AtomicBoolean runRecording) {
		this.runCamera = runCamera;
		this.runRecording = runRecording;
	}

	@Override
	public void run() {
		while (Main.RUNTHREAD) {
			while (runCamera.get()) {
				Platform.runLater(() -> {
					//openCamera();
					//BufferedImage image = getImage();
					//BufferedImage mirror = mirrorImage(image);
					//BufferedImage cut = cutImage(mirror);
					//File saved = saveImage(cut);
					//showSavedImage(saved);
					threadSleep();
					if (runRecording.get()) {

					}
					System.out.println("hello");
					//closeCameraInside(); // Will only happen if we close from the controller
				});
			}
			//closeCameraOutside();
		}
	}

	private void closeCameraOutside() {
		if (hasOpen == true && runCamera.get() == false) {
			//webcam.close();
			hasOpen = false;
			System.out.println("close camera");
		}
	}

	private void closeCameraInside() {
		if (hasOpen == true && runCamera.get() == false) {
			//webcam.close();
			hasOpen = false;
			System.out.println("close camera");
		}
	}

	private void openCamera() {
		if (!hasOpen) {
			//webcam.open();
			hasOpen = true;
			System.out.println("Open camera");
		}
	}

	private void threadSleep() {
		try {
			Thread.sleep(2000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void showSavedImage(File saved) {
		Image image = new Image(saved.toURI().toString());
		cameraImageView.setImage(image);
	}

	private File saveImage(BufferedImage cut) {

		try {
			File outputImage = new File("camera.jpg");
			ImageIO.write(cut, "jpg", outputImage);
			return outputImage;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private BufferedImage cutImage(BufferedImage mirror) {
		return mirror.getSubimage(0, 0, resolutionWidht.intValue(), resolutionHeight.intValue());
	}

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
		try {
			BufferedImage cameraImage = webcam.getImage();
			ByteArrayOutputStream byteImage = new ByteArrayOutputStream();
			ImageIO.write(cameraImage, "jpg", byteImage);
			byte[] streamBytes = byteImage.toByteArray();
			BufferedImage image = ImageIO.read(new ByteArrayInputStream(streamBytes));
			return image;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	public void setComponents(Webcam webcam, ChoiceBox<Integer> sampleIntervallDropdownButton, ChoiceBox<Resolutions> pictureResolutionDropdownButton, ChoiceBox<Integer> classNumberDropdownButton, File selectedSaveToFolderDirectory, Integer boundedBoxWidth, Integer boundedBoxHeight, ImageView cameraImageView) {
		this.webcam = webcam;
		sampleTime = sampleIntervallDropdownButton.getSelectionModel().getSelectedItem().intValue();
		resolutionHeight = pictureResolutionDropdownButton.getSelectionModel().getSelectedItem().getHeight();
		resolutionWidht = pictureResolutionDropdownButton.getSelectionModel().getSelectedItem().getWidth();
		classNumber = classNumberDropdownButton.getSelectionModel().getSelectedItem().intValue();
		this.selectedSaveToFolderDirectory = selectedSaveToFolderDirectory;
		this.boundedBoxWidth = boundedBoxWidth;
		this.boundedBoxHeight = boundedBoxHeight;
		this.cameraImageView = cameraImageView;
	}

}
