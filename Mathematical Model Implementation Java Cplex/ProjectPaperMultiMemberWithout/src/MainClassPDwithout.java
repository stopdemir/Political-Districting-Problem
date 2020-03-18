// BE CAREFUL. THIS MODEL IS FOR MULTI MEMBER (DARALTILMIS)

import java.io.IOException;


import java.util.Arrays;

public class MainClassPDwithout {

	public static void main(String[] args) throws IOException {
		//TODO Auto-generated method stub
		 //double [][] zValues = new double [106][5];
		//zValues=MathematicalFormulationSingle.readInitialData(106,5);
		//System.out.print(Arrays.deepToString(zValues));
		//solveMe(int n, int m, int p, int numRep[], int obje)
		int [] numReps= {2,2};
		MathFormulationPDwithout.solveMe(33, 2, 5,numReps, 1);

		
	}
}
