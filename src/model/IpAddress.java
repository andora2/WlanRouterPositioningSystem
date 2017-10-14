package model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class IpAddress implements Serializable {
	private static final long serialVersionUID = 1L;

	int addr0;
	int addr1;
	int addr2;
	int addr3;
	
}
