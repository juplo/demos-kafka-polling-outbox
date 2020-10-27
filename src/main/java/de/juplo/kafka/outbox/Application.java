package de.juplo.kafka.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CountDownLatch;


@SpringBootApplication
@Slf4j
public class Application
{
  public static void main(String[] args) throws Exception
  {
    SpringApplication.run(Application.class, args);

    final CountDownLatch closeLatch = new CountDownLatch(1);

    Runtime
        .getRuntime()
        .addShutdownHook(new Thread()
        {
          @Override
          public void run()
          {
            log.info("Closing application...");
            closeLatch.countDown();
          }
        });

    closeLatch.await();
  }
}
