package webservices;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.Dependent;

import com.fazecast.jSerialComm.SerialPort;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Callables;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

import arduino.Arduino;


@Dependent
public class ArduinoSensor {
	Arduino board = null;
	boolean isOpenConnection = false;
	String strSelectedComPort = "";
	
	
	public boolean connectToWifi(String i_ssid, String i_pwd, String i_hostname ) {
		Arduino myBoard = connectToArduino();
		if( myBoard != null ){
			String strCmd = "WIFI_CONNECT_CMD:" + i_ssid + "," + i_pwd + "," + i_hostname + ";\n";
			myBoard.serialWrite(strCmd);
			try {
				return new SimpleTimeLimiter().callWithTimeout(connectionToWifiSucceeded(myBoard, i_ssid), 10, TimeUnit.SECONDS, true);
			} 
		    catch (InterruptedException e) {
		        Thread.currentThread().interrupt();
		        throw Throwables.propagate(e);
		    }
		    catch (Exception e) {
		        throw Throwables.propagate(e);
		    }
		    finally {
		        
		    }
		}		
		return false;
	}
		
	private Callable<Boolean> connectionToWifiSucceeded(Arduino board, String i_ssid) throws InterruptedException {
		List<String> serialLines = Arrays.asList( board.serialRead().split("\n") );
		while( !serialLines.isEmpty() ){
			System.out.println(serialLines.toString());
			if( serialLines.stream().anyMatch(serialLine -> serialLine.startsWith("Connected to " + i_ssid)) ){
				return Callables.returning(true);
			}
			serialLines = Arrays.asList( board.serialRead().split("\n") );
			wait(1000);
		}
				
		return Callables.returning(false);
	}

	public SerialPort[] getSeriaPorts() {
		return SerialPort.getCommPorts();
	}

	public void setRequestedSerialPort(String i_strSystemPortName) {
		this.strSelectedComPort = i_strSystemPortName;
	}
		
	protected void closeArduinoConnection(){
		getOpenBoard().closeConnection();
	}

	protected SerialPort getLastNotYetOpenSerialPort(){
		List<SerialPort> portNames = Arrays.asList(SerialPort.getCommPorts());
		return portNames.stream().sorted(Collections.reverseOrder()).filter( serialPort -> !serialPort.isOpen() ).findFirst().orElse(null);
	}
		
	public SerialPort getCurrentSerialPort(){
		SerialPort port = null;
		if( this.strSelectedComPort.isEmpty() ){
			port = getLastNotYetOpenSerialPort();
		} else {
			port = SerialPort.getCommPort(this.strSelectedComPort);
			if(port == null){
				port = getLastNotYetOpenSerialPort();
			} 
		}
		
		if( port!= null) {
			this.strSelectedComPort = port.getSystemPortName();
		}
		
		return port; 
	}
	
	protected Arduino getOpenBoard(){
		if( (board == null) || !isOpenConnection || !board.getSerialPort().getSystemPortName().equals(this.strSelectedComPort) ){
			SerialPort port = getCurrentSerialPort();
			if( (port != null) && !port.isOpen() ){
				if( board != null) board.closeConnection();
				//if( board == null) 
				board = new Arduino(port.getSystemPortName(), 115200);
				isOpenConnection = board.openConnection();
				if( !isOpenConnection ){
					System.out.println("Unable to open the port.");
				}
			}
		}
		return board;
	}

	
	protected Arduino connectToArduino(){
		if( (board == null) || !isOpenConnection || !board.getSerialPort().getSystemPortName().equals(this.strSelectedComPort) ){
			List<SerialPort> portNames = Arrays.asList(SerialPort.getCommPorts());
			if( null == portNames.stream().filter( serialPort -> null != connectToArduino(serialPort) ).findFirst().orElse(null) ){
				return null;
			}
		}
		return board;
	}

	private Arduino connectToArduino(SerialPort serialPort) {
		Arduino resBoard = null;
		if( (serialPort != null) && !serialPort.isOpen() ){
			if( board != null) board.closeConnection();
			//if( board == null) 
			resBoard = new Arduino(serialPort.getSystemPortName(), 115200);
			isOpenConnection = resBoard.openConnection();
			if( !isOpenConnection ){
				System.out.println("Connection failed on port: '" + serialPort.getSystemPortName() + "'");
				board = null;
				return null;
			} else {
				System.out.println("Connection succeeded on port: '" + serialPort.getSystemPortName() + "'");
				board = resBoard;
				return resBoard;
			}
		}
		return resBoard;
	}

}
