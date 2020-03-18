
	//BE CAREFUL. THIS MODEL IS FOR MULTI MEMBER (DARALTILMIS)

	import java.io.IOException;


	import java.util.Arrays;

	public class MainClassMulti {

		public static void main(String[] args) throws IOException {
			//TODO Auto-generated method stub
			 //double [][] zValues = new double [106][5];
			//zValues=MathematicalFormulationSingle.readInitialData(106,5);
			//System.out.print(Arrays.deepToString(zValues));
			//solveMe(int n, int m, int p, int numRep[], int obje)
			//double[][] zValues = new double[68][4];
			//zValues = MathModelMultiMemberWith.readInitialData(68,4);
			
			int [] numReps= {3,1,2,2,2};
			MathModelClass.solveMe(85, 5, 2,numReps, 1);
	
		}
	}
