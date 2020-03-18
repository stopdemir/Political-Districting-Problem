

	import java.io.IOException;
	import ilog.concert.IloException;

	import java.util.Arrays;

	public class MainMethodSingleWith {

		public static void main(String[] args) throws IOException {
			//TODO Auto-generated method stub
			 //double [][] zValues = new double [106][5];
			//zValues=MathematicalFormulationSingle.readInitialData(106,5);
			//System.out.print(Arrays.deepToString(zValues));
			MathModelSingleWith.solveMe(68, 8, 5, 1);
			
		}
	}
