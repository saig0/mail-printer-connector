package org.camunda.bpm.printer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.runtime.Job;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
public class PrintProcessTest {

	@Autowired
	private ManagementService managementService;

	@Test
	public void pollMails() {
		Job timerJob = managementService.createJobQuery().timers().singleResult();
		assertThat(timerJob, is(notNullValue()));

		managementService.executeJob(timerJob.getId());
	}

	@Test
	public void mailPush() throws Exception {
		// start mail connector with process application

		// wait for new messages
		Thread.sleep(30 * 1000);
	}

}
