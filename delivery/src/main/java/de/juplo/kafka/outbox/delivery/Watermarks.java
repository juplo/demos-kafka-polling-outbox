package de.juplo.kafka.outbox.delivery;


public class Watermarks
{
  private long[] watermarks = new long[0];


  public synchronized void set(int partition, long watermark)
  {
    if (partition >= watermarks.length)
    {
      long[] resized = new long[partition + 1];
      for (int i = 0; i < watermarks.length; i++)
        resized[i] = watermarks[i];
      watermarks = resized;
    }

    watermarks[partition] = watermark;
  }

  public synchronized long getLowest()
  {
    long lowest = Long.MAX_VALUE;

    for (int i = 0; i < watermarks.length; i++)
      if (watermarks[i] < lowest)
        lowest = watermarks[i];

    return lowest;
  }
}
