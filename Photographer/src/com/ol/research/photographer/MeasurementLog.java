package com.ol.research.photographer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Vector;

public class MeasurementLog {
	
	Vector<MeasurementResult> ResultVector = new Vector<MeasurementResult> ();
	
	void push(MeasurementResult Result)
	{
		ResultVector.add(Result);
	}
	void clear()
	{
		ResultVector.clear();
	}
	
	void WriteJSON(OutputStream os)
	{
		try {
	        String JSON_message = new String("{\"type\":\"measurementlog\"}#");
            DataOutputStream output = new DataOutputStream(os);     
            output.writeUTF(JSON_message);
            output.flush();
	        
			ObjectOutputStream VectorData = new ObjectOutputStream(os);
			VectorData.writeObject(ResultVector);
			VectorData.flush();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
