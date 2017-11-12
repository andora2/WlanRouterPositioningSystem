package webservices;
import java.awt.HeadlessException;
import java.util.ArrayList;
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
	
	
	public String connectToWifi(String i_ssid, String i_pwd, String i_hostname ) {
		Arduino myBoard = connectToArduino();
		if( myBoard != null ){
			String strCmd = "WIFI_CONNECT_CMD:" + i_ssid + "," + i_pwd + "," + i_hostname + ";\n";
			myBoard.serialWrite(strCmd);
			try {
				if( connectionToWifiSucceeded(myBoard, i_ssid) ){
					return getIp(myBoard);
				}
				//return new SimpleTimeLimiter().callWithTimeout(connectionToWifiSucceeded(myBoard, i_ssid), 10, TimeUnit.SECONDS, true);
			} 
		    catch (InterruptedException e) {
		        Thread.currentThread().interrupt();
		        throw Throwables.propagate(e);
		    }
		    catch (Exception e) {
		        throw Throwables.propagate(e);
		    }
		    finally {
			    	if( myBoard != null ){ 
			    		myBoard.closeConnection();
			    	}
		    	}
		}		
		return "";
	}

	private String getIp(Arduino board) throws InterruptedException {
		String res = "";
		String strCmd = "GET_IP:;\n";
		board.serialWrite(strCmd);
		List<String> serialLines = Arrays.asList( board.serialRead().replaceAll("\n", " ").split(";") );
		int i = 0;
		while( !serialLines.isEmpty() && i<10){
			System.out.println(serialLines.toString());
			res = serialLines.stream().filter(serialLine -> serialLine.trim().startsWith("GOT_IP:")).findFirst().orElse("");
			if( !res.isEmpty() ){
				String[] resList = !res.isEmpty()? res.split(":"): null;
				res = resList.length == 2? resList[1]: "";
				break;
			}
			serialLines = Arrays.asList( board.serialRead().replaceAll("\n", " ").split(";") );
			Thread.sleep(1000);
			i++;
		}
				
		return res;
	}
	
	private boolean isWRPSSensor(Arduino board) throws InterruptedException {
		String strCmd = "IS_WRPS_SENSOR:;\n";
		board.serialWrite(strCmd);
		List<String> serialLines = Arrays.asList( board.serialRead().replaceAll("\n", " ").split(";") );
		int i = 0;
		while( !serialLines.isEmpty() && i<10){
			System.out.println(serialLines.toString());
			String foundReponse = serialLines.stream().filter(serialLine -> serialLine.trim().contains("IS_WRPS_SENSOR:YES")).findFirst().orElse("");
			if( !foundReponse.isEmpty() ){
				return true;
			}
			serialLines = Arrays.asList( board.serialRead().replaceAll("\n", " ").split(";") );
			Thread.sleep(1000);
			i++;
		}
				
		return false;
	}	
	private boolean connectionToWifiSucceeded(Arduino board, String i_ssid) throws InterruptedException {
		return waitForPatternInSerialOutStream( board, "Connected to " + i_ssid, 10);
	}

	private boolean waitForPatternInSerialOutStream(Arduino board, String i_strPattern, int i_nSeconds) throws InterruptedException { 
		List<String> serialLines = Arrays.asList( board.serialRead().replaceAll("\n", " ").split(";") );
		int i = 0;
		while( !serialLines.isEmpty() && i<i_nSeconds){
			System.out.println(serialLines.toString());
			for (String serialLine : serialLines) {
				if(serialLine.trim().startsWith(i_strPattern)){
					return true;
				}
			}
				
			/*if( serialLines.stream().anyMatch(serialLine -> serialLine.trim().startsWith(i_strPattern)) ){
				return true;
			}*/
			serialLines = Arrays.asList( board.serialRead().replaceAll("\n", " ").split(";") );
			Thread.sleep(1000);
			i++;
		}
				
		return false;
	}
	
	public SerialPort[] getSeriaPorts() {
		return SerialPort.getCommPorts();
	}

	public void setRequestedSerialPort(String i_strSystemPortName) {
		this.strSelectedComPort = i_strSystemPortName;
	}
		
	protected void closeArduinoConnection(){
		if( this.board != null ){
			this.board.closeConnection();
		}
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
			try{
				if( resBoard.openConnection() && isWRPSSensor(resBoard) ){
					System.out.println("Connection succeeded on port: '" + serialPort.getSystemPortName() + "'");
					board = resBoard;
					return resBoard;
				}
			} catch (HeadlessException e){
				e.printStackTrace();
				System.out.println("Exception while trying to open a connection the device on the current serial port. Try another port");
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.out.println("Exception during wait for pattern to make sure the serial device is one of our sensors. Try another port");
			}

			System.out.println("Connection failed on port: '" + serialPort.getSystemPortName() + "'");
			board = null;
			return null;
		}
		return resBoard;
	}

}
