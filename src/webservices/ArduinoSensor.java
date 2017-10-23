package webservices;
import javax.enterprise.context.Dependent;

import com.fazecast.jSerialComm.SerialPort;

import arduino.Arduino;


@Dependent
public class ArduinoSensor {
	Arduino board = null;
	boolean isOpenConnection = false;
	String strSelectedComPort = "";
	
	
	public boolean connectToWifi(String i_ssid, String i_pwd ) {
		Arduino myBoard = getOpenBoard();
		if( myBoard != null ){
			String strCmd = "WIFI_CONNECT:" + i_ssid + "," + i_pwd + ";\n";
			myBoard.serialWrite(strCmd);
			System.out.println(myBoard.serialRead());
			return true;
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
		getOpenBoard().closeConnection();
	}

	protected SerialPort getDefaultSerialPort(){
		SerialPort[] portNames = SerialPort.getCommPorts();
		if( portNames.length > 1 && !portNames[1].isOpen() ){
			return portNames[1];
		} else if( portNames.length > 0 && !portNames[0].isOpen() ) {
			return portNames[0];
		} 
		return null;
	}
		
	public SerialPort getCurrentSerialPort(){
		SerialPort port = null;
		if( this.strSelectedComPort.isEmpty() ){
			port = getDefaultSerialPort();
		} else {
			port = SerialPort.getCommPort(this.strSelectedComPort);
			if(port == null){
				port = getDefaultSerialPort();
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

	

}
