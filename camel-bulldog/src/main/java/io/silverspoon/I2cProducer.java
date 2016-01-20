package io.silverspoon;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;

import java.io.IOException;

import io.silverspoon.bulldog.core.io.bus.i2c.I2cBus;
import io.silverspoon.bulldog.core.io.bus.i2c.I2cConnection;

/**
 * The Bulldog producer.
 */
public class I2cProducer extends BulldogProducer {

   private static final Object lock = new Object();
   private final I2cBus i2c;
   private I2cConnection connection;
   private static final int READ_BUFFER_SIZE = 256;

   public I2cProducer(BulldogEndpoint endpoint) {
      super(endpoint);
      i2c = getEndpoint().getBoard().getI2cBuses().get(0);
   }

   public void process(Exchange exchange) throws Exception {
      final byte[] buffer = new byte[READ_BUFFER_SIZE];
      boolean invalidData = false;
      synchronized (lock) {
         final I2cConnection connection = i2c.createI2cConnection(Integer.valueOf(getEndpoint().getAddress()));
         try {
            connection.writeBytes((byte[]) exchange.getIn().getBody());
            if (exchange.getPattern().equals(ExchangePattern.InOut)) {
               connection.readBytes(buffer);
               exchange.getIn().setBody(buffer);
            }
         } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new IOException("Unable to read values from I2C bus (" + i2c.getName() + ")!");
         }
      }
   }
}