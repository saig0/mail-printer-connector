package org.camunda.bpm.printer.cups;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CupsJobStatusService {

	@Value("${printer.name:MyPrinter}")
	private String printerName;

	@Value("${cups.status.waitTime:30}")
	private int waitTimeInSeconds;

	private LocalDateTime lastUpdate;

	private List<String> cachedOpenJobs = new ArrayList<>();

	public boolean isCompleted(String jobId) throws IOException {

		if (fetchNewJobStatus()) {

			cachedOpenJobs = new GetJobStatusCommand(printerName).getOpenJobs();

			lastUpdate = LocalDateTime.now();
		}

		return !cachedOpenJobs.contains(jobId);
	}

	private boolean fetchNewJobStatus() {
		return lastUpdate == null || LocalDateTime.now().isAfter(lastUpdate.withSecond(waitTimeInSeconds));
	}

}
