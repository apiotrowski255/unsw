package src.toolbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OBJFileLoader {

	private static final String RES_LOC = "res/";
	
	public static void loadOBJ(String objFileName){
		FileReader isr = null;
		File objFile = new File(RES_LOC + objFileName +".obj");
		try {
			isr = new FileReader(objFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("File not found in res; don't use any extendtion");
		}
		BufferedReader reader = new BufferedReader(isr);
		String line;
		List<Vector3f> vertices = new ArrayList<Vector3f>();
		List<Vector2f> textures = new ArrayList<Vector2f>();
		List<Vector3f> normals = new ArrayList<Vector3f>();
		List<Vector3f> indices = new ArrayList<Vector3f>();
		
		try {
			line = reader.readLine();
			if (line.startsWith("v ")){
				String[] currentLine = line.split(" ");
				Vector3f vertex = new Vector3f((float) Float.valueOf(currentLine[1]),
						(float) Float.valueOf(currentLine[2]),
						(float) Float.valueOf(currentLine[3]));
				vertices.add(vertex);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error reading the file");
		}
	}
}
