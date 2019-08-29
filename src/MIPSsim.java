/* On my honor, I have neither given nor received unauthorized aid on this assignment */

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MIPSsim {
	
	File output;
	FileWriter writer;
	
	String[] instructions;
	String[] registers;
	String[] datamemory;
	ArrayList<String> INM = new ArrayList<String>();
	ArrayList<String> DAM = new ArrayList<String>();
	ArrayList<String> RGF = new ArrayList<String>();
	ArrayList<String> INB = new ArrayList<String>();
	ArrayList<String> LIB = new ArrayList<String>();
	ArrayList<String> AIB = new ArrayList<String>();
	ArrayList<String> ADB = new ArrayList<String>();
	ArrayList<String> REB = new ArrayList<String>();
	
	int step=0;
	
	public static void main(String[] args) {
		MIPSsim sim = new MIPSsim();
	}
	
	MIPSsim(){
		
		ReadFiles();
		
		//Create simulation.txt
		output = new File("simulation.txt");
		try {
			writer = new FileWriter(output);
		} catch (IOException e) {
			System.out.println("Write error! " + e); 
		}
		
		initialize();
		
		WriteToFile(step); //Step 0
		
		step++;
		
		for(;;) {
			if(!REB.isEmpty()) {
				Write(REB.get(0));
			}
			
			if(!ADB.isEmpty()) {
				Load(ADB.get(0));
			}
			
			if(!AIB.isEmpty()) {
				ALU(AIB.get(0));
			}
			
			if(!LIB.isEmpty()) {
				ADDR(LIB.get(0));
			}
			
			if(!INB.isEmpty()) {
				if(CheckInstr(INB.get(0)).equals("ADD") || CheckInstr(INB.get(0)).equals("SUB") || CheckInstr(INB.get(0)).equals("AND") || CheckInstr(INB.get(0)).equals("OR,")) {
					Issue1(INB.get(0));
				}
				else if(CheckInstr(INB.get(0)).equals("LD,")) {
					Issue2(INB.get(0));
				}
				else {
					try {
						writer.write("Wrong instruction code!");
					} catch (IOException e) {
						System.out.println("Write error! " + e);
					}
					System.out.println("Wrong instruction code!");
					break;
				}
			}
			
			if(!INM.isEmpty()) {
				Decode(INM.get(0));
			}
			
			
			WriteToFile(step);
			
			step++;
			
			if(INM.isEmpty() && INB.isEmpty() && LIB.isEmpty() && AIB.isEmpty() && ADB.isEmpty() && REB.isEmpty()) {
				break;
			}
		}
		
		try {
			writer.close();
		} catch (IOException e) {
			System.out.println("Write error! " + e);
		}
	}
	
	//Read files into arrays
	public void ReadFiles() {
		//Read instructions.txt
		try {
			instructions = readAllLines(Paths.get("instructions.txt"), Charset.defaultCharset());
		} catch (IOException e) {
			System.out.println("Read error! " + e);
		}
		
		//Read registers.txt
		try {
			registers = readAllLines(Paths.get("registers.txt"), Charset.defaultCharset());
		} catch (IOException e) {
			System.out.println("Read error! " + e);
		}
		
		//Read datamemory.txt
		try {
			datamemory = readAllLines(Paths.get("datamemory.txt"), Charset.defaultCharset());
		} catch (IOException e) {
			System.out.println("Read error! " + e);
		}
	}
	
	public static String[] readAllLines(Path path, Charset cs) throws IOException{
		List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
		String[] arr = lines.toArray(new String[lines.size()]);
		return arr;
	}
	
	//Initialize INM, DAM and RGF with file info
	public void initialize() {
		ArrayList<String> arrayList1 = new ArrayList<String>(Arrays.asList(instructions));
		INM = arrayList1;
		ArrayList<String> arrayList2 = new ArrayList<String>(Arrays.asList(datamemory));
		DAM = arrayList2;
		ArrayList<String> arrayList3 = new ArrayList<String>(Arrays.asList(registers));
		RGF = arrayList3;
	}
	
	public void WriteToFile(int step) {
		try {
			writer.write("STEP " + step + ":");
			writer.write(System.lineSeparator());
			writer.write("INM:" + inm());
			writer.write(System.lineSeparator());
			writer.write("INB:" + inb());
			writer.write(System.lineSeparator());
			writer.write("AIB:" + aib());
			writer.write(System.lineSeparator());
			writer.write("LIB:" + lib());
			writer.write(System.lineSeparator());
			writer.write("ADB:" + adb());
			writer.write(System.lineSeparator());
			writer.write("REB:" + reb());
			writer.write(System.lineSeparator());
			writer.write("RGF:" + rgf());
			writer.write(System.lineSeparator());
			writer.write("DAM:" + dam());
			if(!(INM.isEmpty() && INB.isEmpty() && LIB.isEmpty() && AIB.isEmpty() && ADB.isEmpty() && REB.isEmpty())) {
				writer.write(System.lineSeparator());
				writer.write(System.lineSeparator());
			}
			
		} catch (IOException e) {
			System.out.println("Write error! " + e);
		}
	}
	
	public String CheckInstr(String instr) {
		return instr.substring(1, 4);
	}
	
	//Transitions
	public void Decode(String s0) {
		String instrname = INM.get(0).substring(1, 3);
		if(instrname.equals("AD") || instrname.equals("SU") || instrname.equals("AN")) {
			StringBuilder str = new StringBuilder(INM.get(0).substring(0, 8));
			str.append(Read(INM.get(0).substring(8, 10)));
			str.append(INM.get(0).substring(10, 11));
			str.append(Read(INM.get(0).substring(11, 13)));
			str.append(INM.get(0).substring(13, 14));
			INB.add(str.toString());
			INM.remove(0);
		}
		else if(instrname.equals("OR") || instrname.equals("LD")) {
			StringBuilder str = new StringBuilder(INM.get(0).substring(0, 7));
			str.append(Read(INM.get(0).substring(7, 9)));
			str.append(INM.get(0).substring(9, 10));
			str.append(Read(INM.get(0).substring(10, 12)));
			str.append(INM.get(0).substring(12, 13));
			INB.add(str.toString());
			INM.remove(0);
		}
		else {
			System.out.println("Wrong instruction code!");
		}
			
		
	}
	
	public String Read(String s) {
		for(int i=0; i<registers.length; i++) {
			if(s.equals(RGF.get(i).substring(1, 3))) {
				s=RGF.get(i).substring(RGF.get(i).indexOf(",") + 1, RGF.get(i).indexOf(">"));
			}
		}
		return s;
	}
	
	public void Issue1(String instr1) {
		AIB.add(instr1);
		INB.remove(0);
	}
	
	public void Issue2(String instr2) {
		LIB.add(instr2);
		INB.remove(0);
	}
	
	public void ALU(String instr3) {
		String endResult = "";
		String result;
		String outcome;
		String op1;
		String op2;
		String instrname = instr3.substring(1, 3);
		switch(instrname) {
			case "AD":
				result = instr3.substring(5, 7);
				op1 = instr3.substring(8, instr3.lastIndexOf(","));
				op2 = instr3.substring(instr3.lastIndexOf(",") + 1, instr3.indexOf(">"));
				outcome = Integer.toString(Integer.parseInt(op1) + Integer.parseInt(op2));
				StringBuilder str1 = new StringBuilder("<");
				str1.append(result);
				str1.append(",");
				str1.append(outcome);
				str1.append(">");
				endResult = str1.toString();
				break;
			case "SU":
				result = instr3.substring(5, 7);
				op1 = instr3.substring(8, instr3.lastIndexOf(","));
				op2 = instr3.substring(instr3.lastIndexOf(",") + 1, instr3.indexOf(">"));
				outcome = Integer.toString(Integer.parseInt(op1) - Integer.parseInt(op2));
				StringBuilder str2 = new StringBuilder("<");
				str2.append(result);
				str2.append(",");
				str2.append(outcome);
				str2.append(">");
				endResult = str2.toString();
				break;
			case "AN":
				result = instr3.substring(5, 7);
				op1 = instr3.substring(8, instr3.lastIndexOf(","));
				op2 = instr3.substring(instr3.lastIndexOf(",") + 1, instr3.indexOf(">"));
				outcome = Integer.toString(Integer.parseInt(op1) & Integer.parseInt(op2));
				StringBuilder str3 = new StringBuilder("<");
				str3.append(result);
				str3.append(",");
				str3.append(outcome);
				str3.append(">");
				endResult = str3.toString();
				break;
			case "OR":
				result = instr3.substring(4, 6);
				op1 = instr3.substring(7, instr3.lastIndexOf(","));
				op2 = instr3.substring(instr3.lastIndexOf(",") + 1, instr3.indexOf(">"));
				outcome = Integer.toString(Integer.parseInt(op1) | Integer.parseInt(op2));
				StringBuilder str4 = new StringBuilder("<");
				str4.append(result);
				str4.append(",");
				str4.append(outcome);
				str4.append(">");
				endResult = str4.toString();
				break;
		}
		REB.add(endResult);
		AIB.remove(0);
	}
	
	public void ADDR(String instr4) {
		String endResult = "";
		String result;
		String outcome;
		String op1;
		String op2;
		result = instr4.substring(4, 6);
		op1 = instr4.substring(7, 8);
		op2 = instr4.substring(9, 10);
		outcome = Integer.toString(Integer.parseInt(op1) + Integer.parseInt(op2));
		StringBuilder str5 = new StringBuilder("<");
		str5.append(result);
		str5.append(",");
		str5.append(outcome);
		str5.append(">");
		endResult = str5.toString();
		ADB.add(endResult);
		LIB.remove(0);
	}
	
	public void Load(String instr5) {
		String endResult = "";
		String result;
		String op;
		result = instr5.substring(instr5.indexOf("<") + 1, instr5.indexOf(","));
		op = instr5.substring(instr5.indexOf(",") + 1, instr5.indexOf(">"));
		
		for(int i=0; i<datamemory.length; i++) {
			if(op.equals(DAM.get(i).substring(DAM.get(i).indexOf("<") + 1, DAM.get(i).indexOf(",")))) {
				op = DAM.get(i).substring(DAM.get(i).indexOf(",") + 1, DAM.get(i).indexOf(">"));
				break;
			}
		}
		
		StringBuilder str6 = new StringBuilder("<");
		str6.append(result);
		str6.append(",");
		str6.append(op);
		str6.append(">");
		endResult = str6.toString();
		REB.add(endResult);
		ADB.remove(0);
	}
	
	public void Write(String instr6) {
		String endResult = "";
		String temp1 = instr6.substring(instr6.indexOf("<") + 1, instr6.indexOf(","));
		String temp2 = instr6.substring(instr6.indexOf(",") + 1, instr6.indexOf(">"));
		for(int i=0; i<registers.length; i++) {
			if(temp1.equals(RGF.get(i).substring(RGF.get(i).indexOf("<") + 1, RGF.get(i).indexOf(",")))) {
				StringBuilder str7 = new StringBuilder();
				str7.append("<");
				str7.append(temp1);
				str7.append(",");
				str7.append(temp2);
				str7.append(">");
				endResult = str7.toString();
				RGF.set(i, endResult);
			}
		}
		REB.remove(0);
	}
	
	//Places
	public String inm() {
		StringBuilder str = new StringBuilder();
		for(int i=0; i<INM.size(); i++) {
			str.append(INM.get(i));
			if(i<INM.size()-1) {
				str.append(",");
			}
		}
		return str.toString();
	}
	
	public String inb() {
		StringBuilder str = new StringBuilder();
		for(int i=0; i<INB.size(); i++) {
			str.append(INB.get(i));
			if(i<INB.size()-1) {
				str.append(",");
			}
		}
		return str.toString();
	}
	
	public String aib() {
		StringBuilder str = new StringBuilder();
		for(int i=0; i<AIB.size(); i++) {
			str.append(AIB.get(i));
			if(i<AIB.size()-1) {
				str.append(",");
			}
		}
		return str.toString();
	}
	
	public String lib() {
		StringBuilder str = new StringBuilder();
		for(int i=0; i<LIB.size(); i++) {
			str.append(LIB.get(i));
			if(i<LIB.size()-1) {
				str.append(",");
			}
		}
		return str.toString();
	}
	
	public String adb() {
		StringBuilder str = new StringBuilder();
		for(int i=0; i<ADB.size(); i++) {
			str.append(ADB.get(i));
			if(i<ADB.size()-1) {
				str.append(",");
			}
		}
		return str.toString();
	}
	
	public String reb() {
		StringBuilder str = new StringBuilder();
		for(int i=0; i<REB.size(); i++) {
			str.append(REB.get(i));
			if(i<REB.size()-1) {
				str.append(",");
			}
		}
		return str.toString();
	}
	
	public String rgf() {
		StringBuilder str = new StringBuilder();
		for(int i=0; i<RGF.size(); i++) {
			str.append(RGF.get(i));
			if(i<RGF.size()-1) {
				str.append(",");
			}
		}
		return str.toString();
	}
	
	public String dam() {
		StringBuilder str = new StringBuilder();
		for(int i=0; i<DAM.size(); i++) {
			str.append(DAM.get(i));
			if(i<DAM.size()-1) {
				str.append(",");
			}
		}
		return str.toString();
	}
}