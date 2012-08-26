package com.languespeak.dtw;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DTW {

	private final static float BIGVALUE = 99999999999.9f;
	
	public float score = 0.0f;
	public float scoreNorm = 0.0f;
	
	public DTW(String patron, String compare) throws ArrayIndexOutOfBoundsException
	{
	
		ArrayList<ArrayList<Float>> temp1 = readFile(patron);
		ArrayList<ArrayList<Float>> temp2 = readFile(compare);
		float compareSize = temp2.size();
		
		ArrayList<ArrayList<Float>> f1 = null;
		ArrayList<ArrayList<Float>> f2 = null;
		
		if(temp1.size() > temp2.size()){
			f1 = temp2;
			f2 = temp1;
		}else{
			f1 = temp1;
			f2 = temp2;
		}
		//f2 size must be in that range for work   
		if( (f1.size()*2) <f2.size()){
			score = -2.0f;
			return;
		}
		
		double top, mid, bot, cheapest, total;
		
		int I, X, Y, n;
		 
		System.out.println(" " +f1.size()+" "+f2.size());
		
		//Calculate Distance Matrix
		float Dist[][] = new float[f1.size()][f2.size()];
		int i=0,j=0;
		for (ArrayList<Float> xrow : f1) {
			j=0;
			for (ArrayList<Float> yrow : f2) {
				total = 0;
				for(int k=0;k<xrow.size(); k++){
					float operation = xrow.get(k) - yrow.get(k);
					total += operation * operation;
				}
				Dist[i][j] = (float) total;
				j++;
			}
			i++;
		}
		
		float globdist[][] = new float[f1.size()][f2.size()];
		int move[][] = new int[f1.size()][f2.size()];
		
		//First frame the perfect match is 0,0 
		for (j=1; j < f1.size(); j++) {
			globdist[j][0] = BIGVALUE;
			
			 globdist[0][1] = BIGVALUE;
		     globdist[1][1] = globdist[0][0] + Dist[1][1];
		     move[1][1] = 2;
			
			for (j=2; j < f1.size(); j++) {	
				 globdist[j][1] = BIGVALUE;
				 
				for (i=2; i < f2.size(); i++) {
					 globdist[0][i] = BIGVALUE;
		             globdist[1][i] = globdist[0][i-1] + Dist[1][i];
		                
					for (j=2; j < f1.size(); j++) {
						top = globdist[j-1][i-2] + Dist[j][i-1] + Dist[j][i];
                        mid = globdist[j-1][i-1] + Dist[j][i];
                        bot = globdist[j-2][i-1] + Dist[j-1][i] + Dist[j][i];
                        if( (top < mid) && (top < bot))
                        {
                            cheapest = top;
                            I = 1;
                        }
                        else if (mid < bot)
                        {
                            cheapest = mid;
                            I = 2;
                        }
                        else {cheapest = bot;
                            I = 3;
                        }
                        
                        /*if all costs are equal, pick middle path*/
                        if( ( top == mid) && (mid == bot))
                            I = 2;
                            
	                    globdist[j][i] = (float) cheapest;
	                    move[j][i] = I;
					}
				}
			}
		}
		
		int warp[][] = new int[f1.size()*2][3];
	    X = f2.size()-1; Y = f1.size()-1; n=0;
	    warp[n][0] =  X; warp[n][1] =  Y;
	    
	    
	    while (X > 0 && Y > 0) {
	        n=n+1;
	        
	        
	        if (n>f2.size() *2) { System.err.println ("Warning: warp matrix too large!");
	            return;
	        } 
	        
	       
			if (move[Y] [X] == 1 )
			{
				warp[n][0] =  (X-1); warp[n][1] =  Y;
				n=n+1;
				X=X-2; Y = Y-1;

			}else
			if (move[Y] [X] == 2)
			{
				X=X-1; Y = Y-1;

			}else
			if (move[Y] [X] == 3 )
			{
				warp[n] [0] = X;
				warp[n] [1] = (Y-1);
				n=n+1;
				X=X-1; Y = Y-2;

			}else
				System.err.println ("Error: move not defined for X = "+X+" Y = "+Y+" move "+move[Y][X]);

			//Edit to make exit in case get to init vector
			//if(warp[n][0]== 0 || warp[n][1]== 0) break;

	        warp[n] [0] = X;
	        warp[n] [1] = Y;
	        
	    }
	    
	    
	    short temp[][] = new short[f1.size()*2][2];
	    /*flip warp*/
	    for (i=0;i<n;i++) {
	        temp[i][0] = (short) warp[n-i][0];
	        temp[i][1] = (short) warp[n-i][1];
	        
	    }
	    
	    for (i=0;i<n;i++) {
	        warp[i][0] = temp[i][0];
	        warp[i][1] = temp[i][1];
	        
	    }
	    
	    //System.out.println("Warping complete. Writing output file.");
	    
	    try{
	    File fout = new File("result");
	    FileWriter out = new FileWriter(fout);
	    
	    /*print warped trajectory to stdout*/
	    for (i=0;i<n-1;i++){
	    	//System.out.println(" " +  warp[i][0] + " " +  warp[i][1] );
				out.write(warp[i][0]+1+" "+warp[i][1]+1+" "+ Dist[ warp[i][1] ] [ warp[i][0] ] );
	    }
		out.close();
		
	    } catch (IOException e) {
			e.printStackTrace();
		}
        
	            System.out.println(globdist[f1.size()-1][f2.size()-1] );
	            score = globdist[f1.size()-1][f2.size()-1];
	            scoreNorm = score/compareSize;
	}
	
	private ArrayList<ArrayList<Float>> readFile(String namefile){
		
		
		try {
			
			ArrayList<ArrayList<Float>> mat = new ArrayList<ArrayList<Float>>();
			FileInputStream fstream = new FileInputStream(namefile);
			 
		// Get the object of DataInputStream
		  DataInputStream in = new DataInputStream(fstream);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		  String strLine;
		  //Read File Line By Line

		  while ((strLine = br.readLine()) != null)   {
			  ArrayList<Float> row = new ArrayList<Float>();
			  String[] values = strLine.split(" ");
			  for (String v : values) {
				row.add(Float.parseFloat(v));  				  
			  }
			  mat.add(row);
			  
		  }
		  //Close the input stream
		  in.close();
			
			return mat;
			
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
			System.err.println("This file " + namefile +" not here ");
		} catch (IOException e) {
		
			e.printStackTrace();
		}
		
		
		return null;
		
	}
	
	public static void main(String[] args) {
		new DTW("pattern.ascii", "pattern.ascii");
		
		
	}
	
}
