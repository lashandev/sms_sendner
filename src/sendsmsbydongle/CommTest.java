/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sendsmsbydongle;

/**
 *
 * @author Lashan chandika
 */
import com.sun.comm.Win32Driver;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Formatter;
import org.smslib.helper.CommPortIdentifier;
import org.smslib.helper.SerialPort;

public class CommTest {

    private static final String _NO_DEVICE_FOUND = "  no device found";
    private final static Formatter _formatter = new Formatter(System.out);
    static CommPortIdentifier portId;
    static Enumeration<CommPortIdentifier> portList;
    static int bauds[] = {9600};//, 14400, 19200};//, 28800, 33600, 38400, 56000, 57600, 115200};

    /**
     * Wrapper around {@link CommPortIdentifier#getPortIdentifiers()} to be
     * avoid unchecked warnings.
     */
    private static Enumeration<CommPortIdentifier> getCleanPortIdentifiers() {
        return CommPortIdentifier.getPortIdentifiers();
    }

    public static void main(String[] args) {
    }

    public static String getComPort(int result) {
        System.out.println("\nSearching for devices...");
        Win32Driver w32Driver = new Win32Driver();
        w32Driver.initialize();
        portList = getCleanPortIdentifiers();
        boolean isbreaked = false;
        while (portList.hasMoreElements() & !isbreaked) {
            portId = portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                _formatter.format("%nFound port: %-5s%n", portId.getName());
                for (int i = 0; i < bauds.length; i++) {
                    SerialPort serialPort = null;
                    _formatter.format("       Trying at %6d...", bauds[i]);
                    try {
                        InputStream inStream;
                        OutputStream outStream;
                        int c;
                        String response;
                        try {
                            serialPort = portId.open("SMSLibCommTester", 1971);
                            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
                            serialPort.setSerialPortParams(bauds[i], SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                            inStream = serialPort.getInputStream();
                            outStream = serialPort.getOutputStream();
                            serialPort.enableReceiveTimeout(1000);
                            c = inStream.read();
                            while (c != -1) {
                                c = inStream.read();
                            }
                            outStream.write('A');
                            outStream.write('T');
                            outStream.write('\r');
                            Thread.sleep(1000);
                            response = "";
                            StringBuilder sb = new StringBuilder();
                            c = inStream.read();
                            while (c != -1) {
                                sb.append((char) c);
                                c = inStream.read();
                            }
                            response = sb.toString();
                            if (response.indexOf("OK") >= 0) {
                                try {
                                    System.out.print("  Getting Info...");
                                    outStream.write('A');
                                    outStream.write('T');
                                    outStream.write('+');
                                    outStream.write('C');
                                    outStream.write('G');
                                    outStream.write('M');
                                    outStream.write('M');
                                    outStream.write('\r');
                                    response = "";
                                    c = inStream.read();
                                    while (c != -1) {
                                        response += (char) c;
                                        c = inStream.read();
                                    }
                                    String f = response.replaceAll("\\s+OK\\s+", "").replaceAll("\n", "").replaceAll("\r", "");
                                    System.out.println(" Found: " + f);
                                    String[] modemModels = {"e303", "e173","e171","e1550"};
                                    
                                    for (int j = 0; j < modemModels.length; j++) {
                                        if (f.toLowerCase().contains(modemModels[j].toLowerCase())) {
                                            result = 1;
                                            isbreaked = true;//break;
                                            return portId.getName();
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println(_NO_DEVICE_FOUND);
                                    result = 0;
                                }
                            } else {
                                System.out.println(_NO_DEVICE_FOUND);
                                result = 0;
                            }
                        } catch (IOException | InterruptedException e) {
                        }
                    } catch (Exception e) {
                        System.out.print(_NO_DEVICE_FOUND);
                        result = 0;
                        Throwable cause = e;
                        while (cause.getCause() != null) {
                            cause = cause.getCause();
                        }
                        System.out.println(" (" + cause.getMessage() + ")");
                    } finally {
                        if (serialPort != null) {
                            serialPort.close();
                        }
                    }
                }
            }
        }
        System.out.println("\nTest complete.");
        return "";
    }
}
