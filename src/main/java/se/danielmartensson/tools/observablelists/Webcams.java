package se.danielmartensson.tools.observablelists;

import com.github.sarxos.webcam.Webcam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Webcams {

	private String webcamName;
	private Webcam webcam;

	@Override
	public String toString() {
		return webcamName;
	}

}
