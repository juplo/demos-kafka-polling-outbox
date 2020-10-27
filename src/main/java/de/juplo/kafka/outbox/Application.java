package de.juplo.kafka.outbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CountDownLatch;


@SpringBootApplication
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
            closeLatch.countDown();
          }
        });

    closeLatch.await();
  }
}
